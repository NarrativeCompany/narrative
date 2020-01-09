package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateEnumSetType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.dao.ChannelUserDAO;
import org.narrative.network.customizations.narrative.niches.niche.NicheRole;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.Set;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 07:57
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "channelUser_channel_user_uidx", columnNames = {ChannelUser.FIELD__CHANNEL__COLUMN, ChannelUser.FIELD__USER__COLUMN})
})
public class ChannelUser implements DAOObject<ChannelUserDAO> {
    public static final String FIELD__CHANNEL__NAME = "channel";
    public static final String FIELD__CHANNEL__COLUMN = FIELD__CHANNEL__NAME + "_" + Channel.FIELD__OID__NAME;

    public static final String FIELD__USER__NAME = "user";
    public static final String FIELD__USER__COLUMN = FIELD__USER__NAME + "_" + User.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_channelUser_channel")
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_channelUser_user")
    private User user;

    private long roles;
    private long invitedRoles;

    private transient Set rolesResolved;
    private transient Set invitedRolesResolved;

    public ChannelUser(Channel channel, User user) {
        this.channel = channel;
        this.user = user;
    }

    public <R extends Enum<R> & ChannelRole> Set<R> getRolesResolved() {
        if(rolesResolved==null) {
            rolesResolved = getRolesResolved(getChannel().getType().getRoleType(), this::getRoles);
        }
        return rolesResolved;
    }

    public <R extends Enum<R> & ChannelRole> Set<R> getInvitedRolesResolved() {
        if(invitedRolesResolved==null) {
            invitedRolesResolved = getRolesResolved(getChannel().getType().getRoleType(), this::getInvitedRoles);
        }
        return invitedRolesResolved;
    }

    private <R extends Enum<R> & ChannelRole> Set<R> getRolesResolved(Class<R> expectedRoleType, LongSupplier rolesSupplier) {
        assert getChannel().getType().getRoleType().isAssignableFrom(expectedRoleType) : "The expected role type/"+expectedRoleType.getSimpleName()+" should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();

        return HibernateEnumSetType.parseEnumSet(expectedRoleType, rolesSupplier.getAsLong());
    }

    private <T extends Enum<T> & ChannelRole> void setRolesResolved(LongConsumer setRolesFunction, Set<T> rolesResolved) {
        // jw: all we need to do is create a bitmask from the enum set.
        setRolesFunction.accept(HibernateEnumSetType.createBitmask(rolesResolved));
    }

    public Set<NicheRole> getNicheRoles() {
        assert getChannel().getType().getRoleType().equals(NicheRole.class) : "The expected role type/NicheRole should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();
        return getRolesResolved();
    }

    public Set<PublicationRole> getPublicationRoles() {
        assert getChannel().getType().getRoleType().equals(PublicationRole.class) : "The expected role type/PublicationRole should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();
        return getRolesResolved();
    }

    public Set<PublicationRole> getInvitedPublicationRoles() {
        assert getChannel().getType().getRoleType().equals(PublicationRole.class) : "The expected role type/PublicationRole should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();
        return getInvitedRolesResolved();
    }

    public <T extends Enum<T> & ChannelRole> void addRoles(Set<T> roles) {
        assert !isEmptyOrNull(roles) : "This method should always be provided a role.";

        // jw: this primes and provides the rolesResolved, so by changing this we are affecting the cached object and still
        //     need to persist the change to the roles field below.
        Set<T> currentRoles = getRolesResolved();
        for (T role : roles) {
            assert getChannel().getType().getRoleType().isAssignableFrom(role.getClass()) : "The expected role type/" + role.getClass() + " should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();
            currentRoles.add(role);
        }

        setRolesResolved(this::setRoles, currentRoles);
    }

    public <T extends Enum<T> & ChannelRole> void addRoleInvites(Set<T> roles) {
        assert !isEmptyOrNull(roles) : "This method should always be provided a role.";

        Set<T> currentRoles = getInvitedRolesResolved();
        for (T role : roles) {
            assert getChannel().getType().getRoleType().isAssignableFrom(role.getClass()) : "The expected role type/" + role.getClass() + " should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();
            currentRoles.add(role);
        }

        setRolesResolved(this::setInvitedRoles, currentRoles);
    }

    public <T extends Enum<T> & ChannelRole> void removeRole(T role) {
        assert role != null : "This method should always be provided a role.";
        assert getChannel().getType().getRoleType().isAssignableFrom(role.getClass()) : "The expected role type/" + role.getClass() + " should always match the roleType/"+getChannel().getType().getRoleType()+" from the channel/"+channel.getOid()+" type/"+getChannel().getType();

        // jw: just like above, this primes and provides the rolesResolved, so by changing this we are affecting the cached
        //     object and still need to persist the change to the roles field below.
        {
            Set<T> roles = getRolesResolved();
            roles.remove(role);

            setRolesResolved(this::setRoles, roles);
        }

        // bl: also remove from the invited roles
        {
            Set<T> invitedRoles = getInvitedRolesResolved();
            invitedRoles.remove(role);

            setRolesResolved(this::setInvitedRoles, invitedRoles);
        }
    }

    public void deleteIfEmpty() {
        // bl: if there aren't any roles remaining, then delete!
        if(getPublicationRoles().isEmpty() && getInvitedPublicationRoles().isEmpty()) {
            ChannelUser.dao().delete(this);
        }
    }

    public static ChannelUserDAO dao() {
        return NetworkDAOImpl.getDAO(ChannelUser.class);
    }
}
