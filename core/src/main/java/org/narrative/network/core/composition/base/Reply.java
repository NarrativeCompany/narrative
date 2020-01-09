package org.narrative.network.core.composition.base;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.jetbrains.annotations.Nullable;
import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.StringEnumType;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.html.HTMLEmailStyleParser;
import org.narrative.common.util.posting.Formattable;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.composition.base.dao.ReplyDAO;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.master.graemlins.Graemlin;
import org.narrative.network.core.mentions.MentionsUtil;
import org.narrative.network.core.moderation.ModerationStatus;
import org.narrative.network.core.rating.QualityRating;
import org.narrative.network.core.rating.Ratable;
import org.narrative.network.core.rating.RatableType;
import org.narrative.network.core.rating.RatingFields;
import org.narrative.network.core.rating.model.QualityRatingFields;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.shared.posting.Scrubbable;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.util.NetworkConstants;
import org.narrative.network.shared.util.NetworkCoreUtils;
import org.narrative.shared.event.reputation.RatingType;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:37:13 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = Reply.FIELD__THREADING_ORDER__COLUMN), @UniqueConstraint(columnNames = Reply.FIELD__FILE_POINTER_SET__COLUMN)})
@org.hibernate.annotations.Table(appliesTo = "Reply", indexes = {@Index(name = "reply_composition_threadingOrder_idx", columnNames = {Reply.FIELD__COMPOSITION__COLUMN, Reply.FIELD__THREADING_ORDER__COLUMN}), @Index(name = "reply_composition_liveDatetime_idx", columnNames = {Reply.FIELD__COMPOSITION__COLUMN, Reply.FIELD__LIVE_DATETIME__COLUMN})})
public class Reply implements DAOObject<ReplyDAO>, Formattable, ReplyAuthorProvider, Scrubbable, FullTextProvider, PostBase, PostMentionsConsumer<ReplyMentions>, Ratable<Reply> {

    private OID oid;
    private OID userOid;
    private String guestName;
    private transient String bodyAsExtract;

    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    private ReplyStats replyStats;

    /**
     * a null threadingOrder will cause the database to assign a new auto-increment value.
     */
    private Integer threadingOrder = null;
    private Timestamp liveDatetime;
    private String body;
    private Composition composition;
    private Timestamp editDatetime;
    private OID editorUserOid;

    private FilePointerSet filePointerSet;

    private QualityRatingFields qualityRatingFields;

    private ReplyMentions mentions;

    private transient boolean isNew = false;

    public static final String FIELD__COMPOSITION__NAME = "composition";
    public static final String FIELD__THREADING_ORDER__NAME = "threadingOrder";
    public static final String FIELD__FILE_POINTER_SET__NAME = "filePointerSet";
    public static final String FIELD__LIVE_DATETIME__NAME = "liveDatetime";
    public static final String FIELD__USER_OID__NAME = "userOid";

    public static final String FIELD__COMPOSITION__COLUMN = FIELD__COMPOSITION__NAME + "_" + Composition.FIELD__OID__NAME;
    public static final String FIELD__THREADING_ORDER__COLUMN = FIELD__THREADING_ORDER__NAME;
    public static final String FIELD__FILE_POINTER_SET__COLUMN = FIELD__FILE_POINTER_SET__NAME + "_" + FilePointerSet.FIELD__OID__NAME;
    public static final String FIELD__LIVE_DATETIME__COLUMN = FIELD__LIVE_DATETIME__NAME;

    private static final String HAS_JIT_INITED_QUALITY_RATING_PROPERTY = Reply.class.getName() + "-HasJITInitedQualityRating";

    public static final Comparator<Reply> REPLY_DATETIME_COMPARATOR = new Comparator<Reply>() {
        public int compare(Reply o1, Reply o2) {
            int comp = o2.getLiveDatetime().compareTo(o1.getLiveDatetime());
            if (comp != 0) {
                return comp;
            }
            comp = o2.getThreadingOrder().compareTo(o2.getThreadingOrder());
            if (comp != 0) {
                return comp;
            }
            return OID.compareOids(o1.getOid(), o2.getOid());
        }
    };

    @Deprecated
    public Reply() {}

    public Reply(Composition composition, User user) {
        this(composition);
        if (exists(user)) {
            setUserOid(user.getOid());
        }
    }

