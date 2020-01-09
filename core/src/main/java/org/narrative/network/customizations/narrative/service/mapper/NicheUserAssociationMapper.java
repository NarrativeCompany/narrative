package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.service.api.model.NicheUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Date: 10/2/18
 * Time: 8:40 PM
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class, uses = {NicheMapper.class})
public interface NicheUserAssociationMapper {
    /**
     * Map from {@link NicheUserAssociation} entity to {@link NicheUserAssociationDTO}.
     *
     * @param association The incoming Niche User Association entity to map
     * @return The mapped Niche User Association
     */
    NicheUserAssociationDTO mapNicheUserAssociationEntityToNicheUserAssociation(NicheUserAssociation association);

    /**
     * Map from {@link List} of {@link NicheUserAssociation} to {@link List} of {@link NicheUserAssociationDTO}
     *
     * @param associations The incoming Niche User associations
     * @return The mapped Niche User Association
     */
    List<NicheUserAssociationDTO> mapNicheUserAssociationEntitiesToNicheUserAssociations(List<NicheUserAssociation> associations);
}
