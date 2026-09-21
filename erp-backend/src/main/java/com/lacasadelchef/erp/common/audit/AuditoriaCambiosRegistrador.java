package com.lacasadelchef.erp.common.audit;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.stereotype.Component;

/**
 * Engancha {@link AuditoriaCambiosListener} al ciclo de eventos de Hibernate una vez
 * que Spring termino de construir el EntityManagerFactory. Es un bean aparte para que
 * el listener pueda ser un @Component normal (con logging e inyeccion) sin tener que
 * declararlo en hibernate.properties.
 */
@Component
@RequiredArgsConstructor
class AuditoriaCambiosRegistrador {

    private final EntityManagerFactory entityManagerFactory;
    private final AuditoriaCambiosListener listener;

    @PostConstruct
    void registrar() {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        sessionFactory.getServiceRegistry()
                .requireService(EventListenerRegistry.class)
                .appendListeners(EventType.POST_UPDATE, listener);
    }
}
