package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * Date: 2019-03-09
 * Time: 14:44
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("NicheStats")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class NicheStatsDTO implements Serializable {
    private static final long serialVersionUID = 6080304012030421241L;

    private final long nichesForSale;
    private final long nichesAwaitingApproval;
}
