# BookStore - Writing Unit Tests

## What You Learn

*   [How to wirte unit-test for your business logic using Jexxa-Test](#Write-some-tests)
*   [How to validate the arhcitecture of your application](#Validate-the-Architecture)

## What you need

*   Understand tutorial [`BookStore - Using a Repository`](README.md)
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

In addition, **jexxa-test** provides predefined architectural tests for:
*   [Pattern Language](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validate the correct annotation of your application using project [Addend](http://addend.jexxa.io/)
*   [Ports&Adapters Architecture](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validates dependencies between packages of your application
*   [Usage of Aggregates](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validate that your business logic is not exposed

First, add the following dependency to your tests.

```maven
    <dependency>
      <groupId>io.jexxa.jexxatest</groupId>
      <artifactId>jexxa-test</artifactId>
      <version>5.1.0</version>
      <scope>test</scope>
    </dependency>
```

Following code shows a simple validation of our BookStoreService. Some additional tests can be found [here](https://github.com/jexxa-projects/Jexxa/blob/master/tutorials/BookStore/src/test/java/io/jexxa/tutorials/bookstore/applicationservice/BookStoreServiceTest.java).

### Initialize the tests 

As a first step, you initialize your tests by calling `initTest()`: 
*    Initialize JexxaTest before each test 
*    Initialize and request all objects you need for testing and validation 
*    Bootstrap all services as in your main method to ensure the same initial setup as you start your application

```java
class BookStoreServiceTest
{
    private static final ISBN13 ANY_BOOK = createISBN("978-3-86490-387-8");

    private BookStoreService objectUnderTest;       // Object we want to test
    private MessageRecorder  publishedDomainEvents; // Message recorder to validate published DomainEvents
    private BookRepository   bookRepository;        // Repository to validate results in the tests
    
    @BeforeEach
    void initTest()
    {
        // JexxaTest is created for each test. It provides stubs for running your tests so that no
        // mock framework is required. It expects the class name your application!
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        // Request the objects needed for our tests
        objectUnderTest       = jexxaTest.getInstanceOfPort(BookStoreService.class);   // 1. We need the object we want to test
        publishedDomainEvents = jexxaTest.getMessageRecorder(DomainEventSender.class); // 2. A recorder for DomainEvents published via DomainEventSender
        bookRepository        = jexxaTest.getRepository(BookRepository.class);         // 3. Repository managing all books

        // Invoke all bootstrapping services from main to ensure same starting point
        jexxaTest.getJexxaMain().bootstrapAnnotation(DomainService.class);
    }

    //...
}
```
### Write tests 
For all tests we use the arrange-act-assert pattern because it is simple and forces tests to focus on independent, individual behaviors.

#### A simple test to add books into stock 
```java
class BookStoreServiceTest 
{
    // ... initialization of tests 
    @Test
    void addBooksToStock()
    {
        var amount = 5;

        //Act
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Assert
        assertEquals( amount, objectUnderTest.amountInStock(ANY_BOOK) );      // Perform assertion against the object we test
        assertEquals( amount, bookRepository.get(ANY_BOOK).amountInStock() ); // Perform assertion against the repository
        assertTrue( publishedDomainEvents.isEmpty() );                        // Perform assertion against published DomainEvents
    }
    // ... further tests 
}
```
#### Test selling a book

```java
class BookStoreServiceTest 
{
    // ... initialization of tests 
    @Test
    void sellBook() throws BookNotInStockException
    {
        //Arrange
        var amount = 5;
        objectUnderTest.addToStock(ANY_BOOK, amount);

        //Act
        objectUnderTest.sell(ANY_BOOK);

        //Assert
        assertEquals( amount - 1, objectUnderTest.amountInStock(ANY_BOOK) );       // Perform assertion against the object we test
        assertEquals( amount - 1, bookRepository.get(ANY_BOOK).amountInStock() );  // Perform assertion against the repository
        assertTrue( publishedDomainEvents.isEmpty() );                             // Perform assertion against published DomainEvents
    }
    // ... further tests 
}
```
#### Test selling last book
```java
class BookStoreServiceTest 
{
   // ... initialization of tests

    @Test
    void sellLastBook() throws BookNotInStockException
    {
        //Arrange
        objectUnderTest.addToStock(ANY_BOOK, 1);

        //Act
        objectUnderTest.sell(ANY_BOOK);

        //Assert
        assertEquals( 0 , objectUnderTest.amountInStock(ANY_BOOK) );                        // Perform assertion against the object we test
        assertEquals( 1 , publishedDomainEvents.size() );                                   // Perform assertion against the repository
        assertEquals( bookSoldOut(ANY_BOOK), publishedDomainEvents.getMessage(BookSoldOut.class));  // Perform assertion against published DomainEvents
    }

}
```

## Validate the Architecture

Even with a clear architecture, one can easily violate their rules by accident. So I strongly recommend to run some
tests to validate the architecture of your application. Jexxa simplifies this especially if you use its conventions
by providing architectural tests based on [ArchUnit](https://www.archunit.org). This ensures that the validation of 
the architecture is port of your unit tests. 

### Validate Ports and Adapters

To validate the rules of a ports and adapter architecture is quite simple if you follow the conventions of Jexxa. As 
you can see below, you just have to declare the packages including the driven and driving adapter of the application.  

```java
class ArchitectureTest {

    @Test
    void validatePortsAndAdapters()
    {
        portsAndAdapters(BookStore.class)
                // Add all packages providing driven adapter  
                .addDrivenAdapterPackage("persistence")  
                .addDrivenAdapterPackage("messaging")

                // Add all packages providing driving adapter such as  
                // .addDrivingAdapterPackage("messaging")
                
                .validate();
    }
}
```

### Validate the Pattern Language
The following test validates that all classes of your application are annotated. In addition, it validates that all 
`ValueObject` and `DomainObject` classes are implemented as java-records. 

```java
class ArchitectureTest {
    @Test
    void validatePatternLanguage() {
        patternLanguage(BookStore.class).validate();
    }
}
```

### Validate the usage of Aggregates
Aggregates include the business logic and must not leave the application core. In addition, they must only be managed 
by a `Repository`. This leads to some additional rules that can be validated using following test: 

```java
class ArchitectureTest {
    @Test
    void validateAggregateRules()
    {
        aggregateRules(BookStore.class).validate();
    }
}
```
