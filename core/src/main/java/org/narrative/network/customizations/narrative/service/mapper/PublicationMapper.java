package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.common.persistence.ObjectPair;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.PublicationRole;
import org.narrative.network.customizations.narrative.publications.PublicationSettings;
import org.narrative.network.customizations.narrative.publications.PublicationUrlType;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationInvoiceDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPlanDetailDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationPowerUsersDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationProfileDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationSettingsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationUrlsDTO;
import org.narrative.network.customizations.narrative.service.api.model.PublicationUserAssociationDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-08-01
 * Time: 12:00
 *
 * @author jonmark
 */
@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class, InvoiceMapper.class})
public abstract class PublicationMapper {

    /**
     * Autowire our own ChannelMapper. MapStruct won't inject it automatically since it's not strictly
     * needed by any of the mappings here.
     */
    @Autowired
    private ChannelMapper channelMapper;

    @Mapping(source = "statusResolved", target = PublicationDTO.Fields.status)
    @Mapping(source = "channelType", target = PublicationDTO.Fields.type)
    @Mapping(target = "currentUserFollowedItem", ignore = true)
    public abstract PublicationDTO mapPublicationToDto(Publication publication);

    public abstract List<PublicationDTO> mapPublicationsToDtos(List<Publication> publications);

    @AfterMapping
    void map(Publication publication, @MappingTarget PublicationDTO.PublicationDTOBuilder builder) {
        if (publication.getChannel().getFollowedByCurrentUser()!=null) {
            builder.currentUserFollowedItem(channelMapper.mapChannelEntityToCurrentUserFollowedItem(publication.getChannel()));
        }
    }

    @Mapping(source = "publication", target = PublicationDetailDTO.Fields.publication)
    @Mapping(source = "settings.fathomSiteId", target = PublicationDetailDTO.Fields.fathomSiteId)
    @Mapping(source = "settings.headerImageAlignment", target = PublicationDetailDTO.Fields.headerImageAlignment)
    @Mapping(source = "publication", target = PublicationDetailDTO.Fields.urls)
    @Mapping(source = "publication.deletionDatetimeWhenExpired", target = PublicationDetailDTO.Fields.deletionDatetime)
    public abstract PublicationDetailDTO mapPublicationToDetailDto(Publication publication);

    public PublicationUrlsDTO mapPublicationToUrlsDto(Publication publication) {
        PublicationUrlsDTO.PublicationUrlsDTOBuilder builder = PublicationUrlsDTO.builder();
        PublicationSettings settings = publication.getSettings();

        for (PublicationUrlType urlType : PublicationUrlType.values()) {
            String url = settings.getUrl(urlType);

            urlType.setUrl(builder, url);
        }

        return builder.build();
    }

    public abstract PublicationProfileDTO mapPublicationToProfileDto(Publication publication);

    @Mapping(source = "inTrialPeriod", target = PublicationPlanDetailDTO.Fields.withinTrialPeriod)
    @Mapping(source = "renewable", target = PublicationPlanDetailDTO.Fields.withinRenewalPeriod)
    @Mapping(source = "eligibleForWaitListDiscount", target = PublicationPlanDetailDTO.Fields.eligibleForDiscount)
    @Mapping(target = PublicationPlanDetailDTO.Fields.admins, ignore = true)
    @Mapping(target = PublicationPlanDetailDTO.Fields.editors, ignore = true)
    @Mapping(target = PublicationPlanDetailDTO.Fields.writers, ignore = true)
    public abstract PublicationPlanDetailDTO mapPublicationToPlanDto(Publication publication);

    @AfterMapping
    void map(Publication publication, @MappingTarget PublicationPlanDetailDTO.PublicationPlanDetailDTOBuilder builder) {
        Map<PublicationRole, Integer> countsByRole = publication.getChannel().getUserCountsByRole();

        builder.admins(countsByRole.getOrDefault(PublicationRole.ADMIN, 0));
        builder.editors(countsByRole.getOrDefault(PublicationRole.EDITOR, 0));
        builder.writers(countsByRole.getOrDefault(PublicationRole.WRITER, 0));
    }

    @Mapping(source = "invoice", target = PublicationInvoiceDTO.Fields.invoiceDetail)
    public abstract PublicationInvoiceDTO mapPublicationInvoiceToDto(PublicationInvoice publicationInvoice);

    @Mapping(source = "publication", target = PublicationSettingsDTO.Fields.publicationDetail)
    public abstract PublicationSettingsDTO mapPublicationToSettingsDto(Publication publication);

    @Mapping(source = "publication", target = PublicationPowerUsersDTO.Fields.publicationDetail)
    public abstract PublicationPowerUsersDTO mapPublicationToPowerUsersDto(Publication publication);

    public PublicationUserAssociationDTO mapPublicationUserAssociationToDto(Publication publication, ChannelUser channelUser, User forUser) {
        return PublicationUserAssociationDTO.builder()
                .oid(publication.getOid())
                .publication(mapPublicationToDto(publication))
                .owner(isEqual(forUser, publication.getOwner()))
                .roles(exists(channelUser) ? channelUser.getPublicationRoles() : Collections.emptySet())
                .build();
    }

    public List<PublicationUserAssociationDTO> mapPublicationUserAssociationsToDtos(List<ObjectPair<Publication, ChannelUser>> associations, User forUser) {
        if (isEmptyOrNull(associations)) {
            return Collections.emptyList();
        }

        List<PublicationUserAssociationDTO> results = new ArrayList<>(associations.size());
        for (ObjectPair<Publication, ChannelUser> entry : associations) {
            results.add(mapPublicationUserAssociationToDto(entry.getOne(), entry.getTwo(), forUser));
        }

        return results;
    }
}
