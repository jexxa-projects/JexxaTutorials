package io.jexxa.tutorials.bookstore.domainservice;

import io.jexxa.addend.applicationcore.DomainService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@DomainService
public final class DomainEventDispatcher  {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new ConcurrentHashMap<>();
    private static final DomainEventDispatcher DOMAIN_EVENT_PUBLISHER = new DomainEventDispatcher();

    public static DomainEventDispatcher instance()
    {
        return DOMAIN_EVENT_PUBLISHER;
    }

    @SuppressWarnings("unchecked") // We check if the given domainEvent is assignable to a listener. Therefore, the unchecked cast is safe
    public synchronized <T> void publish(final T domainEvent)
    {
        instance()
                .subscribers
                .entrySet()
                .stream()
                .filter(element -> element.getKey().isAssignableFrom(domainEvent.getClass()))
                .flatMap(element -> element.getValue().stream())
                .forEach(element -> ((Consumer<T>) element).accept(domainEvent));
    }

    public synchronized <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber)
    {
        instance().subscribers.putIfAbsent(domainEvent, new HashSet<>());
        instance().subscribers.get(domainEvent).add(subscriber);
    }

    public synchronized void subscribe(Consumer<Object> subscriber)
    {
        instance().subscribe(Object.class, subscriber);
    }

}
