package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.paypal.services.PayPalCheckoutDetails;
import org.narrative.network.customizations.narrative.service.api.model.FiatPaymentDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceStatusDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheAuctionInvoiceDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrvePaymentDTO;
import org.narrative.network.customizations.narrative.service.api.model.PayPalCheckoutDetailsDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ServiceMapperConfig.class, uses = {NicheAuctionMapper.class})
public interface InvoiceMapper {
    /**
     * Map from {@link NicheAuctionInvoice} entity to {@link NicheAuctionInvoiceDTO}.
     *
     * @param nicheAuctionInvoice The incoming niche auction invoice entry entity to map
     * @return The mapped niche auction invoice
     */
    NicheAuctionInvoiceDTO mapNicheAuctionInvoiceToDTO(NicheAuctionInvoice nicheAuctionInvoice);

    @Mapping(source = "usdValue", target = InvoiceDetailDTO.Fields.usdAmount)
    @Mapping(expression = "java(mapNicheAuctionInvoiceToDTO(invoice.getType().isNicheAuction() ? invoice.getInvoiceConsumer() : null))", target = InvoiceDetailDTO.Fields.nicheAuctionInvoice)
    InvoiceDetailDTO mapInvoiceToInvoiceDetailDTO(Invoice invoice);

    InvoiceDTO mapInvoiceToInvoiceDTO(Invoice invoice);

    @Mapping(source = "invoiceForStatusPolling", target = InvoiceStatusDetailDTO.Fields.invoice)
    InvoiceStatusDetailDTO mapInvoiceEntityToInvoiceStatusDTO(Invoice invoice);

    @Mapping(expression = "java(nrvePayment.hasBeenPaid())", target = "hasBeenPaid")
    NrvePaymentDTO mapNrvePaymentToNrvePaymentDTO(NrvePayment nrvePayment);

    @Mapping(source = "feeUsdValue", target = FiatPaymentDTO.Fields.feeUsdAmount)
    @Mapping(source = "usdValue", target = FiatPaymentDTO.Fields.usdAmount)
    @Mapping(source = "totalUsdValue", target = FiatPaymentDTO.Fields.totalUsdAmount)
    @Mapping(expression = "java(fiatPayment.hasBeenPaid())", target = FiatPaymentDTO.Fields.hasBeenPaid)
    FiatPaymentDTO mapFiatPaymentToFiatPaymentDTO(FiatPayment fiatPayment);

    PayPalCheckoutDetailsDTO mapPayPalCheckoutDetailToDTO(PayPalCheckoutDetails checkoutDetails);
}
