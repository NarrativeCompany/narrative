package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Date: 2019-03-08
 * Time: 08:51
 *
 * @author brian
 */
@JsonValueObject
@JsonTypeName("ContentStreamScrollParams")
@Data
@NoArgsConstructor
public class ContentStreamScrollParamsDTO {
    // bl: intentionally making these non-final so that they have accessors Spring needs in order to deserialize the values
    private Instant lastItemDatetime;
    private Instant trendingBuildTime;
    private Long lastItemTrendingScore;
    private Integer lastItemQualityScore;
    private Integer lastItemSecondaryQualityValue;
    private OID lastItemOid;

    private List<OID> nextItemOids;

    @Builder(toBuilder = true)
    public ContentStreamScrollParamsDTO(Instant lastItemDatetime, Instant trendingBuildTime, Long lastItemTrendingScore, Integer lastItemQualityScore, Integer lastItemSecondaryQualityValue, OID lastItemOid, List<OID> nextItemOids) {
        this.lastItemDatetime = lastItemDatetime;
        this.trendingBuildTime = trendingBuildTime;
        this.lastItemTrendingScore = lastItemTrendingScore;
        this.lastItemQualityScore = lastItemQualityScore;
        this.lastItemSecondaryQualityValue = lastItemSecondaryQualityValue;
        this.lastItemOid = lastItemOid;
        this.nextItemOids = nextItemOids;
    }
}
