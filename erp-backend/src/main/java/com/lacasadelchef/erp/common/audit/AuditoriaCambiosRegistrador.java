package com.lacasadelchef.erp.common.audit;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AuditoriaCambiosRegistrador {

    private final EntityManagerFactory entityManagerFactory;
    private final AuditoriaCambiosListener listener;

    @PostConstruct
    void registrar() {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        EventListenerRegistry registro = sessionFactory.getServiceRegistry().requireService(EventListenerRegistry.class);
        registro.appendListeners(EventType.POST_INSERT, listener);
        registro.appendListeners(EventType.POST_UPDATE, listener);
        registro.appendListeners(EventType.POST_DELETE, listener);
    }
}
