# BookStore - Writing Unit Tests

## What You Learn

*   How to test your business logic using Jexxa

## What you need

*   Understand tutorial `BookStore`
*   30 minutes
*   JDK 17 (or higher) installed
*   Maven 3.6 (or higher) installed

## Write some tests
Writing some tests with Jexxa is quite easy. If you implement your driven adapters using Jexxa's driven adapter strategies you can use
package **jexxa-test**. It automatically provides stubs so that you do not need any mock framework. Main advantages are:

*   You can focus on domain logic within your tests.
*   You don't need to use mocks which can lead to validating execution steps within the domain core instead of validating the domain specific use cases
*   Your tests are much easier to read and can teach new developers the use cases of your domain.
*   You can write your tests first without considering the infrastructure first.

First, add the following dependency to your tests.

```maven
    <dependency>
      <groupId>io.jexxa.jexxatest</groupId>
      <artifactId>jexxa-test</artifactId>
      <version>5.0.2</version>
      <scope>test</scope>
    </dependency>
```

Following code shows a simple validation of our BookStoreService. Some additional tests can be found [here](https://github.com/jexxa-projects/Jexxa/blob/master/tutorials/BookStore/src/test/java/io/jexxa/tutorials/bookstore/applicationservice/BookStoreServiceTest.java).

```java
class BookStoreServiceTest
{
    private static final ISBN13 ISBN_13 = createISBN( "978-3-86490-387-8" );
    private static JexxaMain jexxaMain;
    private BookStoreService objectUnderTest;

    private MessageRecorder publishedDomainEvents;
    private BookRepository bookRepository;


    @BeforeAll
    static void initBeforeAll()
    {
        // We recommend instantiating JexxaMain only once for each test class.
        // If you have larger tests this speeds up Jexxa's dependency injection
        jexxaMain = new JexxaMain(BookStoreServiceTest.class)
                .addDDDPackages(BookStore.class);
    }

    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required.
        JexxaTest jexxaTest = new JexxaTest(jexxaMain);

        DomainEventPublisher.reset();
        jexxaMain.bootstrap(DomainEventService.class).with(DomainEventService::registerListener);

        // Query a message recorder for an interface which is defines in your application core.
        publishedDomainEvents = jexxaTest.getMessageRecorder(DomainEventSender.class);
        // Query the repository that is internally used.
        bookRepository = jexxaTest.getRepository(BookRepository.class);
        // Query the application service we want to test.
        objectUnderTest = jexxaTest.getInstanceOfPort(BookStoreService.class);
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
```