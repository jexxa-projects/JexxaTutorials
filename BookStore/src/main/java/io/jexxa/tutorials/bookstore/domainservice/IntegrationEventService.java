package io.jexxa.tutorials.bookstore.domainservice;


import io.jexxa.addend.applicationcore.DomainEventHandler;
import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.tutorials.bookstore.domain.DomainEventPublisher;

@DomainService
public class IntegrationEventService
{
    private final IntegrationEventSender integrationEventSender;
    public IntegrationEventService(IntegrationEventSender integrationEventSender)
    {
        this.integrationEventSender = integrationEventSender;
    }

    public void init()
    {
        DomainEventPublisher.subscribe(this::handleEvent);
    }

    @DomainEventHandler
    public void handleEvent(Object domainEvent)
    {
        integrationEventSender.publish(domainEvent);
    }
}
