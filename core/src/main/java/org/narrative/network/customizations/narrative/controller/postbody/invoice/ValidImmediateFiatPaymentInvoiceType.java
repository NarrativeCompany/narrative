package org.narrative.network.customizations.narrative.controller.postbody.invoice;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Date: 2019-02-06
 * Time: 14:17
 *
 * @author jonmark
 */
@Target({ TYPE, ANNOTATION_TYPE, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { ValidImmediateFiatPaymentInvoiceTypeValidator.class })
@Documented
public @interface ValidImmediateFiatPaymentInvoiceType {
    String message() default "{validImmediateFiatPaymentInvoiceType.defaultMessage}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
