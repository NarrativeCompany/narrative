package org.narrative.network.customizations.narrative.service.api;

import org.narrative.network.customizations.narrative.service.api.model.CountryDTO;

import java.util.List;

public interface CanonicalDataService {
    /**
     * Return a list of countries known to the system
     *
     * @return {@link List} of all {@link CountryDTO} known to the system
     */
    List<CountryDTO> getCountryList();
}
