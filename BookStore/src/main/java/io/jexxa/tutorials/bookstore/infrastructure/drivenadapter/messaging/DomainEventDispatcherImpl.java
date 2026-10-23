package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.common.messaging.InProcessPubSub;
import io.jexxa.tutorials.bookstore.application.port.outbound.DomainEventDispatcher;

import java.util.function.Consumer;

@DrivenAdapter
public final class DomainEventDispatcherImpl implements DomainEventDispatcher {
    private final InProcessPubSub messageBus = new InProcessPubSub();

    @Override
    public <T> void publish(final T domainEvent)
    {
        messageBus.publish(domainEvent);
    }

    @Override
    public synchronized <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber)
    {
        messageBus.subscribe(domainEvent, subscriber);
    }

    @Override
    public synchronized void subscribe(Consumer<Object> subscriber)
    {
        messageBus.subscribe(subscriber);
    }

}
