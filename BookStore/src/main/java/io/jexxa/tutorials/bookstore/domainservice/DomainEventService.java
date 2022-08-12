package io.jexxa.tutorials.bookstore.domainservice;


import io.jexxa.addend.applicationcore.DomainEventHandler;
import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.tutorials.bookstore.domain.DomainEventPublisher;

@DomainService
public class DomainEventService
{
    private final DomainEventSender domainEventSender;
    public DomainEventService(DomainEventSender domainEventSender)
    {
        this.domainEventSender = domainEventSender;
    }

    public void registerListener()
    {
        DomainEventPublisher.subscribe(this::handleEvent);
    }

    @DomainEventHandler
    public void handleEvent(Object domainEvent)
    {
        domainEventSender.publish(domainEvent);
    }
}
