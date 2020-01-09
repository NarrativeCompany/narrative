package org.narrative.network.customizations.narrative.niches.nicheauction.services;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.services.RefundFiatPaymentBaseTask;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-04-17
 * Time: 09:57
 *
 * @author jonmark
 */
public class RefundNicheAuctionSecurityDepositTask extends AreaTaskImpl<Object> {
    private final NicheAuctionSecurityDeposit securityDeposit;

    public RefundNicheAuctionSecurityDepositTask(NicheAuctionSecurityDeposit securityDeposit) {
        this.securityDeposit = securityDeposit;

        assert exists(securityDeposit) : "Should never use this task unless you have a security deposit.";
    }

    @Override
    protected Object doMonitoredTask() {
        FiatPayment payment = securityDeposit.getInvoice().getFiatPayment();

        assert payment.getProcessorType()!=null : "How did we get here if the payment does not have a processorType? Creating the payment should have set that!";

        RefundFiatPaymentBaseTask refundTask = payment.getProcessorType().getPaymentRefundProcessor(payment);

        if (refundTask == null) {
            throw UnexpectedError.getRuntimeException("Should always get a refund task at this point.");
        }

        return getAreaContext().doAreaTask(refundTask);
    }
}
