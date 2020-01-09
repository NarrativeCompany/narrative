package org.narrative.batch.repository;

import org.narrative.batch.model.BatchJobControlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobControlRepository extends JpaRepository<BatchJobControlEntity, String> {
}
