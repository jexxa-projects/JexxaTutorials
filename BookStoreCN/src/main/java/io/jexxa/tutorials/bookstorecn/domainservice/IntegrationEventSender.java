package io.jexxa.tutorials.bookstorecn.domainservice;

import io.jexxa.addend.applicationcore.InfrastructureService;

@InfrastructureService
public interface IntegrationEventSender
{
    void publish(Object domainEvent);
}
