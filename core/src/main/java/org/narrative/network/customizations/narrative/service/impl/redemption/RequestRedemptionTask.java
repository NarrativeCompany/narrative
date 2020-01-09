package org.narrative.network.customizations.narrative.service.impl.redemption;

import org.narrative.common.util.IPHTMLUtil;
import org.narrative.common.util.UnexpectedError;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.WalletTransactionStatus;
import org.narrative.network.core.narrative.wallet.WalletTransactionType;
import org.narrative.network.core.narrative.wallet.services.ProcessWalletTransactionTask;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.services.SendUserRedemptionRequestedEmailTask;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.service.api.ValidationContext;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceFields;
import org.narrative.network.customizations.narrative.service.api.model.input.RequestRedemptionInput;
import org.narrative.network.customizations.narrative.service.impl.user.UpdateProfileAccountConfirmationBaseTask;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-01
 * Time: 15:39
 *
 * @author jonmark
 */
public class RequestRedemptionTask extends UpdateProfileAccountConfirmationBaseTask<Object> {
    private final NrveValue redemptionAmount;
    private final NrveUsdPriceFields nrveUsdPrice;

    private BigDecimal usdValue;

    public RequestRedemptionTask(User user, RequestRedemptionInput input) {
        super(user, input);
        this.redemptionAmount = input.getRedemptionAmount();
        this.nrveUsdPrice = input.getNrveUsdPrice();
    }

    @Override
    protected void validate(ValidationContext context) {
        // jw: first of all, we need to ensure that the user can request a redemption
        if (!getUser().getRedemptionStatus().isSupportsRedemption()) {
            throw UnexpectedError.getRuntimeException("Attempting to request redemption when unable to. status/"+getUser().getRedemptionStatus());
        }

        if (nrveUsdPrice==null) {
            throw UnexpectedError.getRuntimeException("Should always be given a nrveUsdPrice!");
        }

        if (redemptionAmount==null) {
            context.addRequiredFieldError(RequestRedemptionInput.Fields.redemptionAmount);
            return;
        }

        if (redemptionAmount.compareTo(getUser().getWallet().getBalance()) > 0) {
            context.addFieldError(RequestRedemptionInput.Fields.redemptionAmount, "requestRedemptionApiTask.cannotRedeemMoreThan", getUser().getWallet().getBalance().getFormattedWithSuffix());
            return;
        }

        if (!nrveUsdPrice.isValid()) {
            context.addMethodError("requestRedemptionApiTask.nrveUsdPriceExpired");
            return;
        }

        usdValue = nrveUsdPrice.convertToBigDecimal(redemptionAmount);

        // jw: let's ensure tha this redemption will not take the user over their annual limit.
        BigDecimal totalUsdRedeemed = getUser().getTotalRedemptionAmountForCurrentYear().add(usdValue);
        if (totalUsdRedeemed.compareTo(User.REDEMPTION_MAXIMUM_PER_YEAR_IN_USD) > 0) {
            BigDecimal usdValueLeft = User.REDEMPTION_MAXIMUM_PER_YEAR_IN_USD.subtract(getUser().getTotalRedemptionAmountForCurrentYear());

            // jw: let's tell the user exacly how much nrve they can redeem to hit the limit
            NrveValue nrveValueLeft = NrveValue.getNrveValueFromUsd(usdValueLeft, nrveUsdPrice.getNrveUsdPrice(), NrveValue.SCALE, RoundingMode.DOWN);

            context.addFieldError(
                    RequestRedemptionInput.Fields.redemptionAmount,
                    "requestRedemptionApiTask.redemptionExceedsAnnualLimit",
                    nrveValueLeft.getFormattedWithSuffix(),
                    IPHTMLUtil.getLink("mailto:support@narrative.org", wordlet("requestRedemptionApiTask.contactUs"))
            );
        }
    }

    @Override
    protected Object doMonitoredTask() {
        WalletTransaction transaction = getAreaContext().doAreaTask(new ProcessWalletTransactionTask(
                getUser().getWallet(),
                null,
                WalletTransactionType.USER_REDEMPTION,
                WalletTransactionStatus.PENDING,
                redemptionAmount
        ));

        transaction.setUsdAmount(usdValue);

        getAreaContext().doAreaTask(new SendUserRedemptionRequestedEmailTask(getUser(), transaction));

        return null;
    }
}
