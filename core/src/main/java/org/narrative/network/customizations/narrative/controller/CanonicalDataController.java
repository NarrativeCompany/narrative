package org.narrative.network.customizations.narrative.controller;

import org.narrative.network.customizations.narrative.service.api.CanonicalDataService;
import org.narrative.network.customizations.narrative.service.api.model.CountryDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/canonical")
@Validated
public class CanonicalDataController {
    private final CanonicalDataService canonicalDataService;

    public CanonicalDataController(CanonicalDataService canonicalDataService) {
        this.canonicalDataService = canonicalDataService;
    }

    @GetMapping("/countries")
    public List<CountryDTO> getCountries() {
        return canonicalDataService.getCountryList();
    }
}
