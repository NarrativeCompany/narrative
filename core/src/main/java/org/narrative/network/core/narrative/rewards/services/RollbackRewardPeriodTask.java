package org.narrative.network.core.narrative.rewards.services;

import org.narrative.common.persistence.NameValuePair;
import org.narrative.common.persistence.OID;
import org.narrative.common.util.IPDateUtil;
import org.narrative.common.util.SubListIterator;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.core.narrative.rewards.NarrativeCompanyReward;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.RewardTransactionRef;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.UserElectorateReward;
import org.narrative.network.core.narrative.rewards.UserTribunalReward;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.GlobalTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;

import javax.persistence.LockModeType;

import java.time.YearMonth;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 2019-08-06
 * Time: 13:56
 *
 * @author brian
 */
public class RollbackRewardPeriodTask extends AreaTaskImpl<Object> {
    private final RewardPeriod period;

    public RollbackRewardPeriodTask(RewardPeriod period) {
        this.period = period;
    }

    @Override
    protected Object doMonitoredTask() {

        Wallet wallet = period.getWallet();
        // bl: lock it since we're going to do a lot with the wallet
        Wallet.dao().refreshForLock(wallet);

        NrveValue walletBalance = NrveValue.ZERO;

        // bl: first, get all of the revenue transactions _into_ the wallet
        {
            List<WalletTransaction> transactions = WalletTransaction.dao().getTransactionsToWallet(wallet);

            for (WalletTransaction transaction : transactions) {
                WalletTransactionType type = transaction.getType();

                // bl: reward period carryover is from the previous month, so we keep those in-tact
                // bl: same holds true for deleted user abandoned balances and refund reversals. keep those in place.
                if(type.isRewardPeriodCarryover() || type.isDeletedUserAbandonedBalances() || type.isRefundReversal()) {
                    walletBalance = walletBalance.add(transaction.getNrveAmount());
                    continue;
                }

                // bl: lock the from wallet, too
                Wallet fromWallet = transaction.getFromWallet();
                Wallet.dao().refreshForLock(fromWallet);

                // bl: put token mints back
                if(type.isMintedTokens()) {
                    // bl: note that i'm not handling deleting next month's TokenMintYear transaction, if one was created
                    // we would need to undo RecordTokenMintTransactionForYear, but not worrying about it for now.
                } else {
                    assert type.isProratedMonthRevenue() : "Found an unsupported transactionType/" + type;
                    // bl: for ProratedMonthRevenue, we have to also reduce the captures by 1
                    ProratedMonthRevenue revenue = ProratedMonthRevenue.dao().getForWallet(fromWallet, LockModeType.PESSIMISTIC_WRITE);
                    revenue.setCaptures(revenue.getCaptures() - 1);
                }

                // bl: add the funds back into the from wallet for all of these revenue types
                fromWallet.addFunds(transaction.getNrveAmount());

                // bl: finally, delete the transaction now that we've undone it
                WalletTransaction.dao().delete(transaction);
            }
        }

        // bl: now that we have corrected the revenue transactions, we need to correct the actual disbursement transactions
        {
            // bl: there are about 12k of these transactions
            List<OID> transactionOids = WalletTransaction.dao().getTransactionOidsFromWallet(wallet);
            Iterator<List<OID>> iter = new SubListIterator<>(transactionOids);
            while (iter.hasNext()) {
                List<OID> oidChunk = iter.next();
                TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                    @Override
                    protected Object doMonitoredTask() {
                        List<WalletTransaction> transactions = WalletTransaction.dao().getObjectsFromIDs(oidChunk);
                        for (WalletTransaction transaction : transactions) {
                            WalletTransactionType type = transaction.getType();

                            // bl: one special case is Narrative Company revenue. the transfer is to null, so we don't actually
                            // need to adjust a Wallet balance. but, we do need to delete the NarrativeCompanyReward record, also.
                            if(type.isNarrativeCompanyReward()) {
                                NarrativeCompanyReward narrativeCompanyReward = NarrativeCompanyReward.dao().getForPeriod(period);
                                NarrativeCompanyReward.dao().delete(narrativeCompanyReward);
                                continue;
                            }

                            // bl: subtract it from the destination wallet
                            Wallet toWallet = transaction.getToWallet();
                            Wallet.dao().refreshForLock(toWallet);

                            // bl: now, subtract this from the destination wallet's balance since we are reversing the transaction
                            toWallet.removeFunds(transaction.getNrveAmount());

                            // bl: we don't want to keep the carryover transaction, since it will need to be recalculated
                            if(type.isRewardPeriodCarryover()) {
                                assert toWallet.getBalance().equals(NrveValue.ZERO) : "Expecting a ZERO balance for next month, not/" + toWallet.getBalance();
                            } else {

                                // bl: for all other transactions, we need to delete the transaction and also deduct the user's wallet balance
                                assert type.getToWalletType().isUser() : "All other transactions from the RewardPeriod should be to a USER wallet! not/" + type.getToWalletType() + " for transactionType/" + type;

                                RewardTransactionRef rewardTransactionRef;

                                if(type.isContentReward()) {
                                    rewardTransactionRef = RoleContentReward.dao().getFirstBy(new NameValuePair<>(RoleContentReward.Fields.transaction, transaction));
                                } else if(type.isNicheOwnershipReward()) {
                                    rewardTransactionRef = NicheOwnerReward.dao().getFirstBy(new NameValuePair<>(NicheOwnerReward.Fields.transaction, transaction));
                                } else if(type.isNicheModerationReward()) {
                                    rewardTransactionRef = NicheModeratorReward.dao().getFirstBy(new NameValuePair<>(NicheModeratorReward.Fields.transaction, transaction));
                                } else if(type.isActivityReward()) {
                                    rewardTransactionRef = UserActivityReward.dao().getFirstBy(new NameValuePair<>(UserActivityReward.Fields.transaction, transaction));
                                } else if(type.isTribunalReward()) {
                                    rewardTransactionRef = UserTribunalReward.dao().getFirstBy(new NameValuePair<>(UserTribunalReward.Fields.transaction, transaction));
                                } else {
                                    assert type.isElectorateReward() : "Found an unsupported transactionType/" + type;
                                    rewardTransactionRef = UserElectorateReward.dao().getFirstBy(new NameValuePair<>(UserElectorateReward.Fields.transaction, transaction));
                                }

                                // bl: clear out the transaction
                                rewardTransactionRef.setTransaction(null);
                            }
                        }

                        // flush so the foreign key constraints aren't violated
                        WalletTransaction.dao().getGSession().flushSession();

                        // now just delete all of the transactions
                        for (WalletTransaction transaction : transactions) {
                            WalletTransaction.dao().delete(transaction);
                        }
                        return null;
                    }
                });
            }

