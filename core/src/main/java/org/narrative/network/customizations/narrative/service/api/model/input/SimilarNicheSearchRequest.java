package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Date: 10/18/18
 * Time: 9:38 AM
 *
 * @author brian
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SimilarNicheSearchRequest extends NicheInputBase {
    @Builder
    public SimilarNicheSearchRequest(@NotNull String name, @NotNull String description) {
        super(name, description);
    }
}
