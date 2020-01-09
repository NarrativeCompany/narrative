package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.service.api.model.CurrentUserDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class, FormatPreferencesMapper.class, PermissionsMapper.class})
public interface CurrentUserMapper {

    /**
     * Map from {@link User} entity to {@link CurrentUserDTO}.
     *
     * @param user The incoming user
     * @return The current user dto
     */
    @Mapping(source = "user.personalJournal.oid", target = CurrentUserDTO.Fields.personalJournalOid)
    @Mapping(source = "user", target = CurrentUserDTO.Fields.globalPermissions)
    CurrentUserDTO mapUserEntityToCurrentUser(User user);
}
