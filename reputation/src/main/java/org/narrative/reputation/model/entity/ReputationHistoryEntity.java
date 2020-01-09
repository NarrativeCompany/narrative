package org.narrative.reputation.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@IdClass(ReputationHistoryId.class)
public class ReputationHistoryEntity implements Serializable {
    private static final long serialVersionUID = 8156601987003854599L;
    @Id
    private long userOid;
    @Id
    @Convert(converter = RollupPeriodConverter.class)
    private RollupPeriod period;
    @Id
    private LocalDate snapshotDate;
    private int qualityAnalysis;
    private double userQualityFollowerRatio;
    private double userQualityFollowerPctRank;
    private double contentLikePoints;
    private double contentDislikePoints;
    private double commentLikePoints;
    private double commentDislikePoints;
    private int contentRatingsReceivedCount;
    private int corrMajorityVoteCount;
    private int corrTotalVoteCount;
    private int ratingMajorityVoteCount;
    private int ratingTotalVoteCount;
    private boolean kycVerified;
    private boolean conductStatus;
    /**
     * Aggregate reputation score
     */
    private int totalScore;

    @Converter(autoApply = true)
    public static class RollupPeriodConverter implements AttributeConverter<RollupPeriod, Integer> {
        @Override
        public Integer convertToDatabaseColumn(RollupPeriod attribute) {
            return attribute.getOrdinalValue();
        }

        @Override
        public RollupPeriod convertToEntityAttribute(Integer dbData) {
            return RollupPeriod.getRollupPeriod(dbData);
        }
    }
}

