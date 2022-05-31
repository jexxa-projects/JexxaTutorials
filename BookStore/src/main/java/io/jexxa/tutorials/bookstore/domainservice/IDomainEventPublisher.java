package io.jexxa.tutorials.bookstore.domainservice;

import io.jexxa.addend.applicationcore.InfrastructureService;

@InfrastructureService
public interface IDomainEventPublisher
{
    void publish(Object domainEvent);
}
