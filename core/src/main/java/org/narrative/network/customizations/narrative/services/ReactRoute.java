package org.narrative.network.customizations.narrative.services;

import org.narrative.common.util.IPHTMLUtil;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.customizations.narrative.publications.Publication;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 10/23/18
 * Time: 10:19 PM
 *
 * @author brian
 */
public enum ReactRoute {
    HOME("/")
    ,SIGN_IN("/sign-in")
    ,REGISTER("/register")
    ,HQ("/hq")
    ,APPROVALS("/hq/approvals")
    ,APPROVALS_PAGE("/hq/approvals/:page")
    ,APPROVAL_DETAILS("/hq/approval/:referendumOid")
    ,AUCTIONS("/hq/auctions")
    ,AUCTIONS_ACTIVE("/hq/auctions/active")
    ,AUCTIONS_ACTIVE_PAGE("/hq/auctions/active/:page")
    ,AUCTIONS_PENDING_PAYMENT("/hq/auctions/pending-payment")
    ,AUCTIONS_PENDING_PAYMENT_PAGE("/hq/auctions/pending-payment/:page")
    ,AUCTION_DETAILS("/hq/auction/:auctionOid")
    ,INVOICE("/hq/invoice/:invoiceOid")
    ,SUGGEST_NICHE("/suggest-niche")
    ,LEADERSHIP_TRIBUNAL("/hq/leadership/tribunal")
    ,APPEALS("/hq/appeals")
    ,APPEALS_MY_QUEUE("/hq/appeals/my-queue")
    ,APPEALS_MY_QUEUE_PAGE("/hq/appeals/my-queue/:page")
    ,APPEALS_UNDER_REVIEW("/hq/appeals/under-review")
    ,APPEALS_UNDER_REVIEW_PAGE("/hq/appeals/under-review/:page")
    ,APPEALS_COMPLETED_REVIEW("/hq/appeals/completed-review")
    ,APPEALS_COMPLETED_REVIEW_PAGE("/hq/appeals/completed-review/:page")
    ,APPEAL_DETAILS("/hq/appeal/:tribunalIssueOid")
    ,USER_PROFILE("/m/:username")
    ,USER_PROFILE_REWARDS("/m/:username/rewards")
    ,USER_PROFILE_REWARDS_TRANSACTIONS("/m/:username/rewards/transactions")
    ,USER_PROFILE_NICHES("/m/:username/niches")
    ,USER_PROFILE_REPUTATION("/m/:username/reputation")
    ,USER_PROFILE_REFERRAL_PROGRAM("/m/:username/referral-program")
    ,REFERRAL_PROGRAM("/referral-program")
    ,NICHE_DETAILS("/n/:prettyUrlString")
    ,SEARCH("/search")
    ,MEMBER_CP("/member-cp")
    ,MEMBER_EDIT_PROFILE("/member-cp/edit-profile")
    ,MEMBER_ACCOUNT_SETTINGS("/member-cp/account-settings")
    ,MEMBER_PERSONAL_SETTINGS("/member-cp/personal-settings")
    ,MEMBER_NOTIFICATION_SETTINGS("/member-cp/notification-settings")
    ,MEMBER_SECURITY_SETTINGS("/member-cp/security-settings")
    ,MEMBER_NEO_WALLET("/member-cp/neo-wallet")
    ,MEMBER_CERTIFICATION("/member-cp/certification")
    ,CONFIRM_EMAIL("/confirm-email/:userOid/:confirmationToken")
    ,CONFIRM_EMAIL_CHANGE("/confirm-email-change/:userOid/:emailAddressOid/:verificationStep/:confirmationToken")
    ,CANCEL_EMAIL_CHANGE("/cancel-email-change/:userOid/:emailAddressOid/:verificationStep/:confirmationToken")
    ,UNSUBSCRIBE("/unsubscribe/:userOid/:emailAddress/:token")
    ,RESET_PASSWORD("/reset-password/:userOid/:timestamp/:key")
    ,POST("/post/:postId")
    ,CREATE_POST("/create-post/:postOid")
    ,PUBLICATION_DETAILS("/p/:publicationId")
    ,PUBLICATION_MANAGE_POWER_USERS("/manage/power-users", true)
    ,PUBLICATION_MANAGE_REVIEW_QUEUE("/manage/review-queue", true)
    ,PUBLICATION_POWER_USER_INVITATION("/invitation", true)
    ;

    private final String path;
    private final int paramCount;
    private final boolean isPublicationSubPath;

    ReactRoute(String path) {
        this(path, false);
    }

    ReactRoute(String path, boolean isPublicationSubPath) {
        this.path = path;
        Matcher matcher = RoutePattern.PATH_PATTERN.matcher(path);
        int count = 0;
        while(matcher.find()) {
            count++;
        }
        paramCount = count;
        this.isPublicationSubPath = isPublicationSubPath;
    }

    public String getUrl(List<String> params) {
        return getUrl(params.toArray(new String[]{}));
    }

    public String getUrl(String ... params) {
        assert !isPublicationSubPath : "Should only use getUrl for URLs that are NOT Publication sub-paths! not/" + this;
        String baseUrl;
        // bl: if we're in the cluster CP, generate the URL from the narrative platform area.
        if(networkContext().getRequestType().isClusterCp()) {
            baseUrl = Area.dao().getNarrativePlatformArea().getPrimaryAreaUrl();
        } else {
            baseUrl = networkContext().getBaseUrl();
        }
        return baseUrl + getPath(params);
    }

    public String getPublicationUrl(Publication publication, String ... params) {
        assert isPublicationSubPath : "Should only use getPublicationUrl for URLs that are Publication sub-paths! not/" + this;
        return publication.getDisplayUrl() + getPath(params);
    }

    private String getPath(String ... params) {
        assert (params==null ? 0 : params.length) == paramCount : "Invalid params for " + this + ": " + Arrays.toString(params);
        if(paramCount==0) {
            return path;
        }
        int i = 0;
        StringBuffer sb = new StringBuffer();
        Matcher matcher = RoutePattern.PATH_PATTERN.matcher(path);
        while(matcher.find()) {
            matcher.appendReplacement(sb, IPHTMLUtil.getURLEncodedStringButDontEncodeSpacesToPlus(params[i]));
            i++;
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * can't access static fields from Enum constructors, so set the static Pattern in a sub-class
     */
    private static class RoutePattern {
        private static final Pattern PATH_PATTERN = java.util.regex.Pattern.compile(":\\w+");
    }
}
