package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.messaging;

import io.jexxa.adapters.core.messaging.InProcessPubSub;
import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.tutorials.bookstore.application.ports.DomainEventSender;

import java.util.function.Consumer;

@SuppressWarnings("unused")
@DrivenAdapter
public final class DomainEventSenderImpl implements DomainEventSender {
    private final InProcessPubSub messageBus = new InProcessPubSub();

    @Override
    public <T> void publish(final T domainEvent)
    {
        messageBus.publish(domainEvent);
    }

    public synchronized <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber)
    {
        messageBus.subscribe(domainEvent, subscriber);
    }

    public synchronized void subscribe(Consumer<Object> subscriber)
    {
        messageBus.subscribe(subscriber);
    }

}
