package io.jexxa.tutorials.bookstore.applicationservice;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.bookstore.domain.book.Book;
import io.jexxa.tutorials.bookstore.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;

import java.util.List;

import static io.jexxa.tutorials.bookstore.domain.book.Book.newBook;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;

@SuppressWarnings("unused")
@ApplicationService
public class BookStoreService
{
    private final BookRepository bookRepository;

    public BookStoreService (BookRepository bookRepository)
    {
        this.bookRepository = bookRepository;
    }

    public void addToStock(String isbn13, int amount)
    {
        var validatedISBN = createISBN(isbn13);

        var result = bookRepository.search(validatedISBN);
        if (result.isEmpty())
        {
            bookRepository.add(newBook(validatedISBN));
        }

        var book = bookRepository.get(validatedISBN);

        book.addToStock(amount);

        bookRepository.update(book);
    }


    public boolean inStock(String isbn13)
    {
        return inStock(createISBN(isbn13));
    }

    boolean inStock(ISBN13 isbn13)
    {
        return bookRepository
                .search(isbn13)
                .map(Book::inStock)
                .orElse(false);
    }

    public int amountInStock(String isbn13)
    {
        return amountInStock(createISBN(isbn13));
    }

    int amountInStock(ISBN13 isbn13)
    {
        return bookRepository
                .search(isbn13)
                .map(Book::amountInStock)
                .orElse(0);
    }

    public void sell(String isbn13) throws BookNotInStockException
    {
        sell(createISBN(isbn13));
    }

    void sell(ISBN13 isbn13) throws BookNotInStockException
    {
        var book = bookRepository
                .search(isbn13)
                .orElseThrow(BookNotInStockException::new);

        book.sell();

        bookRepository.update(book);
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
