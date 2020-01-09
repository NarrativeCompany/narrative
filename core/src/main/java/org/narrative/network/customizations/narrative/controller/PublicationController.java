package org.narrative.network.customizations.narrative.controller;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.controller.postbody.publication.ChangePublicationOwnerInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.publication.CreatePublicationInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.publication.InvitePublicationPowerUserInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.publication.PublicationPlanInputDTO;
import org.narrative.network.customizations.narrative.controller.postbody.publication.PublicationSettingsInputDTO;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.service.api.PublicationService;
import org.narrative.network.customizations.narrative.service.api.model.InvoiceDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PageDataDTO;
import org.narrative.network.customizations.narrative.service.api.model.PostDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDiscountDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationInvoiceDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPlanDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUserDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUserInvitationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUsersDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationProfileDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.TopNicheDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import java.util.List;

/**
 * Date: 2019-07-31
 * Time: 12:24
 *
 * @author jonmark
 */
@RestController
@RequestMapping("/publications")
@Validated
public class PublicationController {
    public static final String PUBLICATION_ID_PARAM = "publicationId";
    public static final String PUBLICATION_OID_PARAM = "publicationOid";
    public static final String USER_OID_PARAM = "userOid";
    public static final String ROLE_PARAM = "role";

    private static final String CURRENT_USER_PATH = "/current-user";

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping(CURRENT_USER_PATH + "/discount")
    public PublicationDiscountDTO getCurrentUserPublicationDiscount() {
        return publicationService.getCurrentUserPublicationDiscount();
    }

    /**
     * Creates a {@link Publication}
     * @param input {@link CreatePublicationInputDTO} core {@link Publication} details.
     * @return {@link PublicationDTO} details for the created {@link Publication}
     */
    @PostMapping
    public PublicationDTO createPublication(@Valid @RequestBody CreatePublicationInputDTO input) {
        return publicationService.createPublication(input);
    }

    /**
     * Looks up a {@link Publication} given an ID representing either the OID or URL Friendly String of the requested {@link Publication}
     * @param publicationId The OID (with a "id_" prefix) or URL Friendly String of the requested {@link Publication}.
     * @return {@link PublicationDetailDTO} details for the specified {@link Publication}.
     */
    @GetMapping("{"+PUBLICATION_ID_PARAM+"}")
    public PublicationDetailDTO findPublicationByUnknownId(@PathVariable(PUBLICATION_ID_PARAM) String publicationId) {
        return publicationService.findPublicationByUnknownId(publicationId);
    }

    /**
     * Looks up a {@link Publication}'s profile given the OID
     * @param publicationOid The OID of the {@link Publication} to provide {@link PublicationProfileDTO} details for.
     * @return {@link PublicationProfileDTO} details for the specified {@link Publication}.
     */
    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/profile")
    public PublicationProfileDTO getPublicationProfile(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        return publicationService.getPublicationProfile(publicationOid);
    }

    /**
     * Provides {@link PublicationPlanDetailDTO} details for the specified {@link Publication} to the owner of the {@link Publication}
     * @param publicationOid The OID of the {@link Publication} to provide {@link PublicationPlanDetailDTO} details for.
     * @return The {@link PublicationPlanDetailDTO} details for the specified {@link Publication}
     */
    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/plan")
    public PublicationPlanDetailDTO getPublicationPlan(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        return publicationService.getPublicationPlan(publicationOid);
    }

    /**
     * Creates a {@link InvoiceDetailDTO} that once paid will set the requested plan on the specified {@link Publication}.
     * @param publicationOid The OID of the {@link Publication} to apply the plan to.
     * @param input The details for the plan to apply to the {@link Publication}
     * @return The {@link InvoiceDetailDTO} that must be paid before the requested plan will be applied to the specified {@link Publication}
     */
    @PutMapping("{"+PUBLICATION_OID_PARAM+"}/plan")
    public PublicationInvoiceDTO createPublicationInvoice(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @Valid @RequestBody PublicationPlanInputDTO input) {
        return publicationService.createPublicationInvoice(publicationOid, input);
    }

