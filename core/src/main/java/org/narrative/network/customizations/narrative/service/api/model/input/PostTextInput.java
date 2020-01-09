package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-01-15
 * Time: 08:23
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
public class PostTextInput extends PostTextInputBase {
    @Builder
    public PostTextInput(String title, String subTitle, String body, String canonicalUrl) {
        super(title, subTitle, body, canonicalUrl);
    }
}
