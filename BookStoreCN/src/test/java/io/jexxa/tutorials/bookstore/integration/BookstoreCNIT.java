package io.jexxa.tutorials.bookstore.integration;

import io.jexxa.common.drivingadapter.messaging.jms.JMSConfiguration;
import io.jexxa.jexxatest.JexxaIntegrationTest;
import io.jexxa.jexxatest.integrationtest.messaging.MessageBinding;
import io.jexxa.jexxatest.integrationtest.rest.RESTBinding;
import io.jexxa.tutorials.bookstore.BookStoreCN;
import io.jexxa.tutorials.bookstore.applicationservice.BookStoreService;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BookstoreCNIT
{
    private static final String ADD_TO_STOCK = "addToStock";
    private static final String AMOUNT_IN_STOCK = "amountInStock";
    private static final String SELL = "sell";
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );


    private static JexxaIntegrationTest jexxaIntegrationTest;  // Simplified IT testing with jexxa-test
    private static RESTBinding restBinding;                    // Binding to access application under test via REST
    private static MessageBinding messageBinding;              // Binding to access application under test via JMS

    @BeforeAll
    static void initBeforeAll()
    {
        jexxaIntegrationTest = new JexxaIntegrationTest(BookStoreCN.class);
        messageBinding = jexxaIntegrationTest.getMessageBinding();
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
        var messageListener = messageBinding.getMessageListener("BookStore", JMSConfiguration.MessagingType.TOPIC);

        bookStoreService.postRequest(Void.class, ADD_TO_STOCK, ANY_BOOK, 5);
        var inStock = bookStoreService.postRequest(Integer.class, AMOUNT_IN_STOCK, ANY_BOOK );

        //Act - Sell all books in stock
        for (int i = 0; i < inStock; ++i)
        {
            bookStoreService.postRequest(Void.class, SELL, ANY_BOOK);
        }

        // Receive the jms message
        var result = messageListener
                .awaitMessage(5, TimeUnit.SECONDS)
                .pop(BookSoldOut.class);

        //Assert
        assertEquals(ANY_BOOK, result.isbn13());
    }

    @AfterAll
    static void shutDown()
    {
        jexxaIntegrationTest.shutDown();
    }
}
