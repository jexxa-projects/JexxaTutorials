package io.jexxa.tutorials.bookstore.domainservice;

import io.jexxa.core.JexxaMain;
import io.jexxa.jexxatest.JexxaTest;
import io.jexxa.tutorials.bookstore.BookStore;
import io.jexxa.tutorials.bookstore.domain.DomainEventPublisher;
import io.jexxa.tutorials.bookstore.domain.book.BookSoldOut;
import io.jexxa.tutorials.bookstore.domain.book.ISBN13;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.jexxa.tutorials.bookstore.domain.book.BookSoldOut.bookSoldOut;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IDomainEventPublisherTest
{
    //Declare the packages that should be used by Jexxa
    private static final String DRIVEN_ADAPTER  = BookStore.class.getPackageName() + ".infrastructure.drivenadapter";
    private static final String OUTBOUND_PORTS  = BookStore.class.getPackageName() + ".domainservice";

    private JexxaTest jexxaTest;

    @BeforeEach
    void initTest()
    {
        var jexxaMain = new JexxaMain(getClass());
        jexxaMain
                .addToApplicationCore(OUTBOUND_PORTS)
                .addToInfrastructure(DRIVEN_ADAPTER);

        jexxaTest = new JexxaTest(jexxaMain);
        //TODO: Check this
        jexxaMain.bootstrap(DomainEventService.class).with(DomainEventService::init);
    }

    @Test
    void testDomainEvent()
    {
        // Arrange
        var messageRecorder = jexxaTest.getMessageRecorder(DomainEventSender.class);
        var isbn13 = new ISBN13("978-3-86490-387-8");

        // Act
        DomainEventPublisher.publish(bookSoldOut(isbn13));

        // Assert
        assertDoesNotThrow(() -> messageRecorder.getMessage(BookSoldOut.class));
    }

    @Test
    void testInvalidDomainEvent()
    {
        // Arrange
        var isbn13 = new ISBN13("978-3-86490-387-8");

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> DomainEventPublisher.publish(isbn13));
    }

}