package org.narrative.network.customizations.narrative.service.impl.publication;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.ObjectPair;
import org.narrative.common.util.ApplicationError;
import org.narrative.common.util.PageNotFoundError;
import org.narrative.config.cache.spring.CacheManagerDefaultConfig;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentType;
import org.narrative.network.core.content.base.services.ContentList;
import org.narrative.network.core.content.base.services.ContentSort;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.controller.PublicationController;
import org.narrative.network.customizations.narrative.controller.postbody.publication.ChangePublicationOwnerInputDTO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.publications.services.CreatePublicationInvoiceTask;
import org.narrative.network.customizations.narrative.service.api.AreaTaskExecutor;
import org.narrative.network.customizations.narrative.service.api.ContentStreamService;
import org.narrative.network.customizations.narrative.service.api.PublicationService;
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
import org.narrative.network.customizations.narrative.service.api.model.input.CreatePublicationInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationPlanInput;
import org.narrative.network.customizations.narrative.service.api.model.input.PublicationSettingsInput;
import org.narrative.network.customizations.narrative.service.api.model.input.UpdateProfileAccountConfirmationInputBase;
import org.narrative.network.customizations.narrative.service.api.services.ParseObjectFromUnknownIdTask;
import org.narrative.network.customizations.narrative.service.impl.common.PageUtil;
import org.narrative.network.customizations.narrative.service.mapper.PostMapper;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;
import org.narrative.network.customizations.narrative.service.mapper.TopNicheMapper;
import org.narrative.network.shared.security.AccessViolation;
import org.narrative.network.shared.tasktypes.AreaTaskImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-07-31
 * Time: 13:02
 *
 * @author jonmark
 */
@Service
public class PublicationServiceImpl implements PublicationService {
    private final AreaTaskExecutor areaTaskExecutor;
    private final PublicationMapper publicationMapper;
    private final TopNicheMapper topNicheMapper;
    private final PostMapper postMapper;
    private final ContentStreamService contentStreamService;

    public PublicationServiceImpl(AreaTaskExecutor areaTaskExecutor, PublicationMapper publicationMapper, TopNicheMapper topNicheMapper, PostMapper postMapper, ContentStreamService contentStreamService) {
        this.areaTaskExecutor = areaTaskExecutor;
        this.publicationMapper = publicationMapper;
        this.topNicheMapper = topNicheMapper;
        this.postMapper = postMapper;
        this.contentStreamService = contentStreamService;
    }

    @Override
    public PublicationDiscountDTO getCurrentUserPublicationDiscount() {
        networkContext().getPrimaryRole().checkRegisteredUser();
        User user = networkContext().getUser();

        return PublicationDiscountDTO.builder()
                .oid(user.getOid())
                .eligibleForDiscount(user.isEligibleForPublicationDiscount())
                .build();
    }

    @Override
    public PublicationDTO createPublication(CreatePublicationInput input) {
        Publication publication = areaTaskExecutor.executeAreaTask(new CreatePublicationTask(input));

        return publicationMapper.mapPublicationToDto(publication);
    }

    @Override
    public PublicationDetailDTO findPublicationByUnknownId(String publicationId) {
        Publication publication = areaTaskExecutor.executeAreaTask(new ParseObjectFromUnknownIdTask<Publication>(publicationId, PublicationController.PUBLICATION_ID_PARAM) {
            @Override
            protected Publication getFromOid(OID oid) {
                return Publication.dao().get(oid);
            }

            @Override
            protected Publication getFromPrettyUrlString(String prettyUrlString) {
                return Publication.dao().getForPrettyURLString(prettyUrlString);
            }
        });
        // jw: unlike all other requests we will let this through regardless of everything. We need this to render the
        //     wrapper and present the publication shell, even if most routes will result in a error.

        return publicationMapper.mapPublicationToDetailDto(publication);
    }

    @Override
    public PublicationInvoiceDTO createPublicationInvoice(OID publicationOid, PublicationPlanInput input) {
        // jw: the owner needs this in order to renew... Kinda a no brainer.
        Publication publication = getPublication(publicationOid, true);

        PublicationInvoice invoice = areaTaskExecutor.executeAreaTask(new CreatePublicationInvoiceTask(publication, input));

        return publicationMapper.mapPublicationInvoiceToDto(invoice);
    }

