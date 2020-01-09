package org.narrative.network.core.content.base.services;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPStringUtil;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionConsumer;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.base.services.ModeratedCompositionConsumerViolation;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.customizations.narrative.posts.QualityLevel;
import org.narrative.network.shared.replies.services.ReplySortOrder;
import org.narrative.network.shared.services.NetworkException;
import org.narrative.network.shared.tasktypes.CompositionTaskImpl;

import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: Jul 8, 2010
 * Time: 12:12:14 PM
 *
 * @author Jonmark Weber
 */
public class GetCompositionConsumerRepliesTask extends CompositionTaskImpl<Object> {
    private final CompositionConsumer compositionConsumer;
    private final ReplySortOrder sort;
    private final String reply;
    private final int postCountPerPage;

    private int page;
    private List<Reply> replies = Collections.emptyList();
    private int postCountForPagination;
    private int anyQualityReplyCount;
    private int atLeastMediumQualityReplyCount;

    private boolean primeUserRatedReplies;

    private boolean excludeBuried;

    // jw: For the forum topic display we need to only return max_results - 1 results for the first page, which
    //     affects a lot of the calculations performed in this task.  This flag represents that change in logic
    private boolean includeConsumerInPostCountForPagination = false;

    public GetCompositionConsumerRepliesTask(CompositionConsumer compositionConsumer, ReplySortOrder sort, String reply, int postCountPerPage, int page) {
        super(false);

        this.compositionConsumer = compositionConsumer;
        this.sort = sort;
        this.reply = reply;
        this.postCountPerPage = postCountPerPage;
        this.page = page;
    }

    @Override
    protected Object doMonitoredTask() {
        if (compositionConsumer.isModerated()) {
            throw new ModeratedCompositionConsumerViolation(compositionConsumer, areaContext().getAreaRole());
        }

        assert compositionConsumer.isLive() : "The composition consumer should be visible by the point we get here!";

        assert !includeConsumerInPostCountForPagination || sort.isOldestToNewest() : "Should only ever be including consumer is postCountForPagination when sorting oldest to newest! not/" + sort;

        Composition composition = compositionConsumer.getCompositionCache().getComposition();

        int postCountOffset = includeConsumerInPostCountForPagination ? 1 : 0;

        anyQualityReplyCount = composition.getCompositionStats().getReplyCount() + postCountOffset;
        anyQualityReplyCount -= compositionConsumer.getModeratedReplyCount();

        // bl: first look to see if we are looking up a specific reply so we can set the value for excludeBuried
        // appropriately.
        if (!IPStringUtil.isEmpty(reply)) {
            if (!Composition.LAST_REPLY.equals(reply)) {
                Reply replyObject = Reply.dao().get(OID.getOIDFromString(reply));
                if (exists(replyObject)) {
                    int minimumScore = 0;
                    if(compositionConsumer.isSupportsQualityRatingReplies()) {
                        // bl: if the reply is low quality, we have to include all replies, including buried, so the
                        // default minimum of 0 should work. we need to exclude buried, however, as the default sort
                        // if this comment is not buried.
                        excludeBuried = !replyObject.getQualityRatingFields().getQualityLevel().isLow();
                        if(excludeBuried) {
                            minimumScore = QualityLevel.MEDIUM.getMinimumScore();
                        }
                    }
                    page = Reply.dao().getPageNumberForReply(sort, replyObject, postCountPerPage, postCountOffset, minimumScore);
                } else {
                    page = 1;
                }
            }
        }

        // jw: if the consumer supports quality rating, then let's get the atLeastMediumQualityReplyCount
        if (compositionConsumer.isSupportsQualityRatingReplies()) {
            atLeastMediumQualityReplyCount = Reply.dao().getLiveReplyCountWithMinimumQuality(
                composition,
                QualityLevel.MEDIUM.getMinimumScore()
            );
        }

        int minQualityScore;
        if(compositionConsumer.isSupportsQualityRatingReplies() && excludeBuried) {
            minQualityScore = QualityLevel.MEDIUM.getMinimumScore();
            postCountForPagination = atLeastMediumQualityReplyCount;
        } else {
            minQualityScore = QualityLevel.LOW.getMinimumScore();
            postCountForPagination = anyQualityReplyCount;
        }

        int numReplyPages = Math.max(1, ((int) Math.ceil((double) postCountForPagination / (double) postCountPerPage)));
        page = Math.max(1, page);
        if (page > numReplyPages) {
            throw new NetworkException(wordlet("error.content.replyPageInvalid"));
        }

        // jw: get the replies if there are more than the offset
        if (postCountForPagination > postCountOffset) {
            replies = Reply.dao().getSortedForComposition(sort, composition, (page - 1) * postCountPerPage - (page > 1 ? postCountOffset : 0), postCountPerPage - (page > 1 ? 0 : postCountOffset), minQualityScore);

            Reply.dao().primeUsersForReplies(replies);

            // jw: only prime the User Quality Ratings if requested.
            if (primeUserRatedReplies) {
                UserQualityRatedReply.dao().populateQualityRatingsForReplies(
                        getNetworkContext().isLoggedInUser() ? getNetworkContext().getUser() : null,
                        replies
                );
            }
        }

        return null;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public int getPostCountForPagination() {
        return postCountForPagination;
    }

    public int getPage() {
        return page;
    }

    public int getAnyQualityReplyCount() {
        return anyQualityReplyCount;
    }

    public int getAtLeastMediumQualityReplyCount() {
        return atLeastMediumQualityReplyCount;
    }

    public void setIncludeConsumerInPostCountForPagination(boolean includeConsumerInPostCountForPagination) {
        this.includeConsumerInPostCountForPagination = includeConsumerInPostCountForPagination;
    }

    public void setPrimeUserRatedReplies(boolean primeUserRatedReplies) {
        this.primeUserRatedReplies = primeUserRatedReplies;
    }

    public boolean isExcludeBuried() {
        return excludeBuried;
    }

    public void setExcludeBuried(boolean excludeBuried) {
        this.excludeBuried = excludeBuried;
    }
}
