package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * Date: 11/28/18
 * Time: 7:23 AM
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("TopNiche")
@Value
@Builder(toBuilder = true)
@FieldNameConstants
public class TopNicheDTO implements Serializable {
    private final OID oid;
    private final String name;
    private final String prettyUrlString;
    private final long totalPosts;
}
