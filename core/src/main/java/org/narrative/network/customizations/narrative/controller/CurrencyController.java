package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.UsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.service.api.CurrencyService;
import org.narrative.network.customizations.narrative.service.api.model.NrveUsdPriceDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.BigDecimalDeserializer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Date: 2019-07-02
 * Time: 10:18
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/currencies")
@Validated
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping(path = "/nrve-to-usd")
    public ScalarResultDTO<UsdValue> getUsdValue(
            // jw: note: I'm forced to use a BigDecimal here instead of NrveValue because the NrveValueDeserializer is not
            //     used because spring is deserializing the parameter directly using the String constructor (meaning that
            //     the resulting NrveValue is in neurons).
            @RequestParam BigDecimal nrveAmount,
            @RequestParam BigDecimal nrveUsdPrice
    ) {
        return currencyService.getUsdValue(
                nrveAmount != null ? new NrveValue(nrveAmount) : null,
                nrveUsdPrice
        );
    }

    @GetMapping(path = "/nrve-to-usd/price")
    public NrveUsdPriceDTO getNrveUsdPrice() {
        // jw: for now let's only keep the nrveUsdPrice valid for 4 hours.
        return currencyService.getNrveUsdPrice(Duration.ofHours(4));
    }

    @Component
    public static class StringToBigDecimalConverter implements Converter<String, BigDecimal> {
        @Override
        public BigDecimal convert(String source) {
            return BigDecimalDeserializer.convertStringToBigDecimal(source);
        }
    }
}
