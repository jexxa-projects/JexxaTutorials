package io.jexxa.tutorials.bookstorecn.applicationservice;

import io.jexxa.esp.EventStreamingPlatform;
import io.jexxa.jexxatest.JexxaTest;
import io.jexxa.tutorials.bookstorecn.BookStoreCN;
import io.jexxa.tutorials.bookstorecn.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstorecn.domain.book.BookRepository;
import io.jexxa.tutorials.bookstorecn.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstorecn.domain.book.ISBN13;
import io.jexxa.tutorials.bookstorecn.domainservice.IntegrationEventSender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static io.jexxa.jexxatest.JexxaTest.getJexxaTest;
import static io.jexxa.tutorials.bookstorecn.domain.DomainEventPublisher.subscribe;
import static io.jexxa.tutorials.bookstorecn.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookStoreCNServiceTest
{
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    private BookStoreService objectUnderTest;       // Object we want to test
    private BookRepository   bookRepository;        // Repository to validate results in the tests
    private static final EventStreamingPlatform digispine = new EventStreamingPlatform();

    @BeforeAll
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void initBeforeAll()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!

        getJexxaTest(BookStoreCN.class).getJexxaMain()
                .addProperties(digispine.kafkaProperties());

        getJexxaTest(BookStoreCN.class)
                .getJexxaMain()
                .bootstrap(IntegrationEventSender.class).with(sender -> subscribe(BookSoldOut.class, sender::publish));
    }

    @BeforeEach
    void initBeforeEach()
    {
        digispine.reset();
    }


    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        JexxaTest jexxaTest = getJexxaTest(BookStoreCN.class);

        // Request the objects needed for our tests
        objectUnderTest       = jexxaTest.getInstanceOfPort(BookStoreService.class);   // 1. We need the object we want to test
        bookRepository        = jexxaTest.getRepository(BookRepository.class);         // 2. Repository managing all books
    }

    @Test
    void addBooksToStock()
    {
        //Arrange
        var amount = 5;

        //Act
        objectUnderTest.addToStock(ANY_BOOK, amount);
        var result = digispine.latestMessageFromJSON("BookStore", Duration.of(500, ChronoUnit.MILLIS), BookSoldOut.class);

        //Assert
        assertEquals( amount, objectUnderTest.amountInStock(ANY_BOOK) );      // Perform assertion against the object we test
        assertEquals( amount, bookRepository.get(ANY_BOOK).amountInStock() ); // Perform assertion against the repository
        assertTrue( result.isEmpty() );                        // Perform assertion against published DomainEvents
    }


    @Test
    void sellBook()
    {
        //Arrange
        var amount = 5;
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Act / Assert
        assertDoesNotThrow(() -> objectUnderTest.sell(ANY_BOOK));
        var result = digispine.latestMessageFromJSON("BookStore", Duration.of(500, ChronoUnit.MILLIS), BookSoldOut.class);

        //Assert
        assertEquals( amount - 1, objectUnderTest.amountInStock(ANY_BOOK) );       // Perform assertion against the object we test
        assertEquals( amount - 1, bookRepository.get(ANY_BOOK).amountInStock() );  // Perform assertion against the repository
        assertTrue( result.isEmpty() );                                     // Perform assertion against published DomainEvents
    }

    @Test
    void sellBookNotInStock()
    {
        //Arrange - Nothing

        //Act/Assert
        assertThrows(BookNotInStockException.class, () -> objectUnderTest.sell(ANY_BOOK));
    }

    @Test
    void sellLastBook()
    {
        //Arrange
        objectUnderTest.addToStock(ANY_BOOK, 1);

        //Act
        assertDoesNotThrow(() -> objectUnderTest.sell(ANY_BOOK));

        // Receive the jms message
        var result = digispine.latestMessageFromJSON("BookStore", Duration.of(5, ChronoUnit.SECONDS), BookSoldOut.class);

        //Assert
        assertEquals( 0 , objectUnderTest.amountInStock(ANY_BOOK) );                    // Perform assertion against the object we test
        assertTrue(result.isPresent());
        assertEquals( ANY_BOOK, result.get().isbn13());  // Perform assertion against published DomainEvents
    }

}