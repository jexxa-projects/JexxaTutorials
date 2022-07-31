package io.jexxa.tutorials.bookstore.domainservice;


import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.tutorials.bookstore.domain.DomainEventPublisher;

@DomainService
public class DomainEventService
{
    private final IDomainEventSender domainEventSender;
    public DomainEventService(IDomainEventSender domainEventSender)
    {
        this.domainEventSender = domainEventSender;
    }

    public void init()
    {
        DomainEventPublisher.subscribe(Object.class, domainEventSender::handleEvent);
    }
}
