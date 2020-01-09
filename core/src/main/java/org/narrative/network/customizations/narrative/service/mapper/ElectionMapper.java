package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.service.api.model.ElectionDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.ElectionNomineeDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Date: 11/13/18
 * Time: 3:58 PM
 *
 * @author jonmark
 */
@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class, UserMapper.class})
public interface ElectionMapper {
    /**
     * Map from {@link Election} entity to {@link ElectionDTO}.
     *
     * @param election The incoming election entity to map
     * @return The mapped election
     */
    ElectionDTO mapElectionToDto(Election election);

    /**
     * Map from {@link Election} entity to {@link ElectionDetailDTO}.
     *
     * @param election The incoming election entity to map
     * @return The mapped election detail
     */
    @Mapping(source = "election", target = ElectionDetailDTO.Fields.election)
    ElectionDetailDTO mapElectionToDetailDto(Election election);

    /**
     * Map from {@link ElectionNominee} entity to {@link ElectionNomineeDTO}.
     *
     * @param nominee The incoming election nominee entity to map
     * @return The mapped election nominee
     */
    ElectionNomineeDTO mapElectionNomineeToDto(ElectionNominee nominee);

    /**
     * Map a {@link List} of {@link ElectionNominee} to a {@link List} of {@link ElectionNomineeDTO}.
     *
     * @param nominees The incoming list of Election Nominee entities
     * @return The resulting list of Election Nominee DTOs
     */
    List<ElectionNomineeDTO> mapElectionNomineesToDtoList(List<ElectionNominee> nominees);
}
