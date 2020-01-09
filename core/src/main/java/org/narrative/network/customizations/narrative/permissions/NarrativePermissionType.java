package org.narrative.network.customizations.narrative.permissions;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.security.area.base.AreaRole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 2/9/18
 * Time: 2:34 PM
 */
public enum NarrativePermissionType {
    SUGGEST_NICHES(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.suggestNiches")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.suggestNiches")
                    .build(),
            LowReputationPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.suggestNiches")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.lowReputation.suggestNiches")
                    .build(),
            new SuggestNichesPermissionCheck()
    ),
    CREATE_PUBLICATIONS(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.createPublications")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.createPublications")
                    .build()
    ),
    BID_ON_NICHES(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.bidOnNiches")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.bidOnNiches")
                    .build(),
            new BidOnNichesAvailableSlotPermissionCheck(),
            new BidOnNichesLowRepPermissionCheck()
    ),
    SUBMIT_TRIBUNAL_APPEAL(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.submitTribunalAppeal")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.submitTribunalAppeal")
                    .build(),
            LowReputationPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.submitTribunalAppeal")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.lowReputation.submitTribunalAppeal")
                    .build(),
            new SubmitTribunalAppealsPermissionCheck()
    ),
    VOTE_ON_APPROVALS(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.voteOnApprovals")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.voteOnApprovals")
                    .build()
    ),
    RATE_CONTENT(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.rateContent")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.rateContent")
                    .build()
    ),
    NOMINATE_FOR_MODERATOR_ELECTIONS(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.nominateForModeratorElections")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.nominateForModeratorElections")
                    .build()
    ),
    POST_CONTENT(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.postContent")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.postContent")
                    .build()
    ),
    POST_COMMENTS(
            ConductPermissionCheck.builder()
                    .permissionRevokedTitleWordletKey("managedNarrativeCircleType.accessError.title.postComments")
                    .permissionRevokedMessageWordletKey("managedNarrativeCircleType.accessError.conductNegative.postComments")
                    .build()
    ),
    ;

    private final List<NarrativePermissionCheck> permissionChecks;

    NarrativePermissionType(NarrativePermissionCheck... permissionChecks) {
        assert !isEmptyOrNull(permissionChecks) : "You must supply at least one permission check!";
        this.permissionChecks = Collections.unmodifiableList(Arrays.asList(permissionChecks));
    }

    public UserPermission getUserPermissionForAreaRole(AreaRole areaRole) {
        // jw: We want to return the last permission, or the first one where the permission is not granted. This way
        //     as long as the check passes we will move on to the next one.
        UserPermission latestUserPermission = null;
        for (NarrativePermissionCheck permission : permissionChecks) {
            UserPermission userPermission = permission.getUserPermissionForAreaRole(areaRole);
            // jw: let's only return the first one that is not granted. Otherwise we want to fall
            //     through and check them all. If we just use the first one then a subsequent one could fail.
            if (!userPermission.isGranted()) {
                return userPermission;
            }
            latestUserPermission = userPermission;
        }
        // jw: if we did not find any negative checks, then there should have been at least one that passed.
        if (latestUserPermission==null) {
            throw UnexpectedError.getRuntimeException("Should always get a UserPermission!");
        }

        return latestUserPermission;
    }

    public void checkRight(AreaRole areaRole) {
        // bl: every permission requires that you're registered, so just centralize that check here
        areaRole.checkRegisteredCommunityUser();
        for (NarrativePermissionCheck permission : permissionChecks) {
            permission.checkRight(areaRole);
        }
    }
}