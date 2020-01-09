package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.util.ApplicationError;
import org.narrative.config.StaticConfig;
import org.narrative.network.customizations.narrative.service.api.model.ErrorType;
import org.narrative.network.customizations.narrative.service.api.model.ExpiredPublicationDetailDTO;
import org.narrative.network.customizations.narrative.service.mapper.PublicationMapper;

import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 2019-09-26
 * Time: 14:09
 *
 * @author jonmark
 */
public class ExpiredPublicationError extends ApplicationError {
    private final Publication publication;

    public ExpiredPublicationError(Publication publication) {
        super(wordlet("expiredPublicationError.message"));
        assert publication.getStatusResolved().isExpired() : "Should only ever use this for expired publications, not/"+publication.getStatusResolved();

        this.publication = publication;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.EXPIRED_PUBLICATION;
    }

    @Override
    public Object getErrorDetailObject() {
        // jw: to ensure that the error has everything it needs to render we should include the PublicationDTO, and for
        //     that we need the mapper.
        PublicationMapper mapper = StaticConfig.getBean(PublicationMapper.class);

        return ExpiredPublicationDetailDTO.builder()
                .publication(mapper.mapPublicationToDto(publication))
                .deletionDatetime(publication.getDeletionDatetime())
                // jw: also need to include whether the current user is the owner so we can give the upsell in the UI
                .owner(publication.getOwner().isCurrentUserThisUser())
                .build();
    }
}
