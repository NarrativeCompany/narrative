package org.narrative.network.core.content.base;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.IntBitmaskType;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.IPUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.images.ImageDimensions;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionCache;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.composition.base.ShareableObject;
import org.narrative.network.core.composition.base.services.ModeratedCompositionConsumerViolation;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.content.base.dao.ContentDAO;
import org.narrative.network.core.fileondisk.base.FileConsumerFileInfo;
import org.narrative.network.core.fileondisk.base.FileConsumerType;
import org.narrative.network.core.fileondisk.base.FileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.fileondisk.image.ImageType;
import org.narrative.network.core.mentions.MentionsUtil;
import org.narrative.network.core.moderation.Moderatable;
import org.narrative.network.core.moderation.ModeratedContent;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.Ratable;
import org.narrative.network.core.rating.RatableType;
import org.narrative.network.core.rating.RatingFields;
import org.narrative.network.core.rating.model.AgeRatingFields;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.core.rating.model.UserAgeRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedComposition;
import org.narrative.network.core.search.services.ContentIndexRunnable;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostStatus;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.services.ReactRoute;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkConstants;
import org.narrative.shared.event.reputation.RatingType;
import org.quartz.JobBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 4:27:31 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {Content.FIELD__PORTFOLIO__COLUMN, Content.FIELD__CONTENT_TYPE__COLUMN, Content.FIELD__PRETTY_URL_STRING__COLUMN})})
public class Content implements DAOObject<ContentDAO>, FullTextProvider, AuthorProvider, Moderatable<CompositionCache, ContentStats, ContentDAO>, ShareableObject, Ratable<Content> {
    private OID oid;
    private String subject;
    private String subTitle;
    private String extract;
    private ContentType contentType;
    private AgeRating authorAgeRating;
    private AgeRating ageRating;

    private transient String displaySubject;
    private transient String shortDisplaySubject;

    private AreaUserRlm areaUserRlm;
    private String guestName;
    private Portfolio portfolio;
    private OID compositionPartitionOid;
    private IntBitmaskType<ContentStatus> contentStatus;
    private boolean allowReplies;
    private Timestamp liveDatetime;
    private Timestamp lastUpdateDatetime;
    private ModerationStatus moderationStatus;
    private ContentStats contentStats;

    private ImageOnDisk avatarImageOnDisk;

    private String prettyUrlString;

    private QualityRatingFields qualityRatingFields;
    private AgeRatingFields ageRatingFields;

    private FutureContent futureContent;

    private Map<Channel, ChannelContent> channelContents;
    private Set<ChannelContent> nicheChannelContents;
    private Set<TrendingContent> trendingContents;
    private Set<ModeratedContent> moderatedContents;

    private transient final boolean isNew;

    private transient CompositionCache compositionCache;

    public static final String FIELD__SUBJECT__NAME = "subject";
    public static final String FIELD__CONTENT_TYPE__NAME = "contentType";
    public static final String FIELD__AVATAR_IMAGE_ON_DISK__NAME = "avatarImageOnDisk";

    public static final String FIELD__CONTENT_TYPE__COLUMN = FIELD__CONTENT_TYPE__NAME;

    public static final String FIELD__LIVE_DATETIME__NAME = "liveDatetime";

    public static final String FIELD__CHANNEL_CONTENTS = "channelContents";
    public static final String FIELD__NICHE_CHANNEL_CONTENTS = "nicheChannelContents";
    public static final String FIELD__AGE_RATING = "ageRating";
    public static final String FIELD__QUALITY_RATING_FIELDS = "qualityRatingFields";

    public static final String FIELD__CONTENT_TAGS__NAME = "contentTags";
    public static final String FIELD__TRENDING_CONTENTS__NAME = "trendingContents";
    public static final String FIELD__CONTENT_STATS__NAME = "contentStats";
    public static final String FIELD__ALLOW_REPLIES__NAME = "allowReplies";
    public static final String FIELD__MODERATED_CONTENTS__NAME = "moderatedContents";
    public static final String FIELD__FUTURE_CONTENT__NAME = "futureContent";

    public static final String FIELD__PORTFOLIO__NAME = "portfolio";
    public static final String FIELD__PORTFOLIO__COLUMN = FIELD__PORTFOLIO__NAME + "_" + Portfolio.FIELD__OID__NAME;

    public static final String FIELD__MODERATION_STATUS__NAME = "moderationStatus";

    public static final String FIELD__AREA_USER_RLM__NAME = "areaUserRlm";

    public static final String FIELD__CONTENT_STATUS__NAME = "contentStatus";
    public static final String FIELD__LAST_UPDATE_DATETIME__NAME = "lastUpdateDatetime";

    public static final String FIELD__PRETTY_URL_STRING__NAME = "prettyUrlString";
    public static final String FIELD__PRETTY_URL_STRING__COLUMN = FIELD__PRETTY_URL_STRING__NAME;

    public static final String FIELD__COMPOSITION_PARTITION_OID__NAME = "compositionPartitionOid";
    public static final String FIELD__COMPOSITION_PARTITION_OID__COLUMN = FIELD__COMPOSITION_PARTITION_OID__NAME;

    private static final String HAS_JIT_INITED_QUALITY_RATING_PROPERTY = Content.class.getName() + "-HasJITInitedQualityRating";
    private static final String HAS_JIT_INITED_AGE_RATING_PROPERTY = Content.class.getName() + "-HasJITInitedAgeRating";

    @Deprecated
    public Content() {
        isNew = false;
    }

    public Content(Partition compositionPartition, ContentType contentType, Timestamp creationDatetime, ContentConsumer contentConsumer) {
        this.oid = contentConsumer.getOid();
        this.channelContents = newHashMap();
        this.nicheChannelContents = new HashSet<>();
        setCompositionPartitionOid(compositionPartition.getOid());
        setModerationStatus(ModerationStatus.APPROVED);
        setContentType(contentType);
        setLiveDatetime(creationDatetime);
        setLastUpdateDatetime(getLiveDatetime());
        setContentStats(new ContentStats(this));
        setAllowReplies(true);
        this.contentStatus = new IntBitmaskType<>();
        // jw: let's default this to GENERAL and allow the content creation framework to adjust it as needed.
        authorAgeRating = AgeRating.GENERAL;
        ageRating = authorAgeRating;

        qualityRatingFields = new QualityRatingFields(true);
        ageRatingFields = new AgeRatingFields();
        isNew = true;
    }

    @Id
    @Override
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @Transient
    public boolean isNew() {
        return isNew;
    }

