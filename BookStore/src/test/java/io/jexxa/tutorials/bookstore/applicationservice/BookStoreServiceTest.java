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
    private static final ISBN13 ISBN_13 = createISBN( "978-3-86490-387-8" );
    private BookStoreService objectUnderTest;

    private MessageRecorder publishedDomainEvents;
    private BookRepository bookRepository;


    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required.
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        // Query a message recorder for an interface which is defines in your application core.
        publishedDomainEvents = jexxaTest.getMessageRecorder(DomainEventSender.class);
        // Query the repository that is internally used.
        bookRepository = jexxaTest.getRepository(BookRepository.class);
        // Query the application service we want to test.
        objectUnderTest = jexxaTest.getInstanceOfPort(BookStoreService.class);

        jexxaTest.getJexxaMain().bootstrapAnnotation(DomainService.class);
    }

    @Test
    void receiveBook()
    {
        //Arrange
        var amount = 5;

        //Act
        objectUnderTest.addToStock(ISBN_13.value(), amount);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( amount, objectUnderTest.amountInStock(ISBN_13) );
        assertEquals( amount, bookRepository.get( ISBN_13 ).amountInStock() );
        assertTrue( publishedDomainEvents.isEmpty() );
    }


    @Test
    void sellBook() throws BookNotInStockException
    {
        //Arrange
        var amount = 5;
        objectUnderTest.addToStock(ISBN_13.value(), amount);

        //Act
        objectUnderTest.sell(ISBN_13);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( amount - 1, objectUnderTest.amountInStock(ISBN_13) );
        assertEquals( amount - 1, bookRepository.get(ISBN_13).amountInStock() );
        assertTrue( publishedDomainEvents.isEmpty() );
    }

    @Test
    void sellBookNotInStock()
    {
        //Arrange - Nothing

        //Act/Assert
        assertThrows(BookNotInStockException.class, () -> objectUnderTest.sell(ISBN_13));
    }

    @Test
    void sellLastBook() throws BookNotInStockException
    {
        //Arrange
        objectUnderTest.addToStock(ISBN_13.value(), 1);

        //Act
        objectUnderTest.sell(ISBN_13);

        //Assert - Here you can also use all the interfaces for driven adapters defined in your application without running the infrastructure
        assertEquals( 0 , objectUnderTest.amountInStock(ISBN_13) );
        assertEquals( 1 , publishedDomainEvents.size() );
        assertEquals( bookSoldOut(ISBN_13), publishedDomainEvents.getMessage(BookSoldOut.class));
    }

}