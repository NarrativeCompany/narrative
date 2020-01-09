package org.narrative.network.core.narrative.rewards.services.steps;

import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RewardPeriodStep;
import org.narrative.network.core.narrative.rewards.RewardSlice;
import org.narrative.network.core.narrative.rewards.RewardTransactionRef;
import org.narrative.network.core.narrative.rewards.dao.RewardTransactionRefDAO;
import org.narrative.network.core.narrative.rewards.services.slices.RewardSliceProcessorBase;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.narrative.network.shared.tasktypes.TaskRunner;
import org.narrative.network.shared.util.NetworkLogger;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;

/**
 * Date: 2019-05-30
 * Time: 07:05
 *
 * @author brian
 */
public class IssueRewardsStepProcessor extends RewardPeriodStepProcessorBase {
    private static final NetworkLogger logger = new NetworkLogger(IssueRewardsStepProcessor.class);

    public IssueRewardsStepProcessor(RewardPeriod period) {
        super(period, RewardPeriodStep.ISSUE_REWARDS);
    }

    @Override
    public NetworkLogger getLogger() {
        return logger;
    }

    @Override
    protected Object doMonitoredTask() {

        Wallet rewardWallet = period.getWallet();

        // bl: check that the reward wallet balance matches the total of transactions TO the wallet minus transactions FROM the wallet.
        // this way, if there's ever an issue again where some revenue is lost, this sanity check should catch it.
        {
            NrveValue rewardWalletSum = WalletTransaction.dao().getTransactionSumForWallet(rewardWallet);

            if(!rewardWalletSum.equals(rewardWallet.getBalance())) {
                throw UnexpectedError.getRuntimeException("Found an incorrect RewardPeriod balance! transactionSum/" + rewardWalletSum + " walletBalance/" + rewardWallet.getBalance());
            }
        }

        // bl: if the reward wallet balance is negative, then there is nothing we can (or should) do!
        if(rewardWallet.getBalance().compareTo(NrveValue.ZERO) < 0) {
            if(logger.isWarnEnabled()) logger.warn("Skipping rewards issuing since the balance is negative! balance/" + rewardWallet.getBalance() + " period/" + period.getPeriod());
            return null;
        }

        // bl: if we haven't yet set the period's totalRewards, set it now!
        // this allows us to run this step multiple times safely without destroying data along the way.
        if(period.getTotalRewards().equals(NrveValue.ZERO) && !rewardWallet.getBalance().equals(NrveValue.ZERO)) {
            timeExecution("Set RewardPeriod.totalBalance to " + rewardWallet.getBalance(), () -> TaskRunner.doRootAreaTask(getAreaContext().getArea().getOid(), new AreaTaskImpl<Object>() {
                @Override
                protected Object doMonitoredTask() {
                    // bl: load the RewardPeriod in this new transaction so we can set the totalRewards
                    RewardPeriod period = RewardPeriod.dao().get(IssueRewardsStepProcessor.this.period.getOid());
                    // bl: the total rewards are just the reward wallet's balance!
                    period.setTotalRewards(rewardWallet.getBalance());
                    return null;
                }
            }));

            // bl: now that we've updated the object externally, refresh in the current session
            RewardPeriod.dao().refresh(period);
        } else {
            if(logger.isInfoEnabled()) logger.info("Processing " + period.getTotalRewards() + " NRVE for " + period.getPeriod());
        }

        // bl: the totalNrve amount is whatever is set on the RewardPeriod. once this value is set, it should
        // NEVER change, so we should always be able to trust it.
        NrveValue totalNrve = period.getTotalRewards();

        // confirm that all WalletTransaction records into the wallet are COMPLETED. should be none PENDING_FIAT_ADJUSTMENT.
        {
            List<WalletTransaction> incompleteTransactions = WalletTransaction.dao().getForToWalletAndStatus(rewardWallet, WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT);
            if(!incompleteTransactions.isEmpty()) {
                throw UnexpectedError.getRuntimeException("Found " + incompleteTransactions.size() + " transactions still pending fiat adjustment! Can't process rewards in this state! period/" + period.getPeriod());
            }
        }

        NrveValue originalTotalDisbursedAmount = period.getTotalRewardsDisbursed();
        NrveValue originalTotalTransactionTotal = WalletTransaction.dao().getTransactionSumFromWallet(rewardWallet, RewardSlice.ALL_REWARD_TRANSACTION_TYPES);

        if(!originalTotalDisbursedAmount.equals(originalTotalTransactionTotal)) {
            throw UnexpectedError.getRuntimeException("Transaction mismatch with RewardPeriod.totalRewardsDisbursed! Something must have gone awry previously. originalTotalDisbursedAmount/" + originalTotalDisbursedAmount + " originalTotalTransactionTotal/" + originalTotalTransactionTotal);
        }

        // bl: go through each RewardSlice and issue the rewards. the tasks themselves will run in the context
        // of the current session, but they can each have isolated transactions to chunk through processing as necessary
        for (RewardSlice slice : RewardSlice.values()) {
            // bl: skip any reward slices that aren't currently paying out
            if(slice.getCurrentPercent().compareTo(BigDecimal.ZERO) <= 0) {
                if(logger.isInfoEnabled()) logger.info("Skipping " + slice + " as not currently paying out.");
                continue;
            }

            timeExecution("Processing " + slice + " for " + period.getPeriod(), () -> {
                NrveValue originalDisbursedAmount = period.getTotalRewardsDisbursed();
                NrveValue originalTransactionTotal = WalletTransaction.dao().getTransactionSumFromWallet(rewardWallet, EnumSet.of(slice.getWalletTransactionType()));

                RewardSliceProcessorBase processor = slice.getProcessor(period, totalNrve);
                getAreaContext().doAreaTask(processor);

                // bl: we will have distributed rewards, so let's refresh to get the latest distribution balance
                RewardPeriod.dao().refresh(period);
                // bl: could technically also refresh the rewardWallet but we aren't using it for anything other
                // then the OID for querying at this point, so going to save the extra database round trip.

                NrveValue nrveSlice = processor.getNrveSlice();
                NrveValue transactionTotal = WalletTransaction.dao().getTransactionSumFromWallet(rewardWallet, EnumSet.of(slice.getWalletTransactionType()));

                validateTransactionAmounts(slice, transactionTotal, nrveSlice, originalTransactionTotal, originalDisbursedAmount);

                RewardTransactionRefDAO<? extends RewardTransactionRef> dao = slice.getRewardTransactionRefDao();

                // validate that we didn't issue any invalid (e.g. duplicate) transactions. every reward transaction
                // should be associated with a corresponding *Reward object.
                long numInvalidRewardTransactions = dao.getCountInvalidRewardTransactions(period);
                if(numInvalidRewardTransactions>0) {
                    throw UnexpectedError.getRuntimeException("Found " + numInvalidRewardTransactions + " invalid " + slice.getWalletTransactionType() + " transaction records for period/" + period);
                }

                // bl: once it's done, make sure that there aren't any invalid *Reward records. everything should have
                // committed in a separate transaction, so we should be able to do this query to confirm.
                long numInvalidRewards = dao.getCountIncompleteRewardTransactionRefs(period);
                if(numInvalidRewards>0) {
                    throw UnexpectedError.getRuntimeException("Found " + numInvalidRewards + " invalid " + dao.getDAOObjectClass().getSimpleName() + " records for slice/" + slice + " period/" + period);
                }
            });
        }

        // bl: now that we are done, refresh the reward wallet to make sure we have accurate data
        Wallet.dao().refresh(rewardWallet);

        NrveValue newTotalTransactionTotal = WalletTransaction.dao().getTransactionSumFromWallet(rewardWallet, RewardSlice.ALL_REWARD_TRANSACTION_TYPES);
        validateTransactionAmounts(null, newTotalTransactionTotal, totalNrve, originalTotalTransactionTotal, originalTotalDisbursedAmount);
        // make sure the transaction total exactly equals the total rewards disbursed
        if(!newTotalTransactionTotal.equals(period.getTotalRewardsDisbursed())) {
            throw UnexpectedError.getRuntimeException("Transaction verification didn't add up. Didn't properly update the disbursedAmount? period/" + period.getPeriod() + " transactionTotal/" + newTotalTransactionTotal + " disbursedAmount/" + period.getTotalRewardsDisbursed());
        }

        // check that the difference between totalRewards and totalRewardsDisbursed equals
        // the remaining balance left in the RewardPeriod's wallet.
        if(!period.getTotalRewards().subtract(period.getTotalRewardsDisbursed()).equals(rewardWallet.getBalance())) {
            throw UnexpectedError.getRuntimeException("Final balances didn't add up. period/" + period.getPeriod() + " totalRewards/" + period.getTotalRewards() + " totalRewardsDisbursed/" + period.getTotalRewardsDisbursed() + " rewardWalletBalance/" + rewardWallet.getBalance());
        }

        // check that the current month's RewardPeriod wallet is within the margin of error we expect (at most
        // 1 Neuron lost per transaction).
        long transactionCount = WalletTransaction.dao().getTransactionCountFromWallet(rewardWallet, RewardSlice.ALL_REWARD_TRANSACTION_TYPES);
        if(rewardWallet.getBalance().toNeurons() > transactionCount) {
            // bl: if the balance falls outside of the expected threshold, it's possible it's valid, so continue
            // processing. this can happen on lesser used environments (e.g. local) where some rewards aren't paid
            // out at all due to insufficient activity.
            // bl: let's assume this will never happen on production and bail out with an error.
            if(NetworkRegistry.getInstance().isProductionServer()) {
                throw UnexpectedError.getRuntimeException("Final reward wallet balance exceeded the expected threshold. period/" + period.getPeriod() + " rewardWalletBalance/" + rewardWallet.getBalance() + " transactionCount/" + transactionCount);
            }
            // bl: for local/dev/staging servers, let's just send an email to alert of a potential issue
            if(!NetworkRegistry.getInstance().isInstalling()) {
                NetworkRegistry.getInstance().sendDevOpsStatusEmail("Possible Network Rewards Issue", "Final reward wallet balance exceeded the expected threshold. period/" + period.getPeriod() + " rewardWalletBalance/" + rewardWallet.getBalance() + " transactionCount/" + transactionCount);
            }
        }
        return null;
    }

    private void validateTransactionAmounts(RewardSlice slice, NrveValue transactionTotal, NrveValue maxNrve, NrveValue originalTransactionTotal, NrveValue originalDisbursedAmount) {
        if(transactionTotal.compareTo(maxNrve) > 0) {
            throw UnexpectedError.getRuntimeException("Issued more than the total amount of NRVE allocated to this slice! slice/" + slice + " period/" + period.getPeriod() + " maxNrve/" + maxNrve + " transactionTotal/" + transactionTotal);
        }
        // bl: check to make sure that the total of new transactions matches the total added to rewards disbursed. these should
        // ALWAYS be equal or else the processing did something wrong.
        if(!transactionTotal.subtract(originalTransactionTotal).equals(period.getTotalRewardsDisbursed().subtract(originalDisbursedAmount))) {
            throw UnexpectedError.getRuntimeException("Transaction verification didn't add up. Didn't properly update the disbursedAmount? slice/" + slice + " period/" + period.getPeriod() + " originalDisbursedAmount/" + originalDisbursedAmount + " newDisbursedAmount/" + period.getTotalRewardsDisbursed() + " transactionTotal/" + transactionTotal + " originalTransactionTotal/" + originalTransactionTotal);
        }
    }
}
