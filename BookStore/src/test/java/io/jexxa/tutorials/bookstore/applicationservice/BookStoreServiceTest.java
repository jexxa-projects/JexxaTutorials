package io.jexxa.tutorials.bookstore.applicationservice;

import io.jexxa.addend.applicationcore.DomainService;
import io.jexxa.jexxatest.JexxaTest;
import io.jexxa.jexxatest.infrastructure.drivenadapterstrategy.messaging.recording.MessageRecorder;
import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.domain.book.BookNotInStockException;
import io.jexxa.tutorials.bookstore.domain.book.BookRepository;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import io.jexxa.tutorials.bookstore.domainservice.DomainEventSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.jexxa.jexxatest.JexxaTest.getJexxaTest;
import static io.jexxa.tutorials.bookstore.domain.book.BookSoldOut.bookSoldOut;
import static io.jexxa.tutorials.bookstore.domain.book.ISBN13.createISBN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookStoreServiceTest
{
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8" );

    private BookStoreService objectUnderTest;       // Object we want to test
    private MessageRecorder  publishedDomainEvents; // Message recorder to validate published DomainEvents
    private BookRepository   bookRepository;        // Repository to validate results in the tests


    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        objectUnderTest       = jexxaTest.getInstanceOfPort(BookStoreService.class);
        publishedDomainEvents = jexxaTest.getMessageRecorder(DomainEventSender.class);
        bookRepository        = jexxaTest.getRepository(BookRepository.class);

        // Invoke all bootstrapping services from main to ensure same starting point
        jexxaTest.getJexxaMain().bootstrapAnnotation(DomainService.class);
    }

    @Test
    void addBooksToStock()
    {
        //Arrange
        var amount = 5;

        //Act
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( amount, objectUnderTest.amountInStock(ANY_BOOK) );
        assertEquals( amount, bookRepository.get(ANY_BOOK).amountInStock() );
        assertTrue( publishedDomainEvents.isEmpty() );
    }


    @Test
    void sellBook() throws BookNotInStockException
    {
        //Arrange
        var amount = 5;
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Act
        objectUnderTest.sell(ANY_BOOK);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( amount - 1, objectUnderTest.amountInStock(ANY_BOOK) );
        assertEquals( amount - 1, bookRepository.get(ANY_BOOK).amountInStock() );
        assertTrue( publishedDomainEvents.isEmpty() );
    }

    @Test
    void sellBookNotInStock()
    {
        //Arrange - Nothing

        //Act/Assert
        assertThrows(BookNotInStockException.class, () -> objectUnderTest.sell(ANY_BOOK));
    }

    @Test
    void sellLastBook() throws BookNotInStockException
    {
        //Arrange
        objectUnderTest.addToStock(ANY_BOOK, 1);

        //Act
        objectUnderTest.sell(ANY_BOOK);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( 0 , objectUnderTest.amountInStock(ANY_BOOK) );
        assertEquals( 1 , publishedDomainEvents.size() );
        assertEquals( bookSoldOut(ANY_BOOK), publishedDomainEvents.getMessage(BookSoldOut.class));
    }

}