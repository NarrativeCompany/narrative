package org.narrative.network.core.master.admin.actions;

import com.opensymphony.xwork2.Preparable;
import lombok.Data;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.cluster.actions.ClusterAction;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.ProratedRevenueType;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.services.ApplyFiatAdjustmentTask;
import org.narrative.network.core.narrative.rewards.services.ProcessRewardPeriodJob;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.shared.services.ConfirmationMessage;
import org.narrative.network.shared.struts.NetworkResponses;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/2/18
 * Time: 1:40 PM
 */
public class ApplyFiatAdjustmentAction extends ClusterAction implements Preparable {
    public static final String ACTION_NAME = "apply-fiat-adjustment";
    public static final String FULL_ACTION_PATH = "/" + ACTION_NAME;

    public static String ACQUIRED_NRVE_PARAM = "acquiredNrve";
    public static String MONTH_FOR_ADJUSTMENT_PARAM = "monthForAdjustment";

    // jw: these are submitted and used for execution processing
    private BigDecimal acquiredNrve;
    private YearMonth monthForAdjustment;

    // jw: these are derived values (used for input and to validate the above values)
    private RewardPeriod rewardPeriod;
    private int totalPaymentsPending;
    private NrveValue originalNrve;
    private UsdValue totalUsdForNrve;
    private UsdValue totalUsdForFees;
    private UsdValue totalUsd;

    private NrveValue totalProratedRevenue;

    private final Collection<ProratedRevenueDetail> proratedRevenueDetails = new ArrayList<>(ProratedRevenueType.ACTIVE_TYPES.size());

    @Override
    public void prepare() throws Exception {
        // jw: let's always calculate the proratedMonthRevenue and it's totalFiatRevenuePending
        // bl: to determine the RewardPeriod to use, we first need to look up the _oldest_ incomplete reward period.
        rewardPeriod = RewardPeriod.dao().getOldestIncompleteRewardPeriodBefore(RewardUtils.nowYearMonth());

        // bl: if we didn't find an incomplete reward period, then just look up the most recent reward period before this month
        if(!exists(rewardPeriod)) {
            rewardPeriod = RewardPeriod.dao().getLatestRewardPeriodBefore(RewardUtils.nowYearMonth());

            if(!exists(rewardPeriod)) {
                throw UnexpectedError.getRuntimeException("Failed to find a RewardPeriod!");
            }

            assert rewardPeriod.isCompleted() : "Only expecting to get completed RewardPeriods here! rewardPeriod/" + rewardPeriod.getPeriod();
        }

        Map<ProratedRevenueType,ProratedMonthRevenue> proratedMonthRevenues = ProratedMonthRevenue.dao().getForYearMonthByType(rewardPeriod.getPeriod());

        assert proratedMonthRevenues.keySet().containsAll(ProratedRevenueType.ACTIVE_TYPES) : "We should ALWAYS have a ProratedMonthRevenue for all active revenue types for the RewardPeriod month/"+rewardPeriod.getPeriod();

        originalNrve = NrveValue.ZERO;
        BigDecimal usdTotalForNrve = BigDecimal.ZERO;
        BigDecimal usdTotalForFees = BigDecimal.ZERO;
        BigDecimal usdTotal = BigDecimal.ZERO;

        totalProratedRevenue = NrveValue.ZERO;

        for (ProratedMonthRevenue proratedMonthRevenue : proratedMonthRevenues.values()) {
            List<FiatPayment> paymentsPending = FiatPayment.dao().getWithTransactionForToWalletAndStatus(proratedMonthRevenue.getWallet(), WalletTransactionStatus.PENDING_FIAT_ADJUSTMENT);

            NrveValue totalPaymentNrve = NrveValue.ZERO;

            for (FiatPayment payment : paymentsPending) {
                totalPaymentNrve = totalPaymentNrve.add(payment.getNrveAmount());
                // jw: notice that we are not including the fee, this is purely the money meant for NRVE
                usdTotalForNrve = usdTotalForNrve.add(payment.getUsdAmount());
                usdTotalForFees = usdTotalForFees.add(payment.getFeeUsdAmount());
                usdTotal = usdTotal.add(payment.getTotalUsdAmount());
            }

            proratedRevenueDetails.add(new ProratedRevenueDetail(proratedMonthRevenue, totalPaymentNrve));

            originalNrve = originalNrve.add(totalPaymentNrve);
            totalProratedRevenue = totalProratedRevenue.add(proratedMonthRevenue.getTotalNrve());
            totalPaymentsPending += paymentsPending.size();
        }

        totalUsdForNrve = new UsdValue(usdTotalForNrve);
        totalUsdForFees = new UsdValue(usdTotalForFees);
        totalUsd = new UsdValue(usdTotal);
    }

