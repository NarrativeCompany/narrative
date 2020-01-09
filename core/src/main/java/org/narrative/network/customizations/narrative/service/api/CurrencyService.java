package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceDTO;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Date: 2019-07-02
 * Time: 10:26
 *
 * @author jonmark
 */
public interface CurrencyService {
    NrveUsdPriceDTO getNrveUsdPrice(Duration validFor);

    ScalarResultDTO<UsdValue> getUsdValue(NrveValue nrveAmount, BigDecimal nrveUsdPrice);
}
