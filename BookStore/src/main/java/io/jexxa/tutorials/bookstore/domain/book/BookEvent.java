package io.jexxa.tutorials.bookstore.domain.book;


import io.jexxa.addend.applicationcore.DomainEvent;

import java.util.UUID;

@DomainEvent
public sealed interface BookEvent {
    UUID eventId();
    ISBN13 isbn13();

    @DomainEvent
    record BookSold(UUID eventId, ISBN13 isbn13)    implements BookEvent {}
    @DomainEvent
    record BookSoldOut(UUID eventId, ISBN13 isbn13) implements BookEvent {}

    static BookSoldOut bookSoldOut(UUID eventId, ISBN13 isbn13) { return new BookSoldOut(eventId, isbn13); }
    static BookSold bookSold(UUID eventId, ISBN13 isbn13)       { return new BookSold(eventId, isbn13); }
}

