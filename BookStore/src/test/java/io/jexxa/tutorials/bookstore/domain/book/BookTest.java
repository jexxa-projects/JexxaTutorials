package io.jexxa.tutorials.bookstore.domain.book;

import org.junit.jupiter.api.Test;

import static io.jexxa.tutorials.bookstore.domain.book.Book.newBook;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    @Test
    void addToStock() {
        // Arrange
        var objectUnderTest = newBook(ANY_BOOK);
        var amountInStock = 5;

        // Act
        objectUnderTest.addToStock(amountInStock);

        // Assert
        assertEquals(amountInStock, objectUnderTest.amountInStock());
        assertTrue(objectUnderTest.inStock());
    }

    @Test
    void sell() throws BookNotInStockException {
        // Arrange
        var objectUnderTest = newBook(ANY_BOOK);
        var amountInStock = 5;
        objectUnderTest.addToStock(amountInStock);

        // Act / Assert
        var result = objectUnderTest.sell();


        // Assert
        assertEquals(amountInStock - 1, objectUnderTest.amountInStock());
        assertInstanceOf(BookEvent.BookSold.class, result);
        assertTrue(objectUnderTest.inStock());
    }

    @Test
    void sellOutOfStock() {
        // Arrange
        var objectUnderTest = newBook(ANY_BOOK);

        // Act / Assert
        assertThrows( BookNotInStockException.class, objectUnderTest::sell);
    }

    @Test
    void sellLastBook() throws BookNotInStockException {
        // Arrange
        var amountInStock = 1;
        var objectUnderTest = newBook(ANY_BOOK);
        objectUnderTest.addToStock(amountInStock);

        // Act / Assert
        var result = objectUnderTest.sell();

        // Assert
        var soldOutEvent = assertInstanceOf(BookEvent.BookSoldOut.class, result);
        assertEquals(ANY_BOOK, soldOutEvent.isbn13());
        assertEquals(0, objectUnderTest.amountInStock() );
    }


}