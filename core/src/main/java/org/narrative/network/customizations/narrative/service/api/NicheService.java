package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.NrveUsdValue;
import org.narrative.network.customizations.narrative.controller.result.ScalarResultDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.services.NicheList;
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
import org.narrative.network.customizations.narrative.service.api.model.input.CreateNicheRequest;
import org.narrative.network.customizations.narrative.service.api.model.input.NicheInputBase;
import org.narrative.network.customizations.narrative.service.api.model.input.SimilarNicheSearchRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Niche operations service.
 */
public interface NicheService {
    /**
     * Find niches by the specified criteria, page size and page.
     *
     * @param criteria    {@link NicheList} specifying criteria for the search
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link NicheDTO} found
     */
    PageDataDTO<NicheDTO> findNiches(NicheList criteria, Pageable pageRequest);

    /**
     * Get a list of Trending Niche OIDs
     * @param count the number of trending Niche OIDs to return
     * @return a list of trending Niche {@link OID} values
     */
    List<OID> getTrendingNicheOids(int count);

    /**
     * Find a niche by prettyUrlString.
     *
     * @param nicheId {@link String} id for the niche of interest. Either the OID or the URL friendly id prefixed with "id_"
     * @return {@link NicheDetailDTO} if found, null otherwise
     */
    NicheDetailDTO findNicheByUnknownId(String nicheId);

    /**
     * Find niche profile by prettyUrlString.
     *
     * @param nicheId {@link String} id for the niche of interest. Either the OID or the URL friendly id prefixed with "id_"
     * @return {@link NicheProfileDTO} if found, null otherwise
     */
    NicheProfileDTO findNicheProfileByUnknownId(String nicheId);

    /**
     * Find niche moderator slots settings by prettyUrlString.
     *
     * @param nicheId {@link String} id for the niche of interest. Either the OID or the URL friendly id prefixed with "id_"
     * @return {@link NicheModeratorSlotsDTO} if found, null otherwise
     */
    NicheModeratorSlotsDTO findNicheModeratorSlotsByUnknownId(String nicheId);

    /**
     * Update niche moderator slots.
     * @param nicheOid the {@link OID} of the niche to update the moderator slot count for
     * @param moderatorSlots
     * @return the updated {@link NicheModeratorSlotsDTO}
     */
    NicheModeratorSlotsDTO updateNicheModeratorSlots(OID nicheOid, int moderatorSlots);

    /**
     * get a {@link Niche} from a nicheId
     * @param nicheId the nicheId to look up
     * @return the {@link Niche}, if found
     */
    Niche getNicheFromUnknownId(String nicheId);

    /**
     * Create a new Niche.
     *
     * @param createNicheRequest The {@link CreateNicheRequest} that represents the niche to create.
     * @return NicheDTO representing the newly created niche
     */
    ReferendumDTO createNiche(CreateNicheRequest createNicheRequest);

    /**
     * Submit a request to update an existing Niche.
     *
     *
     * @param niche The niche being updated
     * @param nicheInput The {@link NicheInputBase} that represents the niche changes to submit.
     * @return The {@link TribunalIssueDetailDTO} documenting the update request.
     */
    TribunalIssueDetailDTO submitNicheUpdateRequest(Niche niche, NicheInputBase nicheInput);

    /**
     * Find similar niches by name and description. Used when suggesting new niches.
     *
     * @param similarNicheSearchRequest The {@link SimilarNicheSearchRequest} that represents the niche details to find similar niches for.
     * @return {@link List} of {@link NicheDTO} found
     */
    List<NicheDTO> findSimilarNiches(SimilarNicheSearchRequest similarNicheSearchRequest);

    /**
     * Find similar niche OIDs by niche OID.
     *
     * @param nicheOid The niche OID find similar niche OIDs for.
     * @return {@link List} of {@link OID} found
     */
    List<OID> findSimilarNicheOids(OID nicheOid);

    /**
     * Generate a NicheDTO list response from a list of niche OIDs.
     *
     * @param nicheOids The list of niche OIDs to process.
     * @return {@link List} of {@link NicheDTO} corresponding to the given niche OIDs
     */
    List<NicheDTO> getNicheDTOsForNicheOids(List<OID> nicheOids);

    /**
     * Get the list of Niches of interest
     *
     * @return {@link List} of {@link NicheDTO} corresponding to the Niches of interest
     */
    List<NicheDTO> getNichesOfInterest();

    /**
     * Get a list of random followers of a niche
     *
     * @param nicheOid The niche to look up followers for
     * @param limit the number of followers to return
     * @return {@link PageDataDTO} with a list of the {@link UserDTO} representing niche followers
     */
    PageDataDTO<UserDTO> getRandomNicheFollowers(OID nicheOid, int limit);

    /**
     * Get a {@link List} of {@link RewardPeriodDTO} objects for this Niche.
     * @param nicheOid the niche to get the list of {@link RewardPeriodDTO} objects for
     * @return a list of {@link RewardPeriodDTO} objects for this niche, based on its approval and rejection dates
     */
    List<RewardPeriodDTO> getNicheRewardPeriods(OID nicheOid);

    /**
     * Get all-time Rewards earned for the Niche
     * @param nicheOid the niche to get all-time rewards for
     * @return {@link NrveUsdValue} containing the niche's all-time rewards
     */
    ScalarResultDTO<NrveUsdValue> getNicheAllTimeRewards(OID nicheOid);

    /**
     * Get rewards for the given month
     * @param nicheOid the niche to get month rewards for
     * @param yearMonthStr the month to get rewards for. required.
     * @return {@link NicheRewardPeriodStatsDTO} containing stats for the Niche's rewards in the given month
     */
    NicheRewardPeriodStatsDTO getNicheRewardPeriodRewards(OID nicheOid, String yearMonthStr);

    /**
     * Get a list of posts on the leaderboard for the given month. If month is omitted, then the leaderboard
     * will include results for all time.
     * @param nicheOid the niche to get the leaderboard for
     * @param yearMonthStr the month to get the leaderboard for. optional.
     * @param limit the number of items to return on the leaderboard
     * @return {@link List} of {@link RewardLeaderboardPostDTO} corresponding to the top posts in the Niche for the given period.
     */
    List<RewardLeaderboardPostDTO> getNichePostLeaderboard(OID nicheOid, String yearMonthStr, int limit);

    /**
     * Get a list of users on the leaderboard for the given month. If month is omitted, then the leaderboard
     * will include results for all time.
     * @param nicheOid the niche to get the leaderboard for
     * @param yearMonthStr the month to get the leaderboard for. optional.
     * @param limit the number of items to return on the leaderboard
     * @return {@link List} of {@link RewardLeaderboardUserDTO} corresponding to the top content creators in the Niche for the given period.
     */
    List<RewardLeaderboardUserDTO> getNicheCreatorLeaderboard(OID nicheOid, String yearMonthStr, int limit);

    /**
     * Find niche moderator elections that are open for nominations by page size and page.
     *
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link NicheModeratorElectionDTO} found
     */
    PageDataDTO<NicheModeratorElectionDTO> findNicheModeratorElections(Pageable pageRequest);

    /**
     * Find Niche Moderator Election by a specific OID.
     *
     * @param nicheModeratorElectionOid The OID of the Niche Moderator Election to find.
     * @return Matching {@link NicheModeratorElectionDTO}
     */
    NicheModeratorElectionDetailDTO findNicheModeratorElectionByOid(OID nicheModeratorElectionOid);
}
