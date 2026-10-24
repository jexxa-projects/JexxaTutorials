package io.jexxa.tutorials.bookstorecn.application.service;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.bookstorecn.application.port.outbound.DomainEventDispatcher;
import io.jexxa.tutorials.bookstorecn.domain.book.Book;
import io.jexxa.tutorials.bookstorecn.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstorecn.domain.book.BookRepository;
import io.jexxa.tutorials.bookstorecn.domain.book.ISBN13;


import java.util.List;

import static io.jexxa.tutorials.bookstorecn.domain.book.Book.newBook;


@SuppressWarnings("unused")
@ApplicationService
public class BookStoreService
{
    private final BookRepository bookRepository;
    private final DomainEventDispatcher domainEventDispatcher;

    public BookStoreService (BookRepository bookRepository, DomainEventDispatcher domainEventDispatcher)
    {
        this.bookRepository = bookRepository;
        this.domainEventDispatcher = domainEventDispatcher;
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
        domainEventDispatcher.publish(bookEvent);
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
