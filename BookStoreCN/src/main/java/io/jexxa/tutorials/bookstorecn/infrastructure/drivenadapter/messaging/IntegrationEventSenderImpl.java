package io.jexxa.tutorials.bookstorecn.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.applicationcore.DomainEvent;
import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.esp.drivenadapter.EventSender;
import io.jexxa.tutorials.bookstorecn.domainservice.IntegrationEventSender;

import java.util.Objects;
import java.util.Properties;

import static io.jexxa.esp.drivenadapter.EventSenderFactory.createEventSender;


@SuppressWarnings("unused")
@DrivenAdapter
public class IntegrationEventSenderImpl implements IntegrationEventSender {
    private final EventSender eventSender;

    public IntegrationEventSenderImpl(Properties properties)
    {
        // Request a MessageSender from the framework, so that we can configure it in our properties file
        eventSender = createEventSender(IntegrationEventSender.class, properties);
    }

    @Override
    public void publish(Object domainEvent)
    {
        // We just allow sending DomainEvents
        validateDomainEvent(domainEvent);

        // For publishing a DomainEvent, we use a fluent API in Jexxa
        eventSender
                .send(domainEvent)
                .toTopic("BookStore")
                .addHeader("Type", domainEvent.getClass().getSimpleName())
                .asJSON();
    }

    private void validateDomainEvent(Object domainEvent)
    {
        Objects.requireNonNull(domainEvent);
        if ( domainEvent.getClass().getAnnotation(DomainEvent.class) == null )
        {
            throw new IllegalArgumentException("Given object is not annotated with @DomainEvent");
        }
    }
}
