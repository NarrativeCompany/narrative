package org.narrative.network.customizations.narrative.personaljournal;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelConsumer;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueType;
import org.narrative.network.customizations.narrative.personaljournal.dao.PersonalJournalDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Date: 2018-12-19
 * Time: 10:19
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PersonalJournal implements DAOObject<PersonalJournalDAO>, ChannelConsumer {
    @Id
    private OID oid;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Fields.oid)
    @ForeignKey(name = "fk_personalJournal_user")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = Fields.oid)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private Channel channel;

    /**
     * @deprecated for hibernate use only
     */
    public PersonalJournal() {}

    public PersonalJournal(User user) {
        // jw: since the User needs to be saved first, and the Channel must have its OID specified specifically, let's
        //     pull the OID off of the user manually and not rely on field assignment from user. That way the Channels
        //     constructor can use it to set its OID, and we are explicit that we need a user whose OID has already been
        //     set.
        this.oid = user.getOid();
        this.user = user;
        this.channel = new Channel(this);
    }


    @Override
    @Transient
    public ChannelType getChannelType() {
        return ChannelType.PERSONAL_JOURNAL;
    }

    @Override
    @Transient
    public User getChannelOwner() {
        return getUser();
    }

    @Override
    public String getName() {
        throw UnexpectedError.getRuntimeException("This method should never be used!");
    }

    @Override
    public String getNameForHtml() {
        throw UnexpectedError.getRuntimeException("This method should never be used!");
    }

    @Override
    public String getDisplayUrl() {
        throw UnexpectedError.getRuntimeException("This method should never be used!");
    }

    @Override
    public TribunalIssueType getPossibleTribunalIssueType() {
        throw UnexpectedError.getRuntimeException("This method should never be used!");
    }

    @Override
    public boolean isCanCurrentRolePost() {
        // bl: only the personal journal user can post to it!
        return getUser().isCurrentUserThisUser();
    }

    public static PersonalJournalDAO dao() {
        return NetworkDAOImpl.getDAO(PersonalJournal.class);
    }
}