    /**
     * Creates a {@link PublicationSettingsDTO} detailing the {@link Publication}'s current settings.
     * @param publicationOid The OID of the {@link Publication} to fetch settings for.
     * @return The {@link PublicationSettingsDTO} detailing the {@link Publication}'s settings.
     */
    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/settings")
    public PublicationSettingsDTO getPublicationSettings(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        return publicationService.getPublicationSettings(publicationOid);
    }

    @PutMapping("{"+PUBLICATION_OID_PARAM+"}/settings")
    public PublicationSettingsDTO updatePublicationSettings(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @Valid @RequestBody PublicationSettingsInputDTO input) {
        return publicationService.updatePublicationSettings(publicationOid, input);
    }

    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/top-niches")
    public List<TopNicheDTO> getTopNiches(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @Positive @RequestParam(defaultValue = "30") int count) {
        return publicationService.getTopNiches(publicationOid, count);
    }

    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/power-users")
    public PublicationPowerUsersDTO getPublicationPowerUsers(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        return publicationService.getPublicationPowerUsers(publicationOid);
    }

    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/power-users/{" + USER_OID_PARAM + "}")
    public PublicationPowerUserDTO getPublicationPowerUserDetails(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @PathVariable(USER_OID_PARAM) OID userOid) {
        return publicationService.getPublicationPowerUser(publicationOid, userOid);
    }

    @PostMapping("{"+PUBLICATION_OID_PARAM+"}/power-users/{" + USER_OID_PARAM + "}/invites")
    public PublicationPowerUsersDTO invitePublicationPowerUser(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @PathVariable(USER_OID_PARAM) OID userOid, @Valid @RequestBody InvitePublicationPowerUserInputDTO input) {
        return publicationService.invitePublicationPowerUser(publicationOid, userOid, input.getRoles());
    }

    @DeleteMapping("{"+PUBLICATION_OID_PARAM+"}/power-users/{" + USER_OID_PARAM + "}/{" + ROLE_PARAM + "}")
    public PublicationPowerUsersDTO deletePublicationPowerUser(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @PathVariable(USER_OID_PARAM) OID userOid, @PathVariable(ROLE_PARAM) PublicationRole role) {
        return publicationService.removePublicationPowerUser(publicationOid, userOid, role);
    }

    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/power-users" + CURRENT_USER_PATH + "/invitation")
    public PublicationPowerUserInvitationDTO getPublicationPowerUserInvitation(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        return publicationService.getPublicationPowerUserInvitation(publicationOid);
    }

    @PutMapping("{"+PUBLICATION_OID_PARAM+"}/power-users" + CURRENT_USER_PATH + "/invitation")
    public void acceptPublicationPowerUserInvitation(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        publicationService.acceptPublicationPowerUserInvitation(publicationOid);
    }

    @DeleteMapping("{"+PUBLICATION_OID_PARAM+"}/power-users" + CURRENT_USER_PATH + "/invitation")
    public void declinePublicationPowerUserInvitation(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid) {
        publicationService.declinePublicationPowerUserInvitation(publicationOid);
    }

    @PutMapping("{"+PUBLICATION_OID_PARAM+"}/owner")
    public PublicationPowerUsersDTO changePublicationOwner(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @Valid @RequestBody ChangePublicationOwnerInputDTO input) {
        return publicationService.changePublicationOwner(publicationOid, input.getUserOid(), input);
    }

    @GetMapping("{"+PUBLICATION_OID_PARAM+"}/moderated-posts")
    public PageDataDTO<PostDTO> getModeratedPosts(@PathVariable(PUBLICATION_OID_PARAM) OID publicationOid, @PageableDefault(size = 30) Pageable pageRequest) {
        return publicationService.getModeratedPosts(publicationOid, pageRequest);
    }
}
