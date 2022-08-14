package io.jexxa.tutorials.contractmanagement.domain;

import io.jexxa.addend.applicationcore.Observer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
@Observer
public final class DomainEventPublisher {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new HashMap<>();
    private static final DomainEventPublisher instance = new DomainEventPublisher();

    public static DomainEventPublisher instance() {
        return instance;
    }

    @SuppressWarnings("unchecked") // we check whether the given domainEvent is assignable to listener, or not. Therefore, the unchecked cast is safe
    public static <T> void publish(final T domainEvent)
    {
        instance()
                .subscribers
                .entrySet()
                .stream()
                .filter(element -> element.getKey().isAssignableFrom(domainEvent.getClass()))
                .flatMap(element -> element.getValue().stream())
                .forEach(element -> ((Consumer<T>) element).accept(domainEvent));
    }

    public static <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber)
    {
        instance().subscribers.putIfAbsent(domainEvent, new HashSet<>());
        instance().subscribers.get(domainEvent).add(subscriber);
    }

    public static void subscribe(Consumer<Object> subscriber)
    {
        subscribe(Object.class, subscriber);
    }

    private DomainEventPublisher()
    {
        //Private constructor
    }
}
