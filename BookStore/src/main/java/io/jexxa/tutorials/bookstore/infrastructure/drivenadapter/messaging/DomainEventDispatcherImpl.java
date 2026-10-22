package io.jexxa.tutorials.bookstore.infrastructure.drivenadapter.messaging;

import io.jexxa.addend.infrastructure.DrivenAdapter;
import io.jexxa.tutorials.bookstore.application.port.outbound.DomainEventDispatcher;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@DrivenAdapter
public final class DomainEventDispatcherImpl implements DomainEventDispatcher {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> void publish(final T domainEvent)
    {
        subscribers
                .entrySet()
                .stream()
                .filter(element -> element.getKey().isAssignableFrom(domainEvent.getClass()))
                .flatMap(element -> element.getValue().stream())
                .forEach(element -> ((Consumer<T>) element).accept(domainEvent));
    }

    @Override
    public synchronized <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber)
    {
        subscribers.putIfAbsent(domainEvent, new HashSet<>());
        subscribers.get(domainEvent).add(subscriber);
    }

    @Override
    public synchronized void subscribe(Consumer<Object> subscriber)
    {
        subscribe(Object.class, subscriber);
    }

}
