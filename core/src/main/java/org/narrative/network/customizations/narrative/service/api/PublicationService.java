package org.narrative.network.customizations.narrative.service.api;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDiscountDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUserInvitationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationInvoiceDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPlanDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUsersDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationProfileDTO;
import org.narrative.network.customizations.narrative.service.api.model.TopNicheDTO;
import org.narrative.network.customizations.narrative.service.api.model.input.CreatePublicationInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationPlanInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationSettingsInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Date: 2019-07-31
 * Time: 12:52
 *
 * @author jonmark
 */
public interface PublicationService {

    /**
     * Get the current user's Publication discount details
     * @return {@link PublicationDiscountDTO} for the current user
     */
    PublicationDiscountDTO getCurrentUserPublicationDiscount();

    /**
     * Creates a publication provided {@link CreatePublicationInput} data with an optional logo.
     * @param input The core details for the {@link Publication} that should be created.
     * @return {@link PublicationDTO} representing the created {@link Publication}.
     */
    PublicationDTO createPublication(CreatePublicationInput input);

    /**
     * Looks up {@link PublicationDetailDTO} details from a provided ID representing either the OID or prettyUrlString of the {@link Publication}.
     * @param publicationId The OID (with a "id_" prefix) or prettyUrlString of the {@link Publication} to lookup.
     * @return {@link PublicationDetailDTO} details for the specified {@link Publication}
     */
    PublicationDetailDTO findPublicationByUnknownId(String publicationId);

    /**
     * Creates a {@link InvoiceDetailDTO} for the specified {@link Publication} allowing the owner to upgrade or renew
     * their plan based on the plan they requested a {@link InvoiceDetailDTO} for, and the state of the {@link Publication}
     * @param publicationOid The OID of the {@link Publication} to create a {@link InvoiceDetailDTO} for
     * @param input The details for the plan to create the {@link InvoiceDetailDTO} with
     * @return The {@link InvoiceDetailDTO} that must be paid before the requested plan will take effect on the {@link Publication}
     */
    PublicationInvoiceDTO createPublicationInvoice(OID publicationOid, PublicationPlanInput input);

    /**
     * Returns the {@link PublicationProfileDTO} for the specified {@link Publication}.
     * @param publicationOid The OID of the {@link Publication} to fetch the {@link PublicationProfileDTO} for
     * @return The {@link PublicationProfileDTO} details for the specified {@link Publication}
     */
    PublicationProfileDTO getPublicationProfile(OID publicationOid);

    /**
     * Creates a {@link PublicationPlanDetailDTO} for the specified {@link Publication}.
     * @param publicationOid The OID of the {@link Publication} to create a {@link PublicationPlanDetailDTO} for
     * @return The {@link PublicationPlanDetailDTO} details for the specified {@link Publication}
     */
    PublicationPlanDetailDTO getPublicationPlan(OID publicationOid);

    /**
     * Creates a {@link PublicationSettingsDTO} for the specified {@link Publication}.
     * @param publicationOid The OID of the {@link Publication} to create a {@link PublicationSettingsDTO} for
     * @return The {@link PublicationSettingsDTO} details for the specified {@link Publication}
     */
    PublicationSettingsDTO getPublicationSettings(OID publicationOid);

    /**
     * Updates a {@link Publication}'s settings using the provided {@link PublicationSettingsInput} details
     * @param publicationOid The OID of the {@link Publication} to create a {@link InvoiceDetailDTO} for
     * @param input The {@link PublicationSettingsInput} details to update the {@link Publication} with
     * @return The {@link PublicationSettingsDTO} after all changes have been applied.
     */
    PublicationSettingsDTO updatePublicationSettings(OID publicationOid, PublicationSettingsInput input);

    /**
     * Get a list of the top Niches posted to in this Publication
     * @param publicationOid thee OID of the {@link Publication} to get the top Niches for
     * @param count the number of Niches to return
     * @return List of the {@link TopNicheDTO} objects representing the top niches posted to in this publication.
     */
    List<TopNicheDTO> getTopNiches(OID publicationOid, int count);

    /**
     * Creates a {@link PublicationPowerUsersDTO} for the specified {@link Publication}.
     * @param publicationOid The OID of the {@link Publication} to get {@link PublicationPowerUsersDTO} for
     * @return The {@link PublicationPowerUsersDTO} data for the specified {@link Publication}
     */
    PublicationPowerUsersDTO getPublicationPowerUsers(OID publicationOid);

    /**
     * Get the {@link PublicationPowerUserDTO} for the specified user
     * @param publicationOid the {@link Publication}
     * @param userOid the {@link User}
     * @return The {@link PublicationPowerUserDTO} representing the user's current status in the Publication
     */
    PublicationPowerUserDTO getPublicationPowerUser(OID publicationOid, OID userOid);

    /**
     * Invite the specified user to be a Power User with one or more roles in this {@link Publication}
     * @param publicationOid the {@link Publication}
     * @param userOid the {@link User}
     * @param roles the set of {@link PublicationRole} to invite the user to
     * @return The {@link PublicationPowerUsersDTO} with updated Power Users data
     */
    PublicationPowerUsersDTO invitePublicationPowerUser(OID publicationOid, OID userOid, Set<PublicationRole> roles);

    /**
     * Remove the specified user from the specified Power User role in this {@link Publication}
     * @param publicationOid the {@link Publication}
     * @param userOid the {@link User}
     * @param role the {@link PublicationRole} to remove the user from
     * @return The {@link PublicationPowerUsersDTO} with updated Power Users data
     */
    PublicationPowerUsersDTO removePublicationPowerUser(OID publicationOid, OID userOid, PublicationRole role);

    /**
     * Change the Publication owner
     * @param publicationOid The OID of the {@link Publication} to change the owner for
     * @param userOid The OID of the {@link User} to make the new owner
     * @param input The {@link UpdateProfileAccountConfirmationInputBase} with the current owner's password and 2FA (if applicable)
     * @return The {@link PublicationDetailDTO} data for the Publication with the new owner
     */
    PublicationPowerUsersDTO changePublicationOwner(OID publicationOid, OID userOid, UpdateProfileAccountConfirmationInputBase input);

    /**
     * Get the Publication Power User invitation details for the specified user (who must be the current user)
     * @param publicationOid The OID of the {@link Publication} to get the invitation details for
     * @return The {@link PublicationPowerUserInvitationDTO} data for the invitation
     */
    PublicationPowerUserInvitationDTO getPublicationPowerUserInvitation(OID publicationOid);

    /**
     * Accept the Publication Power User invitation for the specified user (who must be the current user).
     * Accepts all outstanding invited roles.
     * @param publicationOid The OID of the {@link Publication}
     */
    void acceptPublicationPowerUserInvitation(OID publicationOid);

    /**
     * Decline/delete the Publication Power User invitation for the specified user (who must be the current user).
     * Declines all outstanding invited roles.
     * @param publicationOid The OID of the {@link Publication}
     */
    void declinePublicationPowerUserInvitation(OID publicationOid);

    /**
     * Get the list of moderated posts for the control panel review queue
     * @param publicationOid The OID of the {@link Publication}
     * @param pageRequest Paging information for this request
     * @return {@link PageDataDTO} of {@link PostDTO} found
     */
    PageDataDTO<PostDTO> getModeratedPosts(OID publicationOid, Pageable pageRequest);
}
