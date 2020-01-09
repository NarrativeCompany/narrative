package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.common.util.InvalidParamError;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.service.api.LedgerEntryService;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntriesDTO;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryDTO;
import org.narrative.network.customizations.narrative.service.api.model.LedgerEntryScrollParamsDTO;
import org.narrative.network.customizations.narrative.util.LedgerEntryScrollable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.Collection;
import java.util.Set;

/**
 * Date: 8/10/18
 * Time: 9:02 AM
 *
 * @author brian
 */

@RestController
@RequestMapping("/ledger-entries")
@Validated
public class LedgerEntryController {
    public static final String CHANNEL_OID_PARAM = "channelOid";

    private final LedgerEntryService ledgerEntryService;
    private final NarrativeProperties narrativeProperties;

    public LedgerEntryController(LedgerEntryService ledgerEntryService, NarrativeProperties narrativeProperties) {
        this.ledgerEntryService = ledgerEntryService;
        this.narrativeProperties = narrativeProperties;
    }

    @GetMapping(path = "/{ledgerEntryOid}")
    public LedgerEntryDTO findLedgerEntryByOid(@PathVariable("ledgerEntryOid") OID ledgerEntryOid){
        return ledgerEntryService.findLedgerEntryByOid(ledgerEntryOid);
    }

    @GetMapping("/channel/{" + CHANNEL_OID_PARAM + "}")
    public LedgerEntriesDTO findLedgerEntriesForChannel(
            @PathVariable(CHANNEL_OID_PARAM) OID channelOid,
            @Valid LedgerEntryScrollable scrollable,
            LedgerEntryScrollParamsDTO scrollParams
    ) {
        if (scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        return ledgerEntryService.findLedgerEntriesForChannel(channelOid, scrollable);
    }

    @GetMapping("/user/{"+UserController.USER_OID_PARAM+"}")
    public LedgerEntriesDTO findLedgerEntriesForUser(
            @PathVariable(UserController.USER_OID_PARAM) OID userOid,
            @RequestParam(required = false) Set<LedgerEntryType> entryTypes,
            @Valid LedgerEntryScrollable scrollable,
            LedgerEntryScrollParamsDTO scrollParams
    ) {
        if (scrollParams!=null) {
            scrollable.setScrollParams(scrollParams);
        }

        Set<LedgerEntryType> entryTypesResolved = getEntryTypesResolved(entryTypes, LedgerEntryType.TYPES_WITH_ACTOR);

        return ledgerEntryService.findLedgerEntriesForUser(userOid, entryTypesResolved, scrollable);
    }

    private Set<LedgerEntryType> getEntryTypesResolved(Set<LedgerEntryType> entryTypes, Set<LedgerEntryType> validEntryTypes) {
        // jw: first, if no types were provided, let's just default to all
        if (CollectionUtils.isEmpty(entryTypes)) {
            return validEntryTypes;
        }

        // jw: secondly, if they specified any types that are not supported by this endpoint, let's give an invalid
        //     parameter error.
        if (!validEntryTypes.containsAll(entryTypes)) {
            // jw: subtract the valid options from the provided options so that we can give a meaningful response.
            Collection<LedgerEntryType> invalidTypes = CollectionUtils.subtract(entryTypes, validEntryTypes);
            throw new InvalidParamError("entryTypes", StringUtils.join(invalidTypes, ", "));
        }

        // jw: guess what we were given is valid, let's turn that into a
        return entryTypes;
    }
}
