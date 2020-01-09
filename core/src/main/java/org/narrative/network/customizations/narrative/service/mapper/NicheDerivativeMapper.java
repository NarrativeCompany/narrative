package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorSlotsDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheProfileDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class, UserMapper.class, ElectionMapper.class, TribunalIssueMapper.class})
public interface NicheDerivativeMapper {
    /**
     * Map a {@link List} of {@link Niche} to a {@link List} of {@link NicheDTO}.
     *
     * @param nicheEntityList The incoming list of Niche entities
     * @return The resulting list of niches
     */
    public abstract List<NicheDTO> mapNicheEntityListToNicheList(List<Niche> nicheEntityList);

    /**
     * Map a {@link Niche} to a {@link NicheDetailDTO}.
     * @param nicheEntity The incoming list of Niche entities.
     * @return Resulting list of Niche Details.
     */
    @Mapping(source = "nicheEntity", target = "niche")
    public abstract NicheDetailDTO mapNicheEntityToNicheDetail(Niche nicheEntity);

    /**
     * Map from {@link NicheModeratorElection} entity to {@link NicheModeratorElectionDTO}.
     *
     * @param election The incoming election entity to map
     * @return The mapped election
     */
    public abstract NicheModeratorElectionDTO mapNicheModeratorElectionToDto(NicheModeratorElection election);

    /**
     * Map a {@link List} of {@link NicheModeratorElection} to a {@link List} of {@link NicheModeratorElectionDTO}.
     *
     * @param electionsList The incoming list of Niche Moderator Election entities
     * @return The resulting list of Niche Moderator Election DTOs
     */
    public abstract List<NicheModeratorElectionDTO> mapNicheModeratorElectionListToDtoList(List<NicheModeratorElection> electionsList);

    /**
     * Map from {@link NicheModeratorElection} entity to {@link NicheModeratorElectionDTO}.
     *
     * @param election The incoming election entity to map
     * @return The mapped election
     */
    public abstract NicheModeratorElectionDetailDTO mapNicheModeratorElectionToDetailDto(NicheModeratorElection election);

    /**
     * Map from {@link Niche} entity to {@link NicheProfileDTO}
     *
     * @param niche The incoming niche entity to map
     * @return The mapped profile
     */
    @Mapping(source = "niche", target = NicheProfileDTO.Fields.niche)
    public abstract NicheProfileDTO mapNicheToNicheProfileDto(Niche niche);

    /**
     * Map from {@link Niche} entity to {@link NicheModeratorSlotsDTO}
     *
     * @param niche The incoming niche entity to map
     * @return The mapped niche moderator slots DTO
     */
    @Mapping(source = "niche", target = NicheModeratorSlotsDTO.Fields.niche)
    @Mapping(source = "niche.activeModeratorElection.election", target = NicheModeratorSlotsDTO.Fields.activeModeratorElection)
    public abstract NicheModeratorSlotsDTO mapNicheToNicheModeratorSlotsDto(Niche niche);
}
