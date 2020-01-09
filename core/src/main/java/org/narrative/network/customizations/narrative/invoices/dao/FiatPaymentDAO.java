package org.narrative.network.customizations.narrative.invoices.dao;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

import java.util.List;

/**
 * Date: 2019-02-04
 * Time: 10:24
 *
 * @author jonmark
 */
public class FiatPaymentDAO extends GlobalDAOImpl<FiatPayment, OID> {
    public FiatPaymentDAO() {
        super(FiatPayment.class);
    }

    public FiatPayment getByTransactionId(String transactionId) {
        return getUniqueBy(new NameValuePair<>(FiatPayment.Fields.transactionId, transactionId));
    }

    public List<FiatPayment> getWithTransactionForToWalletAndStatus(Wallet toWallet, WalletTransactionStatus status) {
        return getGSession().createNamedQuery("fiatPayment.getWithTransactionForToWalletAndStatus", FiatPayment.class)
                .setParameter("toWallet", toWallet)
                .setParameter("status", status)
                .list();
    }

    public NrveValue getTransactionSumToWallet(Wallet toWallet) {
        NrveValue value = getGSession().createNamedQuery("fiatPayment.getTransactionSumToWallet", NrveValue.class)
                .setParameter("toWallet", toWallet)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }
}
