package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.service.api.model.UserReputationDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;

/**
 * Date: 2018-12-14
 * Time: 09:25
 *
 * @author brian
 */
@Mapper(config = ServiceMapperConfig.class)
public interface UserReputationMapper {
    UserReputationDTO mapUserReputationEntityToUserReputationDTO(UserReputation userReputation);
}
