package org.narrative.network.customizations.narrative.invoices.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.invoices.NrvePaymentStatus;
import org.narrative.network.shared.daobase.GlobalDAOImpl;
import org.hibernate.LockMode;

/**
 * Date: 2019-02-04
 * Time: 10:24
 *
 * @author jonmark
 */
public class NrvePaymentDAO extends GlobalDAOImpl<NrvePayment, OID> {
    public NrvePaymentDAO() {
        super(NrvePayment.class);
    }

    public NrvePayment getPendingPayment(String neoAddress, NrveValue nrve) {
        return getPendingPayment(neoAddress, nrve, LockMode.NONE);
    }

    public NrvePayment getPendingPayment(String neoAddress, NrveValue nrveAmount, LockMode lockMode) {
        return (NrvePayment)getGSession().getNamedQuery("nrvePayment.getPendingPayment")
                .setParameter("neoAddress", neoAddress)
                .setParameter("nrveAmount", nrveAmount)
                .setParameter("pendingPaymentStatus", NrvePaymentStatus.PENDING_PAYMENT)
                .setLockMode("payment", lockMode)
                .uniqueResult();
    }

    public NrveValue getTransactionSumToWallet(Wallet toWallet) {
        NrveValue value = getGSession().createNamedQuery("nrvePayment.getTransactionSumToWallet", NrveValue.class)
                .setParameter("toWallet", toWallet)
                .uniqueResult();

        return value != null ? value : NrveValue.ZERO;
    }
}
