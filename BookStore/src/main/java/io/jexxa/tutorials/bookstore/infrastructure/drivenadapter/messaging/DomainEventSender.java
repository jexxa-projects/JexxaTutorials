package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.applicationcore.DomainEvent;
import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSender;
import io.jexxa.tutorials.bookstore.domainservice.IDomainEventSender;

import java.util.Objects;
import java.util.Properties;

import static io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager.getMessageSender;

@SuppressWarnings("unused")
@DrivenAdapter
public class DomainEventSender implements IDomainEventSender {
    private final MessageSender messageSender;

    public DomainEventSender(Properties properties)
    {
        messageSender = getMessageSender(IDomainEventSender.class, properties);
    }


    private void validateDomainEvent(Object domainEvent)
    {
        Objects.requireNonNull(domainEvent);
        if ( domainEvent.getClass().getAnnotation(DomainEvent.class) == null )
        {
            throw new IllegalArgumentException("Given object is not annotated with @DomainEvent");
        }
    }

    @Override
    public void handleEvent(Object domainEvent)
    {
        validateDomainEvent(domainEvent);
        messageSender
                .send(domainEvent)
                .toTopic("BookStoreTopic")
                .asJson();
    }
}