            // bl: now that we're reversed all rewards, the last piece is to clear the NicheContentReward.reward values
            NicheContentReward.dao().clearNicheContentRewardValuesForPeriod(period);

            // bl: double-check that we deleted them all!
            transactionOids = WalletTransaction.dao().getTransactionOidsFromWallet(wallet);
            if(!transactionOids.isEmpty()) {
                throw UnexpectedError.getRuntimeException("Found remaining transactions from the reward period wallet! count/" + transactionOids.size());
            }
        }

        // bl: now that we are done, assert that the balance we calculated matches reality!
        NrveValue actualWalletBalance = WalletTransaction.dao().getTransactionSumForWallet(wallet);
        assert walletBalance.equals(actualWalletBalance) : "Found wallet balance mismatch! calculatedBalance/" + walletBalance + " transactionSum/" + actualWalletBalance;

        // bl: now set the balance to the sum of the transactions that we kept
        wallet.setBalance(walletBalance);

        // bl: clear out total rewards and disbursed since those were obviously wrong
        period.setTotalRewards(NrveValue.ZERO);
        period.setTotalRewardsDisbursed(NrveValue.ZERO);

        // bl: treat it as having already been scheduled, and refunds are already completed. not rolling those back now.
        // bl: additionally, all of the reward activity steps (for the *Reward tables) should be fine and shouldn't need to be touched, so
        // keeping those as completed here, too.
        period.setCompletedSteps(EnumSet.of(
                RewardPeriodStep.SCHEDULE_PROCESSING_JOB,
                RewardPeriodStep.PROCESS_PRORATED_REVENUE_REFUNDS,
                RewardPeriodStep.POPULATE_CONTENT_ACTIVITY,
                RewardPeriodStep.POPULATE_USER_ACTIVITY
        ));
        period.setCompletedDatetime(null);

        // bl: we also need to undo the cleanup step that creates the RewardPeriod and ProratedMonthRevenue for 2 months in the future
        {
            YearMonth yearMonthToDelete = period.getPeriod().plusMonths(2);
            RewardPeriod existingRewardPeriod = RewardPeriod.dao().getForYearMonth(yearMonthToDelete);
            for (ProratedRevenueType type : ProratedRevenueType.ACTIVE_TYPES) {
                ProratedMonthRevenue proratedMonthRevenue = ProratedMonthRevenue.dao().getForYearMonthAndType(yearMonthToDelete, type);
                // bl: this will cascade to delete the Wallet and NeoWallet
                ProratedMonthRevenue.dao().delete(proratedMonthRevenue);
            }
            // bl: this will cascade to delete the Wallet
            RewardPeriod.dao().delete(existingRewardPeriod);
        }

        OID rewardPeriodOid = period.getOid();

        // bl: now that we are done rolling everything back, let's reschedule the reward period at the end of partition group
        PartitionGroup.addEndOfPartitionGroupRunnable(() -> {
            TaskRunner.doRootGlobalTask(new GlobalTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    RewardPeriod period = RewardPeriod.dao().get(rewardPeriodOid);
                    ProcessRewardPeriodJob.schedule(period, true, (long)IPDateUtil.MINUTE_IN_MS);
                    return null;
                }
            });
        });
        return null;
    }
}
