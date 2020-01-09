package org.narrative.network.customizations.narrative.niches.referendum.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.CoreUtils;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.composition.base.CompositionCache;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.CompositionConsumerStats;
import org.narrative.network.core.composition.base.CompositionConsumerType;
import org.narrative.network.core.composition.base.CompositionType;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.fileondisk.base.FileConsumerFileInfo;
import org.narrative.network.core.fileondisk.base.FileConsumerType;
import org.narrative.network.core.fileondisk.base.FileMetaDataProvider;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumType;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumDAO;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.security.PrimaryRole;

import javax.persistence.Transient;

import java.sql.Timestamp;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/27/18
 * Time: 1:49 PM
 */
public abstract class ReferendumCompositionConsumer implements CompositionConsumer<CompositionCache, CompositionConsumerStats, ReferendumDAO> {

    public abstract ReferendumType getType();

    @Override
    public CompositionType getCompositionType() {
        return CompositionType.REFERENDUM;
    }

    @Override
    public void checkGeneralCompositionConsumerAccess(PrimaryRole primaryRole) {
        // jw: there are no view rights specific to Referendums
    }

    @Override
    public CompositionConsumerType getCompositionConsumerType() {
        return CompositionConsumerType.REFERENDUM;
    }

    @Override
    public void setCompositionPartition(Partition compositionPartition) {
        throw UnexpectedError.getRuntimeException("Should never set the compositionPartition directly on Referendums!");
    }

    @Override
    public boolean hasViewRight(AreaRole areaRole) {
        // jw: currently, Referendums are viewable by everyone.
        return true;
    }

    @Override
    public void checkRightToReply(AreaRole areaRole) {
        if (!hasReplyRight(areaRole)) {
            throw new AccessViolation(wordlet("referendumCompositionConsumer.cannotReply"));
        }
    }

    @Override
    public User getUser() {
        // jw: no real notion of a Author of a referendum, since they are all automated about a different stage of a Niches life.
        return null;
    }

    @Override
    public AuthZone getAuthZone() {
        return getPortfolio().getArea().getAuthZone();
    }

    @Transient
    @Override
    public ContentType getContentType() {
        return null;
    }

    @Transient
    @Override
    public boolean isVetoSearchResult(PrimaryRole role) {
        throw UnexpectedError.getRuntimeException("Should never get a Referendum as a search result!");
    }

    @Transient
    @Override
    public CompositionConsumerStats getStats() {
        throw UnexpectedError.getRuntimeException("No stats supported for this class.");
    }

    @Transient
    @Override
    public CompositionConsumerStats getStatsForUpdate() {
        throw UnexpectedError.getRuntimeException("No stats supported for this class.");
    }

    @Transient
    @Override
    public String getTitleForDisplay() {
        return getTitle();
    }

    @Transient
    @Override
    public String getShortDisplaySubject() {
        return CoreUtils.elipse(getTitle(), Content.MAX_SHORT_TITLE_LENGTH);
    }

    @Transient
    @Override
    public String getGuestNameResolved() {
        return null;
    }

    @Transient
    @Override
    public PrimaryRole getPrimaryRole() {
        return PrimaryRole.getPrimaryRole(getAuthZone(), getUser(), null);
    }

    @Transient
    @Override
    public PrimaryRole getRealAuthorPrimaryRole() {
        return getPrimaryRole();
    }

    @Transient
    @Override
    public boolean isHasComposition() {
        // jw: we will create the Composition when the Referendum is created!
        return true;
    }

    @Transient
    @Override
    public boolean isLive() {
        // jw: We do not support moderation for Referendums!
        return true;
    }

    @Transient
    @Override
    public boolean isFeatured() {
        return false;
    }

    @Transient
    @Override
    public boolean isModerated() {
        return false;
    }

    @Transient
    @Override
    public boolean isDeleted() {
        return false;
    }

    @Transient
    @Override
    public int getModeratedReplyCount() {
        return 0;
    }

    @Transient
    @Override
    public boolean hasViewRepliesRight(AreaRole areaRole) {
        return hasViewRight(areaRole);
    }

    @Transient
    @Override
    public boolean isDoesCurrentUserHaveViewRight() {
        return hasViewRight(areaContext().getAreaRole());
    }

