package org.narrative.network.customizations.narrative.service.api.model.validators;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Date: 2019-01-15
 * Time: 08:30
 *
 * @author jonmark
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidPostTextInputValidator.class })
@Documented
public @interface ValidPostTextInput {
    String message() default "{validPostTextInput.defaultMessage}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
