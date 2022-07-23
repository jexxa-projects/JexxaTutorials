package io.jexxa.tutorials.bookstore.domain.book;

import io.jexxa.addend.applicationcore.DomainEvent;

@DomainEvent
public record BookSoldOut(ISBN13 isbn13)
{
    public static BookSoldOut bookSoldOut(ISBN13 isbn13)
    {
        return new BookSoldOut(isbn13);
    }
}
