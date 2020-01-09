package org.narrative.network.core.rating.model;

import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.core.rating.AgeRating;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Data
@NoArgsConstructor
public class UserAgeRatedObject<DAO extends UserRatedObjectDAO> extends UserRatedObject<DAO, AgeRating> {
    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private AgeRating ageRating;

    public UserAgeRatedObject(User user, AgeRating ageRating) {
        super(user);
        this.ageRating = ageRating;
    }

    @Transient
    @Override
    public AgeRating getRatingValue() {
        return getAgeRating();
    }

    @Override
    public void setRatingValue(AgeRating ageRating) {
        setAgeRating(ageRating);
    }
}
