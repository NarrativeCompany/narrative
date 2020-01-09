package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.security.area.community.advanced.services.GlobalSecurable;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.permissions.NarrativePermissionType;
import org.narrative.network.customizations.narrative.permissions.UserPermission;
import org.narrative.network.customizations.narrative.service.api.model.permissions.BidOnNichesPermissionDTO;
import org.narrative.network.customizations.narrative.service.api.model.permissions.GlobalPermissionsDTO;
import org.narrative.network.customizations.narrative.service.api.model.permissions.PermissionDTO;
import org.narrative.network.customizations.narrative.service.api.model.permissions.StandardPermissionDTO;
import org.narrative.network.customizations.narrative.service.api.model.permissions.SubmitTribunalAppealsPermissionDTO;
import org.narrative.network.customizations.narrative.service.api.model.permissions.SuggestNichesPermissionDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import java.util.Map;

/**
 * Date: 10/17/18
 * Time: 7:37 PM
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public abstract class PermissionsMapper {
    /**
     * Create a {@link GlobalPermissionsDTO} from a {@link User}
     * @param user the {@link User} to map to {@link GlobalPermissionsDTO}
     * @return the new {@link GlobalPermissionsDTO}
     */
    public GlobalPermissionsDTO mapGlobalPermissionsMapToDto(User user) {
        if (user == null) {
            return null;
        }

        GlobalPermissionsDTO.GlobalPermissionsDTOBuilder globalPermissionsDTO = GlobalPermissionsDTO.builder();

        Map<NarrativePermissionType, UserPermission> narrativePermissions = user.getNarrativePermissions();
        globalPermissionsDTO.voteOnApprovals(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.VOTE_ON_APPROVALS)));
        globalPermissionsDTO.rateContent(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.RATE_CONTENT)));
        globalPermissionsDTO.nominateForModeratorElection(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.NOMINATE_FOR_MODERATOR_ELECTIONS)));
        globalPermissionsDTO.postContent(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.POST_CONTENT)));
        globalPermissionsDTO.postComments(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.POST_COMMENTS)));

        globalPermissionsDTO.submitTribunalAppeals(mapUserPermissionToSubmitTribunalAppealsPermissionDto(narrativePermissions.get(NarrativePermissionType.SUBMIT_TRIBUNAL_APPEAL)));
        globalPermissionsDTO.suggestNiches(mapUserPermissionToSuggestNichesPermissionDto(narrativePermissions.get(NarrativePermissionType.SUGGEST_NICHES)));
        globalPermissionsDTO.bidOnNiches(mapUserPermissionToBidOnNichesPermissionDto(narrativePermissions.get(NarrativePermissionType.BID_ON_NICHES)));
        globalPermissionsDTO.createPublications(mapUserPermissionToStandardPermissionDto(narrativePermissions.get(NarrativePermissionType.CREATE_PUBLICATIONS)));

        Map<GlobalSecurable, PermissionDTO> globalPermissions = user.getGlobalPermissions();
        globalPermissionsDTO.participateInTribunalActions(globalPermissions.get(GlobalSecurable.PARTICIPATE_IN_TRIBUNAL_ACTIONS));
        globalPermissionsDTO.removeAupViolations(globalPermissions.get(GlobalSecurable.REMOVE_AUP_VIOLATIONS));

        return globalPermissionsDTO.build();
    }

    /**
     * Create a {@link StandardPermissionDTO} from a {@link UserPermission}
     * @param userPermission the {@link UserPermission} to map to {@link StandardPermissionDTO}
     * @return the new {@link StandardPermissionDTO}
     */
    public abstract StandardPermissionDTO mapUserPermissionToStandardPermissionDto(UserPermission userPermission);

    /**
     * Create a {@link SuggestNichesPermissionDTO} from a {@link UserPermission}
     * @param userPermission the {@link UserPermission} to map to {@link SuggestNichesPermissionDTO}
     * @return the new {@link SuggestNichesPermissionDTO}
     */
    public abstract SuggestNichesPermissionDTO mapUserPermissionToSuggestNichesPermissionDto(UserPermission userPermission);

    /**
     * Create a {@link BidOnNichesPermissionDTO} from a {@link UserPermission}
     * @param userPermission the {@link UserPermission} to map to {@link BidOnNichesPermissionDTO}
     * @return the new {@link BidOnNichesPermissionDTO}
     */
    public abstract BidOnNichesPermissionDTO mapUserPermissionToBidOnNichesPermissionDto(UserPermission userPermission);

    /**
     * Create a {@link SubmitTribunalAppealsPermissionDTO} from a {@link UserPermission}
     * @param userPermission the {@link UserPermission} to map to {@link SubmitTribunalAppealsPermissionDTO}
     * @return the new {@link SubmitTribunalAppealsPermissionDTO}
     */
    public abstract SubmitTribunalAppealsPermissionDTO mapUserPermissionToSubmitTribunalAppealsPermissionDto(UserPermission userPermission);
}
