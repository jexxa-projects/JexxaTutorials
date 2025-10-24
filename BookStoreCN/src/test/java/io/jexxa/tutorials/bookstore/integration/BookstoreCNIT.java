package io.jexxa.tutorials.bookstore.integration;

import io.jexxa.esp.digispine.DigiSpine;
import io.jexxa.jexxatest.JexxaIntegrationTest;
import io.jexxa.jexxatest.integrationtest.rest.RESTBinding;
import io.jexxa.tutorials.bookstore.BookStoreCN;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookstoreCNIT
{
    private static final String ADD_TO_STOCK = "addToStock";
    private static final String AMOUNT_IN_STOCK = "amountInStock";
    private static final String SELL = "sell";
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    private static JexxaIntegrationTest jexxaIntegrationTest;  // Simplified IT testing with jexxa-test
    private static RESTBinding restBinding;                    // Binding to access application under test via REST

    @BeforeAll
    static void initBeforeAll()
    {
        jexxaIntegrationTest = new JexxaIntegrationTest(BookStoreCN.class);
        restBinding = jexxaIntegrationTest.getRESTBinding();
    }


    @Test
    void testStartupApplication()
    {
        //Arrange -
        var boundedContext = restBinding.getBoundedContext();

        //Act / Assert
        var result = boundedContext.contextName();

        //Assert
        assertEquals(BookStoreCN.class.getSimpleName(), result);
    }

    @Test
    void testAddBook()
    {
        //Arrange
        var bookStoreService = restBinding.getRESTHandler(BookStoreService.class);

        var addedBooks = 5;
        var inStock = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Act
        bookStoreService.postRequest(Void.class, ADD_TO_STOCK, ANY_BOOK, addedBooks);
        var result = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Assert
        assertEquals(inStock + addedBooks, result);
    }

    @Test
    void testSellLastBook()
    {
        //Arrange
        var bookStoreService = restBinding.getRESTHandler(BookStoreService.class);

        bookStoreService.postRequest(Void.class, ADD_TO_STOCK, ANY_BOOK, 5);
        var inStock = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Act - Sell all books in stock
        for (int i = 0; i < inStock; ++i)
        {
            bookStoreService.postRequest(Void.class, SELL, ANY_BOOK);
        }

        // Receive the jms message
      //  var result = digispine.latestMessageFromJSON("BookStore", Duration.of(5, ChronoUnit.SECONDS), BookSoldOut.class);

        //Assert
      //  assertTrue(result.isPresent());
      //  assertEquals(ANY_BOOK, result.get().isbn13());
    }

    @AfterAll
    static void shutDown()
    {
        jexxaIntegrationTest.shutDown();
    }
}
