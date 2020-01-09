package org.narrative.network.core.rating.model;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.dao.UserQualityRatedCompositionDAO;
import org.narrative.network.core.user.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@FieldNameConstants
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "userQualityRatedComposition_composition_userOid_uidx", columnNames = {UserQualityRatedComposition.FIELD__COMPOSITION__COLUMN, UserQualityRatedComposition.FIELD__USER_OID__COLUMN})
})
public class UserQualityRatedComposition extends UserQualityRatedObject<UserQualityRatedCompositionDAO> {
    public static final String FIELD__COMPOSITION__NAME = "composition";
    public static final String FIELD__COMPOSITION__COLUMN = FIELD__COMPOSITION__NAME+"_"+Composition.FIELD__OID__NAME;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_userQualityRatedComposition_composition")
    private Composition composition;

    public UserQualityRatedComposition(User user, QualityRating qualityRating, Composition composition) {
        super(user, qualityRating);
        this.composition = composition;
    }

    public static UserQualityRatedCompositionDAO dao() {
        return DAOImpl.getDAO(UserQualityRatedComposition.class);
    }
}
