package io.jexxa.tutorials.bookstore.domain.common;

public interface DomainEventSubscriber<T> {
    void handleEvent(Object aDomainEvent);

    Class<T> subscribedToEventType();
}
