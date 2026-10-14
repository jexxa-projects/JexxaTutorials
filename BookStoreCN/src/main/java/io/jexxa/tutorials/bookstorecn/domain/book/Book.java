package io.jexxa.tutorials.bookstorecn.domain.book;

import io.jexxa.addend.applicationcore.Aggregate;
import io.jexxa.addend.applicationcore.AggregateFactory;
import io.jexxa.addend.applicationcore.AggregateID;

import java.util.UUID;

import static io.jexxa.tutorials.bookstorecn.domain.book.BookEvent.bookSold;
import static io.jexxa.tutorials.bookstorecn.domain.book.BookEvent.bookSoldOut;

@Aggregate
public final class Book
{
    private final ISBN13 isbn13;
    private int amountInStock = 0;

    private Book(ISBN13 isbn13)
    {
        this.isbn13 = isbn13;
    }

    @AggregateID
    public ISBN13 getISBN13()
    {
        return isbn13;
    }

    public boolean inStock()
    {
        return amountInStock > 0;
    }

    public int amountInStock()
    {
        return amountInStock;
    }

    public void addToStock( int amount )
    {
        amountInStock += amount;
    }

    public BookEvent sell() throws BookNotInStockException
    {
        if ( ! inStock() )
        {
            throw new BookNotInStockException();
        }

        amountInStock -= 1;

        if ( amountInStock <= 0 ) {
            return bookSoldOut(UUID.randomUUID(), isbn13);
        } else  {
            return bookSold(UUID.randomUUID(), isbn13);
        }

    }

    @AggregateFactory(Book.class)
    public static Book newBook(ISBN13 isbn13)
    {
        return new Book(isbn13);
    }
}
