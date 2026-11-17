package io.jexxa.tutorials.bookstore.infrastructure.bootstrap;

import io.jexxa.tutorials.bookstore.application.service.BookStoreService;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;

import java.util.Objects;
import java.util.stream.Stream;

import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;


@SuppressWarnings("unused")
public class ReferenceLibrary
{
    private final BookStoreService bookStoreService;

    public ReferenceLibrary(BookStoreService bookStoreService)
    {
        this.bookStoreService = Objects.requireNonNull(bookStoreService);
        addLatestBooks();
    }

    public void addLatestBooks()
    {
        getLatestBooks()
                .forEach(bookStoreService::registerBook);
    }

    /**
     * Some Random books found in internet
     */
    private Stream<ISBN13> getLatestBooks()
    {
        return Stream.of(
                createISBN("978-1-60309-025-4"),
                createISBN("978-1-60309-047-6"),
                createISBN("978-1-60309-322-4"),
                createISBN("978-1-891830-85-3"),
                createISBN("978-1-60309-016-2"),
                createISBN("978-1-60309-265-4")
        );
    }
}
