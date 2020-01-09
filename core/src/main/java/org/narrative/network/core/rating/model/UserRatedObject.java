package org.narrative.network.core.rating.model;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.rating.RatingValue;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.time.Instant;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class UserRatedObject<DAO extends UserRatedObjectDAO, RV extends RatingValue> implements DAOObject<DAO> {
    public static final String FIELD__USER_OID__NAME = "userOid";
    public static final String FIELD__USER_OID__COLUMN = FIELD__USER_OID__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    private OID userOid;
    @NotNull
    private Instant ratingDatetime;
    private int pointValue;

    public UserRatedObject(User user) {
        this.userOid = user.getOid();
        this.ratingDatetime = Instant.now();
        this.pointValue = user.getReputationWithoutCache().getAdjustedVotePoints();
    }

    @Transient
    public abstract RV getRatingValue();

    public abstract void setRatingValue(RV ratingValue);

    @Transient
    public User getUser() {
        return User.dao().get(getUserOid());
    }
}
