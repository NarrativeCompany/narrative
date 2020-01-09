package org.narrative.network.customizations.narrative.service.api.model.permissions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Date: 10/17/18
 * Time: 5:34 PM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("GlobalPermissions")
@Value
@FieldNameConstants
@Builder
public class GlobalPermissionsDTO {
    private final SuggestNichesPermissionDTO suggestNiches;
    private final StandardPermissionDTO createPublications;
    private final BidOnNichesPermissionDTO bidOnNiches;
    private final SubmitTribunalAppealsPermissionDTO submitTribunalAppeals;
    private final PermissionDTO participateInTribunalActions;
    private final PermissionDTO removeAupViolations;
    private final StandardPermissionDTO voteOnApprovals;
    private final StandardPermissionDTO rateContent;
    private final StandardPermissionDTO nominateForModeratorElection;
    private final StandardPermissionDTO postContent;
    private final StandardPermissionDTO postComments;
}
