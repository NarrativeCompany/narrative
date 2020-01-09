package org.narrative.network.core.composition.base;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.narrative.common.core.services.interceptors.BypassHtmlDisable;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.IPStringUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.html.HTMLAttributeStripper;
import org.narrative.common.util.html.HTMLEmailStyleParser;
import org.narrative.common.util.html.HTMLParser;
import org.narrative.common.util.posting.Formattable;
import org.narrative.common.util.posting.FullTextProvider;
import org.narrative.common.util.posting.HtmlTextMassager;
import org.narrative.common.util.posting.MessageTextMassager;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.composition.base.dao.CompositionDAO;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentConsumer;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.master.graemlins.Graemlin;
import org.narrative.network.core.mentions.MentionsUtil;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.posts.NarrativePostContent;
import org.narrative.network.shared.posting.Scrubbable;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;
import org.narrative.network.shared.util.NetworkConstants;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:37:07 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Composition implements DAOObject<CompositionDAO>, Formattable, Scrubbable, FullTextProvider, PostBase, PostMentionsConsumer<CompositionMentions> {

    public static final String LAST_REPLY = "lastReply";
    public static final String UNREAD_REPLY = "unread";

    private OID oid;
    private OID areaOid;
    private OID userOid;
    private String guestName;
    private String body;
    private String canonicalUrl;
    private Set<Reply> replies;
    private FilePointerSet filePointerSet;
    private Timestamp editDatetime;
    private OID editorUserOid;
    private CompositionStats compositionStats;
    private List<NarrativePostContent> narrativePosts;
    private CompositionType compositionType;

    private CompositionMentions mentions;

    private Set<FilePointerSet> filePointerSets;

    @Deprecated
    public Composition() {}

    public Composition(CompositionType compositionType, Area area) {
        this(compositionType, null, area);
    }

    public Composition(CompositionType compositionType, User user, Area area) {
        assert exists(area) : "Area must always be set for all compositions now!";
        this.compositionType = compositionType;
        if (exists(user)) {
            setUserOid(user.getOid());
        }
        this.areaOid = area.getOid();
        compositionStats = new CompositionStats(this);
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @Column(nullable = false)
    @Index(name = "composition_area_oid_idx")
    public OID getAreaOid() {
        return areaOid;
    }

    public void setAreaOid(OID areaOid) {
        this.areaOid = areaOid;
    }

    @Transient
    public Area getArea() {
        return Area.dao().get(areaOid);
    }

    public OID getUserOid() {
        return userOid;
    }

    public void setUserOid(OID userOid) {
        this.userOid = userOid;
    }

    @Transient
    public User getUser() {
        return getCompositionConsumer().getUser();
    }

    @Override
    @Transient
    public User getAuthor() {
        return getCompositionConsumer().getAuthor();
    }

    @Transient
    @Override
    public User getRealAuthor() {
        return getCompositionConsumer().getRealAuthor();
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

    @Override
    @Transient
    public PrimaryRole getPrimaryRole() {
        return getCompositionConsumer().getPrimaryRole();
    }

    @Transient
    @Override
    public PrimaryRole getRealAuthorPrimaryRole() {
        return getCompositionConsumer().getRealAuthorPrimaryRole();
    }

    public static final int MAX_BODY_LENGTH = NetworkConstants.MAX_BODY_LENGTH;

    @Basic(fetch = FetchType.EAGER, optional = false)
    @Column(columnDefinition = "mediumtext", nullable = false)
    @Length(max = MAX_BODY_LENGTH)
    @Lob
    public String getBody() {
        return body;
    }

    @BypassHtmlDisable
    public void setBody(String body) {
        this.body = body;
    }

    @Transient
    private String getBodyWithMentions() {
        if (!exists(getCompositionConsumer()) || !getCompositionConsumer().getCompositionConsumerType().isSupportsMentions()) {
            return getBody();
        }

        return MentionsUtil.getLinkedHtml(getBody());
    }

    @Override
    @Transient
    public String getBodyResolved() {
        String body = getBodyWithMentions();

        // bl: Froala needs to store contenteditable="true" on some fields. we don't want those fields to be editable
        // when rendered in the post body, though. to fix, let's just strip out all contenteditable attributes.
        return new HTMLAttributeStripper("contenteditable", body, HTMLParser.FragmentType.BODY).parse();
    }

    @Transient
    public String getBodyForEdit() {
        // jw: let's include the empty paragraph stub at the end of the post so that the editor has a natural place to
        //     place their cursor at the end. If they leave it we will just strip it off anyways.
        return getBodyWithMentions() + HtmlTextMassager.STUB_PARAGRAPH_HTML;
    }

    @Transient
    @Override
    public String getBodyForEmail() {
        String ret = getBodyResolved();
        // bl: also need to enforce HTML styles on emails.
        HTMLEmailStyleParser parser = new HTMLEmailStyleParser(ret);
        return parser.parse();
    }

    public static final int MIN_CANONICAL_URL_LENGTH = NetworkConstants.MIN_URL_LENGTH;
    public static final int MAX_CANONICAL_URL_LENGTH = NetworkConstants.MAX_URL_LENGTH;

    @Length(min=MIN_CANONICAL_URL_LENGTH, max=MAX_CANONICAL_URL_LENGTH)
    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = Composition.FIELD__OID__NAME, foreignKey = @javax.persistence.ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME))
    public CompositionStats getCompositionStats() {
        return compositionStats;
    }

    public void setCompositionStats(CompositionStats compositionStats) {
        this.compositionStats = compositionStats;
    }

    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getEditDatetime() {
        return editDatetime;
    }

    public void setEditDatetime(Timestamp editDatetime) {
        this.editDatetime = editDatetime;
    }

    public OID getEditorUserOid() {
        return editorUserOid;
    }

    public void setEditorUserOid(OID editorUserOid) {
        this.editorUserOid = editorUserOid;
    }

    public void scrub() {
        // trim the body
        if (!IPStringUtil.isEmpty(getBody())) {
            MessageTextMassager massager = new MessageTextMassager(this, Graemlin.DEFAULT_GRAEMLIN_MAP, true);
            // bl: we don't want to use graemlin auto-conversion on posts!
            massager.setGraemlinsEnabled(false);
            massager.setSupportedElementNamesToAttributes(HtmlTextMassager.FROALA_SUPPORTED_HTML_ELEMENTS_TO_SUPPORTED_ATTRIBUTES);

            massager.massageBody();
        }
    }

    @Transient
    @org.jetbrains.annotations.NotNull
    public String getFullText(boolean includeFileContents) {
        StringBuilder fullText = new StringBuilder(getBodyResolved());
        if (hasAttachments()) {
            fullText.append(" ");
            fullText.append(getFilePointerSet().getFullText(includeFileContents));
        }
        return fullText.toString();
    }

    public static CompositionDAO dao() {
        return DAOImpl.getDAO(Composition.class);
    }

    /**
     * @return
     * @deprecated for hibernate cascade use only.  You should only get replies via query
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = Reply.FIELD__COMPOSITION__NAME, cascade = CascadeType.REMOVE)
    public Set<Reply> getReplies() {
        return replies;
    }

    public void setReplies(Set<Reply> replies) {
        this.replies = replies;
    }

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_composition_filePointerSet")
    public FilePointerSet getFilePointerSet() {
        return filePointerSet;
    }

    public void setFilePointerSet(FilePointerSet filePointerSet) {
        this.filePointerSet = filePointerSet;
    }

    // bl: don't want to cascade the FilePointerSets on delete. they should cascade automatically via Composition.filePointerSet and Reply.filePointerSet.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = FilePointerSet.FIELD__COMPOSITION__NAME)
    public Set<FilePointerSet> getFilePointerSets() {
        return filePointerSets;
    }

    public void setFilePointerSets(Set<FilePointerSet> filePointerSets) {
        this.filePointerSets = filePointerSets;
    }

    @Transient
    public boolean hasAttachments() {
        return exists(getFilePointerSet()) && getFilePointerSet().getFileCount() > 0;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = NarrativePostContent.FIELD__OID__NAME, cascade = CascadeType.REMOVE)
    public List<NarrativePostContent> getNarrativePosts() {
        return narrativePosts;
    }

    public void setNarrativePosts(List<NarrativePostContent> narrativePosts) {
        this.narrativePosts = narrativePosts;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public CompositionType getCompositionType() {
        return compositionType;
    }

    public void setCompositionType(CompositionType compositionType) {
        this.compositionType = compositionType;
    }

    @Transient
    @Override
    public Composition getComposition() {
        return this;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    public CompositionMentions getMentions() {
        return mentions;
    }

    public void setMentions(CompositionMentions mentions) {
        this.mentions = mentions;
    }

    @Override
    @Transient
    public CompositionMentions getMentionsForUpdate() {
        CompositionMentions mentions = getMentions();
        if (!exists(mentions)) {
            mentions = new CompositionMentions(this);
            CompositionMentions.dao().save(mentions);
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

    @Transient
    public String getBodyAsExtract() {
        // bl: we don't want to use graemlin auto-conversion on posts!
        return HtmlTextMassager.getBodyAsExtract(body, Content.MAX_EXTRACT_LENGTH, null);
    }

    @Transient
    public String getSubjectResolved() {
        return getCompositionConsumer().getTitle();
    }

    @Transient
    public String getShortSubjectResolved() {
        return CoreUtils.elipse(getSubjectResolved(), Content.MAX_SHORT_TITLE_LENGTH);
    }

    @Transient
    public Content getContent() {
        assert getCompositionType().isContent() : "Should only attempt to get Content for content compositions! oid/" + getOid() + " type/" + getCompositionType();
        Area area = getArea();

        return networkContext().doAreaTask(area, new AreaTaskImpl<Content>(false) {
            @Override
            protected Content doMonitoredTask() {
                return Content.dao().get(getOid());
            }
        });
    }

    @Transient
    public CompositionConsumer getCompositionConsumer() {
        switch (getCompositionType()) {
            case CONTENT:
                return getContent();
            case REFERENDUM:
                return Referendum.dao().get(getOid());
        }
        throw UnexpectedError.getRuntimeException("Unsupported CompositionType for Composition subject! type/" + getCompositionType());
    }

    public static void loadCompositions(List<? extends CompositionConsumer> compositionConsumers, final boolean isContent) {
        // map the consumers by partition
        Map<Partition, Set<CompositionConsumer>> consumerMap = new HashMap<>();
        for (CompositionConsumer compositionConsumer : compositionConsumers) {
            Set<CompositionConsumer> consumerSet = consumerMap.get(compositionConsumer.getCompositionPartition());
            if (consumerSet == null) {
                consumerMap.put(compositionConsumer.getCompositionPartition(), consumerSet = new HashSet<>());
            }
            consumerSet.add(compositionConsumer);
        }

        // get the compositions for each partition
        for (final Map.Entry<Partition, Set<CompositionConsumer>> entry : consumerMap.entrySet()) {
            networkContext().doCompositionTask(entry.getKey(), new CompositionTaskImpl<Object>(false) {
                protected Object doMonitoredTask() {
                    //get all the composition oids
                    Set<OID> allCompositionOids = new HashSet<>();
                    Map<ContentType, Set<OID>> contentTypeOidMap = new HashMap<>();

                    for (CompositionConsumer compositionConsumer : entry.getValue()) {
                        allCompositionOids.add(compositionConsumer.getCompositionOid());
                        if (isContent) {
                            Content content = (Content) compositionConsumer;
                            Set<OID> typeSet = contentTypeOidMap.get(content.getContentType());
                            if (typeSet == null) {
                                typeSet = new HashSet<>();
                                contentTypeOidMap.put(content.getContentType(), typeSet);
                            }
                            typeSet.add(compositionConsumer.getCompositionOid());
                        }
                    }

                    // prime the objects not in the cache
                    CompositionStats.dao().primeObjectsNotInCache(allCompositionOids);
                    List<Composition> nonCachedComps = Composition.dao().primeObjectsNotInCache(allCompositionOids);
                    Map<OID, Composition> nonCachedMap = Composition.dao().getIDToObjectsFromObjects(nonCachedComps);

                    // prime the content consumers
                    Map<ContentType, Map<OID, ? extends ContentConsumer>> typeToIDToCCMap = new HashMap<>();
                    if (isContent) {
                        for (Map.Entry<ContentType, Set<OID>> entry : contentTypeOidMap.entrySet()) {
                            ContentType type = entry.getKey();
                            List nonCachedCC = type.getDAO().primeObjectsNotInCache(entry.getValue());

                            Map<OID, ? extends ContentConsumer> nonCachedCCMap = getCompositionSession().getIDToObjectsFromObjects(nonCachedCC);
                            typeToIDToCCMap.put(type, nonCachedCCMap);
                        }
                    }

                    // prime the filePointerSets
                    FilePointerSet.dao().getAllForCompositionOidsDeepFetch(nonCachedMap.keySet());

                    // assign the compositions and content consumers to the content
                    for (CompositionConsumer compositionConsumer : entry.getValue()) {
                        //set the composition depending on if it was found in the cache or not
                        Composition comp = nonCachedMap.get(compositionConsumer.getCompositionOid());
                        if (!exists(comp)) {
                            comp = Composition.dao().get(compositionConsumer.getCompositionOid());
                        }
                        compositionConsumer.initCompositionCache(getCompositionSession());
                        compositionConsumer.getCompositionCache().setComposition(comp);
                    }
                    return null;
                }
            });
        }
    }
}
