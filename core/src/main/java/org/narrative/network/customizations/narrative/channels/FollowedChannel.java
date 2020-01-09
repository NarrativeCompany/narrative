package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.dao.FollowedChannelDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 9/6/18
 * Time: 2:57 PM
 *
 * @author jonmark
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(
        name="follower_channel_uidx",
        columnNames = {FollowedChannel.FIELD__FOLLOWER__COLUMN, FollowedChannel.FIELD__CHANNEL__COLUMN}
)})
public class FollowedChannel implements DAOObject<FollowedChannelDAO> {
    public static final String FIELD__FOLLOWER__NAME = "follower";
    public static final String FIELD__CHANNEL__NAME = "channel";

    public static final String FIELD__FOLLOWER__COLUMN = FIELD__FOLLOWER__NAME + "_" + User.FIELD__OID__NAME;
    public static final String FIELD__CHANNEL__COLUMN = FIELD__CHANNEL__NAME + "_" + Channel.FIELD__OID__NAME;

    private OID oid;

    private User follower;
    private Channel channel;
    private Instant followDatetime;

    /**
     * @deprecated for hibernate use only
     */
    public FollowedChannel() {}

    public FollowedChannel(User follower, Channel channel) {
        this.follower = follower;
        this.channel = channel;
        this.followDatetime = Instant.now();
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_followedChannel_follower")
    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_followedChannel_channel")
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    public Instant getFollowDatetime() {
        return followDatetime;
    }

    public void setFollowDatetime(Instant watchDatetime) {
        this.followDatetime = watchDatetime;
    }

    public static FollowedChannelDAO dao() {
        return NetworkDAOImpl.getDAO(FollowedChannel.class);
    }
}
