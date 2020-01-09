package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import org.narrative.network.customizations.narrative.service.api.model.permissions.GlobalPermissionsDTO;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

/**
 * Value object representing a the Current User.
 */
@JsonValueObject
@JsonTypeName("CurrentUser")
@Value
@FieldNameConstants
@Builder(toBuilder = true)
public class CurrentUserDTO {
    private final UserDTO user;
    private final OID personalJournalOid;
    private final UserAgeStatus userAgeStatus;
    private final FormatPreferencesDTO formatPreferences;
    private final GlobalPermissionsDTO globalPermissions;
}
