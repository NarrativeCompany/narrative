package org.narrative.network.core.rating.model;

import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.dao.UserRatedObjectDAO;
import org.narrative.network.core.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Data
@NoArgsConstructor
public class UserQualityRatedObject<DAO extends UserRatedObjectDAO> extends UserRatedObject<DAO,QualityRating> {
    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private QualityRating qualityRating;

    public UserQualityRatedObject(User user, QualityRating qualityRating) {
        super(user);
        this.qualityRating = qualityRating;
    }

    @Transient
    @Override
    public QualityRating getRatingValue() {
        return getQualityRating();
    }

    @Override
    public void setRatingValue(QualityRating qualityRating) {
        setQualityRating(qualityRating);
    }
}
