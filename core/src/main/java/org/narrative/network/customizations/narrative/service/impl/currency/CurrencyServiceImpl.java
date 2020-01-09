package org.narrative.network.customizations.narrative.service.impl.currency;

import org.narrative.network.core.settings.global.services.GlobalSettingsUtil;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.CurrencyService;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceFields;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Date: 2019-07-02
 * Time: 10:27
 *
 * @author jonmark
 */
@Service
public class CurrencyServiceImpl implements CurrencyService {
    @Override
    public NrveUsdPriceDTO getNrveUsdPrice(Duration validFor) {
        assert validFor != null : "Should always be provided a duration that the conversion rate should be valid for.";

        BigDecimal nrveUsdPrice = GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice();

        return NrveUsdPriceDTO.create(nrveUsdPrice, validFor);
    }

    @Override
    public ScalarResultDTO<UsdValue> getUsdValue(NrveValue nrveAmount, BigDecimal nrveUsdPrice) {
        // jw: if we were not given a nrveAmount there is nothing to convert...
        if (nrveAmount == null) {
            return ScalarResultDTO.<UsdValue>builder().build();
        }

        // jw: if no conversion rate was provided then let's default to the current market value.
        if (nrveUsdPrice ==null) {
            nrveUsdPrice = GlobalSettingsUtil.getGlobalSettings().getNrveUsdPrice();
        }

        UsdValue usdValue = NrveUsdPriceFields.convert(nrveUsdPrice, nrveAmount);

        return ScalarResultDTO.<UsdValue>builder()
                .value(usdValue)
                .build();
    }
}
