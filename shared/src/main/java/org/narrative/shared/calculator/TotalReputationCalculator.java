package org.narrative.shared.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.narrative.shared.reputation.config.ReputationConstants.*;

public class TotalReputationCalculator {
    /**
     * Calculate the total reputation score from components
     *
     * @param qualityAnalysisScore    The current quality analysis score
     * @param isConductStatusNegative true if conduct is negative, false otherwise
     * @param isKycVerified           true if KYC verified, false otherwise
     */
    public static int calculateTotalScore(double qualityAnalysisScore, boolean isConductStatusNegative, boolean isKycVerified) {
        BigDecimal qualityAnalysisComponent = BigDecimal.valueOf(qualityAnalysisScore).multiply(QUALITY_ANALYSIS_COMPONENT_MULTIPLIER);
        BigDecimal conductStatusComponent = calculateConductNegative(isConductStatusNegative).multiply(CONDUCT_STATUS_COMPONENT_MULTIPLIER);
        BigDecimal kycVerifiedComponent = calculateKycVerified(isKycVerified).multiply(KYC_COMPONENT_MULTIPLIER);

        return qualityAnalysisComponent
                .add(kycVerifiedComponent)
                .add(conductStatusComponent)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private static BigDecimal calculateKycVerified(boolean isKycVerified) {
        return BigDecimal.valueOf(calculateKycVerifiedScore(isKycVerified));
    }

    public static int calculateKycVerifiedScore(boolean isKycVerified) {
        return isKycVerified ? KYC_VERIFIED_SCORE : 0;
    }

    private static BigDecimal calculateConductNegative(boolean isConductStatusNegative) {
        return BigDecimal.valueOf(isConductStatusNegative ? 0 : CONDUCT_NOT_NEGATIVE_SCORE);
    }
}