    @Override
    public void validate() {
        if (monthForAdjustment==null) {
            throw UnexpectedError.getRuntimeException("We should always be given a monthForAdjustment!");
        }

        if (!isEqual(rewardPeriod.getPeriod(), monthForAdjustment)) {
            throw new ApplicationError("The form was loaded on one month, and submitted on another!");
        }

        if (validateNotNullWithLabel(acquiredNrve, ACQUIRED_NRVE_PARAM, "Acquired NRVE")) {
            if (acquiredNrve.scale() > NrveValue.SCALE) {
                addFieldError(ACQUIRED_NRVE_PARAM, "The provided Acquired NRVE has more decimals than are supported by Neurons (8)!");
            }

            // jw: if we were given a negative adjustment it cannot exceed the value of NRVE pending.
            if (new NrveValue(acquiredNrve).compareTo(NrveValue.ZERO) < 0) {
                addFieldError(ACQUIRED_NRVE_PARAM, "The provided Acquired NRVE value cannot be negative!");
            } else if(totalPaymentsPending==0) {
                // bl: if there aren't any payments for the month, then the adjustment amount _must_ be zero.
                if (!NrveValue.ZERO.equals(acquiredNrve)) {
                    addFieldError(ACQUIRED_NRVE_PARAM, "The Acquired NRVE must be zero since there weren't any transactions for the month!");
                }
            }
        }
    }

    public String input() throws Exception {
        return INPUT;
    }

    @Override
    public String execute() throws Exception {
        if (!isSupportSubmissions()) {
            throw UnexpectedError.getRuntimeException("We should never submit this action when the period is not in a submittable state.");
        }

        // bl: distribute the acquiredNrve proportionally between Niche revenue and Publication revenue
        RewardUtils.distributeNrveProportionally(new NrveValue(acquiredNrve), proratedRevenueDetails, ProratedRevenueDetail::getOriginalNrve, ProratedRevenueDetail::setAcquiredNrve);

        for (ProratedRevenueDetail proratedRevenueDetail : proratedRevenueDetails) {
            // bl: the adjustment is just the difference between the acquired NRVE and the original NRVE
            NrveValue adjustment = proratedRevenueDetail.getAcquiredNrve().subtract(proratedRevenueDetail.getOriginalNrve());
            getNetworkContext().doAreaTask(Area.dao().getNarrativePlatformArea(), new ApplyFiatAdjustmentTask(
                    proratedRevenueDetail.getProratedMonthRevenue(),
                    adjustment
            ));
        }

        // jw: next, let's schedule the job
        ProcessRewardPeriodJob.schedule(getRewardPeriod());

        setConfirmationMessageForSubsequentRequest(new ConfirmationMessage("Fiat adjustment has been applied."));

        return NetworkResponses.redirectResponse();
    }

    public RewardPeriod getRewardPeriod() {
        return rewardPeriod;
    }

    public boolean isSupportSubmissions() {
        // bl: only want to allow submissions if the month we are processing is actually eligible for processing!
        // also, if any steps have completed already, then it can't be re-submitted!
        return getRewardPeriod().isEligibleForRewardProcessing() && getRewardPeriod().getCompletedSteps().isEmpty();
    }

    public void setAcquiredNrve(BigDecimal acquiredNrve) {
        this.acquiredNrve = acquiredNrve;
    }

    public void setMonthForAdjustment(YearMonth monthForAdjustment) {
        this.monthForAdjustment = monthForAdjustment;
    }

    public int getTotalPaymentsPending() {
        return totalPaymentsPending;
    }

    public NrveValue getOriginalNrve() {
        return originalNrve;
    }

    public UsdValue getTotalUsdForNrve() {
        return totalUsdForNrve;
    }

    public UsdValue getTotalUsdForFees() {
        return totalUsdForFees;
    }

    public UsdValue getTotalUsd() {
        return totalUsd;
    }

    public NrveValue getTotalProratedRevenue() {
        return totalProratedRevenue;
    }

    @Override
    public String getSubMenuResource() {
        return ACTION_NAME;
    }

    @Data
    private static class ProratedRevenueDetail {
        private final ProratedMonthRevenue proratedMonthRevenue;
        private final NrveValue originalNrve;
        private NrveValue acquiredNrve = NrveValue.ZERO;
    }
}
