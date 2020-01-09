package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.user.services.preferences.FormatPreferences;
import org.narrative.network.customizations.narrative.service.api.model.FormatPreferencesDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ServiceMapperConfig.class)
public interface FormatPreferencesMapper {
    /**
     * Map from {@link FormatPreferences} entity to {@link FormatPreferencesDTO}.
     *
     * @param formatPreferences The incoming format preferences
     * @return The mapped format preferences DTO
     */
    @Mapping(expression = "java(formatPreferences.getLocale().toLanguageTag())", target = "localeForNumber")
    FormatPreferencesDTO mapFormatPreferencesEntityToFormatPreferences(FormatPreferences formatPreferences);
}
