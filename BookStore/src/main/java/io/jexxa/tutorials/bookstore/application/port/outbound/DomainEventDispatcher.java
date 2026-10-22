package io.jexxa.tutorials.bookstore.application.port.outbound;

import java.util.function.Consumer;

public interface DomainEventDispatcher {
    <T> void publish(T domainEvent);

    <T> void subscribe(Class<T> domainEvent, Consumer<T> subscriber);

    void subscribe(Consumer<Object> subscriber);
}
