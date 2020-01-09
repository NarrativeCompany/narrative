package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.persistence.*;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.dao.*;

import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.daobase.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-10-03
 * Time: 12:56
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
public class DeletedChannel implements DAOObject<DeletedChannelDAO> {
    // jw: Similar to the Channel itself, this entity will have the same OID as the Channel it is being created to represent.
    @Id
    private OID oid;

    @NotNull
    @Type(type= IntegerEnumType.TYPE)
    private ChannelType type;

    // jw: note: we are using the legacy max from Niche for this to support deletions of legacy Niches.
    @NotNull
    @Length(min = ChannelConsumer.MIN_NAME_LENGTH, max = Niche.LEGACY_MAX_NICHE_NAME_LENGTH)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_deletedChannel_owner")
    private User owner;

    public DeletedChannel(Channel channel) {
        assert exists(channel) : "This should be created before the channel has been removed.";

        oid = channel.getOid();
        type = channel.getType();
        name = channel.getConsumer().getName();
        owner = channel.getConsumer().getChannelOwner();
    }

    public static DeletedChannelDAO dao() {
        return NetworkDAOImpl.getDAO(DeletedChannel.class);
    }
}
