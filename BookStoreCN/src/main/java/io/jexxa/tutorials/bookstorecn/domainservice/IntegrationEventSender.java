package io.jexxa.tutorials.bookstorecn.domainservice;

import io.jexxa.addend.applicationcore.InfrastructureService;
import io.jexxa.tutorials.bookstorecn.domain.book.BookSoldOut;

@InfrastructureService
public interface IntegrationEventSender
{
    void publish(BookSoldOut domainEvent);
}
