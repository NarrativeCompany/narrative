package org.narrative.network.customizations.narrative.service.impl.tribunal;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEvent;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEventType;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.Ratable;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.rating.model.UserQualityRatedObject;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.shared.event.reputation.RatingType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Date: 2019-05-29
 * Time: 09:28
 *
 * @author brian
 */
public class RecordUserActivityEventsForAupReportersTask<R extends Ratable<R>> extends GlobalTaskImpl<Object> {
    private final R ratable;

    public RecordUserActivityEventsForAupReportersTask(R ratable) {
        this.ratable = ratable;
    }

    @Override
    protected Object doMonitoredTask() {
        UserRatedObjectDAO<R, ? extends UserQualityRatedObject, OID, QualityRating> dao = ratable.getRatableType().getDao(RatingType.QUALITY);
        Set<OID> userOids = dao.getRatingsForUsersAndValues(ratable, EnumSet.of(QualityRating.DISLIKE_CONTENT_VIOLATES_AUP)).keySet();
        Collection<User> users = User.dao().getObjectsFromIDsWithCache(userOids);
        // bl: create an event for each user
        for (User user : users) {
            UserActivityRewardEvent event = new UserActivityRewardEvent(user, UserActivityRewardEventType.REPORTED_AUP_VIOLATION_REMOVED);
            UserActivityRewardEvent.dao().save(event);
        }
        return null;
    }
}
