package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.channels.ChannelType;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheOwnershipRewardDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class})
public abstract class NicheMapper {

    /**
     * Autowire our own ChannelMapper. MapStruct won't inject it automatically since it's not strictly
     * needed by any of the mappings here.
     */
    @Autowired
    private ChannelMapper channelMapper;

    /**
     * Map from {@link Niche} entity to {@link NicheDTO}.
     *
     * @param nicheEntity The incoming niche entity to map
     * @return The mapped niche
     */
    @Mapping(source = "suggester.areaUser.user", target = "suggester")
    @Mapping(source = "owner.areaUser.user", target = "owner")
    @Mapping(source = "channelType", target = NicheDTO.Fields.type)
    @Mapping(target = "currentUserFollowedItem", ignore = true)
    public abstract NicheDTO mapNicheEntityToNiche(Niche nicheEntity);

    @AfterMapping
    void map(Niche nicheEntity, @MappingTarget NicheDTO.NicheDTOBuilder builder) {
        if(nicheEntity.getChannel().getFollowedByCurrentUser()!=null) {
            builder.currentUserFollowedItem(channelMapper.mapChannelEntityToCurrentUserFollowedItem(nicheEntity.getChannel()));
        }
    }

    public NicheOwnershipRewardDTO mapNicheOwnershipReward(ObjectPair<Niche, NrveUsdValue> pair) {
        return NicheOwnershipRewardDTO.builder()
                .niche(mapNicheEntityToNiche(pair.getOne()))
                .reward(pair.getTwo())
                .build();
    }

    public abstract List<NicheOwnershipRewardDTO> mapNicheOwnershipRewards(List<ObjectPair<Niche, NrveUsdValue>> pairs);
}

