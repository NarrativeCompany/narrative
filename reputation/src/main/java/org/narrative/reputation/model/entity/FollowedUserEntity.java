package org.narrative.reputation.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@IdClass(FollowedUserEntity.class)
public class FollowedUserEntity implements Serializable {
    private static final long serialVersionUID = 6932466722860750791L;

    @Id
    private long followingUserOid;
    @Id
    private long followedUserOid;
}
