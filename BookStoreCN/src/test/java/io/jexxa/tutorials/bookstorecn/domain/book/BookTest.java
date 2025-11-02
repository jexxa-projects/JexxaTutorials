package io.jexxa.tutorials.bookstorecn.domain.book;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;


import static io.jexxa.tutorials.bookstorecn.domain.DomainEventPublisher.subscribe;
import static io.jexxa.tutorials.bookstorecn.domain.book.Book.newBook;
import static io.jexxa.tutorials.bookstorecn.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void sell() {
        // Arrange
        var objectUnderTest = newBook(ANY_BOOK);
        var amountInStock = 5;
        objectUnderTest.addToStock(amountInStock);

        // Act / Assert
        assertDoesNotThrow(objectUnderTest::sell);

        // Assert
        assertEquals(amountInStock - 1, objectUnderTest.amountInStock());
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
    void sellLastBook() {
        // Arrange
        var amountInStock = 1;
        var domainEventRecorder = new DomainEventRecorder();
        subscribe(BookSoldOut.class, domainEventRecorder::receive);

        var objectUnderTest = newBook(ANY_BOOK);
        objectUnderTest.addToStock(amountInStock);

        // Act / Assert
        assertDoesNotThrow(objectUnderTest::sell);

        // Assert
        assertEquals(0, objectUnderTest.amountInStock() );
        assertEquals(1, domainEventRecorder.getDomainEvents().size() );
    }

    private static class DomainEventRecorder {
        private final List<BookSoldOut> domainEvents = new ArrayList<>();

        public void receive(BookSoldOut bookSoldOut)
        {
            domainEvents.add(bookSoldOut);
        }

        List<BookSoldOut> getDomainEvents()
        {
            return domainEvents;
        }
    }

}