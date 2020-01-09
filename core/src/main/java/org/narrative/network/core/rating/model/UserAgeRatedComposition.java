package org.narrative.network.core.rating.model;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.rating.dao.UserAgeRatedCompositionDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.core.rating.AgeRating;
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
        @UniqueConstraint(name = "userAgeRatedComposition_composition_userOid_uidx", columnNames = {UserAgeRatedComposition.FIELD__COMPOSITION__COLUMN, UserAgeRatedComposition.FIELD__USER_OID__COLUMN})
})
public class UserAgeRatedComposition extends UserAgeRatedObject<UserAgeRatedCompositionDAO> {
    public static final String FIELD__COMPOSITION__NAME = "composition";
    public static final String FIELD__COMPOSITION__COLUMN = FIELD__COMPOSITION__NAME+"_"+Composition.FIELD__OID__NAME;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_userAgeRatedComposition_composition")
    private Composition composition;

    public UserAgeRatedComposition(User user, AgeRating ageRating, Composition composition) {
        super(user, ageRating);
        this.composition = composition;
    }

    public static UserAgeRatedCompositionDAO dao() {
        return DAOImpl.getDAO(UserAgeRatedComposition.class);
    }
}
