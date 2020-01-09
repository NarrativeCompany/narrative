package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.core.search.actions.SearchType;
import org.narrative.network.customizations.narrative.service.api.SearchService;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.SearchResultsDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

import java.util.List;

@RestController
@RequestMapping("/search")
@Validated
public class SearchController {
    public static final String FILTER_PARAM = "filter";
    public static final String CHANNEL_PARAM = "channel";

    private final SearchService searchService;
    private final NarrativeProperties narrativeProperties;

    public SearchController(SearchService searchService, NarrativeProperties narrativeProperties) {
        this.searchService = searchService;
        this.narrativeProperties = narrativeProperties;
    }

    @GetMapping
    public SearchResultsDTO find(
            @NotEmpty @RequestParam String keyword,
            @RequestParam(name=FILTER_PARAM, required = false) SearchType searchType,
            @RequestParam(name=CHANNEL_PARAM, required = false) OID channelOid,
            @RequestParam(required = false) Integer startIndex,
            @Positive @RequestParam(defaultValue = "50") int count
    ) {
        count = Math.min(count, narrativeProperties.getSpring().getMvc().getMaxPageSize());

        return searchService.search(keyword, searchType, channelOid, startIndex, count);
    }

    @GetMapping("/active-niches")
    public List<NicheDTO> findActiveNichesByName(
            @NotEmpty @RequestParam String name,
            @Positive @RequestParam(defaultValue = "50") int count
    ){
        count = Math.min(count, narrativeProperties.getSpring().getMvc().getMaxPageSize());
        return searchService.findActiveNichesByName(name, count);

    }
}
