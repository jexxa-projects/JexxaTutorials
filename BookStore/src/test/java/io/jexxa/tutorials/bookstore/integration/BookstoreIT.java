package io.jexxa.tutorials.bookstore.integration;

import io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration;
import io.jexxa.jexxatest.JexxaIntegrationTest;
import io.jexxa.jexxatest.integrationtest.messaging.JMSBinding;
import io.jexxa.jexxatest.integrationtest.rest.RESTBinding;
import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.application.service.BookStoreService;
import io.jexxa.tutorials.bookstore.domain.book.BookEvent;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.stream.IntStream;

import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookstoreIT
{
    private static final String ADD_TO_STOCK = "addToStock";
    private static final String AMOUNT_IN_STOCK = "amountInStock";
    private static final String SELL = "sell";
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );


    private static JexxaIntegrationTest jexxaIntegrationTest;  // Simplified IT testing with jexxa-test
    private static RESTBinding restBinding;                    // Binding to access an application under test via REST
    private static JMSBinding jmsBinding;                      // Binding to access application under test via JMS

    @BeforeAll
    static void initBeforeAll()
    {
        jexxaIntegrationTest = new JexxaIntegrationTest(BookStore.class);
        jmsBinding = jexxaIntegrationTest.getBinding(JMSBinding.class);
        restBinding = jexxaIntegrationTest.getBinding(RESTBinding.class);
    }


    @Test
    void testStartupApplication()
    {
        //Arrange -
        var boundedContext = restBinding.getBoundedContext();

        //Act / Assert
        var result = boundedContext.contextName();

        //Assert
        assertEquals(BookStore.class.getSimpleName(), result);
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
        var messageListener = jmsBinding.getListener("BookStore", JMSConfiguration.MessagingType.TOPIC);

        bookStoreService.postRequest(Void.class, ADD_TO_STOCK, ANY_BOOK, 5);
        var inStock = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Act - Sell all books in stock
        for (int i = 0; i < inStock ; ++i)
        {
            bookStoreService.postRequest(Void.class, SELL, ANY_BOOK);
        }

        // Await 5 messages from JMS
        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(50)) // Prüft alle 50ms den Zustand
                .until(() -> messageListener.getAll().size() == inStock);

        //Validate first 4 of type BookSold.class
        IntStream.range(0, inStock - 1)
                .mapToObj(_ -> messageListener.pop(BookEvent.BookSold.class))
                .forEach(event -> assertEquals(ANY_BOOK, event.isbn13()));

        //Validate first 4 of type BookSoldOut.class
        var finalEvent = messageListener.pop(BookEvent.BookSoldOut.class);

        // Nutzt assertAll für ein schönes, gruppiertes Fehlerprotokoll
        assertAll("Finale Event validation",
                () -> assertNotNull(finalEvent),
                () -> assertEquals(ANY_BOOK, finalEvent.isbn13())
        );
    }

    @AfterAll
    static void shutDown()
    {
        jexxaIntegrationTest.shutDown();
    }
}
