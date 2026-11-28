package io.jexxa.tutorials.bookstore.application.services;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.bookstore.application.ports.DomainEventSender;
import io.jexxa.tutorials.bookstore.application.ports.IntegrationEventSender;
import io.jexxa.tutorials.bookstore.domain.book.Book;
import io.jexxa.tutorials.bookstore.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;

import java.util.List;

import static io.jexxa.tutorials.bookstore.domain.book.Book.newBook;

@SuppressWarnings("unused")
@ApplicationService
public class BookStoreService
{
    private final BookRepository bookRepository;
    private final DomainEventSender domainEventSender;
    private final IntegrationEventSender integrationEventSender;

    /**
     * Forward domain events to the integration messaging adapter.
     * Note: Production systems may require explicit event selection,
     *  mapping, versioning, and reliable delivery. That's why we provide an DomainEventSender (for internal)
     *  and an IntegrationEventSender (for externally) sending messages
     */
    public BookStoreService(BookRepository bookRepository, DomainEventSender domainEventSender, IntegrationEventSender  integrationEventSender)
    {
        this.bookRepository = bookRepository;
        this.domainEventSender = domainEventSender;
        this.integrationEventSender = integrationEventSender;
    }

    public void registerBook(ISBN13 isbn13)
    {
        addToStock(isbn13, 0);
    }

    public void addToStock(ISBN13 isbn13, int amount)
    {
        if (!bookRepository.isRegistered(isbn13))
        {
            bookRepository.add(newBook(isbn13));
        }

        var book = bookRepository.get(isbn13);

        book.addToStock(amount);

        bookRepository.update(book);
    }


    public boolean inStock(ISBN13 isbn13)
    {
        return bookRepository
                .search(isbn13)
                .map(Book::inStock)
                .orElse(false);
    }

    public int amountInStock(ISBN13 isbn13)
    {
        return bookRepository
                .search(isbn13)
                .map(Book::amountInStock)
                .orElse(0);
    }

    public void removeFromStock(ISBN13 isbn13)
    {
        bookRepository.remove(isbn13);
    }

    public void sell(ISBN13 isbn13) throws BookNotInStockException
    {
        var book = bookRepository
                .search(isbn13)
                .orElseThrow(BookNotInStockException::new);

        var bookEvent = book.sell();

        bookRepository.update(book);
        domainEventSender.publish(bookEvent);
        integrationEventSender.publish(bookEvent);
    }

    public List<ISBN13> getBooks()
    {
        return bookRepository
                .getAll()
                .stream()
                .map(Book::getISBN13)
                .toList();
    }
}
