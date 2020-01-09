package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.UnexpectedError;
import org.narrative.common.util.ValidationHandler;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-20
 * Time: 07:30
 *
 * @author brian
 */
@Data
@FieldNameConstants
@Validated
public class TempFileUploadInput {
    @NotNull
    private final OID oid;
    @NotEmpty
    private final String token;
}