    @Override
    public PublicationProfileDTO getPublicationProfile(OID publicationOid) {
        Publication publication = getPublication(publicationOid, false);

        return publicationMapper.mapPublicationToProfileDto(publication);
    }

    @Override
    public PublicationPlanDetailDTO getPublicationPlan(OID publicationOid) {
        // jw: the owner needs to be able to access this page to renew their plan if expired.
        Publication publication = getPublication(publicationOid, true);

        publication.checkCurrentRoleOwner();

        return publicationMapper.mapPublicationToPlanDto(publication);
    }

    @Override
    public PublicationSettingsDTO getPublicationSettings(OID publicationOid) {
        // jw: the owner should not need to access the settings when the publication is expired.
        Publication publication = getPublication(publicationOid, false);
        publication.checkCurrentRoleAccess(PublicationRole.ADMIN);

        return publicationMapper.mapPublicationToSettingsDto(publication);
    }

    @Override
    public PublicationSettingsDTO updatePublicationSettings(OID publicationOid, PublicationSettingsInput input) {
        // jw: like above, the owner should never need to update the settings either.
        Publication publication = getPublication(publicationOid, false);

        publication.checkCurrentRoleAccess(PublicationRole.ADMIN);

        areaTaskExecutor.executeAreaTask(new UpdatePublicationSettingsTask(publication, input));

        return publicationMapper.mapPublicationToSettingsDto(publication);
    }

    @Cacheable(cacheNames = CacheManagerDefaultConfig.CacheName.CACHE_PUBLICATIONSERVICE_TOP_NICHES)
    @Override
    public List<TopNicheDTO> getTopNiches(OID publicationOid, int count) {
        Publication publication = getPublication(publicationOid, false);

        List<ObjectPair<Niche,Number>> topNicheData = ChannelContent.dao().getNichesMostPostedToInChannel(publication.getChannel(), count);
        return topNicheMapper.mapTopNichePairsListToTopNicheList(topNicheData);
    }

    @Override
    public PublicationPowerUsersDTO getPublicationPowerUsers(OID publicationOid) {
        // jw: we need to allow the owner to access the power users when the publication is expired.
        Publication publication = getPublication(publicationOid, true);

        // bl: in order to access Power Users, you must be a Power User in some capacity (owner, admin, editor, or writer)
        publication.checkCurrentRoleAnyRole();

        return publicationMapper.mapPublicationToPowerUsersDto(publication);
    }

    @Override
    public PublicationPowerUserDTO getPublicationPowerUser(OID publicationOid, OID userOid) {
        Set<PublicationRole> inviteeRoles = EnumSet.noneOf(PublicationRole.class);
        // jw: The owner should never be inviting when the publication is expired.
        return doPowerUserTask(publicationOid, userOid, null, false, true, (publication, user, channelUser) -> {
            if(exists(channelUser)) {
                // bl: for purposes of this request, we want to include both active roles and invited roles
                inviteeRoles.addAll(CollectionUtils.union(channelUser.getPublicationRoles(), channelUser.getInvitedPublicationRoles()));
            }

            return PublicationPowerUserDTO.builder()
                    .oid(userOid)
                    .roles(inviteeRoles)
                    .build();
        });
    }

    @Override
    public PublicationPowerUsersDTO invitePublicationPowerUser(OID publicationOid, OID userOid, Set<PublicationRole> roles) {
        // jw: The owner should never be inviting users while the publication is expired.
        return doPowerUserTask(publicationOid, userOid, roles, false, true, (publication, user, channelUser) -> {
            boolean isNew = !exists(channelUser);

            Set<PublicationRole> inviteRoles = EnumSet.copyOf(roles);

            // bl: filter out any roles that the user already has, including invites
            if(!isNew) {
                inviteRoles.removeAll(channelUser.getPublicationRoles());
                inviteRoles.removeAll(channelUser.getInvitedPublicationRoles());
            }

            if(!inviteRoles.isEmpty()) {
                // bl: check to make sure slots are open for the roles being added
                for (PublicationRole role : inviteRoles) {
                    // bl: if it's already full, then don't allow adding the user
                    if(role.isPlanLimitReached(publication)) {
                        throw new ApplicationError(wordlet("publication.cantAddRole", role.getNameForDisplay()));
                    }
                }

                if(isNew) {
                    channelUser = new ChannelUser(publication.getChannel(), user);
                }

                // bl: if you are inviting yourself, then no invite necessary
                if (user.isCurrentUserThisUser()) {
                    channelUser.addRoles(inviteRoles);
                } else {
                    channelUser.addRoleInvites(inviteRoles);
                }

                if(isNew) {
                    ChannelUser.dao().save(channelUser);
                }

                // bl: now that the invite has been recorded, send the invite email if necessary
                if (!user.isCurrentUserThisUser()) {
                    areaTaskExecutor.executeAreaTask(new SendPublicationPowerUserInvitationEmailTask(publication, networkContext().getUser(), user, inviteRoles));
                }
            }

            return publicationMapper.mapPublicationToPowerUsersDto(publication);
        });
    }

