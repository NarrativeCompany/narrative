package org.narrative.network.customizations.narrative.service.api.model.input;

import org.narrative.common.persistence.OID;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Date: 2019-01-04
 * Time: 15:17
 *
 * @author jonmark
 */
@Data
@EqualsAndHashCode()
@ToString(callSuper = true)
@Validated
@FieldNameConstants
public class PostInput extends PostTextInputBase {
    private final boolean draft;

    private final OID publishToPrimaryChannel;
    private final boolean disableComments;
    private final boolean ageRestricted;

    private final List<OID> publishToNiches;

    @Builder
    public PostInput(boolean draft, String title, String subTitle, String body, String canonicalUrl, OID publishToPrimaryChannel, boolean disableComments, boolean ageRestricted, List<OID> publishToNiches) {
        super(title, subTitle, body, canonicalUrl);
        this.draft = draft;
        this.publishToPrimaryChannel = publishToPrimaryChannel;
        this.disableComments = disableComments;
        this.ageRestricted = ageRestricted;
        this.publishToNiches = publishToNiches;
    }

    /**
     * bl: Lombok will add the field name constants to this class, but i'm defining them explicitly
     * so that we have inheritance of Fields from superclasses (and subclasses can use it accordingly)
     * lombok feature request: https://github.com/rzwitserloot/lombok/issues/2090
     */
    public static class Fields extends PostTextInputBase.Fields {}
}
