package org.narrative.network.customizations.narrative.invoices;

import org.narrative.network.customizations.narrative.NrveValue;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FiatPaymentTest {

    @Tested
    FiatPayment fiatPayment;

    @Test
    void calculateUsdAmount_void_correctlySetsState() {

        // Mock  calculateUsdAmount(BigDecimal nrveUsdPrice, NrveValue nrveAmount) because we're not testing it here
        new Expectations(FiatPayment.class) {{
            fiatPayment.setupUiAmounts();
        }};

        fiatPayment.setNrveAmount(new NrveValue(NrveValue.NEURONS_PER_NRVE.longValue()));
        // Call method under test
        fiatPayment.calculateUsdAmount(new BigDecimal(100L));

        // Verify state
        assertEquals(0, BigDecimal.valueOf(100).compareTo(fiatPayment.getUsdAmount()));
        assertEquals(0, BigDecimal.valueOf(15).compareTo(fiatPayment.getFeeUsdAmount()));
    }

    @Test
    void calculateUsdAmount_8000NrveAt1Cent_Costs80Dollars(@Mocked NrveValue nrveAmount) {

        new Expectations() {{
            // Mock the call to nrveAmount.getValue
            nrveAmount.getValue();
            result = BigDecimal.valueOf(8000);
        }};

        // Call method under test
        BigDecimal retVal = fiatPayment.calculateUsdAmount(new BigDecimal("0.01"), nrveAmount);
        assertEquals(0, retVal.compareTo(new BigDecimal("80.00")));

    }

    @Test
    void calculateUsdAmount_7000NrveAt1Cent_Costs75Dollars(@Mocked NrveValue nrveAmount) {

        new Expectations() {{
            // Mock the call to nrveAmount.getValue
            nrveAmount.getValue();
            result = BigDecimal.valueOf(7000);
        }};

        // Call method under test
        BigDecimal retVal = fiatPayment.calculateUsdAmount(new BigDecimal("0.01"), nrveAmount);
        assertEquals(0, retVal.compareTo(new BigDecimal("75.00")));
    }
}