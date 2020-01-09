package org.narrative.network.customizations.narrative.service.mapper.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-07-10
 * Time: 13:53
 *
 * @author brian
 */
class BigDecimalDeserializerTest {
    @Test
    void test_bigDecimalWithCommas_parses() {
        assertEquals(new BigDecimal("1234567.8901"), BigDecimalDeserializer.convertStringToBigDecimal("1,234,567.8901"));
    }
    @Test
    void test_bigDecimalWithoutCommas_parses() {
        String valueToTest = "1234567.8901";
        assertEquals(new BigDecimal(valueToTest), BigDecimalDeserializer.convertStringToBigDecimal(valueToTest));
    }
}
