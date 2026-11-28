package io.jexxa.tutorials.bookstore.application.ports;

import io.jexxa.addend.applicationcore.InfrastructureService;

@InfrastructureService
public interface IntegrationEventSender
{
    void publish(Object domainEvent);
}
