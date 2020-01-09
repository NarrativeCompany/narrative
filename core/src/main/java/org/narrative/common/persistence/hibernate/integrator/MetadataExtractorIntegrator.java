package org.narrative.common.persistence.hibernate.integrator;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Integrator to keep references to {@link Database} and {@link org.hibernate.boot.Metadata} for later introspection.
 */
public class MetadataExtractorIntegrator implements org.hibernate.integrator.spi.Integrator {
    private Metadata metadata;
    private Database database;

    @Override
    public void integrate(
            Metadata metadata,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {

        this.metadata = metadata;
        this.database = metadata.getDatabase();
    }

    @Override
    public void disintegrate(
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    }

    public Database getDatabase() {
        return database;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}