package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.FollowerQualityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FollowerQualityRepository extends JpaRepository<FollowerQualityEntity, Long> {
    @Query("select coalesce(max(fq.userOid), 0) from FollowerQualityEntity fq where fq.batchJobId=?1")
    long findMaxUserOidForBatchJobId(long batchJobId);

    @Modifying
    @Query(nativeQuery = true, value =
            "insert into FollowerQuality (\n" +
            "  userOid,\n" +
            "  lastUpdated,\n" +
            "  userQualityFollowerRatio,\n" +
            "  userQualityFollowerPctRank,\n" +
            "  batchJobId\n" +
            ")\n" +
            "    select\n" +
            "      crUser.userOID,\n" +
            "      @lastUpdated \\:= CURRENT_TIMESTAMP,\n" +
            "      @userQualityFollowerRatio \\:= coalesce(count(1) / :totalQualityMembers, 0),\n" +
            "      0,\n" +
            "      @batchJobId \\:= :currentBatchJobId\n" +
            "    from\n" +
            "       ( select\n" +
            "          userOid\n" +
            "         from\n" +
            "            CurrentReputation cr\n" +
            "         where\n" +
            "            cr.userOid > :maxLastUserOid\n" +
            "         order by\n" +
            "            userOid limit :maxBatchSize\n" +
            "       ) crUser\n" +
            "       join  FollowedUser fu on fu.followedUserOid = crUser.userOid\n" +
            "       join CurrentReputation crFollower on crFollower.userOid = fu.followingUserOid\n" +
            "    where\n" +
            "       crFollower.totalScore >= :meanRepScore\n" +
            "    group by\n" +
            "       crUser.userOid\n" +
            "on duplicate key update\n" +
            "  lastUpdated = @lastUpdated,\n" +
            "  userQualityFollowerRatio = @userQualityFollowerRatio,\n" +
            "  batchJobId = @batchJobId")
    int updateUserQualityFollowerRatio(@Param("maxLastUserOid") long maxLastUserOid,
                                        @Param("currentBatchJobId") long currentBatchJobId,
                                        @Param("maxBatchSize") int maxBatchSize,
                                        @Param("meanRepScore") double meanRepScore,
                                        @Param("totalQualityMembers") long totalQualityMembers);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
            "update\n" +
            "    FollowerQuality fq,\n" +
            "    (\n" +
            "        select\n" +
            "            fqi.userOid\n" +
            "        from\n" +
            "            (\n" +
            "                select\n" +
            "                    userOid\n" +
            "                from\n" +
            "                    CurrentReputation cr\n" +
            "                where\n" +
            "                    cr.userOid > :maxLastUserOid\n" +
            "                order by\n" +
            "                    useroid\n" +
            "                limit :maxBatchSize\n" +
            "            ) crLimit\n" +
            "            join FollowerQuality fqi on fqi.userOid = crLimit.userOid\n" +
            "        where\n" +
            "            fqi.batchJobId < :currentBatchJobId\n" +
            "    ) notUpdated\n" +
            "set\n" +
            "    fq.batchJobId = :currentBatchJobId,\n" +
            "    fq.userQualityFollowerRatio = 0.0,\n" +
            "    fq.userQualityFollowerPctRank = 0.0\n" +
            "where\n" +
            "    fq.userOid = notUpdated.userOid")
    int updateNoQualityFollowerUsers(@Param("maxLastUserOid") long maxLastUserOid,
                                     @Param("currentBatchJobId") long currentBatchJobId,
                                     @Param("maxBatchSize") int maxBatchSize);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "update\n" +
                    "    FollowerQuality fq,\n" +
                    "    (\n" +
                    "        select\n" +
                    "            res.userOid,\n" +
                    "            res.userQualityFollowerRatio,\n" +
                    "            round(((@total - rank + 1) / @total) * 100, 2) AS userQualityFollowerPctRank\n" +
                    "        from\n" +
                    "            (\n" +
                    "            select\n" +
                    "                *,\n" +
                    "                @prev \\:= @curr,\n" +
                    "                @curr \\:= src.userQualityFollowerRatio,\n" +
                    "                @rank \\:= IF(@prev = @curr, @rank, @rank + 1) as rank\n" +
                    "            from\n" +
                    "                (select\n" +
                    "                    userOid,\n" +
                    "                    userQualityFollowerRatio\n" +
                    "                from\n" +
                    "                    FollowerQuality) as src,\n" +
                    "                (select\n" +
                    "                    @curr \\:= null,\n" +
                    "                    @prev \\:= null,\n" +
                    "                    @rank \\:= 0,\n" +
                    "                    @total \\:= count(distinct userQualityFollowerRatio)\n" +
                    "                from FollowerQuality) AS var\n" +
                    "        order by\n" +
                    "            userQualityFollowerRatio DESC) AS res\n" +
                    "    ) ranked_rows\n" +
                    "set\n" +
                    "    fq.userQualityFollowerPctRank = ranked_rows.userQualityFollowerPctRank\n" +
                    "where\n" +
                    "    fq.userOid=ranked_rows.userOid")
    int updateUserQualityPercentileRank();
}
