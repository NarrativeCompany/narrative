package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 2019-09-25
 * Time: 08:58
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("FollowedPublications")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FollowedPublicationsDTO extends FollowsBase<PublicationDTO> {
    @Builder
    public FollowedPublicationsDTO(List<PublicationDTO> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        super(items, hasMoreItems, scrollParams);
    }
}
