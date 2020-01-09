package org.narrative.network.customizations.narrative.paypal;

import org.narrative.common.util.enums.*;

/**
 * Date: 2019-01-30
 * Time: 12:03
 *
 * @author jonmark
 */
public enum PayPalConfigurationMode implements IntegerEnum {
    SANDBOX(0, "sandbox", "sandbox")
    ,PRODUCTION(0, "production", "live")
    ;

    private final int id;
    // jw: this is used for the checkout javascript
    private final String checkoutMode;
    // jw: this is used for the mode on
    private final String apiContextMode;

    PayPalConfigurationMode(int id, String checkoutMode, String apiContextMode) {
        this.id = id;
        this.checkoutMode = checkoutMode;
        this.apiContextMode = apiContextMode;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getCheckoutMode() {
        return checkoutMode;
    }

    public String getApiContextMode() {
        return apiContextMode;
    }}