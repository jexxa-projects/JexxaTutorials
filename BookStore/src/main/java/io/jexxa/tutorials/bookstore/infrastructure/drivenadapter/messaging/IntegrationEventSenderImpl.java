package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.applicationcore.DomainEvent;
import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSender;
import io.jexxa.tutorials.bookstore.domainservice.IntegrationEventSender;

import java.util.Objects;
import java.util.Properties;

import static io.jexxa.infrastructure.drivenadapterstrategy.messaging.MessageSenderManager.getMessageSender;

@SuppressWarnings("unused")
@DrivenAdapter
public class IntegrationEventSenderImpl implements IntegrationEventSender {
    private final MessageSender messageSender;

    public IntegrationEventSenderImpl(Properties properties)
    {
        messageSender = getMessageSender(IntegrationEventSender.class, properties);
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
    public void sendEvent(Object domainEvent)
    {
        validateDomainEvent(domainEvent);
        messageSender
                .send(domainEvent)
                .toTopic("BookStoreTopic")
                .asJson();
    }
}
