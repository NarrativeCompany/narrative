package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Date: 2019-06-03
 * Time: 15:56
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public interface RewardPeriodMapper {
    /**
     * Map from {@link RewardPeriod} entity to {@link RewardPeriodDTO}.
     *
     * @param rewardPeriod The incoming RewardPeriod to map
     * @return The mapped RewardPeriod
     */
    @Mapping(source = "formatted", target = RewardPeriodDTO.Fields.name)
    @Mapping(source = RewardPeriod.Fields.period, target = RewardPeriodDTO.Fields.yearMonth)
    RewardPeriodDTO mapRewardPeriodEntityToRewardPeriodDTO(RewardPeriod rewardPeriod);

    List<RewardPeriodDTO> mapRewardPeriodEntityListToRewardPeriodDTOList(List<RewardPeriod> rewardPeriods);
}
