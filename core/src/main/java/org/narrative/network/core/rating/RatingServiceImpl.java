package org.narrative.network.core.rating;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.reputation.services.CreateEventMessageTask;
import org.narrative.shared.event.reputation.ConsensusChangedEvent;
import org.narrative.shared.event.reputation.RatingEvent;
import org.narrative.shared.event.reputation.RatingType;
import org.narrative.shared.event.reputation.ReputationEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    @Override
    public <R extends Ratable<R>,
            RV extends RatingValue<URO>,
            URO extends UserRatedObject<DAO,RV>,
            DAO extends UserRatedObjectDAO<R, URO, OID, RV>>
    URO setRating(R ratable, User user, RatingType ratingType, RV ratingValue, String reason) {
        RatableType ratableType = ratable.getRatableType();
        assert ratingValue==null || ratingType==ratingValue.getRatingType() : "RatingType mismatch! supplied/" + ratingType + " but ratingValueType/" + ratingValue.getRatingType();

        // bl: refresh and lock the Ratable up front so that only one concurrent request is allowed.
        ratable.refreshForLock();

        DAO dao = ratableType.getDao(ratingType);

        RatingFields<RV> ratingFields = ratable.getRatingFields(ratingType);

        RV usersOldRatingValue = null;
        RV oldConsensus = ratingFields.getEffectiveRatingValue();

        URO userRatedObject = dao.getRatingForUser(user, ratable);
        if(!exists(userRatedObject)) {
            // bl: if there is no rating value and no current rating, then nothing to do!
            if(ratingValue==null) {
                return null;
            }
            userRatedObject = dao.createRatingForUser(user, ratingValue, ratable);
            dao.save(userRatedObject);
        } else {
            usersOldRatingValue = userRatedObject.getRatingValue();

            // bl: if the rating isn't changing, then bail out; nothing to do!
            if(isEqual(usersOldRatingValue, ratingValue)) {
                return userRatedObject;
            }

            // Rating exists so subtract the old rating first
            usersOldRatingValue.removeVote(ratable, userRatedObject);

            if(ratingValue==null) {
                // bl: if there is no ratingValue, then it's just a vote being removed. delete the UserRatedObject
                dao.delete(userRatedObject);
                userRatedObject = null;
            } else {
                // update the vote and point value now to match the user's current reputation
                userRatedObject.setRatingValue(ratingValue);
                userRatedObject.setPointValue(user.getReputationWithoutCache().getAdjustedVotePoints());
            }
        }

        // bl: in order to support removing votes, we need to handle null. we only need to update
        // the UserRatedObject if it actually exists.
        if(userRatedObject!=null) {
            // bl: if the user is a moderator of one of the niches the post is made to, their vote has 3x impact
            Integer ratingMultiplier = ratable.getRatingMultiplier(user);
            if(ratingMultiplier!=null) {
                userRatedObject.setPointValue(userRatedObject.getPointValue()*ratingMultiplier);
            }

            // now that the old rating has been handled above (if necessary), just add the new points!
            ratingValue.addVote(ratable, userRatedObject, reason);
        }

        // bl: always need to do the onRatingUpdate, even if the vote was removed
        ratable.onRatingUpdate(ratingType);

        RV newConsensus = ratingFields.getEffectiveRatingValue();

        boolean isConsensusChange = !isEqual(oldConsensus, newConsensus);

        // bl: if this is the user's first vote or if consensus didn't change, send a RatingEvent.
        // if the consensus changed, then we'll handle the consensus change in bulk via ConsensusChangedEvent below.
        if(usersOldRatingValue==null || !isConsensusChange) {
            RatingEvent.RatingEventBuilder builder = RatingEvent.builder()
                    .userOid(user.getOid().getValue())
                    .wasRatedWithConsensus(isEqual(usersOldRatingValue, oldConsensus));
            // bl: handle vote removal separate from revoting
            if(ratingValue==null) {
                builder
                        .removeVote(true);
            } else {
                builder
                        .revote(usersOldRatingValue!=null)
                        .ratedWithConsensus(isEqual(ratingValue, newConsensus));
            }

            networkContext().doGlobalTask(new CreateEventMessageTask(builder.build()));
        }

        // bl: if the consensus changes, we need to trigger a consensus changed reputation event.
        if(isConsensusChange) {
            // bl: only treat consensus correlation as unchanged if the user has a rating before and after.
            // if the user didn't have a rating either before or after, then, by definition, the user's correlation changed.
            boolean isUserConsensusCorrelationUnchanged = usersOldRatingValue!=null && ratingValue!=null && isEqual(usersOldRatingValue, oldConsensus) == isEqual(ratingValue, newConsensus);
            // bl: always exclude the user from consensus changes on their first vote. their rating event will be handled above.
            // or, if the user's consensus correlation is unchanged (if this user's vote edit is what flipped consensus)
            boolean excludeUserFromConsensus = usersOldRatingValue==null || isUserConsensusCorrelationUnchanged;
            onRatingConsensusChanged(dao, ratable, oldConsensus, newConsensus, excludeUserFromConsensus ? user : null);
        }

        // bl: not recording ledger entries for now. we'll handle that later in #2556

        return userRatedObject;
    }

    private <R extends Ratable<R>,
            RV extends RatingValue<URO>,
            URO extends UserRatedObject<DAO,RV>,
            DAO extends UserRatedObjectDAO<R, URO, OID, RV>>
    void onRatingConsensusChanged(DAO dao, R ratable, RV oldConsensus, RV newConsensus, User userToExclude) {
        Set<RV> ratingValues = new HashSet<>();
        if(oldConsensus!=null) {
            ratingValues.add(oldConsensus);
        }
        if(newConsensus!=null) {
            ratingValues.add(newConsensus);
        }
        // bl: get everyone who has a vote with one of the two consensus values so that we can create
        // the map of User OIDs to in-consensus.
        Map<OID,RV> userRatings = dao.getRatingsForUsersAndValues(ratable, ratingValues);

        if(exists(userToExclude)) {
            userRatings.remove(userToExclude.getOid());
        }

        Map<Long,Boolean> userConsensusMap = userRatings.entrySet().stream()
                // bl: the boolean value of the map should be true if the user is correlated to the new consensus
                .collect(Collectors.toMap(e -> e.getKey().getValue(), e -> isEqual(e.getValue(), newConsensus)));

        // if the map is empty, no sense in sending the reputation event.
        if(userConsensusMap.isEmpty()) {
            return;
        }

        // bl: create and send the consensus changed event
        ReputationEvent event = ConsensusChangedEvent.builder()
                .usersConsensusChangedMap(userConsensusMap)
                .build();
        networkContext().doGlobalTask(new CreateEventMessageTask(event));
    }
}
