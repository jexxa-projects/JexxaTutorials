package io.jexxa.tutorials.bookstore.applicationservice;

import io.jexxa.jexxatest.JexxaTest;
import io.jexxa.jexxatest.infrastructure.messaging.recording.MessageRecorder;
import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import io.jexxa.tutorials.bookstore.domainservice.IntegrationEventSender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.jexxa.jexxatest.JexxaTest.getJexxaTest;
import static io.jexxa.tutorials.bookstore.domain.DomainEventPublisher.subscribe;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookStoreServiceTest
{
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    private BookStoreService objectUnderTest;       // Object we want to test
    private MessageRecorder publishedDomainEvents; // Message recorder to validate published DomainEvents
    private BookRepository   bookRepository;        // Repository to validate results in the tests

    @BeforeAll
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void initBeforeAll()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        getJexxaTest(BookStore.class)
                .getJexxaMain()
                .bootstrap(IntegrationEventSender.class).with(sender -> subscribe(BookSoldOut.class, sender::publish));
    }


    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        // Request the objects needed for our tests
        objectUnderTest       = jexxaTest.getInstanceOfPort(BookStoreService.class);   // 1. We need the object we want to test
        publishedDomainEvents = jexxaTest.getMessageRecorder(IntegrationEventSender.class); // 2. A recorder for DomainEvents published via DomainEventSender
        bookRepository        = jexxaTest.getRepository(BookRepository.class);         // 3. Repository managing all books
    }

    @Test
    void addBooksToStock()
    {
        //Arrange
        var amount = 5;

        //Act
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Assert
        assertEquals( amount, objectUnderTest.amountInStock(ANY_BOOK) );      // Perform assertion against the object we test
        assertEquals( amount, bookRepository.get(ANY_BOOK).amountInStock() ); // Perform assertion against the repository
        assertTrue( publishedDomainEvents.isEmpty() );                        // Perform assertion against published DomainEvents
    }


    @Test
    void sellBook()
    {
        //Arrange
        var amount = 5;
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Act / Assert
        assertDoesNotThrow(() -> objectUnderTest.sell(ANY_BOOK));

        //Assert
        assertEquals( amount - 1, objectUnderTest.amountInStock(ANY_BOOK) );       // Perform assertion against the object we test
        assertEquals( amount - 1, bookRepository.get(ANY_BOOK).amountInStock() );  // Perform assertion against the repository
        assertTrue( publishedDomainEvents.isEmpty() );                                     // Perform assertion against published DomainEvents
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

        //Assert
        assertEquals( 0 , objectUnderTest.amountInStock(ANY_BOOK) );                    // Perform assertion against the object we test
        assertEquals( 1 , publishedDomainEvents.size() );                               // Perform assertion against the repository
        assertEquals( ANY_BOOK, publishedDomainEvents.getMessage(BookSoldOut.class).isbn13());  // Perform assertion against published DomainEvents
    }

}