    /**
     * bl: changed so that the min subject length for Hibernate validation purposes is actually 0.  otherwise,
     * we'll get errors when doing mobile posts with no subject that would prevent posts from going through.
     */
    @NotNull
    @Length(min = 0, max = NetworkConstants.MAX_SUBJECT_LENGTH)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
        // bl: if a new subject is being set, set the displaySubject to null.
        // this will ensure the displaySubject will be recalculated accordingly
        // based on the new value for the subject.
        displaySubject = null;
        shortDisplaySubject = null;
    }

    @Transient
    public String getSubjectForHtml() {
        return HtmlTextMassager.disableHtml(getSubject());
    }

    @Transient
    public String getDisplaySubject() {
        if (displaySubject == null) {
            assert !isEmpty(getSubject()) : "Should always have a subject for all content! contentType/" + getContentType() + " oid/" + getOid();
            displaySubject = getSubject();
        }

        return displaySubject;
    }

    public static final int MAX_SHORT_TITLE_LENGTH = 50;

    @Transient
    public String getShortDisplaySubject() {
        if (shortDisplaySubject == null) {
            shortDisplaySubject = CoreUtils.elipse(getDisplaySubject(), MAX_SHORT_TITLE_LENGTH);
        }

        return shortDisplaySubject;
    }

    public static final int MAX_SUBTITLE_LENGTH = NetworkConstants.MAX_SUBJECT_LENGTH;

    @Length(min = 0, max = MAX_SUBTITLE_LENGTH)
    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public static final int MIN_EXTRACT_LENGTH = 0;
    public static final int MAX_EXTRACT_LENGTH = 500;

    @NotNull
    @Length(min = MIN_EXTRACT_LENGTH, max = MAX_EXTRACT_LENGTH)
    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }

    @Transient
    public String getExtractResolved() {
        return MentionsUtil.getLinkedHtml(getExtract());
    }

    @Transient
    public String getExtractForOgDescription() {
        // bl: only include the extract for OpenGraph description metadata when there is no subtitle.
        if(!isEmpty(getSubTitle())) {
            return null;
        }
        String plainTextExtract = MentionsUtil.getPlainTextMentions(getExtract());
        // bl: align the extract length to match the subtitle max length since this extract is only used
        // when there is no subtitle
        return IPStringUtil.getStringTruncatedToEndOfWord(plainTextExtract, MAX_SUBTITLE_LENGTH);
    }

    @Transient
    @Override
    public String getExtractForEmail() {
        return getExtractResolved();
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    @Type(type = IntegerEnumType.TYPE)
    @Column(columnDefinition = "tinyint")
    @NotNull
    public AgeRating getAuthorAgeRating() {
        return authorAgeRating;
    }

    public void setAuthorAgeRating(AgeRating authorAgeRating) {
        this.authorAgeRating = authorAgeRating;
    }

    @Transient
    public AgeRating getAuthorAgeRatingForEdit() {
        // bl: when editing a post, we only include the author AgeRating when the current user can edit it.
        // this is used to indicate whether the Age Rating tool should be displayed on the page or not
        // bl: if the primary channel is locked, then only Publication editors can edit, so we don't want to
        // include the age rating.
        return isPrimaryChannelLocked() ? null : getAuthorAgeRating();
    }

    public void updateAuthorAgeRating(AgeRating ageRating) {
        // jw: short out if they are the same, since there is nothing to do.
        if (getAuthorAgeRating() == ageRating) {
            return;
        }

        setAuthorAgeRating(ageRating);

        onRatingUpdate(RatingType.AGE);
    }

    @Type(type = IntegerEnumType.TYPE)
    @Column(columnDefinition = "tinyint")
    @NotNull
    @Override
    public AgeRating getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(AgeRating ageRating) {
        this.ageRating = ageRating;
    }

    @Transient
    @Override
    public Area getArea() {
        return getPortfolio().getArea();
    }

    @Transient
    @Override
    public AuthZone getAuthZone() {
        return getArea().getAuthZone();
    }

    @Transient
    @Override
    public boolean isVetoSearchResult(PrimaryRole role) {
        return !isContentLive() || !hasViewRight(role.getAreaRoleForArea(getArea()));
    }

    @NotNull
    public OID getCompositionPartitionOid() {
        return compositionPartitionOid;
    }

    public void setCompositionPartitionOid(OID compositionPartitionOid) {
        this.compositionPartitionOid = compositionPartitionOid;
    }

    @Transient
    public Partition getCompositionPartition() {
        return Partition.dao().get(getCompositionPartitionOid());
    }

    @Transient
    @Override
    public void setCompositionPartition(Partition compositionPartition) {
        throw UnexpectedError.getRuntimeException("Should never attempt to set a composition partition on content after its been created!");
    }

    @Transient
    @Override
    public OID getCompositionOid() {
        return getOid();
    }

    @Transient
    public Composition getComposition() {
        return Composition.dao().get(getOid());
    }

    /**
     * This is a method which keeps a cache of composition items related to this content.  It can only be inited
     * when the correct composition is in scope, but it can live past the task it's in as long as the composition
     * session is still open.  Items in the cache can either be retrieved just-in-time or they can be prepopulated in
     * batch.
     *
     * @return
     */
    @Transient
    @Override
    public CompositionCache getCompositionCache() {
        if (compositionCache != null) {
            return compositionCache;
        }
        assert PartitionType.COMPOSITION.hasCurrentSession() && isEqual(PartitionType.COMPOSITION.currentPartitionOid(), getCompositionPartitionOid()) : "Can't call getCompositionCache() without being in the scope of the proper Composition database first, or without having called initCompositionCache!";
        initCompositionCache(PartitionType.COMPOSITION.currentSession());

        return compositionCache;
    }

    @Transient
    public boolean isHasCompositionCache() {
        return compositionCache != null;
    }

    @Transient
    @Override
    public void initCompositionCache(GSession compositionSession) {
        this.compositionCache = new CompositionCache(getOid(), compositionSession);
    }

    @NotNull
    public boolean isAllowReplies() {
        return allowReplies;
    }

    public void setAllowReplies(boolean allowReplies) {
        this.allowReplies = allowReplies;
    }

    @Transient
    @Override
    public boolean isAllowRepliesResolved() {
        // bl: make sure that we handle ContentTypes that never allow replies.
        return getContentType().isAllowsReplies() && isAllowReplies();
    }

    @Transient
    public boolean isDisabled() {
        return getContentStatus().isThis(ContentStatus.DISABLED);
    }

    public void setDisabled(boolean disabled) {
        getContentStatus().set(ContentStatus.DISABLED, disabled);
    }

    @NotNull
    //@Temporal(TemporalType.TIMESTAMP)
    @Index(name = "content_live_datetime_idx")
    public Timestamp getLiveDatetime() {
        return liveDatetime;
    }

    public void setLiveDatetime(Timestamp liveDatetime) {
        this.liveDatetime = liveDatetime;
    }

    public void updateLiveDatetime(Timestamp liveDatetime) {
        setLiveDatetime(liveDatetime);

        setLastUpdateDatetime(liveDatetime);
    }

    @NotNull
    //@Temporal(TemporalType.TIMESTAMP)
    @Index(name = "content_last_update_datetime_idx")
    public Timestamp getLastUpdateDatetime() {
        return lastUpdateDatetime;
    }

    public void setLastUpdateDatetime(Timestamp lastUpdateDatetime) {
        this.lastUpdateDatetime = lastUpdateDatetime;
    }

    public void syncStats(CompositionStats compositionStats) {
        setLastUpdateDatetime(compositionStats.getLastUpdateDatetime());
    }

    @Transient
    public Timestamp getItemDatetimeForSearchIndex() {
        return getLiveDatetime();
    }

    @Transient
    public boolean isDraft() {
        return getContentStatus().isThis(ContentStatus.DRAFT);
    }

    public void setDraft(boolean draft) {
        getContentStatus().set(ContentStatus.DRAFT, draft);
    }

    /**
     * determine if this content is live.  it is live as long as it is not one of the following:
     * 1) a draft
     * 2) a future publication
     * 3) a moderated piece of content
     *
     * @return true if the content is live.  false if it is not.
     */
    @Transient
    public boolean isContentLive() {
        if (getContentStatus().isThis(ContentStatus.ACTIVE) && getModerationStatus().isLive()) {
            return true;
        }
        return false;
    }

    @NotNull
    @Type(type = StringEnumType.TYPE)
    // despite the NotNull annotation, this field isn't currently being defined
    // as NotNull, presumably due to the columnDefinition below.
    @Column(columnDefinition = ModerationStatus.ENUM_FIELD_TYPE)
    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    @Override
    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_content_portfolio")
    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    @Transient
    public AreaRlm getAreaRlm() {
        return getPortfolio().getAreaRlm();
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @ForeignKey(name = "FK9BEFCC59A04A123C")
    public AreaUserRlm getAreaUserRlm() {
        return areaUserRlm;
    }

    public void setAreaUserRlm(AreaUserRlm areaUserRlm) {
        this.areaUserRlm = areaUserRlm;
    }

    @Transient
    public boolean isAreaUserAuthor() {
        return exists(getAreaUserRlm());
    }

    @Transient
    public AreaUser getAreaUser() {
        return isAreaUserAuthor() ? AreaUserRlm.getAreaUser(getAreaUserRlm()) : null;
    }

    @Transient
    public User getUser() {
        return isAreaUserAuthor() ? getAreaUser().getUser() : null;
    }

    @Override
    @Transient
    public User getAuthor() {
        return getUser();
    }

    @Transient
    public boolean isAuthorAllowed() {
        return true;
    }

    public static final int MIN_GUEST_NAME_LENGTH = User.MIN_DISPLAY_NAME_LENGTH;
    public static final int MAX_GUEST_NAME_LENGTH = User.MAX_DISPLAY_NAME_LENGTH;

    @Length(min = MIN_GUEST_NAME_LENGTH, max = MAX_GUEST_NAME_LENGTH)
    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    @Transient
    @Override
    public String getGuestNameResolved() {
        return getGuestName();
    }

    private transient PrimaryRole primaryRole;

    @Override
    @Transient
    public PrimaryRole getPrimaryRole() {
        if (primaryRole == null) {
            // bl: if this is an anonymous author post, suppress the guest label since it's redundant.
            primaryRole = PrimaryRole.getPrimaryRole(getArea().getAuthZone(), getUser(), getGuestNameResolved());
        }
        return primaryRole;
    }

    @Transient
    @Override
    public PrimaryRole getRealAuthorPrimaryRole() {
        User realAuthor = getRealAuthor();
        if (exists(realAuthor)) {
            return realAuthor;
        }
        return getPrimaryRole();
    }

    @Transient
    @Override
    public User getRealAuthor() {
        return getUser();
    }

    @NotNull
    public IntBitmaskType<ContentStatus> getContentStatus() {
        return contentStatus;
    }

    public void setContentStatus(IntBitmaskType<ContentStatus> contentStatus) {
        this.contentStatus = contentStatus;
    }

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = Content.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME))
    public ContentStats getContentStats() {
        return contentStats;
    }

    public void setContentStats(ContentStats contentStats) {
        this.contentStats = contentStats;
    }

    @Transient
    @Override
    public boolean isFeatured() {
        // bl: posts are featured if there is a featured until datetime
        return getFeaturedUntilDatetime()!=null;
    }

    @Transient
    @Override
    public boolean isSupportsQualityRatingReplies() {
        return getContentType().isSupportsQualityRatingReplies();
    }

    /**
     * @return the map of Channel to ChannelContent
     * @deprecated hibernate bug.  Use getChannelContentsInited
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = ChannelContent.Fields.content, cascade = javax.persistence.CascadeType.ALL)
    @MapKey(name = ChannelContent.Fields.channel)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<Channel, ChannelContent> getChannelContents() {
        return channelContents;
    }

    public void setChannelContents(Map<Channel, ChannelContent> channelContents) {
        this.channelContents = channelContents;
    }

    @Transient
    public Map<Channel, ChannelContent> getChannelContentsInited() {
        return initHibernateMap(getChannelContents());
    }

    @Transient
    public Collection<ChannelContent> getChannelContentsCollection() {
        return getChannelContentsInited().values();
    }

    /**
     * this duplicate OneToMany is just so that we have a separate association path that we can join through for ContentList.
     * if we try to join to channelContents twice from the same Criteria object, we get:
     * org.hibernate.QueryException: duplicate association path: channelContents
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = ChannelContent.Fields.content)
    public Set<ChannelContent> getNicheChannelContents() {
        return nicheChannelContents;
    }

    public void setNicheChannelContents(Set<ChannelContent> nicheChannelContents) {
        this.nicheChannelContents = nicheChannelContents;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = TrendingContent.FIELD__CONTENT)
    @Cascade({CascadeType.REMOVE})
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public Set<TrendingContent> getTrendingContents() {
        return trendingContents;
    }

    public void setTrendingContents(Set<TrendingContent> trendingContents) {
        this.trendingContents = trendingContents;
    }

    @Transient
    public TrendingContent getCurrentTrendingContent() {
        return TrendingContent.dao().getCurrentTrendingContent(this);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = ModeratedContent.Fields.oid)
    @Cascade({CascadeType.REMOVE})
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public Set<ModeratedContent> getModeratedContents() {
        return moderatedContents;
    }

    public void setModeratedContents(Set<ModeratedContent> moderatedContents) {
        this.moderatedContents = moderatedContents;
    }

    @Transient
    public ModeratedContent getModeratedContent() {
        return ModeratedContent.dao().get(getOid());
    }

    public void approveContent() {
        ModeratedContent moderatedContent = getModeratedContent();
        if(exists(moderatedContent)) {
            ModeratedContent.dao().delete(moderatedContent);
        }
        // bl: finally, set the post status and the ChannelContent status to APPROVED
        setModerationStatus(ModerationStatus.APPROVED);
        getPrimaryChannelContent().approvePost();
    }

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "fk_content_futureContent")
    public FutureContent getFutureContent() {
        return futureContent;
    }

    public void setFutureContent(FutureContent futureContent) {
        this.futureContent = futureContent;
    }

    @Override
    @Length(min = NetworkConstants.MIN_PRETTY_URL_STRING_LENGTH, max = NetworkConstants.MAX_PRETTY_URL_STRING_LENGTH)
    public String getPrettyUrlString() {
        return prettyUrlString;
    }

    public void setPrettyUrlString(String prettyUrlString) {
        this.prettyUrlString = prettyUrlString;
    }

    public QualityRatingFields getQualityRatingFields() {
        return qualityRatingFields;
    }

    public void setQualityRatingFields(QualityRatingFields qualityRatingFields) {
        this.qualityRatingFields = qualityRatingFields;
    }

    private transient boolean hasInitedQualityRatingByCurrentUser;
    private transient QualityRating qualityRatingByCurrentUser;
    @Transient
    public QualityRating getQualityRatingByCurrentUser() {
        if (qualityRatingByCurrentUser ==null && !hasInitedQualityRatingByCurrentUser) {
            // jw: ensure that we only do this once!
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_QUALITY_RATING_PROPERTY);

            if (networkContext().isLoggedInUser()) {
                UserQualityRatedComposition rating = UserQualityRatedComposition.dao().getRatingForUser(
                        networkContext().getUser(),
                        this
                );
                qualityRatingByCurrentUser = exists(rating) ? rating.getQualityRating() : null;
            }
            hasInitedQualityRatingByCurrentUser = true;
        }

        return qualityRatingByCurrentUser;
    }

    public void setQualityRatingByCurrentUser(QualityRating qualityRatingByCurrentUser) {
        this.hasInitedQualityRatingByCurrentUser = true;
        this.qualityRatingByCurrentUser = qualityRatingByCurrentUser;
    }

    public void setAgeRatingByCurrentUser(AgeRating ageRatingByCurrentUser) {
        this.hasInitedAgeRatingByCurrentUser = true;
        this.ageRatingByCurrentUser = ageRatingByCurrentUser;
    }

    public AgeRatingFields getAgeRatingFields() {
        return ageRatingFields;
    }

    public void setAgeRatingFields(AgeRatingFields ageRatingFields) {
        this.ageRatingFields = ageRatingFields;
    }

    private transient boolean hasInitedAgeRatingByCurrentUser;
    private transient AgeRating ageRatingByCurrentUser;
    @Transient
    public AgeRating getAgeRatingByCurrentUser() {
        // bl: if the user is the author of the post, then return null; don't ever want to show the "internal"
        // age rating by the author externally as if it's a normal vote.
        // bl: unless the post's primary channel is locked, in which case we treat the author like all other users
        if(exists(getUser()) && getUser().isCurrentUserThisUser() && !isPrimaryChannelLocked()) {
            return null;
        }

        if (ageRatingByCurrentUser==null && !hasInitedAgeRatingByCurrentUser) {
            // jw: ensure that we only do this once!
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_AGE_RATING_PROPERTY);

            if (networkContext().isLoggedInUser()) {
                UserAgeRatedComposition rating = UserAgeRatedComposition.dao().getRatingForUser(
                        networkContext().getUser(),
                        this
                );
                ageRatingByCurrentUser = exists(rating) ? rating.getAgeRating() : null;
            }
            hasInitedAgeRatingByCurrentUser = true;
        }

        return ageRatingByCurrentUser;
    }

    @Transient
    public String getIdForUrl() {
        return !isEmpty(getPrettyUrlString()) ? getPrettyUrlString() : getOid().toString();
    }

    @OneToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_content_avatarImageOnDisk")
    public ImageOnDisk getAvatarImageOnDisk() {
        return avatarImageOnDisk;
    }

    public void setAvatarImageOnDisk(ImageOnDisk avatarImageOnDisk) {
        this.avatarImageOnDisk = avatarImageOnDisk;
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public String getFullText(boolean includeFileContents) {
        StringBuilder fullText = new StringBuilder();
        fullText.append(getSubject());
        fullText.append(" ");
        fullText.append(getExtractResolved());
        return fullText.toString();
    }

    public void checkViewRight(AreaRole areaRole) {
        checkViewRight(areaRole, this);
    }

    @Transient
    public boolean isDoesCurrentUserHaveViewRight() {
        return hasViewRight(getArea().getAreaRoleForCurrentUser());
    }

    @Transient
    public boolean hasViewRight(AreaRole areaRole) {
        return hasViewRight(areaRole, this);
    }

    @Override
    public boolean hasViewRepliesRight(AreaRole areaRole) {
        return hasViewRepliesRight(areaRole, this);
    }

    public static void checkViewRight(final AreaRole areaRole, final Content content) {
        assert isEqual(areaRole.getArea(), content.getArea()) : "Areas must match for view content security checks! content/" + content.getOid() + " contentArea/" + content.getArea().getOid() + " areaRole/" + areaRole.getOid() + " areaRoleArea/" + areaRole.getArea().getOid();

        checkContentTypeViewAccess(areaRole, content);

        // jw: this will make sure to check the appropriate permissions and give a valuable access violation if the content
        //     is not visible or accessible.
        ModeratedCompositionConsumerViolation.checkViewRight(content, areaRole);
    }

    private static void checkContentTypeViewAccess(AreaRole areaRole, Content content) {
        ContentType contentType = content.getContentType();

        // jw: any service methods that rely on this utility method should all be fine to get this error.
        if (!areaRole.getPrimaryRole().getPermittedAgeRatings().contains(content.getAgeRating())) {
            // jw: finally, let's allow authors to see their own posts, even if they would not normally meet the age
            //     limit.
            if (!exists(content.getAuthor()) || !areaRole.isActiveRegisteredAreaUser() || !isEqual(content.getAuthor(), areaRole.getUser())) {
                throw new AccessViolation(wordlet(
                        "postServiceImpl.cannotAccessRestrictedContent",
                        AgeRating.RESTRICTED.getMinimumAgeYears()
                ));
            }
        }
    }

    public static boolean hasViewRight(AreaRole areaRole, Content content) {
        try {
            checkViewRight(areaRole, content);
            return true;

        } catch (AccessViolation av) {
            return false;
        }
    }

    public static boolean hasViewRepliesRight(AreaRole areaRole, Content content) {
        if (!hasViewRight(areaRole, content)) {
            return false;
        }

        // bl: for all other content types, if you can view the content, you can also view replies!
        return true;
    }

    @Transient
    public boolean isDoesCurrentUserHaveRightToReply() {
        return hasReplyRight(getCurrentAreaRole());
    }

    @Transient
    @Override
    public boolean hasReplyRight(AreaRole areaRole) {
        try {
            checkRightToReply(areaRole);
            return true;
        } catch (AccessViolation av) {
            return false;
        }
    }

    public void checkRightToReply(AreaRole areaRole) {
        // bl: at this point, content must not be closed.
        if (!isAllowReplies()) {
            throw new AccessViolation(wordlet("error.accessViolation.youDoNotHaveRightToReply"));
        }

        // must be able to view the content first and foremost.
        checkViewRight(areaRole);

        // bl: must be able to view replies in order to reply!
        if (!hasViewRepliesRight(areaRole)) {
            throw new AccessViolation(wordlet("error.accessViolation.youDoNotHaveRightToReply"));
        }
    }

    /**
     * Returns true if the content can be managed by the current user
     */
    @Transient
    public boolean isManageableByCurrentUser() {
        return isManageableByAreaRole(getCurrentAreaRole());
    }

    @Override
    public void checkManageable(AreaRole areaRole) {
        checkModeratableByAreaRole(areaRole);
    }

    private transient boolean ignoreEditableDeletableChecksForDto = false;

    public void setIgnoreEditableDeletableChecksForDto() {
        ignoreEditableDeletableChecksForDto = true;
    }

    @Transient
    @Override
    public boolean isEditableByCurrentUser() {
        return isCanCurrentUserEdit();
    }

    @Transient
    public Boolean isEditableByCurrentUserBoolean() {
        if(ignoreEditableDeletableChecksForDto) {
            return null;
        }
        boolean canEdit = isCanCurrentUserEdit();
        return canEdit ? true : null;
    }

    @Override
    @Transient
    public boolean isDeletableByCurrentUser() {
        return isCurrentUserAbleToDelete();
    }

    @Transient
    public Boolean isDeletableByCurrentUserBoolean() {
        if(ignoreEditableDeletableChecksForDto) {
            return null;
        }
        boolean canDelete = isCurrentUserAbleToDelete();
        return canDelete ? true : null;
    }

    /**
     * Returns true if the content can be managed by the current user
     */
    @Transient
    public boolean isRepliesEditableByCurrentUser() {
        // bl: only the author can ever edit replies in Narrative
        if(getContentType().isNarrativePost()) {
            return false;
        }
        return isManageableByCurrentUser();
    }

    /**
     * Returns true if all replies can be deleted by the current user
     */
    @Transient
    public boolean isRepliesDeletableByCurrentUser() {
        if(getContentType().isNarrativePost()) {
            // bl: for narrative posts, the only time someone can delete all replies is if they are a publication editor
            // and the post is live in the publication. note that if it's pending publication approval, then editors
            // can not yet delete replies
            return isManageableByCurrentUser() && getPrimaryChannelContent().getStatus().isApproved();
        }
        return isManageableByCurrentUser();
    }

    public boolean isManageableByAreaRole(AreaRole areaRole) {
        assert isEqual(areaRole.getArea(), getArea()) : "Area mismatch when attempting security check for content management";

        try {
            checkModeratableByAreaRole(areaRole);
            return true;
        } catch (AccessViolation av) {
            return false;
        }
    }

    public void checkModeratableByAreaRole(AreaRole areaRole) {
        assert isEqual(areaRole.getArea(), getArea()) : "Area mismatch when attempting security check for content management";

        // bl: for Narrative posts, moderation now applies to Publication editors
        if (getContentType().isNarrativePost()) {
            Publication publication = getSubmittedToPublication();
            // bl: if it's published to a publication, then publication editors have moderate rights
            if(exists(publication)) {
                // bl: if the user is an editor, then they are a moderator of this post
                if(publication.isDoesPrimaryRoleHaveAccess(areaRole.getPrimaryRole(), PublicationRole.EDITOR)) {
                    return;
                }
            }
            // bl: this always throws an exception since nobody has global moderate permissions in narrative
            getContentType().checkModerateRight(areaRole);
            return;
        }

        assert false : "Found unsupported ContentType: " + getContentType();
    }

    @Transient
    public boolean isCurrentUserAuthor() {
        return isEqualOrNull(getRealAuthor(), networkContext().getUser());
    }

    @Transient
    public boolean isCurrentUserAbleToDelete() {
        if(getContentType().isNarrativePost()) {
            if (exists(getRealAuthor()) && isCurrentUserAuthor()) {
                // bl: if the current user is the author, allow deletion unless it has been submitted to a publication,
                // which can be identified if the primary channel is locked
                return !isPrimaryChannelLocked();
            }

            // bl: nobody else can delete narrative posts other than the author, so not doing a manageable check here
            return false;
        }

        if (isManageableByCurrentUser()) {
            return true;
        }

        // for content types that don't support authors (chats, KB entries, support docs), you can't delete if you can't manage.
        if (!getContentType().isSupportsAuthor()) {
            return false;
        }

        // if this has a author:
        if (exists(getRealAuthor()) && isCurrentUserAuthor()) {
            // always allow authors to delete their posts.
            return true;
        }

        return false;
    }

    @Transient
    public boolean isModerated() {
        return getModerationStatus().isModerated();
    }

    @Transient
    public FileMetaDataProvider getPrimaryPictureProvider() {
        return getAvatarImageOnDisk();
    }

    @Transient
    public FileConsumerType getFileConsumerType() {
        return FileConsumerType.AREA_CONTENT;
    }

    @Transient
    public String getFileUrlBase() {
        return getArea().getPrimaryAreaUrl();
    }

    @Transient
    public FileMetaDataProvider getFileMetaDataProvider(boolean isPrimaryPicture) {
        assert isPrimaryPicture : "Should only attempt to get chat event primary picture!";
        return getPrimaryPictureProvider();
    }

    public FileConsumerFileInfo getFileInfo(PrimaryRole currentRole, final OID filePointerOid, boolean primaryPicture) throws AccessViolation {
        AccessViolation accessViolation = null;

        //security check
        try {
            checkGeneralCompositionConsumerAccess(currentRole);
        } catch (AccessViolation e) {
            accessViolation = e;
        }

        return getFileConsumerFileInfoForContent(this, accessViolation, filePointerOid, primaryPicture);
    }

    public static FileConsumerFileInfo getFileConsumerFileInfoForContent(final Content content, AccessViolation accessViolation, final OID filePointerOid, boolean primaryPicture) {
        FileOnDisk fileOnDisk;
        FilePointer filePointer;

        // bl: now that we are using primaryPicture for video thumbnails, too, we need to first do out lookup by filePointerOid
        // if one was supplied.  this way, we will be sure to get the proper FilePointer from which to get the primary picture.
        // previously, we were testing primaryPicture first, which meant always getting the FilePointer for the first
        // image on the content, even if a different filePointerOid had been supplied.
        if (filePointerOid != null) {
            // assume that we're in an area in this case.
            filePointer = networkContext().doCompositionTask(content.getCompositionPartition(), new CompositionTaskImpl<FilePointer>(false) {
                protected FilePointer doMonitoredTask() {
                    FilePointer filePointer = FilePointer.dao().get(filePointerOid);
                    if (!exists(filePointer)) {
                        throw UnexpectedError.getIgnorableRuntimeException("Attempted to get a filePointer oid/" + filePointerOid + " for content oid/" + content.getOid() + " but didn't find FilePointer in composition partition oid/" + content.getCompositionPartition().getOid());
                    }
                    if (!IPUtil.isEqual(content.getOid(), filePointer.getFilePointerSet().getCompositionOid())) {
                        throw UnexpectedError.getIgnorableRuntimeException("Attempted to get a filePointer for content c/" + content.getOid() + " that belonged to a different piece of content c/" + filePointer.getFilePointerSet().getCompositionOid() + " fps/" + filePointer.getFilePointerSet().getOid());
                    }
                    return filePointer;
                }
            });
            fileOnDisk = filePointer.getFileOnDisk();
        } else {
            if (!primaryPicture) {
                throw UnexpectedError.getIgnorableRuntimeException("Attempted to getFileInfo for Content, but didn't request primaryPicture and filePointerOid was null!");
            }
            FileMetaDataProvider primaryPictureFilePointer = content.getPrimaryPictureProvider();
            if (exists(primaryPictureFilePointer)) {
                fileOnDisk = primaryPictureFilePointer.getFileOnDisk();
            } else {
                // no fileOnDisk found for this request, so we'll return a file not found or inline image FNF.
                fileOnDisk = null;
            }
            if (isOfType(primaryPictureFilePointer, FilePointer.class)) {
                filePointer = cast(primaryPictureFilePointer, FilePointer.class);
            } else {
                filePointer = null;
            }
        }

        if (exists(filePointer)) {
            return new FileConsumerFileInfo(accessViolation, filePointer);
        } else {
            return new FileConsumerFileInfo(accessViolation, fileOnDisk);
        }
    }

    @Override
    public void checkGeneralCompositionConsumerAccess(PrimaryRole primaryRole) {
        checkViewRight(primaryRole.getAreaRoleForArea(getArea()));
    }

    @Transient
    public boolean isCanCurrentUserEdit() {
        try {
            checkEditRightForCurrentUser();
            return true;
        } catch (AccessViolation av) {
            return false;
        }
    }

    @Transient
    public void checkEditRightForCurrentUser() {
        // moderators can edit!
        if (isManageableByCurrentUser()) {
            return;
        }

        if (!exists(getRealAuthor()) || !isCurrentUserAuthor()) {
            throw new AccessViolation(wordlet("post.cannotEditOthers"));
        }

        if(getContentType().isNarrativePost()) {
            // bl: if the post has been submitted to a publication (i.e. primary channel is locked), then it can not be edited by the author
            if(isPrimaryChannelLocked()) {
                throw new AccessViolation(wordlet("post.cannotEditPublicationPost"));
            }
        }

        // bl: always allow edits of your own drafts
        if (isDraft()) {
            return;
        }

        // bl: at this point, content must not be closed. if it's closed, then you can't edit on the basis of
        // being the original author (or edit grace period). instead, the "manageable" check will have to grant you access
        // jw: we will allow authors of narrative posts to edit then regardless if the post allows comments or not.
        if (!isAllowReplies() && !getContentType().isNarrativePost()) {
            throw new AccessViolation(wordlet("post.cannotEdit"));
        }
    }

    @Transient
    @Override
    public String getTitle() {
        return getSubject();
    }

    @Transient
    public String getEditContentUrl() {
        assert getContentType().isNarrativePost() : "Only support Narrative posts!";
        return ReactRoute.CREATE_POST.getUrl(getOid().toString());
    }

    @Transient
    @Override
    public FileUsageType getAttachmentFileUsageType() {
        return getContentType().getAttachmentFileUsageType();
    }

    @Transient
    @Override
    public String getTypeNameForDisplay() {
        return getContentType().getNameForDisplay();
    }

    @Transient
    @Override
    public String getTypeLowercaseNameForDisplay() {
        return getContentType().getNameForDisplayLowercase();
    }

    @Override
    @Transient
    public CompositionConsumerType getCompositionConsumerType() {
        return getContentType().getCompositionConsumerType();
    }

    @Transient
    @Override
    public ContentStats getStats() {
        return getContentStats();
    }

    @Transient
    @Override
    public ContentStats getStatsForUpdate() {
        return ContentStats.dao().lock(getContentStats());
    }

    @Transient
    @Override
    public String getTitleForDisplay() {
        return getDisplaySubject();
    }

    @Transient
    @Override
    public boolean isHasComposition() {
        return true;
    }

    @Transient
    @Override
    public boolean isLive() {
        return isContentLive();
    }

    @Transient
    @Override
    public boolean isDeleted() {
        return getContentStatus().isThis(ContentStatus.DELETED);
    }

    @Transient
    @Override
    public int getModeratedReplyCount() {
        return getContentStats().getModeratedReplyCount();
    }

    @Transient
    @Override
    public String getDisplayUrl() {
        return getDisplayUrl(getIdForUrl());
    }

    @Transient
    @Override
    public String getPermalinkUrl() {
        return getDisplayUrl(getOid());
    }

    private String getDisplayUrl(Object id) {
        return ReactRoute.POST.getUrl(id.toString());
    }

    @Transient
    @Override
    public CompositionType getCompositionType() {
        return CompositionType.CONTENT;
    }

    @Transient
    private AreaRole getCurrentAreaRole() {
        // bl: can't necessarily get the AreaRole off of the AreaContext since we might be displaying group content
        // on the main site (e.g. in member profile activity streams). so, ensure that we always grab the AreaRole
        // for the Area in which this Content was posted.
        return networkContext().getPrimaryRole().getAreaRoleForArea(getArea());
    }

    @Override
    public String getDisplayReplyUrl(OID replyOid) {
        // jw: per https://java.net/jira/browse/JAVASERVERFACES-3353 we will delegate to the interfaces default method to get around EL compiler issues
        // bl: this is also needed in order to get around VerifyErrors ("Illegal use of nonvirtual function call") in Javassist
        return Moderatable.super.getDisplayReplyUrl(replyOid);
    }

    @Transient
    @Override
    public Timestamp getLiveDatetimeForSort() {
        // jw: per https://java.net/jira/browse/JAVASERVERFACES-3353 we will delegate to the interfaces default method to get around EL compiler issues
        // bl: this is also needed in order to get around VerifyErrors ("Illegal use of nonvirtual function call") in Javassist
        return Moderatable.super.getLiveDatetimeForSort();
    }

    @Transient
    public boolean isHasTitleImage() {
        assert getContentType().isSupportsTitleImage() : "Should only ever get the title image for content that supports it! ct/" + getContentType();

        return exists(getAvatarImageOnDisk());
    }

    @Transient
    public String getTitleImageUrl() {
        return getTitleImageUrl(ImageType.MEDIUM);
    }

    @Transient
    public String getTitleImageLargeUrl() {
        return getTitleImageUrl(ImageType.LARGE);
    }

    @Transient
    public Integer getTitleImageLargeWidth() {
        if (!isHasTitleImage()) {
            return null;
        }
        ImageDimensions imageDimensions = getImageDimensionsForResizedImage(getAvatarImageOnDisk(), ImageType.LARGE);
        return imageDimensions.getWidth();
    }

    @Transient
    public Integer getTitleImageLargeHeight() {
        if (!isHasTitleImage()) {
            return null;
        }
        ImageDimensions imageDimensions = getImageDimensionsForResizedImage(getAvatarImageOnDisk(), ImageType.LARGE);
        return imageDimensions.getHeight();
    }

    @Transient
    public String getTitleImageSquareUrl() {
        return getTitleImageUrl(ImageType.LARGE_SQUARE_THUMBNAIL);
    }

    private String getTitleImageUrl(ImageType imageType) {
        if (!isHasTitleImage()) {
            return null;
        }

        // jw: Since files are now served by Google Cloud Storage, let's allow the ImageOnDisk to give us our URL.
        return getAvatarImageOnDisk().getImageUrl(imageType);
    }

    @Override
    public void storeJobData(JobBuilder jobBuilder) {
        // jw: this is needed in order to get around VerifyErrors ("Illegal use of nonvirtual function call") in Javassist
        Moderatable.super.storeJobData(jobBuilder);
    }

    @Transient
    public boolean isPublishedToPersonalJournal() {
        assert getContentType().isNarrativePost() : "Should only ever call this method for Narrative Posts!";

        // jw: not sure how this
        if (!exists(getAuthor())) {
            throw UnexpectedError.getRuntimeException("Should always have an author!");
        }

        // jw: pretty simple, if the authors personal journal channel is in the list, return it.
        return getChannelContentsInited().containsKey(getAuthor().getPersonalJournal().getChannel());
    }

    private transient boolean includePublicationForModeratedContent;

    public void setIncludePublicationForModeratedContent(boolean includePublicationForModeratedContent) {
        this.includePublicationForModeratedContent = includePublicationForModeratedContent;
    }

    @Transient
    public Publication getPublishedToPublication() {
        // bl: this method is only used to drive DTOs via the API. as such, there are scenarios where we need
        // to include the publication: the user's pending posts list and the EditPostDetailDTO.
        if(includePublicationForModeratedContent) {
            // bl: include the publication regardless of what its status is
            return getSubmittedToPublication();
        }
        return getSubmittedToPublication(NarrativePostStatus.APPROVED);
    }

    @Transient
    public Publication getSubmittedToPublication() {
        return getSubmittedToPublication(null);
    }

    private Publication getSubmittedToPublication(NarrativePostStatus status) {
        assert getContentType().isNarrativePost() : "Should only ever call this method for Narrative Posts!";

        // bl: have to look through the posted channels and find the primary
        ChannelContent channelContent = getPrimaryChannelContent();
        if(channelContent!=null) {
            // bl: only return Publication if it matches the given status.
            // bl: note that the status here will never be blocked since primaryChannelContent excludes blocked posts
            if(status==null || status==channelContent.getStatus()) {
                // bl: Channel.getPublication will return null if it's not a Publication
                return channelContent.getChannel().getPublication();
            }
        }
        return null;
    }

    @Transient
    public Boolean isPendingPublicationApproval() {
        ChannelContent primaryChannelContent = getPrimaryChannelContent();
        if(!exists(primaryChannelContent)) {
            return null;
        }
        if(!primaryChannelContent.getChannel().getType().isPublication()) {
            return null;
        }
        if(!primaryChannelContent.getStatus().isModerated()) {
            return null;
        }
        // jw: only expose this field to the author, and people who can manage the content.
        if(!getAuthor().isCurrentUserThisUser() && !isManageableByCurrentUser()) {
            return null;
        }
        return true;
    }

    @Transient
    public Timestamp getModerationDatetime() {
        Boolean pendingApproval = isPendingPublicationApproval();
        return (pendingApproval != null && pendingApproval) ? new Timestamp(getPrimaryChannelContent().getModerationDatetime().toEpochMilli()) : null;
    }

    @Transient
    public ChannelContent getPrimaryChannelContent() {
        return getChannelContentsInited().values().stream()
                .filter(cc -> cc.getChannel().getType().isPrimaryPublishingChannel() && !cc.getStatus().isBlocked())
                .findFirst()
                .orElse(null);
    }

    @Transient
    public boolean isPrimaryChannelLocked() {
        // bl: the primary channel is locked if the post is not a draft or future publication and it's in a Publication.
        // note that the post won't necessarily be live if the post is fully moderated, awaiting Publication review
        ChannelContent primaryChannelContent = getPrimaryChannelContent();
        return !isDraft() && exists(primaryChannelContent) && !primaryChannelContent.getChannel().getType().isAllowPrimaryChannelChanges();
    }

    @Transient
    public Boolean isFeaturedInPublication() {
        Publication publication = getSubmittedToPublication();
        // bl: return null if it's not posted to a publication since it doesn't apply.
        return exists(publication) ? isFeatured() : null;
    }

    @Transient
    public Instant getFeaturedUntilDatetime() {
        ChannelContent channelContent = getPrimaryChannelContent();
        // bl: it's featured if the primary ChannelContent is featured until a date in the future
        boolean isFeatured = channelContent!=null && channelContent.getStatus().isApproved() && channelContent.getFeaturedUntilDatetime()!=null && Instant.now().isBefore(channelContent.getFeaturedUntilDatetime());
        // bl: return null if the featured datetime is in the past
        if(!isFeatured) {
            return null;
        }
        // bl: if it's featured, then it must also have an image! shouldn't ever get a case where we have a featured
        // post that doesn't have an image!
        assert isHasTitleImage() : "Found a post that is featured without a title image! oid/" + getOid();
        return channelContent.getFeaturedUntilDatetime();
    }

    @Transient
    public boolean isBlockedInChannel(Channel channel) {
        ChannelContent channelContent = getChannelContentsInited().get(channel);
        return exists(channelContent) && channelContent.getStatus().isBlocked();
    }

    @Transient
    public List<Publication> getAvailablePublications() {
        if(!networkContext().isLoggedInUser()) {
            return Collections.emptyList();
        }
        User user = networkContext().getUser();
        List<Channel> channels = ChannelUser.dao().getChannelsForUserByTypeAndRole(user, ChannelType.PUBLICATION, PublicationRole.WRITER);
        // bl: remove any publications in which this post has been rejected
        channels.removeIf(this::isBlockedInChannel);
        FollowedChannel.dao().populateChannelsFollowedByCurrentUserField(user, channels);
        List<Publication> publications = channels.stream()
                .map(Channel::getPublication)
                // jw: remove any publications that are not active.
                .filter(p -> p.getStatus().isActive())
                .sorted(Publication.NAME_COMPARATOR)
                .collect(Collectors.toList());

        return publications;
    }

    @Transient
    public Collection<Niche> getPublishedToNiches() {
        assert getContentType().isNarrativePost() : "Should only ever call this method for Narrative Posts!";

        // jw: ensure that the niches are ordered by name
        Set<Niche> niches = new TreeSet<>(Niche.NAME_COMPARATOR);

        for (ChannelContent channelContent : getChannelContentsCollection()) {
            // jw: skip any none niche channels
            if (!channelContent.getChannel().getType().isNiche()) {
                continue;
            }

            // jw: skip any channels where the content is not active
            if (!channelContent.getStatus().isApproved()) {
                continue;
            }

            // jw: finally, let's add the niche.
            niches.add(channelContent.getChannel().getNiche());
        }

        return niches;
    }

    @Transient
    public Collection<Channel> getPublishedToChannels() {
        assert getContentType().isNarrativePost() : "Should only ever call this method for Narrative Posts!";

        return getChannelContentsInited().values().stream()
                .filter(cc -> cc.getStatus().isApproved())
                .map(ChannelContent::getChannel)
                .collect(Collectors.toList());
    }

    @Transient
    public Collection<OID> getBlockedInNicheOids() {
        assert getContentType().isNarrativePost() : "Should only ever call this method for Narrative Posts!";

        return getChannelContentsCollection().stream()
                // bl: only include niche channels
                .filter(cc -> cc.getChannel().getType().isNiche())
                // bl: only include niches that have blocked this post
                .filter(cc -> cc.getStatus().isBlocked())
                .map(cc -> cc.getChannel().getOid())
                .collect(Collectors.toSet());
    }

    @Transient
    @Override
    public RatableType getRatableType() {
        return RatableType.CONTENT;
    }

    @Override
    public <T extends RatingFields> T getRatingFields(RatingType ratingType) {
        if(ratingType.isQuality()) {
            return (T)getQualityRatingFields();
        }

        assert ratingType.isAge() : "Only support quality and age ratings for Content, not/" + ratingType;
        return (T)getAgeRatingFields();
    }

    @Override
    public void onRatingUpdate(RatingType ratingType) {
        // bl: for quality ratings, we just need to update the RatingFields score
        if(ratingType.isQuality()) {
            getQualityRatingFields().recalculateScore();
            return;
        }

        assert ratingType.isAge() : "Only support quality and age ratings for Content, not/" + ratingType;
        AgeRating originalAgeRating = getAgeRating();
        AgeRatingFields ageRatingFields = getAgeRatingFields();
        // bl: for age ratings, we have to determine if the community's rating should trump the author's
        if(ageRatingFields.getTotalVotePoints()>=ageRatingFields.getMinimumScoreForApi()) {
            setAgeRating(ageRatingFields.getEffectiveRatingValue());
        } else {
            // bl: if we don't have at least 1 vote point cast, then the author's vote wins
            setAgeRating(getAuthorAgeRating());
        }

        // if the age rating changes, we need to re-index!
        if (originalAgeRating!=getAgeRating()) {
            ContentIndexRunnable.registerContentIndexRunnable(this);
        }
    }

    @Override
    public Integer getRatingMultiplier(User user) {
        // bl: there is never a rating multiplier for posts. everyone just gets their standard reputation-adjusted vote points
        return null;
    }

    public boolean isNicheModerator(User user) {
        for (Niche niche : getPublishedToNiches()) {
            if(niche.isNicheModerator(user)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void refreshForLock() {
        dao().refreshForLock(this);
    }

    public static ContentDAO dao() {
        return DAOImpl.getDAO(Content.class);
    }
}
