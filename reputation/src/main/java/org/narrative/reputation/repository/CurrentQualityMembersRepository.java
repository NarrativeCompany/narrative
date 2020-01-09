package org.narrative.reputation.repository;

import org.narrative.reputation.model.entity.CurrentQualityMembersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentQualityMembersRepository extends JpaRepository<CurrentQualityMembersEntity, Integer> {
}
