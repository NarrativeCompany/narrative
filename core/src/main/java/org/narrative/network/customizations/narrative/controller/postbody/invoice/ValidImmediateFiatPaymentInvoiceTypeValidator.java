package org.narrative.network.customizations.narrative.controller.postbody.invoice;

import org.narrative.network.customizations.narrative.invoices.InvoiceType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Date: 2019-02-06
 * Time: 14:18
 *
 * @author jonmark
 */
public class ValidImmediateFiatPaymentInvoiceTypeValidator implements ConstraintValidator<ValidImmediateFiatPaymentInvoiceType, InvoiceType> {

    @Override
    public void initialize(ValidImmediateFiatPaymentInvoiceType annotation) {}

    @Override
    public boolean isValid(InvoiceType invoiceType, ConstraintValidatorContext context) {
        // jw: we have a NotNull Annotation as well to ensure that this is not null, so let's only return false if we have
        //     a value to test.
        return invoiceType==null || invoiceType.isImmediateFiatPaymentType();
    }
}
