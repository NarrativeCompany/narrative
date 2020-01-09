package org.narrative.network.core.narrative.rewards;

import org.narrative.network.core.narrative.wallet.WalletType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date: 2019-05-16
 * Time: 15:30
 *
 * @author jonmark
 */
class ProratedRevenueTypeTest {

    // Test names should follow the standard: MethodName_StateUnderTest_ExpectedBehavior
    @Test
    void Constructor_WalletTransaction_TransactionWalletsMakeSense() {
        for (ProratedRevenueType type : ProratedRevenueType.values()) {
            assertNotNull(type.getWalletType());
            assertNotNull(type.getRevenueTransactionType());
            assertTrue(type.getRevenueTransactionType().isValidTransaction(
                    type.getWalletType()
                    // jw: currently, we only support transfers to REWARD_PERIOD, so hard code that.
                    , WalletType.REWARD_PERIOD
            ));
        }
    }
}