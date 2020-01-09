package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Date: 2019-03-22
 * Time: 20:30
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("FollowScrollParams")
@Data
@NoArgsConstructor
public class FollowScrollParamsDTO {
    private String lastItemName;
    private OID lastItemOid;

    @Builder(toBuilder = true)
    public FollowScrollParamsDTO(String lastItemName, OID lastItemOid) {
        this.lastItemName = lastItemName;
        this.lastItemOid = lastItemOid;
    }
}
