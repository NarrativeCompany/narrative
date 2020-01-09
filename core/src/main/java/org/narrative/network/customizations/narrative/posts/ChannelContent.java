package org.narrative.network.customizations.narrative.posts;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.posts.dao.ChannelContentDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Date: 2019-01-03
 * Time: 12:11
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(name="uidx_channelContent_content_channel", columnNames = {ChannelContent.COLUMN__CONTENT, ChannelContent.COLUMN__CHANNEL})})
public class ChannelContent implements DAOObject<ChannelContentDAO> {
    // jw: we will need these constants to be defined directly so we can create the column constants below. I want to use
    //     the lombok FIELD_X constants but those cannot be used for static final fields or class level annotations. It's
    //     a chicken and egg problem. Lombok loads the classes and creates the static field name constants, but it needs
    //     to initialize the class to do that. So, the class tries to create the COLUMN__ constants here, using the lombok
    //     constants that have not been created yet. Lombok build dies, and none of it's injected code gets created. Not good,
    //     and exceptionally hard to track down since the compilation just gives you over a hundred errors trying to reference
    //     getters and setters, and field constants that were never created.  Blurg.
    public static final String FIELD__CONTENT = "content";
    public static final String FIELD__CHANNEL = "channel";

    public static final String COLUMN__CONTENT = FIELD__CONTENT + "_" + Content.FIELD__OID__NAME;
    public static final String COLUMN__CHANNEL = FIELD__CHANNEL + "_" + Channel.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(optional = false)
    @ForeignKey(name = "fk_channelContent_content")
    private Content content;

    @ManyToOne(optional = false)
    @ForeignKey(name = "fk_channelContent_channel")
    private Channel channel;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private NarrativePostStatus status;

    @Type(type= HibernateInstantType.TYPE)
    private Instant moderationDatetime;

    @Type(type= HibernateInstantType.TYPE)
    private Instant featuredDatetime;

    @Type(type= HibernateInstantType.TYPE)
    private Instant featuredUntilDatetime;

    /**
     * @deprecated for hibernate use only
     */
    public ChannelContent() {}

    public ChannelContent(Channel channel, Content content) {
        this.channel = channel;
        this.content = content;
        this.status = NarrativePostStatus.APPROVED;
    }

    public void moderatePost() {
        setStatus(NarrativePostStatus.MODERATED);
        setModerationDatetime(Instant.now());
    }

    public void approvePost() {
        setStatus(NarrativePostStatus.APPROVED);
        setModerationDatetime(null);
    }

    public void featurePost(FeaturePostDuration duration) {
        assert duration != null : "This method should only be used when featuring a post!";
        assert getContent().isHasTitleImage() : "Should only attempt to feature a post that has a title image!";

        Instant now = Instant.now();

        setFeaturedDatetime(now);
        setFeaturedUntilDatetime(now.plus(duration.getDuration()));
    }

    public void unfeaturePost() {
        setFeaturedDatetime(null);
        setFeaturedUntilDatetime(null);
    }

    public static ChannelContentDAO dao() {
        return NetworkDAOImpl.getDAO(ChannelContent.class);
    }
}
