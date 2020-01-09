package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.TopNicheDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Date: 2019-01-18
 * Time: 10:48
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class})
public interface TopNicheMapper {

    /**
     * Map an {@link ObjectPair} of Niche name and count to a {@link TopNicheDTO}.
     *
     * @param pair The Niche and count pair
     * @return The resulting TopNicheDTO
     */
    @Mapping(source = "one." + Niche.FIELD__OID__NAME, target = TopNicheDTO.Fields.oid)
    @Mapping(source = "one." + Niche.FIELD__NAME__NAME, target = TopNicheDTO.Fields.name)
    @Mapping(source = "one." + Niche.FIELD__PRETTY_URL_STRING, target = TopNicheDTO.Fields.prettyUrlString)
    @Mapping(expression = "java(pair.getTwo().longValue())", target = TopNicheDTO.Fields.totalPosts)
    TopNicheDTO mapTopNichePairToTopNiche(ObjectPair<Niche,Number> pair);

    /**
     * Map a {@link List} of Niche name and count pairs to a {@link List} of {@link TopNicheDTO}.
     *
     * @param pairs The incoming list of Niche and count pairs
     * @return The resulting list of TopNicheDTOs
     */
    List<TopNicheDTO> mapTopNichePairsListToTopNicheList(List<ObjectPair<Niche,Number>> pairs);
}
