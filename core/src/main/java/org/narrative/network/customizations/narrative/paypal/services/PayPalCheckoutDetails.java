package org.narrative.network.customizations.narrative.paypal.services;

import org.narrative.common.util.GBigDecimal;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.UsdValue;
import com.paypal.base.rest.APIContext;

import java.math.BigDecimal;

/**
 * Date: 2019-02-04
 * Time: 19:36
 *
 * @author jonmark
 */
public class PayPalCheckoutDetails {
    private final GBigDecimal usdAmount;
    private final NarrativeProperties.PayPal.ApiConfig apiConfig;

    public PayPalCheckoutDetails(BigDecimal usdAmount, NarrativeProperties.PayPal.ApiConfig apiConfig) {
        assert usdAmount!=null && usdAmount.compareTo(BigDecimal.ZERO) > 0 : "Should always have a positive usdAmount!";
        assert apiConfig!=null : "Should always have a paypal config";

        this.usdAmount = new GBigDecimal(usdAmount);
        this.apiConfig = apiConfig;
    }

    public UsdValue getUsdAmount() {
        return new UsdValue(usdAmount.getValue());
    }

    public String getAmountForPayPal() {
        return usdAmount.getFormattedWithTwoDecimals();
    }

    public String getClientMode() {
        return apiConfig.getMode().getCheckoutMode();
    }

    public String getClientId() {
        return apiConfig.getClientId();
    }

    public APIContext getApiContext() {
        return apiConfig.getApiContext();
    }
}