    @Transient
    @Override
    public boolean isDoesCurrentUserHaveRightToReply() {
        return hasReplyRight(areaContext().getAreaRole());
    }

    @Transient
    @Override
    public boolean isManageableByCurrentUser() {
        return false;
    }

    @Transient
    @Override
    public boolean isManageableByAreaRole(AreaRole areaRole) {
        return false;
    }

    @Override
    public void checkManageable(AreaRole areaRole) {
        throw UnexpectedError.getRuntimeException("There is no management involved in referendums!");
    }

    @Transient
    @Override
    public boolean isEditableByCurrentUser() {
        // jw: Referendums are automatically created and maintained by the system. There is no such thing as editing a referendum directly.
        return false;
    }

    @Transient
    @Override
    public boolean isDeletableByCurrentUser() {
        // jw: like above, the lifecycle of a referendum is controlled by the system.
        return false;
    }

    @Transient
    @Override
    public boolean isRepliesEditableByCurrentUser() {
        // jw: for now, only the author of a reply can edit a reply, though we may want to consider allowing tribunal members to delete replies.
        return false;
    }

    @Transient
    @Override
    public boolean isRepliesDeletableByCurrentUser() {
        // bl: for now, only the author of a reply can delete it.
        return false;
    }

    @Transient
    @Override
    public boolean isDraft() {
        return false;
    }

    @Override
    public FileMetaDataProvider getFileMetaDataProvider(boolean isPrimaryPicture) {
        throw UnexpectedError.getRuntimeException("Should not attempt to get files for this class.");
    }

    @Transient
    @Override
    public FileConsumerType getFileConsumerType() {
        return null;
    }

    @Transient
    @Override
    public String getFileUrlBase() {
        throw UnexpectedError.getRuntimeException("Should not attempt to get files for this class.");
    }

    @Transient
    @Override
    public FileConsumerFileInfo getFileInfo(PrimaryRole currentRole, OID filePointerOid, boolean primaryPicture) {
        throw UnexpectedError.getRuntimeException("Should not attempt to get files for this class.");
    }

    @Transient
    @Override
    public Area getArea() {
        return getAuthZone().getArea();
    }

    @Transient
    @Override
    public OID getCompositionOid() {
        return getOid();
    }

    private transient CompositionCache compositionCache;

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
        assert PartitionType.COMPOSITION.hasCurrentSession() && isEqual(PartitionType.COMPOSITION.currentPartitionOid(), getCompositionPartition().getOid()) : "Can't call getComposition() without being in the scope of the proper Composition database first, or without having called setCompositionCached!";
        initCompositionCache(PartitionType.COMPOSITION.currentSession());

        return compositionCache;
    }

    @Transient
    @Override
    public void initCompositionCache(GSession compositionSession) {
        this.compositionCache = new CompositionCache(getCompositionOid(), compositionSession);
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

    @Transient
    @Override
    public String getTypeNameForDisplay() {
        return getCompositionType().getNameForDisplay();
    }

    @Transient
    @Override
    public String getTypeLowercaseNameForDisplay() {
        return getCompositionType().getNameForDisplayLowercase();
    }

    @Transient
    @Override
    public FileUsageType getAttachmentFileUsageType() {
        return FileUsageType.ATTACHMENT;
    }

    @Override
    public String getIdForUrl() {
        // jw: we don't currently support prettyUrlStrings for Referendums
        return getOid().toString();
    }

    @Override
    public String getPrettyUrlString() {
        // jw: we don't currently support prettyUrlStrings for Referendums
        return null;
    }

    @Transient
    public String getPermalinkUrl() {
        return getDisplayUrl();
    }

    @Override
    public String getDisplayReplyUrl(OID replyOid) {
        return CompositionConsumer.super.getDisplayReplyUrl(replyOid);
    }

    @Transient
    @Override
    public Timestamp getLiveDatetimeForSort() {
        // jw: per https://java.net/jira/browse/JAVASERVERFACES-3353 we will delegate to the interfaces default method to get around EL compiler issues
        return CompositionConsumer.super.getLiveDatetimeForSort();
    }

    @Transient
    @Override
    public boolean isSupportsQualityRatingReplies() {
        return getType().isSupportsQualityRatingReplies();
    }
}
