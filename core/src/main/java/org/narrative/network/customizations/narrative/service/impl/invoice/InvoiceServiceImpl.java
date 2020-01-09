package org.narrative.network.customizations.narrative.service.impl.invoice;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceStatus;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.niches.nicheauction.services.EnsureInvoiceFiatPaymentTask;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.InvoiceService;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceStatusDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrvePaymentDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.FiatPaymentInput;
import org.narrative.network.customizations.narrative.service.api.model.input.ImmediateFiatPaymentInput;
import org.narrative.network.customizations.narrative.service.api.model.input.NrvePaymentInput;
import org.narrative.network.customizations.narrative.service.mapper.InvoiceMapper;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.springframework.stereotype.Service;

import static org.narrative.common.util.CoreUtils.*;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final AreaTaskExecutor areaTaskExecutor;
    private final InvoiceMapper invoiceMapper;

    public InvoiceServiceImpl(AreaTaskExecutor areaTaskExecutor, InvoiceMapper invoiceMapper) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    public InvoiceDetailDTO getInvoice(OID invoiceOid) {
        Invoice invoice = getInvoiceFromOid(invoiceOid, true);

        return invoiceMapper.mapInvoiceToInvoiceDetailDTO(invoice);
    }

    @Override
    public InvoiceStatusDetailDTO getInvoicePaymentStatus(OID invoiceOid) {
        // quick check up front to see if the invoice is still invoiced. if so, we don't need to fetch the object graph.
        InvoiceStatus status = Invoice.dao().getInvoiceStatus(invoiceOid);
        // bl: the base, most common case is that the status is still invoiced, in which case we return the simplest result.
        if(status!=null && status.isInvoiced()) {
            return InvoiceStatusDetailDTO.builder().status(status).build();
        }
        Invoice invoice = getInvoiceFromOid(invoiceOid, false);

        return invoiceMapper.mapInvoiceEntityToInvoiceStatusDTO(invoice);
    }

    // jw: handles getting the Invoice object and verifying that the current user has the right to view it.
    private Invoice getInvoiceFromOid(OID invoiceOid, boolean setupFiatPayments) {
        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Invoice>(false) {
            @Override
            protected Invoice doMonitoredTask() {
                Invoice invoice = PayInvoiceBaseTask.getAndValidateInvoice(invoiceOid, getAreaContext().getAreaRole());

                if (setupFiatPayments && invoice.getStatus().isInvoiced()) {
                    if (getAreaContext().doAreaTask(new EnsureInvoiceFiatPaymentTask(invoice))) {
                        // jw: since the task updated the invoice in a separate transaction, we need to refresh the object.
                        Invoice.dao().refresh(invoice);
                    }
                }

                return invoice;
            }
        });
    }

    @Override
    public NrvePaymentDTO putNrvePayment(NrvePaymentInput nrvePaymentInput, OID invoiceOid) {
        NrvePayment nrvePayment = areaTaskExecutor.executeAreaTask(new PayInvoiceWithNrveTask(nrvePaymentInput, invoiceOid));
        return invoiceMapper.mapNrvePaymentToNrvePaymentDTO(nrvePayment);
    }

    @Override
    public InvoiceDetailDTO deleteNrvePayment(OID invoiceOid) {
        Invoice invoice = areaTaskExecutor.executeAreaTask(new AreaTaskImpl<Invoice>() {
            @Override
            protected Invoice doMonitoredTask() {
                Invoice invoice = PayInvoiceBaseTask.getAndValidateInvoice(invoiceOid, getAreaContext().getAreaRole());
                NrvePayment payment = invoice.getNrvePayment();

                // jw: Only delete it if it has not already been paid, otherwise we need to deliver the invoice to
                //     the client and let them re-render with the new data!
                if (exists(payment) && !payment.hasBeenPaid()) {
                    invoice.setNrvePayment(null);
                    NrvePayment.dao().delete(payment);
                }

                return invoice;
            }
        });

        return invoiceMapper.mapInvoiceToInvoiceDetailDTO(invoice);
    }

    @Override
    public InvoiceDetailDTO putFiatPayment(FiatPaymentInput paymentInput, OID invoiceOid) {
        FiatPayment fiatPayment = areaTaskExecutor.executeAreaTask(new ProcessInvoiceFiatPaymentTask(
                invoiceOid
                , paymentInput.getProcessorType()
                , paymentInput.getPaymentToken()

        ));
        return invoiceMapper.mapInvoiceToInvoiceDetailDTO(fiatPayment.getInvoice());
    }

    @Override
    public InvoiceDetailDTO postImmediateFiatPayment(ImmediateFiatPaymentInput paymentInput) {
        Invoice invoice = areaTaskExecutor.executeAreaTask(new ProcessImmediateFiatPaymentTask(
                paymentInput.getProcessorType()
                , paymentInput.getInvoiceType()
                , paymentInput.getPaymentToken()
        ));
        return invoiceMapper.mapInvoiceToInvoiceDetailDTO(invoice);
    }
}
