package org.narrative.network.core.rating;

import com.google.common.collect.ImmutableMap;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.rating.dao.UserAgeRatedCompositionDAO;
import org.narrative.network.core.rating.dao.UserQualityRatedCompositionDAO;
import org.narrative.network.core.rating.dao.UserQualityRatedReplyDAO;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.rating.model.UserRatedObject;
import org.narrative.shared.event.reputation.RatingType;

import java.util.Map;

public enum RatableType {
    CONTENT(ImmutableMap.of(
            RatingType.QUALITY, UserQualityRatedCompositionDAO.class,
            RatingType.AGE, UserAgeRatedCompositionDAO.class
        )
    ),
    REPLY(ImmutableMap.of(
            RatingType.QUALITY, UserQualityRatedReplyDAO.class
        )
    )
    ;

    private final Map<RatingType, Class<? extends UserRatedObjectDAO>> ratingTypeDaoClasses;

    RatableType(Map<RatingType, Class<? extends UserRatedObjectDAO>> ratingTypeDaoClasses) {
        this.ratingTypeDaoClasses = ratingTypeDaoClasses;
    }

    public <R extends Ratable<R>,
            RV extends RatingValue<URO>,
            URO extends UserRatedObject<?,RV>,
            DAO extends UserRatedObjectDAO<R, URO, OID, RV>>
    DAO getDao(RatingType ratingType) {
        Class<? extends UserRatedObjectDAO> daoClass = ratingTypeDaoClasses.get(ratingType);
        if(daoClass==null) {
            throw UnexpectedError.getRuntimeException("Found unsupported RatingType for " + this + ": " + ratingType);
        }
        return (DAO)DAOImpl.getDAOFromDAOClass(daoClass);
    }

    public boolean isContent() {
        return this==CONTENT;
    }

    public boolean isReply() {
        return this==REPLY;
    }
}