    @Override
    public PublicationPowerUsersDTO removePublicationPowerUser(OID publicationOid, OID userOid, PublicationRole role) {
        // jw: the admin will need access to this tool when the publication is expired in order to potentially get below
        //     the threshold for a different plan.
        return doPowerUserTask(publicationOid, userOid, EnumSet.of(role), true, true, (publication, user, channelUser) -> {
            if(exists(channelUser)) {
                boolean wasInvitation = channelUser.getInvitedPublicationRoles().contains(role);

                // bl: check to make sure the user actually needs to be deleted to prevent sending the email
                // when nothing actually happens :)
                if(wasInvitation || channelUser.getPublicationRoles().contains(role)) {
                    channelUser.removeRole(role);

                    channelUser.deleteIfEmpty();

                    // bl: no need to send this email if the user is the one performing the action
                    if(!channelUser.getUser().isCurrentUserThisUser()) {
                        areaTaskExecutor.executeAreaTask(new SendPublicationPowerUserRemovedEmailTask(publication, networkContext().getUser(), user, role, wasInvitation));
                    }
                }
            }

            return publicationMapper.mapPublicationToPowerUsersDto(publication);
        });
    }

    @Override
    public PublicationPowerUsersDTO changePublicationOwner(OID publicationOid, OID userOid, UpdateProfileAccountConfirmationInputBase input) {
        // jw: we should probably allow this process as well when the publication is expired in case another user wants to
        //     take possession of the publication and shoulder the expense of renewal.
        return doPowerUserTask(publicationOid, userOid, null, false, true, (publication, newOwner, newOwnerChannelUser) -> {
            // bl: in addition to the general power user permission check, need to ensure the current role is the
            // owner in order to be able to change the owner!
            publication.checkCurrentRoleOwner();

            if(!exists(newOwnerChannelUser) || !newOwnerChannelUser.getPublicationRoles().contains(PublicationRole.ADMIN)) {
                throw new ApplicationError(wordlet("publicationService.cantChangeOwnerToNonAdmin"));
            }
            areaTaskExecutor.executeAreaTask(new ChangePublicationOwnerTask(publication, newOwner, networkContext().getUser(), input));

            // return the PublicationDetailDTO reflecting the new owner
            return publicationMapper.mapPublicationToPowerUsersDto(publication);
        });
    }

    private <T> T doPowerUserTask(OID publicationOid, OID userOid, Set<PublicationRole> inviteRoles, boolean allowCurrentUser, boolean allowForOwnerWhenExpired, PowerUserTask<T> task) {
        Publication publication = getPublication(publicationOid, allowForOwnerWhenExpired);

        try {
            // bl: make sure this user can manage invites of the specified role type
            if(isEmptyOrNull(inviteRoles)) {
                // bl: if no roles are specified, then we'll look for inviting ANY role
                publication.checkCurrentRoleInviteAnyRoles();
            } else {
                publication.checkCurrentRoleInviteRoles(inviteRoles);
            }
        } catch(AccessViolation av) {
            // bl: ignore the AccessViolation if the current user is allowed
            if(!allowCurrentUser || !networkContext().getPrimaryRole().getOid().equals(userOid)) {
                throw av;
            }
        }

        User user = User.dao().getForApiParam(userOid, ChangePublicationOwnerInputDTO.Fields.userOid);

        ChannelUser channelUser = ChannelUser.dao().getForChannelAndUser(publication.getChannel(), user);

        return task.doTask(publication, user, channelUser);
    }

    private interface PowerUserTask<T> {
        T doTask(Publication publication, User user, ChannelUser channelUser);
    }

