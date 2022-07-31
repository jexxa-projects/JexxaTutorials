package io.jexxa.tutorials.bookstore.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DomainEventPublisher {
    private final Map<Class<?>, Set<Consumer<?>>> subscribers = new HashMap<>();
    private static final DomainEventPublisher instance = new DomainEventPublisher();

    public static DomainEventPublisher instance() {
        return instance;
    }

    public static void reset()
    {
        instance().subscribers.clear();
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

    private DomainEventPublisher()
    {
        //Private constructor
    }
}
