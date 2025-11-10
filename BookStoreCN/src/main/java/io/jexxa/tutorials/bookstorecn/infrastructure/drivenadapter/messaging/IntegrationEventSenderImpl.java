package io.jexxa.tutorials.bookstorecn.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.esp.drivenadapter.EventSender;
import io.jexxa.tutorials.bookstorecn.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstorecn.domainservice.IntegrationEventSender;

import java.util.Properties;

import static io.jexxa.esp.drivenadapter.EventSenderFactory.createEventSender;


@SuppressWarnings("unused")
@DrivenAdapter
public class IntegrationEventSenderImpl implements IntegrationEventSender {
    private final EventSender eventSender;

    public IntegrationEventSenderImpl(Properties properties)
    {
        // Request an EventSender and configure it to given properties
        eventSender = createEventSender(IntegrationEventSender.class, properties);
    }

    @Override
    public void publish(BookSoldOut domainEvent)
    {
        // For publishing a DomainEvent, we use a fluent API in Jexxa
        eventSender
                // In contrast to messaging, it is highly recommended to use a key, typically the aggregateID
                .send(domainEvent)
                .toTopic("BookStore")
                .addHeader("Type", domainEvent.getClass().getSimpleName())
                .asJSON();
    }
}
