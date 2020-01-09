package org.narrative.network.core.narrative.rewards;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardEventDAO;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 2019-05-29
 * Time: 08:34
 *
 * @author brian
 */
@Getter
@Setter
@Entity
@Proxy
@NoArgsConstructor
public class UserActivityRewardEvent implements DAOObject<UserActivityRewardEventDAO> {

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_userRewardActivityEvent_user")
    private User user;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private UserActivityRewardEventType type;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant eventDatetime;

    public UserActivityRewardEvent(User user, @NotNull UserActivityRewardEventType type) {
        assert type.isUsesUserActivityRewardEvent() : "Should never create UserActivityRewardEvent for types that don't support it!";
        this.user = user;
        this.type = type;
        this.eventDatetime = Instant.now();
    }

    public static UserActivityRewardEventDAO dao() {
        return NetworkDAOImpl.getDAO(UserActivityRewardEvent.class);
    }
}
