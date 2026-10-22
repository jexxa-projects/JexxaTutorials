package io.jexxa.tutorials.bookstore.application.port.outbound;

import io.jexxa.addend.applicationcore.InfrastructureService;

@InfrastructureService
public interface IntegrationEventSender
{
    void publish(Object domainEvent);
}
