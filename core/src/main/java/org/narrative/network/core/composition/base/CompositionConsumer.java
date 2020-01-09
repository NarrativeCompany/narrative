package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.DAO;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.GSession;
import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.FutureContent;
import org.narrative.network.core.content.base.SEOObject;
import org.narrative.network.core.fileondisk.base.FileConsumer;
import org.narrative.network.core.fileondisk.base.FileUsageType;
import org.narrative.network.core.rating.AgeRating;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.security.PrimaryRole;
import org.narrative.network.shared.services.AuthorProvider;

import javax.persistence.Transient;

import java.sql.Timestamp;
import java.util.Collections;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jun 29, 2010
 * Time: 9:43:36 AM
 *
 * @author brian
 */
public interface CompositionConsumer<T extends CompositionCache, S extends CompositionConsumerStats, D extends DAO> extends FileConsumer, SEOObject, DAOObject<D>, AuthorProvider {

    OID getCompositionOid();

    T getCompositionCache();

    void initCompositionCache(GSession compositionSession);

    CompositionType getCompositionType();

    ContentType getContentType();

    String getPrettyUrlString();

    Timestamp getLiveDatetime();

    boolean isVetoSearchResult(PrimaryRole role);

    String getTitle();

    void checkGeneralCompositionConsumerAccess(PrimaryRole primaryRole);

    S getStats();

    S getStatsForUpdate();

    CompositionConsumerType getCompositionConsumerType();

    Area getArea();

    String getTitleForDisplay();

    String getShortDisplaySubject();

    PrimaryRole getPrimaryRole();

    boolean isHasComposition();

    Partition getCompositionPartition();

    void setCompositionPartition(Partition compositionPartition);

    boolean isLive();

    boolean isFeatured();

    boolean isModerated();

    boolean isDeleted();

    boolean isAllowRepliesResolved();

    int getModeratedReplyCount();

    boolean hasViewRight(AreaRole areaRole);

    boolean hasViewRepliesRight(AreaRole areaRole);

    boolean isDoesCurrentUserHaveViewRight();

    boolean hasReplyRight(AreaRole areaRole);

    void checkRightToReply(AreaRole areaRole);

    boolean isDoesCurrentUserHaveRightToReply();

    boolean isEditableByCurrentUser();

    boolean isDeletableByCurrentUser();

    boolean isManageableByCurrentUser();

    boolean isManageableByAreaRole(AreaRole areaRole);

    void checkManageable(AreaRole areaRole);

    boolean isRepliesEditableByCurrentUser();

    boolean isRepliesDeletableByCurrentUser();

    boolean isDraft();

    boolean isNew();

    User getUser();

    Portfolio getPortfolio();

    String getTypeNameForDisplay();

    String getTypeLowercaseNameForDisplay();

    String getIdForUrl();

    FileUsageType getAttachmentFileUsageType();

    // jw: Due to JSP compilation issues we need to override these default "defender" methods in all implementing classes
    //     Otherwise we will get a PropertyNotFoundException from the EL parser:
    //     Caused by: javax.el.PropertyNotFoundException: Property 'testString' not found on type org.narrative.network.core.content.base.Content
    //     at javax.el.BeanELResolver$BeanProperties.get(BeanELResolver.java:244)~[el-api.jar:3.0.FR]
    // bl: we also need to override the defaults in order to get around VerifyErrors ("Illegal use of nonvirtual function call") in Javassist
    default String getDisplayReplyUrl(OID replyOid) {
        // jw: lets just use the displayUrl if no replyOid is provided, this makes this code easier to rely on in other
        //     places where the replyOid may not be set.
        if (replyOid == null) {
            return getDisplayUrl();
        }
        String replyOidString = replyOid.toString();
        // bl: we'll generate URLs in the java for the React UI when processing JSP emails, so use the react param name in that case
        String replyParamName = networkContext().isProcessingJspEmail() ? CompositionType.COMMENT_PARAM_NAME : CompositionType.REPLY_PARAM_NAME;
        return newString(IPHTMLUtil.getParametersAsURL(getDisplayUrl(), Collections.singletonMap(replyParamName, replyOidString)), "#", replyOidString);
    }

    // jw: Due to JSP compilation issues we need to override these default "defender" methods in all implementing classes
    //     Otherwise we will get a PropertyNotFoundException from the EL parser:
    //     Caused by: javax.el.PropertyNotFoundException: Property 'testString' not found on type org.narrative.network.core.content.base.Content
    //     at javax.el.BeanELResolver$BeanProperties.get(BeanELResolver.java:244)~[el-api.jar:3.0.FR]
    // bl: we also need to override the defaults in order to get around VerifyErrors ("Illegal use of nonvirtual function call") in Javassist
    default Timestamp getLiveDatetimeForSort() {
        if (getCompositionType().isContent()) {
            Content content = (Content) this;

            if (exists(content.getFutureContent())) {
                FutureContent futureContent = content.getFutureContent();

                return futureContent.getSaveDatetime();
            }
        }

        return getLiveDatetime();
    }

    boolean isSupportsQualityRatingReplies();

    @Transient
    public default AgeRating getAgeRating() {
        throw UnexpectedError.getRuntimeException("Attempting to get effective age rating from consumer type that does not support it/"+getCompositionConsumerType());
    }

}
