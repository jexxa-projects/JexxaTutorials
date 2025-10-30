package io.jexxa.tutorials.bookstore.domain.book;

import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.domain.DomainEventPublisher;
import org.junit.jupiter.api.Test;

import static io.jexxa.jexxatest.JexxaTest.getJexxaTest;
import static io.jexxa.tutorials.bookstore.domain.book.Book.newBook;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
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
        var jexxaTest = getJexxaTest(BookStore.class);
        var amountInStock = 1;
        var domainEventRecorder = jexxaTest.getDomainEventRecorder(BookSoldOut.class, DomainEventPublisher::subscribe);

        var objectUnderTest = newBook(ANY_BOOK);
        objectUnderTest.addToStock(amountInStock);

        // Act / Assert
        assertDoesNotThrow(objectUnderTest::sell);

        // Assert
        assertEquals(0, objectUnderTest.amountInStock() );
        assertEquals(1, domainEventRecorder.get().size() );
    }


}