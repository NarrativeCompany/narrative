package org.narrative.network.customizations.narrative.service.impl.common;

import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.locations.Country;
import org.narrative.network.customizations.narrative.service.api.CanonicalDataService;
import org.narrative.network.customizations.narrative.service.api.model.CountryDTO;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CanonicalDataServiceImpl implements CanonicalDataService {
    private final Locale defaultLocale;

    public CanonicalDataServiceImpl(WebMvcProperties webMvcProperties) {
        this.defaultLocale = webMvcProperties.getLocale();
    }

    @Cacheable(cacheNames = {CacheManagerDefaultConfig.CacheName.CACHE_CANONDATASVC_COUNTRIES_LIST})
    @Override
    public List<CountryDTO> getCountryList() {
        return Country.getOrderedCountries(defaultLocale).stream()
                .map(country -> new CountryDTO(country.name(), country.getCountryDisplayName()))
                .collect(Collectors.toList());
    }
}
