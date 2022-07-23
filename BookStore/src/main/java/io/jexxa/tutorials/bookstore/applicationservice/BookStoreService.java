package io.jexxa.tutorials.bookstore.applicationservice;

import io.jexxa.addend.applicationcore.ApplicationService;
import io.jexxa.tutorials.bookstore.domain.book.Book;
import io.jexxa.tutorials.bookstore.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstore.domain.book.IBookRepository;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;

import java.util.List;
import java.util.Objects;

import static io.jexxa.tutorials.bookstore.domain.book.Book.newBook;

@SuppressWarnings("unused")
@ApplicationService
public class BookStoreService
{
    private final IBookRepository ibookRepository;

    public BookStoreService (IBookRepository ibookRepository)
    {
        this.ibookRepository = Objects.requireNonNull(ibookRepository);
    }

    public void addToStock(String isbn13, int amount)
    {
        var validatedISBN = new ISBN13(isbn13);

        var result = ibookRepository.search(validatedISBN);
        if (result.isEmpty())
        {
            ibookRepository.add(newBook(validatedISBN));
        }

        var book = ibookRepository.get(validatedISBN);

        book.addToStock(amount);

        ibookRepository.update(book);
    }


    public boolean inStock(String isbn13)
    {
        return inStock(new ISBN13(isbn13));
    }

    boolean inStock(ISBN13 isbn13)
    {
        return ibookRepository
                .search(isbn13)
                .map(Book::inStock)
                .orElse(false);
    }

    public int amountInStock(String isbn13)
    {
        return amountInStock(new ISBN13(isbn13));
    }

    int amountInStock(ISBN13 isbn13)
    {
        return ibookRepository
                .search(isbn13)
                .map(Book::amountInStock)
                .orElse(0);
    }

    public void sell(String isbn13) throws BookNotInStockException
    {
        sell(new ISBN13(isbn13));
    }

    void sell(ISBN13 isbn13) throws BookNotInStockException
    {
        var book = ibookRepository
                .search(isbn13)
                .orElseThrow(BookNotInStockException::new);

        book.sell();

        ibookRepository.update(book);
    }

    public List<ISBN13> getBooks()
    {
        return ibookRepository
                .getAll()
                .stream()
                .map(Book::getISBN13)
                .toList();
    }

}
