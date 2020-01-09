package org.narrative.network.customizations.narrative.service.api.model.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

/**
 * Date: 2019-01-15
 * Time: 08:27
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class PostTextInputBase {
    private final String title;
    private final String subTitle;
    private final String body;
    private final String canonicalUrl;

    public PostTextInputBase(String title, String subTitle, String body, String canonicalUrl) {
        this.title = title;
        this.subTitle = subTitle;
        this.body = body;
        this.canonicalUrl = canonicalUrl;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Fields {}
}
