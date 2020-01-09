package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.ReputationHistoryEntity;
import org.narrative.reputation.model.entity.RollupPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Transactional
public interface ReputationHistoryRepository extends JpaRepository<ReputationHistoryEntity, ReputationHistoryEntity> {
    @Query("select max(h.snapshotDate) from ReputationHistoryEntity h where h.period = ?1")
    LocalDate findMaxSnapshotDateForRollupPeriod(RollupPeriod rollupPeriod);

    @Query("select max(h.userOid) from ReputationHistoryEntity h where h.period = ?1 and h.snapshotDate = ?2")
    Long findMaxUserOidForRollupPeriodAndDate(RollupPeriod rollupPeriod, LocalDate localDate);

    @Query("select max(h.snapshotDate) from ReputationHistoryEntity h where h.period = ?1 and h.snapshotDate <= ?2")
    LocalDate findSourceDateOfTypeLessThanOrEqualCompareDate(RollupPeriod rollupPeriod, LocalDate compareSourceDate);

    long countByPeriodAndSnapshotDate(RollupPeriod rollupPeriod, LocalDate snapshotDate);

    @Modifying
    @Query(nativeQuery = true, value =
    "insert into ReputationHistory( " +
    "    userOid, " +
    "    snapshotDate, " +
    "    period, " +
    "    qualityAnalysis, " +
    "    kycVerified, " +
    "    conductStatus, " +
    "    totalScore, " +
    "    commentLikePoints, " +
    "    commentDislikePoints, " +
    "    contentLikePoints, " +
    "    contentDislikePoints, " +
    "    contentRatingsReceivedCount, " +
    "    userQualityFollowerRatio, " +
    "    userQualityFollowerPctRank, " +
    "    ratingMajorityVoteCount, " +
    "    ratingTotalVoteCount, " +
    "    corrMajorityVoteCount, " +
    "    corrTotalVoteCount " +
    ") " +
    "select " +
    "    userOid, " +
    "    :rollupDate, " +
    "    :rollupTypeOrdinal, " +
    "    qualityAnalysis, " +
    "    kycVerified, " +
    "    conductStatus, " +
    "    totalScore, " +
    "    commentLikePoints, " +
    "    commentDislikePoints, " +
    "    contentLikePoints, " +
    "    contentDislikePoints, " +
    "    contentRatingsReceivedCount, " +
    "    userQualityFollowerRatio, " +
    "    userQualityFollowerPctRank, " +
    "    ratingMajorityVoteCount, " +
    "    ratingTotalVoteCount, " +
    "    corrMajorityVoteCount, " +
    "    corrTotalVoteCount " +
    "from " +
    "     ConsolidatedCurReputation cr " +
    "where " +
    "     cr.userOid > :prevBatchMaxUserOid " +
    "order by " +
    "     userOid " +
    "limit :batchSize")
    int insertCurrentReputationBatchIntoHistory(
            @Param("batchSize") int batchSize,
            @Param("rollupDate") LocalDate rollupDate,
            @Param("rollupTypeOrdinal") int rollupTypeOrdinal,
            @Param("prevBatchMaxUserOid") long prevBatchMaxUserOid);

    @Modifying
    @Query(nativeQuery = true, value =
            "insert into ReputationHistory( " +
                    "    userOid, " +
                    "    snapshotDate, " +
                    "    period, " +
                    "    qualityAnalysis, " +
                    "    kycVerified, " +
                    "    conductStatus, " +
                    "    totalScore, " +
                    "    commentLikePoints, " +
                    "    commentDislikePoints, " +
                    "    contentLikePoints, " +
                    "    contentDislikePoints, " +
                    "    contentRatingsReceivedCount, " +
                    "    userQualityFollowerRatio, " +
                    "    userQualityFollowerPctRank, " +
                    "    ratingMajorityVoteCount, " +
                    "    ratingTotalVoteCount, " +
                    "    corrMajorityVoteCount, " +
                    "    corrTotalVoteCount " +
                    ") " +
                    "select " +
                    "    userOid, " +
                    "    :rollupDate, " +
                    "    :rollupTypeOrdinal, " +
                    "    qualityAnalysis, " +
                    "    kycVerified, " +
                    "    conductStatus, " +
                    "    totalScore, " +
                    "    commentLikePoints, " +
                    "    commentDislikePoints, " +
                    "    contentLikePoints, " +
                    "    contentDislikePoints, " +
                    "    contentRatingsReceivedCount, " +
                    "    userQualityFollowerRatio, " +
                    "    userQualityFollowerPctRank, " +
                    "    ratingMajorityVoteCount, " +
                    "    ratingTotalVoteCount, " +
                    "    corrMajorityVoteCount, " +
                    "    corrTotalVoteCount " +
                    "from " +
                    "     ReputationHistory rh " +
                    "where " +
                    "     rh.snapshotDate = :sourceDate and " +
                    "     rh.period = :sourceRollupTypeOrdinal and " +
                    "     rh.userOid > :prevBatchMaxUserOid " +
                    "order by " +
                    "     userOid " +
                    "limit :batchSize")
    int insertHistoryByDateAndTypeIntoIntoHistory(
            @Param("batchSize") int batchSize,
            @Param("sourceDate") LocalDate sourceDate,
            @Param("sourceRollupTypeOrdinal") int sourceRollupTypeOrdinal,
            @Param("rollupDate") LocalDate rollupDate,
            @Param("rollupTypeOrdinal") int rollupTypeOrdinal,
            @Param("prevBatchMaxUserOid") long prevBatchMaxUserOid);
}
