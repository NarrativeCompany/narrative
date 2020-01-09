package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 2019-03-22
 * Time: 20:14
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("FollowedUsers")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FollowedUsersDTO extends FollowsBase<FollowedUserDTO> {
    @Builder
    public FollowedUsersDTO(List<FollowedUserDTO> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams) {
        super(items, hasMoreItems, scrollParams);
    }
}
