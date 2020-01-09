package org.narrative.network.customizations.narrative.service.api.model.validators;

import org.narrative.network.customizations.narrative.service.api.model.input.PostTextInput;

import javax.validation.ConstraintValidator;

/**
 * Date: 2019-01-15
 * Time: 08:22
 *
 * @author jonmark
 */
public class ValidPostTextInputValidator extends ValidPostTextInputValidatorBase<PostTextInput> implements ConstraintValidator<ValidPostTextInput, PostTextInput> {

    @Override
    public void initialize(ValidPostTextInput constraintAnnotation) {}

    @Override
    protected boolean isDraft(PostTextInput input) {
        // jw: this validator is used when the user switches from the textual page to niche selection with the intent of
        //     publishing. As a result, let's never consider this a draft for purposes of validation.
        return false;
    }
}