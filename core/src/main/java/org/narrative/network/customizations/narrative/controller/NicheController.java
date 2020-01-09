package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.config.properties.NarrativeProperties;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.postbody.niche.CreateNicheInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.niche.SimilarNicheSearchInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.niche.UpdateNicheInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.niche.UpdateNicheModeratorSlotsInputDTO;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheStatus;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheList;
import org.narrative.network.customizations.narrative.service.api.NicheService;
import org.narrative.network.customizations.narrative.service.api.model.NicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorElectionDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheModeratorSlotsDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheProfileDTO;
import org.narrative.network.customizations.narrative.service.api.model.NicheRewardPeriodStatsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.ReferendumDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardPostDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardLeaderboardUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.RewardPeriodDTO;
import org.narrative.network.customizations.narrative.service.api.model.TribunalIssueDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.UserDTO;
import org.narrative.network.customizations.narrative.service.impl.StaticMethodWrapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import java.util.List;

@RestController
@RequestMapping("/niches")
@Validated
public class NicheController {
    public static final String NICHE_ID_PARAM = "nicheId";
    public static final String NICHE_OID_PARAM = "nicheOid";

    private final NicheService nicheService;
    private final NarrativeProperties narrativeProperties;
    private final StaticMethodWrapper staticMethodWrapper;

    public NicheController(NicheService nicheService, NarrativeProperties narrativeProperties, StaticMethodWrapper staticMethodWrapper) {
        this.nicheService = nicheService;
        this.narrativeProperties = narrativeProperties;
        this.staticMethodWrapper = staticMethodWrapper;
    }

    /**
     * Find all niches by page
     *
     * @param pageRequest Page information for the request
     * @return {@link PageDataDTO} of {@link NicheDTO}
     */
    @GetMapping
    public PageDataDTO<NicheDTO> findNiches(@PageableDefault(size = 50) Pageable pageRequest) {
        NicheList criteria = new NicheList();
        criteria.setNotStatus(NicheStatus.REJECTED);
        criteria.doSortByField(NicheList.SortField.LAST_STATUS_UPDATE_DATETIME);

        return nicheService.findNiches(criteria, pageRequest);
    }

    /**
     * Get Trending niches
     * @param count The number of trending Niches to fetch
     * @return {@link List} of {@link NicheDTO}
     */
    @GetMapping("/trending")
    public List<NicheDTO> getTrendingNiches(@Positive @RequestParam(defaultValue = "10") int count) {
        // bl: first get the cached list of OIDs
        List<OID> trendingNicheOids = nicheService.getTrendingNicheOids(count);
        // then get the DTOs to return. note that this has a similar aspect-weaving requirement as findSimilarNiches below.
        return nicheService.getNicheDTOsForNicheOids(trendingNicheOids);
    }

    /**
     * Find a niche by OID
     *
     * @param nicheId The {@link String} id of the niche of interest. Either the OID, or the URL friendly id prefixed with "id_".
     * @return {@link NicheDetailDTO}
     */
    @GetMapping("{" + NICHE_ID_PARAM + "}")
    public NicheDetailDTO findNicheByUnknownId(@PathVariable(NICHE_ID_PARAM) String nicheId) {
        return nicheService.findNicheByUnknownId(nicheId);
    }

    /**
     * Find niche profile by ID     *
     * @param nicheId The {@link String} id of the niche of interest. Either the OID, or the URL friendly id prefixed with "id_".
     * @return {@link NicheDetailDTO}
     */
    @GetMapping("{" + NICHE_ID_PARAM + "}/profile")
    public NicheProfileDTO findNicheProfileByUnknownId(@PathVariable(NICHE_ID_PARAM) String nicheId) {
        return nicheService.findNicheProfileByUnknownId(nicheId);
    }

    /**
     * Find niche moderator slots by ID
     *
     * @param nicheId The {@link String} id of the niche of interest. Either the OID, or the URL friendly id prefixed with "id_".
     * @return {@link NicheModeratorSlotsDTO}
     */
    @GetMapping("{" + NICHE_ID_PARAM + "}/moderator-slots")
    public NicheModeratorSlotsDTO findNicheModeratorSlotsByUnknownId(@PathVariable(NICHE_ID_PARAM) String nicheId) {
        return nicheService.findNicheModeratorSlotsByUnknownId(nicheId);
    }

    /**
     * Update niche moderator slot settings
     *
     * @param nicheOid The {@link OID} of the niche to set the moderator slot count for.
     * @return {@link NicheModeratorSlotsDTO}
     */
    @PutMapping("{" + NICHE_OID_PARAM + "}/moderator-slots")
    public NicheModeratorSlotsDTO updateNicheModeratorSlots(@PathVariable(NICHE_OID_PARAM) OID nicheOid, @Valid @RequestBody UpdateNicheModeratorSlotsInputDTO updateNicheModeratorSlotsInput) {
        return nicheService.updateNicheModeratorSlots(nicheOid, updateNicheModeratorSlotsInput.getModeratorSlots());
    }

    /**
     * Find similar niches by name and description.
     *
     * @param similarNicheSearchInputDTO   POSTed DTO containing the the niche name and the niche description to use in order to check for similar niches.
     * @return {@link List} of {@link NicheDTO} found
     */
    @PostMapping(path = "/similar")
    public List<NicheDTO> findSimilarNiches(@Valid @RequestBody SimilarNicheSearchInputDTO similarNicheSearchInputDTO) {
        return nicheService.findSimilarNiches(similarNicheSearchInputDTO);
    }

