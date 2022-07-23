package io.jexxa.tutorials.bookstore.domainservice;

import io.jexxa.tutorials.bookstore.domain.common.DomainEventPublisher;
import io.jexxa.tutorials.bookstore.domain.common.DomainEventSubscriber;

public class DomainEventService implements DomainEventSubscriber<Object> {

    private final IDomainEventPublisher iDomainEventPublisher;

    public DomainEventService(IDomainEventPublisher iDomainEventPublisher)
    {
        this.iDomainEventPublisher = iDomainEventPublisher;
    }

    public void init()
    {
        DomainEventPublisher.instance().subscribe(this);
    }

    @Override
    public void handleEvent(Object aDomainEvent)
    {
        iDomainEventPublisher.publish(aDomainEvent);
    }

    @Override
    public Class<Object> subscribedToEventType() {
        return Object.class;
    }
}