    public Reply(Composition composition) {
        isNew = true;
        setComposition(composition);

        setLiveDatetime(new Timestamp(System.currentTimeMillis()));
        setReplyStats(new ReplyStats(this));

        setQualityRatingFields(new QualityRatingFields(true));
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
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
     * userOid is nullable.  a null value indicates a reply by guest.
     */
    public OID getUserOid() {
        return userOid;
    }

    public void setUserOid(OID userOid) {
        this.userOid = userOid;
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

    // can't be NotNull or else Hibernate will stick "not null" after auto_increment, which is invalid.
    /*@NotNull*/
    @Range(min = 0)
    // bl: making this updatable=false to prevent Hibernate from trying to update the threading order (to null)
    // on an update following the insert.  this was resulting in errors that we were setting a not null field to null.
    // since it is auto_increment, null is only a valid value on the initial insert.
    // bl: changing to updatable=true (the default) since we will now be updating the threadingOrder when approving
    // replies. i don't know of a scenario where Hibernate would try to reinsert a null value on update after the insert.
    // that change was originally made in 2006 (by me). i think enough has changed since then that this should be
    // acceptable/possible now.
    @Column(columnDefinition = "int not null auto_increment")
    // need a unique constraint on this field in order for Hibernate to properly generate
    // the create table query.  so, using the @Table annotation on the class definition.
    /*@Index(name="reply_threading_order_idx")*/ public Integer getThreadingOrder() {
        return threadingOrder;
    }

    public void setThreadingOrder(Integer threadingOrder) {
        this.threadingOrder = threadingOrder;
    }

    @NotNull
    public Timestamp getLiveDatetime() {
        return liveDatetime;
    }

    public void setLiveDatetime(Timestamp liveDatetime) {
        this.liveDatetime = liveDatetime;
    }

    public static final int MIN_BODY_LENGTH = 1;
    public static final int MAX_BODY_LENGTH = NetworkConstants.MAX_BODY_LENGTH;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(columnDefinition = "mediumtext", nullable = false)
    @Length(min = MIN_BODY_LENGTH, max = MAX_BODY_LENGTH)
    @Lob
    public String getBody() {
        return body;
    }

    @BypassHtmlDisable
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    @Transient
    public String getBodyResolved() {
        return MentionsUtil.getLinkedHtml(getBody());
    }

    @Transient
    @Override
    public String getBodyForEmail() {
        String ret = getBodyResolved();
        // bl: also need to enforce HTML styles on emails.
        HTMLEmailStyleParser parser = new HTMLEmailStyleParser(ret);
        return parser.parse();
    }

    @Transient
    public String getBodyAsExtract() {
        if (bodyAsExtract != null) {
            return bodyAsExtract;
        }
        return bodyAsExtract = getBodyAsExtract(Content.MAX_EXTRACT_LENGTH);
    }

    private transient String bodyForReferendumVoteComment;

    @Transient
    public String getBodyForReferendumVoteComment() {
        if (bodyForReferendumVoteComment != null) {
            return bodyForReferendumVoteComment;
        }
        return bodyForReferendumVoteComment = getBodyAsExtract(ReferendumVote.MAX_COMMENT_LENGTH);
    }

    @Transient
    private String getBodyAsExtract(int length) {
        String bodyToGenerateExtractFrom = HtmlTextMassager.getBodyAsExtract(getBodyResolved(), length, Graemlin.DEFAULT_GRAEMLIN_MAP);

        if (bodyToGenerateExtractFrom.isEmpty()) {
            return IPStringUtil.getStringTruncatedToEndOfWord(NetworkCoreUtils.wordlet("reply.bodyExtract.default", getComposition().getSubjectResolved()), length);
        }

        return bodyToGenerateExtractFrom;
    }

    @Transient
    @Override
    public String getExtractForEmail() {
        return getBodyAsExtract();
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_reply_composition")
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getEditDatetime() {
        return editDatetime;
    }

    public void setEditDatetime(Timestamp editDatetime) {
        this.editDatetime = editDatetime;
    }

    @Transient
    public Timestamp getLastUpdateDatetime() {
        if (getEditDatetime() == null) {
            return getLiveDatetime();
        }

        return getEditDatetime();
    }

    public OID getEditorUserOid() {
        return editorUserOid;
    }

    public void setEditorUserOid(OID editorUserOid) {
        this.editorUserOid = editorUserOid;
    }

    @ManyToOne(optional = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "fk_reply_filePointerSet")
    public FilePointerSet getFilePointerSet() {
        return filePointerSet;
    }

    public void setFilePointerSet(FilePointerSet filePointerSet) {
        this.filePointerSet = filePointerSet;
    }

    @Transient
    public boolean isHasAttachments() {
        return CoreUtils.exists(getFilePointerSet()) && getFilePointerSet().getFileCount() > 0;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public ReplyMentions getMentions() {
        return mentions;
    }

    public void setMentions(ReplyMentions mentions) {
        this.mentions = mentions;
    }

    @Override
    @Transient
    public ReplyMentions getMentionsForUpdate() {
        ReplyMentions mentions = getMentions();
        if (!exists(mentions)) {
            mentions = new ReplyMentions(this);
            ReplyMentions.dao().save(mentions);
            setMentions(mentions);
        }

        return mentions;
    }

    @Transient
    @Override
    public List<User> getMentionedMembersToNotify() {
        return PostMentionsConsumer.super.getMentionedMembersToNotify();
    }

    @Override
    public void addNotifiedMentionedMembers(List<User> mentionedMembers) {
        PostMentionsConsumer.super.addNotifiedMentionedMembers(mentionedMembers);
    }

    @NotNull
    @Type(type = StringEnumType.TYPE)
    // despite the NotNull annotation, this field isn't currently being defined
    // as NotNull, presumably due to the columnDefinition below.
    @Column(columnDefinition = ModerationStatus.ENUM_FIELD_TYPE)
    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = Reply.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME))
    public ReplyStats getReplyStats() {
        return replyStats;
    }

    public void setReplyStats(ReplyStats replyStats) {
        this.replyStats = replyStats;
    }

    public void scrub() {
        if (!IPStringUtil.isEmpty(getBody())) {
            new MessageTextMassager(this, Graemlin.DEFAULT_GRAEMLIN_MAP, true).massageBody();
        }
        setGuestName(HtmlTextMassager.sanitizePlainTextString(getGuestName(), false));
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public String getFullText(boolean includeFileContents) {
        StringBuilder fullText = new StringBuilder();
        fullText.append(getBodyResolved());
        if (!isEmpty(getGuestName())) {
            fullText.append(" ");
            fullText.append(getGuestName());
        }
        if (isHasAttachments()) {
            fullText.append(" ");
            fullText.append(getFilePointerSet().getFullText(includeFileContents));
        }

        return fullText.toString();
    }

    @Transient
    @Nullable
    public Area getArea() {
        return getComposition().getArea();
    }

    /**
     * Gets the areaUserRlm for this reply.  IMPORTANT!!!! Will only work if you have the correct realm already
     * in scope.  This should work 99% of the time since you will most likely already be in the correct area when
     * accessing this reply
     * @return The areaUserRlm record of the autor, or null if its a guest post
     */
    // bl: removing since we now have single reply chains which means that the proper realm for the specified
    // AreaUserRlm
    /*@Transient
    @Nullable
    public AreaUserRlm getAreaUserRlm() {
        if (areaUserOid != null)
            return AreaUserRlm.dao().get(areaUserOid);

        return null;
    }*/

    /**
     * Gets the User for this reply.
     *
     * @return The User record of the author, or null if its a guest post
     */
    @Transient
    @Nullable
    public User getUser() {
        if (getUserOid() != null) {
            return User.dao().get(getUserOid());
        }

        return null;
    }

    @Override
    @Transient
    public User getAuthor() {
        return getUser();
    }

    @Transient
    @Override
    public User getRealAuthor() {
        return getAuthor();
    }

    private transient PrimaryRole primaryRole;

    /**
     * Returns the role of the user who replied.  Either a user or a guest depending
     * on what type of role replied.
     *
     * @return the PrimaryRole that composed this reply
     */
    @Override
    @Transient
    public PrimaryRole getPrimaryRole() {
        if (primaryRole == null) {
            primaryRole = PrimaryRole.getPrimaryRole(getArea().getAuthZone(), getUser(), getGuestNameResolved());
        }
        return primaryRole;
    }

    @Transient
    @Override
    public PrimaryRole getRealAuthorPrimaryRole() {
        return getPrimaryRole();
    }

    @Transient
    public boolean isReplyLive() {
        return getModerationStatus().isLive();
    }

    public QualityRatingFields getQualityRatingFields() {
        return qualityRatingFields;
    }

    public void setQualityRatingFields(QualityRatingFields qualityRatingFields) {
        this.qualityRatingFields = qualityRatingFields;
    }

    @Transient
    public boolean isCurrentUserAuthor() {
        return exists(getAuthor()) && getAuthor().isCurrentUserThisUser();
    }

    public void checkEditableByCurrentUser() {
        CompositionConsumer compositionConsumer = getComposition().getCompositionConsumer();
        // bl: the user is a manager for this CompositionConsumer as indicated by the general ability to edit all replies.
        if (compositionConsumer.isRepliesEditableByCurrentUser()) {
            return;
        }

        // bl: at this point, in order to edit, you must be the author and also be able to reply generally
        if (isCurrentUserAuthor() && compositionConsumer.isDoesCurrentUserHaveRightToReply()) {
            return;
        }

        // can't edit other people's posts as a non-admin!
        throw new AccessViolation(wordlet("post.cannotEditOthers"));
    }

    @Transient
    public boolean isDeletableByCurrentUser() {
        CompositionConsumer consumer = getComposition().getCompositionConsumer();
        // bl: the current user can delete if they are the author (or wall "owner")
        // AND they have the right to reply to this CompositionConsumer still.
        // bl: for Narrative, removing the requirement that you must be able to reply. users can be Conduct Negative
        // and thus not able to reply, but they should still be able to delete their posts.
        if (isCurrentUserGenerallyAbleToDelete()) {
            return true;
        }
        // bl: Narrative Staff can delete all comments, too. note that this includes post comments as well
        // as referendum comments.
        if (networkContext().getPrimaryRole().isCanRemoveAupViolations()) {
            return true;
        }

        // bl: if the above condition isn't met, then the reply can only be deleted if the user can generally
        // delete all replies on this CompositionConsumer.
        return consumer.isRepliesDeletableByCurrentUser();
    }

    @Transient
    private boolean isCurrentUserGenerallyAbleToDelete() {
        // bl: authors can always generally delete their own posts
        return isCurrentUserAuthor();
    }

    @Transient
    public Map<String, Object> getDataAttributesBase() {
        Map<String, Object> data = newLinkedHashMap();
        data.put("data-reply-oid", getOid());
        data.put("data-consumer-oid", getComposition().getOid());

        return data;
    }

    @Transient
    @Override
    public RatableType getRatableType() {
        return RatableType.REPLY;
    }

    @Override
    public <T extends RatingFields> T getRatingFields(RatingType ratingType) {
        assert ratingType.isQuality() : "Only support quality ratings for Replies, not/" + ratingType;
        return (T)getQualityRatingFields();
    }

    @Override
    public void onRatingUpdate(RatingType ratingType) {
        assert ratingType.isQuality() : "Only support quality ratings for Replies, not/" + ratingType;
        // bl: replies are easy; just recalculate the quality score
        getQualityRatingFields().recalculateScore();
    }

    private static final int MODERATOR_RATING_MULTIPLIER = 3;
    private static final int AUTHOR_RATING_MULTIPLIER = MODERATOR_RATING_MULTIPLIER;

    @Override
    public Integer getRatingMultiplier(User user) {
        // bl: only special handling is for replies to content
        Composition composition = getComposition();
        if(composition.getCompositionType().isContent()) {
            Content content = composition.getContent();
            // bl: the post author gets 3x vote points for comments
            if(isEqual(content.getAuthor(), user)) {
                return AUTHOR_RATING_MULTIPLIER;
            }
            // bl: niche moderators get 3x vote points for comments, as well
            if(content.isNicheModerator(user)) {
                return MODERATOR_RATING_MULTIPLIER;
            }
        }
        // for everything else, there is no multiplier
        return null;
    }

    @Override
    public void refreshForLock() {
        dao().refreshForLock(this);
    }

    private transient boolean hasInitedQualityRatingByCurrentUser;
    private transient QualityRating qualityRatingByCurrentUser;
    @Transient
    public QualityRating getQualityRatingByCurrentUser() {
        if (qualityRatingByCurrentUser ==null && !hasInitedQualityRatingByCurrentUser) {
            // jw: ensure that we only do this once!
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_QUALITY_RATING_PROPERTY);

            if (networkContext().isLoggedInUser()) {
                UserQualityRatedReply rating = UserQualityRatedReply.dao().getRatingForUser(
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

    public static ReplyDAO dao() {
        return DAOImpl.getDAO(Reply.class);
    }

}
