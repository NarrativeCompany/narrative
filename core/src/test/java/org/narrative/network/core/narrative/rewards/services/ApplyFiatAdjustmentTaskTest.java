package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.dao.ProratedMonthRevenueDAO;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.dao.WalletDAO;
import org.narrative.network.core.narrative.wallet.dao.WalletTransactionDAO;
import org.narrative.network.customizations.narrative.NrveValue;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-20
 * Time: 20:17
 *
 * @author jonmark
 */
class ApplyFiatAdjustmentTaskTest {

    @Test
    void doMonitoredTask_Success_SimulatedTaskExecution(@Mocked WalletDAO walletDao, @Mocked ProratedMonthRevenueDAO proratedMonthRevenueDAO, @Mocked WalletTransactionDAO transactionDao) {
        // jw: let's design this test so that it uses as few constants as possible.
        Random random = new Random();

        NrveValue adjustment = NrveValue.ZERO;

        // jw: let's set things up so that we expect 1 NRVE to removed from all but the last transaction
        ProratedMonthRevenue revenue = new ProratedMonthRevenue(ProratedRevenueType.NICHE_REVENUE, RewardUtils.APRIL_2019);
        // jw: let's setup the wallet with 100 NRVE in non-fiat funds.
        NrveValue nrveBalance = new NrveValue(BigDecimal.valueOf(100));
        revenue.getWallet().setBalance(nrveBalance);

        // jw: let's track what the original transaction values were before we adjust them.
        Map<OID, NrveValue> originalTransactionValues = new HashMap<>();

        List<WalletTransaction> fiatTransactions = new ArrayList<>();
        // jw: let's create between 5 and 15 "fiat transactions"
        int totalTransactions = random.nextInt(11)+5;
        for (int i = 1; i <= totalTransactions; i++) {
            WalletTransaction transaction = new WalletTransaction(
                    null,
                    revenue.getWallet(),
                    WalletTransactionType.NICHE_PAYMENT,
                    WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT,
                    // jw: let's set each transactions value to be between 2 and 50
                    new NrveValue(BigDecimal.valueOf(2+random.nextInt(49)))
            );
            transaction.setOid(new OID(i));
            fiatTransactions.add(transaction);
            // jw: make sure to add the fiat transaction to the wallet.
            revenue.getWallet().addFunds(transaction.getNrveAmount());

            // jw: for every fiat transaction let's add one NRVE to the adjustment.
            adjustment = adjustment.add(NrveValue.ONE);

            // jw: add the original value
            originalTransactionValues.put(transaction.getOid(), transaction.getNrveAmount());
        }

        NrveValue fiatBalance = revenue.getWallet().getBalance().subtract(nrveBalance);

        // jw: now that we are done setting up the transactions let's go ahead and add some neurons to ensure we test
        //     rounding loss and capturing at the end.
        NrveValue roundingLoss = new NrveValue((long) fiatTransactions.size() - 1);
        adjustment = adjustment.add(roundingLoss);

        NrveValue adjustedFiatBalance = fiatBalance.add(adjustment);

        // jw: let's track the wallet balance before we adjust it.
        NrveValue originalBalance = revenue.getWallet().getBalance();

        // jw: now the fun part, we need to mock several methods to get this working.
        new Expectations() {{
            // jw: need to return a mocked ProratedMonthRevenueDAO
            ProratedMonthRevenue.dao();
            result = proratedMonthRevenueDAO;
        }};
        new Expectations() {{
            // jw: capture and ignore the refreshForLock
            proratedMonthRevenueDAO.refreshForLock((ProratedMonthRevenue) any);
        }};

        new Expectations() {{
            // jw: need to return a mocked WalletDAO
            Wallet.dao();
            result = walletDao;
        }};
        new Expectations() {{
            // jw: capture and ignore the refreshForLock
            walletDao.refreshForLock((Wallet) any);
        }};

        new Expectations() {{
            // jw: need to return a mocked WalletTransactionDAO
            WalletTransaction.dao();
            result = transactionDao;
        }};
        new Expectations() {{
            // jw: provide the manufactured fiatTransactions when the task searches for them.
            transactionDao.getForToWalletAndStatus(revenue.getWallet(), WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT);
            result = fiatTransactions;
        }};
        new Expectations() {{
            // bl: provide the status map
            transactionDao.getTransactionSumByToWalletType(revenue.getWallet(), revenue.getType().getPaymentTransactionType());
            Map<WalletTransactionStatus,NrveValue> statusMap = new HashMap<>();
            statusMap.put(WalletTransactionStatus.COMPLETED, nrveBalance);
            statusMap.put(WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT, fiatBalance);
            result = statusMap;
        }};
        // bl: mock the NeoTransaction methods since we shouldn't need them
        new Expectations(revenue) {{
            revenue.recordNrvePaymentNeoTransaction();
            NeoTransaction nrveNeoTransaction = new NeoTransaction();
            nrveNeoTransaction.setNrveAmount(nrveBalance);
            result = nrveNeoTransaction;
        }};
        new Expectations(revenue) {{
            revenue.recordFiatPaymentNeoTransaction();
            NeoTransaction fiatNeoTransaction = new NeoTransaction();
            fiatNeoTransaction.setNrveAmount(adjustedFiatBalance);
            result = fiatNeoTransaction;
        }};

        // jw: I think we are finally ready to simulate this thing!
        ApplyFiatAdjustmentTask task = new ApplyFiatAdjustmentTask(revenue, adjustment);

        // jw: we are not going to simulate the whole environment, so just manually trigger doMonitoredTask.
        task.doMonitoredTask();

        assertEquals(adjustment, revenue.getWallet().getBalance().subtract(originalBalance), "The balance should have changed by the amount we expected.");
        // jw: the RewardUtils.distributePoolToStakeHolders method is tested through RewardUtilsTest, so lets just focus
        //     on ensuring that the differences between all transaction values totals the amount we expect.
        NrveValue actualTransactionAdjustment = NrveValue.ZERO;

        for (WalletTransaction transaction : fiatTransactions) {
            actualTransactionAdjustment = actualTransactionAdjustment.add(transaction.getNrveAmount().subtract(originalTransactionValues.get(transaction.getOid())));

            // jw: let's also ensure that all transactions were properly flagged as completed.
            assertEquals(WalletTransactionStatus.COMPLETED, transaction.getStatus());
        }
        assertEquals(adjustment, actualTransactionAdjustment, "The transactions should have been adjusted by the expected amount.");
    }
}