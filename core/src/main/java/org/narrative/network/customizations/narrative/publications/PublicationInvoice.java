package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.persistence.*;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.services.RefundProratedRevenueTask;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.NrveValueDetail;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.InvoiceConsumer;
import org.narrative.network.customizations.narrative.invoices.InvoicePaymentBase;
import org.narrative.network.customizations.narrative.publications.dao.*;

import org.narrative.network.shared.daobase.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-23
 * Time: 10:37
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PublicationInvoice implements DAOObject<PublicationInvoiceDAO>, InvoiceConsumer {
    // jw: we will give the publication owner 1 hour to pay the invoice
    public static final Duration INVOICE_PERIOD = Duration.ofHours(1);

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = PublicationInvoice.Fields.invoice)})
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_publicationInvoice_publication")
    private Publication publication;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = PublicationInvoice.Fields.oid)
    @ForeignKey(name = "fk_publicationInvoice_invoice")
    private Invoice invoice;

    @NotNull
    @Type(type=IntegerEnumType.TYPE)
    private PublicationPlanType plan;

    public PublicationInvoice(Publication publication, Invoice invoice, PublicationPlanType plan) {
        assert exists(publication) : "We should always be provided a Publications!";
        assert exists(invoice) : "We should always be provided a invoice!";
        assert plan != null : "We should always be provided a plan!";

        this.publication = publication;
        this.invoice = invoice;
        this.plan = plan;
    }

    @Override
    public String getInvoiceConsumerTypeName() {
        return wordlet("publicationInvoice.invoiceConsumerTypeName");
    }

    @Override
    public String getConsumerDisplayName() {
        return getPublication().getName();
    }

    @Override
    public String getConsumerDisplayUrl() {
        return null;
    }

    @Override
    public BigDecimal getNrveUsdPrice() {
        throw UnexpectedError.getRuntimeException("This method should never be called since we manually create the FiatPayment up front with the locked in nrveUsdPrice.");
    }

    public boolean isForPlanUpgrade() {
        assert !isEqual(getInvoice(), getPublication().getChannel().getPurchaseInvoice()) : "This method should only be used before the invoice has been set as the paid invoice.";

        return !getPublication().isInTrialPeriod() && getPublication().getUpgradePlans().contains(getPlan());
    }

    public Instant getNewEndDatetime() {
        assert !isEqual(getInvoice(), getPublication().getChannel().getPurchaseInvoice()) : "This method should only be used before the invoice has been set as the paid invoice.";

        // jw: For publications it's all about endDatetime which will depend on what kind of invoice this is. If it is
        //     a upgrade, or the endDatetime is in the past then the endDatetime should be a year from now, otherwise it
        //     should be a year from when the Publication's current plan ends.
        Instant planStartDatetime = getPublication().getEndDatetime();
        if (isForPlanUpgrade() || getPublication().getStatusResolved().isExpired()) {
            planStartDatetime = Instant.now();
        }

        return planStartDatetime.atOffset(RewardUtils.REWARDS_ZONE_OFFSET).plus(Publication.PLAN_PERIOD).toInstant();
    }

    public NrveValueDetail getEstimatedRefundAmount() {
        assert getInvoice().getStatus().isInvoiced() : "This method should only be used while the invoice is pending payment!";

        // jw: we only give refunds for plan upgrades.
        if (!isForPlanUpgrade()) {
            return null;
        }

        // jw: and only if the publication already has a purchased invoice.
        Invoice purchaseInvoice = getPublication().getChannel().getPurchaseInvoice();
        if (!exists(purchaseInvoice)) {
            return null;
        }

        // jw: we are finally ready to calculate the refund amount using the same logic we will use once we issue the
        //     refund.
        InvoicePaymentBase payment = purchaseInvoice.getPurchasePaymentResolved();
        NrveValue refundAmount = RefundProratedRevenueTask.calculateRefundAmount(
                payment,
                // jw: the original payment should have been made to a prorated month revenue, so let's get that unlocked
                //     since we are not actively going to be using the value right now... No need to hold anything else up.
                payment.getPaymentWalletTransaction().getToWallet().getProratedMonthRevenue()
        );
        return new NrveValueDetail(refundAmount);
    }

    public static PublicationInvoiceDAO dao() {
        return NetworkDAOImpl.getDAO(PublicationInvoice.class);
    }
}
