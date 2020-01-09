package org.narrative.network.customizations.narrative.service.api.model.validators;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Date: 2019-01-07
 * Time: 10:34
 *
 * @author jonmark
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidPostInputValidator.class })
@Documented
public @interface ValidPostInput {
    String message() default "{validNarrativePostInput.defaultMessage}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
