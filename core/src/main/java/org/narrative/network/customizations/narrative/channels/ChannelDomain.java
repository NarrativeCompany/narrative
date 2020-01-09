package org.narrative.network.customizations.narrative.channels;

import org.narrative.common.persistence.*;
import org.narrative.network.customizations.narrative.channels.dao.*;

import org.narrative.network.shared.daobase.*;

import org.narrative.network.shared.util.NetworkConstants;
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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-07-31
 * Time: 07:39
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
@Table(uniqueConstraints = {@UniqueConstraint(name = "channelDomain_domainName_uidx", columnNames = {ChannelDomain.FIELD__DOMAIN_NAME__COLUMN})})
public class ChannelDomain implements DAOObject<ChannelDomainDAO> {
    public static final String FIELD__DOMAIN_NAME__NAME = "domainName";
    public static final String FIELD__DOMAIN_NAME__COLUMN = FIELD__DOMAIN_NAME__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ForeignKey(name = "fk_channelDomain_channel")
    private Channel channel;

    @NotNull
    @Length(min = 0, max = NetworkConstants.MAX_FQDN_LENGTH)
    private String domainName;

    public static ChannelDomainDAO dao() {
        return NetworkDAOImpl.getDAO(ChannelDomain.class);
    }
}
