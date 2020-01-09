package org.narrative.shared.reputation.config;

import java.math.BigDecimal;

public class ReputationConstants {
    public static final int MIN_REPUTATION_SCORE = 0;
    public static final int MAX_REPUTATION_SCORE = 100;

    public static final int KYC_VERIFIED_SCORE = 100;
    public static final int CONDUCT_NOT_NEGATIVE_SCORE = 100;

    // Total score constants
    public static final BigDecimal QUALITY_ANALYSIS_COMPONENT_MULTIPLIER = new BigDecimal("0.6");
    public static final BigDecimal CONDUCT_STATUS_COMPONENT_MULTIPLIER = new BigDecimal("0.1");
    public static final BigDecimal KYC_COMPONENT_MULTIPLIER = new BigDecimal("0.3");

    // Quality Analysis constants
    public static final BigDecimal FOLLOWERS_MULTIPLIER = new BigDecimal(0.1);
    public static final BigDecimal POSTED_CONTENT_AND_COMMENTS_MULTIPLIER = new BigDecimal("0.4");
    public static final BigDecimal CORRELATION_OF_VOTES_MULTIPLIER = new BigDecimal("0.25");
    public static final BigDecimal CORRELATION_OF_RATINGS_MULTIPLIER = new BigDecimal("0.25");
}
