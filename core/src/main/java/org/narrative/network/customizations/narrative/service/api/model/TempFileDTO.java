package org.narrative.network.customizations.narrative.service.api.model;

import org.narrative.common.persistence.OID;
import lombok.Builder;
import lombok.Value;

/**
 * Date: 2019-08-19
 * Time: 15:50
 *
 * @author brian
 */
@Value
@Builder
public class TempFileDTO {
    private final OID oid;
    private final String token;
    private final String url;
}
