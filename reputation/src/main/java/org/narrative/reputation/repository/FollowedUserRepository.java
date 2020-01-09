package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.FollowedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowedUserRepository extends JpaRepository<FollowedUserEntity, FollowedUserEntity> {
}