    /**
     * Get Niches of interest
     *
     * @return {@link List} of {@link NicheDTO} of interest
     */
    @GetMapping(path = "/interest")
    public List<NicheDTO> findNichesOfInterest() {
        return nicheService.getNichesOfInterest();
    }

    /**
     * Find similar niches by OID.
     *
     * @param nicheOid Niche OID to use when finding similar niches.
     * @return {@link List} of {@link NicheDTO} found
     */
    @GetMapping(path = "/similar/{" + NICHE_OID_PARAM + "}")
    public List<NicheDTO> findSimilarNiches(@PathVariable(NICHE_OID_PARAM) OID nicheOid) {
        // todo: ideally this could all be encapsulated into NicheService/Impl, but that will require aspect weaving: #679
        // for now, have it split into two different service methods, but getNicheDTOsForNicheOids is a bit wonky of a design.
        List<OID> similarNicheOids = nicheService.findSimilarNicheOids(nicheOid);
        return nicheService.getNicheDTOsForNicheOids(similarNicheOids);
    }

    /**
     * Submit a new niche for creation.
     *
     * @param createNicheInputDTO   POSTed DTO containing the the niche name, niche description and verification state to use to create a new niche.
     * @return {@link List} of {@link NicheDTO} found
     */
    @PostMapping
    public ReferendumDTO createNiche(@Valid @RequestBody CreateNicheInputDTO createNicheInputDTO) {
        return nicheService.createNiche(createNicheInputDTO);
    }

    /**
     * Update an existing niche.
     *
     * @param updateNicheInputDTO PUT DTO containing the the niche name, niche description and to use to update the niche.
     * @return The {@link TribunalIssueDetailDTO} documenting the update request.
     */
    @PutMapping("/{" + NICHE_OID_PARAM + "}")
    public TribunalIssueDetailDTO updateNiche(@NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid, @Valid @RequestBody UpdateNicheInputDTO updateNicheInputDTO) {
        Niche niche = staticMethodWrapper.getNicheDAO().getForApiParam(nicheOid, NICHE_OID_PARAM);
        return nicheService.submitNicheUpdateRequest(niche, updateNicheInputDTO);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/followers")
    public PageDataDTO<UserDTO> getRandomNicheFollowers(@NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid, @Positive @RequestParam(defaultValue = "20") Integer limit) {
        // bl: make sure the limit is not great than our overall maxPageSize
        limit = Math.min(limit, narrativeProperties.getSpring().getMvc().getMaxPageSize());
        return nicheService.getRandomNicheFollowers(nicheOid, limit);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/reward-periods")
    public List<RewardPeriodDTO> getNicheRewardPeriods(@NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid) {
        return nicheService.getNicheRewardPeriods(nicheOid);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/all-time-rewards")
    public ScalarResultDTO<NrveUsdValue> getNicheAllTimeRewards(@NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid) {
        return nicheService.getNicheAllTimeRewards(nicheOid);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/period-rewards")
    public NicheRewardPeriodStatsDTO getNicheRewardPeriodRewards(
            @NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid,
            @RequestParam(name=RewardsController.MONTH_PARAM) String yearMonthStr) {
        return nicheService.getNicheRewardPeriodRewards(nicheOid, yearMonthStr);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/post-leaderboard")
    public List<RewardLeaderboardPostDTO> getNichePostLeaderboard(
            @NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid,
            @RequestParam(name=RewardsController.MONTH_PARAM, required = false) String yearMonthStr,
            @Positive @RequestParam(defaultValue = "5") Integer limit) {
        // bl: make sure the limit is not great than our overall maxPageSize
        limit = Math.min(limit, narrativeProperties.getSpring().getMvc().getMaxPageSize());
        return nicheService.getNichePostLeaderboard(nicheOid, yearMonthStr, limit);
    }

    @GetMapping("/{" + NICHE_OID_PARAM + "}/creator-leaderboard")
    public List<RewardLeaderboardUserDTO> getNicheCreatorLeaderboard(
            @NotNull @PathVariable(NICHE_OID_PARAM) OID nicheOid,
            @RequestParam(name=RewardsController.MONTH_PARAM, required = false) String yearMonthStr,
            @Positive @RequestParam(defaultValue = "5") Integer limit) {
        // bl: make sure the limit is not great than our overall maxPageSize
        limit = Math.min(limit, narrativeProperties.getSpring().getMvc().getMaxPageSize());
        return nicheService.getNicheCreatorLeaderboard(nicheOid, yearMonthStr, limit);
    }

    /**
     * Find all Niche Moderator Elections by page
     *
     * @param pageRequest Page information for the request
     * @return {@link PageDataDTO} of {@link NicheModeratorElectionDTO}
     */
    @GetMapping(path = "/moderator-elections")
    public PageDataDTO<NicheModeratorElectionDTO> findModeratorElections(@PageableDefault(size = 50) Pageable pageRequest) {
        return nicheService.findNicheModeratorElections(pageRequest);
    }

    /**
     * Find a specific Niche Moderator Election by OID.
     *
     * @param electionOid The OID of the Niche Moderator Election we are looking for.
     * @return {@link NicheModeratorElectionDTO}
     */
    @GetMapping("/moderator-elections/" + ElectionController.ELECTION_OID_PARAMSPEC)
    public NicheModeratorElectionDetailDTO findModeratorElection(@PathVariable(ElectionController.ELECTION_OID_PARAM) OID electionOid) {
        return nicheService.findNicheModeratorElectionByOid(electionOid);
    }
}

