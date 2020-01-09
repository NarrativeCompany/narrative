package org.narrative.network.customizations.narrative.controller.postbody.tempfile;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.service.api.model.input.TempFileUploadInput;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Date: 2019-08-20
 * Time: 07:29
 *
 * @author brian
 */
public class TempFileUploadInputDTO extends TempFileUploadInput {
    public TempFileUploadInputDTO(
            @JsonProperty(Fields.oid) @NotNull OID oid,
            @JsonProperty(Fields.token) @NotEmpty String token
    ) {
        super(oid, token);
    }
}
