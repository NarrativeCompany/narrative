package org.narrative.common.persistence.hibernate.integrator;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.function.Consumer;

/**
 * Register listeners via {@link Consumer} with the supplied {@link ServiceRegistry}.
 */
public class ListenerIntegrator implements Integrator {
    private Consumer<ServiceRegistry> listenerRegistrationConsumer;

    public ListenerIntegrator(){};

    public ListenerIntegrator(Consumer<ServiceRegistry> listenerRegistrationConsumer) {
        this.listenerRegistrationConsumer = listenerRegistrationConsumer;
    }

    /**
     *
     * We need this for cases where the listener is not initialized at ListenerIntegrator construction time.
     *
     * @param listenerRegistrationConsumer The consumer function to use
     */
    public void setListenerRegistrationConsumer(Consumer<ServiceRegistry> listenerRegistrationConsumer) {
        this.listenerRegistrationConsumer = listenerRegistrationConsumer;
    }

    /**
     * Perform integration.
     *
     * @param metadata        The "compiled" representation of the mapping information
     * @param sessionFactory  The session factory being created
     * @param serviceRegistry The session factory's service registry
     */
    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
        if (listenerRegistrationConsumer == null) {
            throw new IllegalArgumentException("listenerRegistrationConsumer has not been bound");
        }
        listenerRegistrationConsumer.accept(serviceRegistry);
    }

    /**
     * Tongue-in-cheek name for a shutdown callback.
     *
     * @param sessionFactory  The session factory being closed.
     * @param serviceRegistry That session factory's service registry
     */
    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {

    }
}
