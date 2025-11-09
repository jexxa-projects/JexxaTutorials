package io.jexxa.tutorials.bookstorecn.integration;

import io.jexxa.esp.EventBinding;
import io.jexxa.jexxatest.JexxaIntegrationTest;
import io.jexxa.jexxatest.integrationtest.rest.RESTBinding;
import io.jexxa.tutorials.bookstorecn.BookStoreCN;
import io.jexxa.tutorials.bookstorecn.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstorecn.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstorecn.domain.book.ISBN13;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static io.jexxa.tutorials.bookstorecn.domain.book.ISBN13.createISBN;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BookstoreCNIT
{
    private static final String ADD_TO_STOCK = "addToStock";
    private static final String AMOUNT_IN_STOCK = "amountInStock";
    private static final String SELL = "sell";
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    private static JexxaIntegrationTest jexxaIntegrationTest;  // Simplified IT testing with jexxa-test
    private static RESTBinding restBinding;                    // Binding to access the application under test via REST
    private static EventBinding eventBinding;                    // Binding to access the application under test via REST

    @BeforeAll
    static void initBeforeAll()
    {
        jexxaIntegrationTest = new JexxaIntegrationTest(BookStoreCN.class);
        restBinding = jexxaIntegrationTest.getBinding(RESTBinding.class);
        eventBinding = jexxaIntegrationTest.getBinding(EventBinding.class);
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
        var result = new ArrayList<BookSoldOut>();

        var bookStoreService = restBinding.getRESTHandler(BookStoreService.class);
        eventBinding.getListener("BookStore", result::add, String.class, BookSoldOut.class);

        bookStoreService.postRequest(Void.class, ADD_TO_STOCK, ANY_BOOK, 5);
        var inStock = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Act - Sell all books in stock
        for (int i = 0; i < inStock; ++i)
        {
            bookStoreService.postRequest(Void.class, SELL, ANY_BOOK);
        }

        // Receive the event
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> !result.isEmpty());


        //Assert
        assertFalse(result.isEmpty());
        assertEquals(ANY_BOOK, result.getFirst().isbn13());
    }

    @AfterAll
    static void shutDown()
    {
        jexxaIntegrationTest.shutDown();
    }
}