    @Override
    public PublicationPowerUserInvitationDTO getPublicationPowerUserInvitation(OID publicationOid) {
        return doPowerUserInvitationTask(publicationOid, (publication, channelUser) ->
                PublicationPowerUserInvitationDTO.builder()
                        .oid(channelUser.getUser().getOid())
                        .invitedRoles(channelUser.getInvitedPublicationRoles())
                        .build()
        );
    }

    @Override
    public void acceptPublicationPowerUserInvitation(OID publicationOid) {
        doPowerUserInvitationTask(publicationOid, (publication, channelUser) -> {
            // bl: accept all of the invited roles
            Set<PublicationRole> invitedRoles = channelUser.getInvitedPublicationRoles();

            if(!invitedRoles.isEmpty()) {
                channelUser.addRoles(invitedRoles);
                // bl: clear out the invited roles
                channelUser.setInvitedRoles(0L);

                areaTaskExecutor.executeAreaTask(new SendPublicationPowerUserInviteResponseEmailTask(publication, networkContext().getUser(), invitedRoles, true));
            }

            return null;
        });
    }

    @Override
    public void declinePublicationPowerUserInvitation(OID publicationOid) {
        doPowerUserInvitationTask(publicationOid, (publication, channelUser) -> {
            // bl: clone the set so that we can use it after we remove the user
            Set<PublicationRole> invitedRoles = EnumSet.copyOf(channelUser.getInvitedPublicationRoles());

            if(!invitedRoles.isEmpty()) {
                // bl: remove all of the invited roles
                invitedRoles.forEach(channelUser::removeRole);
                // bl: delete the channel user if it's empty
                channelUser.deleteIfEmpty();

                areaTaskExecutor.executeAreaTask(new SendPublicationPowerUserInviteResponseEmailTask(publication, networkContext().getUser(), invitedRoles, false));
            }

            return null;
        });
    }

    private <T> T doPowerUserInvitationTask(OID publicationOid, PowerUserInvitationTask<T> task) {
        // jw: The invitation process should work while a publication is expired so that users can accept/decline invitations
        Publication publication = Publication.dao().getForApiParam(publicationOid, PublicationController.PUBLICATION_OID_PARAM);

        ChannelUser channelUser = ChannelUser.dao().getForChannelAndUser(publication.getChannel(), networkContext().getUser());

        if(!exists(channelUser) || channelUser.getInvitedPublicationRoles().isEmpty()) {
            throw new PageNotFoundError(wordlet("publication.invitationNotFound"));
        }

        return task.doTask(publication, channelUser);
    }

    private interface PowerUserInvitationTask<T> {
        T doTask(Publication publication, ChannelUser channelUser);
    }

    @Override
    public PageDataDTO<PostDTO> getModeratedPosts(OID publicationOid, Pageable pageRequest) {
        Publication publication = getPublication(publicationOid, false);

        // bl: only Publication editors can access the review queue
        publication.checkCurrentRoleAccess(PublicationRole.EDITOR);

        return areaTaskExecutor.executeAreaTask(new AreaTaskImpl<PageDataDTO<PostDTO>>(false) {
            @Override
            protected PageDataDTO<PostDTO> doMonitoredTask() {
                ContentList contentList = new ContentList(getAreaContext().getPortfolio(), ContentType.NARRATIVE_POST);
                contentList.doSetRowsPerPage(pageRequest.getPageSize());
                contentList.setPage(pageRequest.getPageNumber() + 1);
                contentList.setChannel(publication.getChannel());
                contentList.setModerated(true);
                contentList.setSort(ContentSort.CHANNEL_MODERATION_DATETIME);
                contentList.setSortAsc(false);
                contentList.doCount(true);
                List<Content> contents = getAreaContext().doAreaTask(contentList);

                contentStreamService.populateFollowedChannelsForContentList(getNetworkContext().getUser(), contents);

                List<PostDTO> postDTOs = postMapper.mapContentListToPostDTOList(contents);

                return PageUtil.buildPage(postDTOs, pageRequest, contentList.getCount());
            }
        });
    }

    private static Publication getPublication(OID publicationOid, boolean allowForOwnerWhenExpired) {
        Publication publication = Publication.dao().getForApiParam(publicationOid, PublicationController.PUBLICATION_OID_PARAM);
        publication.assertNotExpired(allowForOwnerWhenExpired);

        return publication;
    }
}
