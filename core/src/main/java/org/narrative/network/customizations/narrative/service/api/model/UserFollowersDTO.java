package org.narrative.network.customizations.narrative.service.api.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.narrative.network.customizations.narrative.serialization.jackson.annotation.JsonValueObject;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Date: 2019-03-23
 * Time: 12:44
 *
 * @author jonmark
 */
@JsonValueObject
@JsonTypeName("UserFollowers")
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserFollowersDTO extends FollowsBase<FollowedUserDTO> {
    private final int totalFollowers;

    @Builder(toBuilder = true)
    public UserFollowersDTO(List<FollowedUserDTO> items, boolean hasMoreItems, FollowScrollParamsDTO scrollParams, int totalFollowers) {
        super(items, hasMoreItems, scrollParams);
        this.totalFollowers = totalFollowers;
    }
}
