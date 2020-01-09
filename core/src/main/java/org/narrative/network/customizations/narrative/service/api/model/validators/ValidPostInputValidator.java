package org.narrative.network.customizations.narrative.service.api.model.validators;

import org.narrative.network.customizations.narrative.service.api.model.input.PostInput;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-01-07
 * Time: 10:36
 *
 * @author jonmark
 */
public class ValidPostInputValidator extends ValidPostTextInputValidatorBase<PostInput> implements ConstraintValidator<ValidPostInput, PostInput> {
    @Override
    public void initialize(ValidPostInput constraintAnnotation) {}

    @Override
    protected boolean isDraft(PostInput input) {
        // jw: for a full post input let's drive the draft flag from the data itself.
        return input.isDraft();
    }

    @Override
    protected boolean validatePost(PostInput input, ConstraintValidatorContext context) {
        // jw: First, let's allow the parent to validate the title, subtitle and body.
        boolean isValid = super.validatePost(input, context);

        // jw: finally, and this is not 100% solid since we are not saturating any objects, let's ensure that we are being
        //     requested to publish to at least one place
        if (input.getPublishToPrimaryChannel()==null && isEmptyOrNull(input.getPublishToNiches())) {
            context.buildConstraintViolationWithTemplate("{validNarrativePostInputValidator.mustPublishSomewhere}").addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
