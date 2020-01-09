package org.narrative.reputation;

import org.narrative.batch.model.BatchEntityMarker;
import org.narrative.batch.repository.BatchRepositoryMarker;
import org.narrative.reputation.model.entity.EntityMarker;
import org.narrative.reputation.repository.RepositoryMarker;
import org.narrative.shared.jpa.type.TypeMarker;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan( basePackageClasses = {EntityMarker.class, BatchEntityMarker.class, TypeMarker.class})
@EnableJpaRepositories(basePackageClasses = {RepositoryMarker.class, BatchRepositoryMarker.class})
@EnableIntegration
@EnableScheduling
@EnableBatchProcessing
public class NarrativeReputationApplication {
    public static void main(String[] args) {
        SpringApplication.run(NarrativeReputationApplication.class, args);
    }
}
