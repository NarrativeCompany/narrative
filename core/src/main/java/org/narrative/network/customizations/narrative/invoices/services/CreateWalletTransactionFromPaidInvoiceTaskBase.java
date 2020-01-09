package org.narrative.network.customizations.narrative.invoices.services;

import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;

/**
 * Date: 2019-05-20
 * Time: 07:45
 *
 * @author jonmark
 */
public abstract class CreateWalletTransactionFromPaidInvoiceTaskBase extends AreaTaskImpl<WalletTransaction> {
    protected final Invoice invoice;

    protected CreateWalletTransactionFromPaidInvoiceTaskBase(Invoice invoice) {
        assert invoice.getStatus().isPaid() : "The provided invoice should be paid by this point!";

        this.invoice = invoice;
    }
}
