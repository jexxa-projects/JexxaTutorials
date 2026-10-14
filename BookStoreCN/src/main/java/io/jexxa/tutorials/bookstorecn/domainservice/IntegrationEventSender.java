package io.jexxa.tutorials.bookstorecn.domainservice;

import io.jexxa.addend.applicationcore.InfrastructureService;
import io.jexxa.tutorials.bookstorecn.domain.book.BookEvent;

@InfrastructureService
public interface IntegrationEventSender
{
    void publish(BookEvent domainEvent);
}
