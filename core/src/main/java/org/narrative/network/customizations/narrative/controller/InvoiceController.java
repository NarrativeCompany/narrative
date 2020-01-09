package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.invoice.FiatPaymentInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.invoice.ImmediateFiatPaymentInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.invoice.NrvePaymentInputDTO;
import org.narrative.network.customizations.narrative.service.api.InvoiceService;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceStatusDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrvePaymentDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/invoices")
@Validated
public class InvoiceController {

    public static final String INVOICE_OID_PARAM = "invoiceOid";
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{" + INVOICE_OID_PARAM + "}")
    public InvoiceDetailDTO getInvoice(@PathVariable(INVOICE_OID_PARAM) OID invoiceOid) {
        return invoiceService.getInvoice(invoiceOid);
    }

    @GetMapping("/{" + INVOICE_OID_PARAM + "}/status")
    public InvoiceStatusDetailDTO getInvoiceStatus(@PathVariable(INVOICE_OID_PARAM) OID invoiceOid) {
        return invoiceService.getInvoicePaymentStatus(invoiceOid);
    }

    @PutMapping("/{" + INVOICE_OID_PARAM + "}/nrve-payment")
    public NrvePaymentDTO putNrvePayment(@Valid @RequestBody NrvePaymentInputDTO nrvePaymentInput, @PathVariable(INVOICE_OID_PARAM) OID invoiceOid) {
        return invoiceService.putNrvePayment(nrvePaymentInput, invoiceOid);
    }

    @DeleteMapping("/{" + INVOICE_OID_PARAM + "}/nrve-payment")
    public InvoiceDetailDTO deleteNrvePayment(@PathVariable(INVOICE_OID_PARAM) OID invoiceOid) {
        return invoiceService.deleteNrvePayment(invoiceOid);
    }

    @PutMapping("/{" + INVOICE_OID_PARAM + "}/fiat-payment")
    public InvoiceDetailDTO putFiatPayment(@Valid @RequestBody FiatPaymentInputDTO paymentInput, @PathVariable(INVOICE_OID_PARAM) OID invoiceOid) {
        return invoiceService.putFiatPayment(paymentInput, invoiceOid);
    }

    @PostMapping("/fiat-payment")
    public InvoiceDetailDTO postImmediateFiatPayment(@Valid @RequestBody ImmediateFiatPaymentInputDTO paymentInput) {
        return invoiceService.postImmediateFiatPayment(paymentInput);
    }
}
