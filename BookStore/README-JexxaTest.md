# BookStore – Writing Tests

This tutorial demonstrates how to test a Jexxa application at different levels while keeping business logic independent of infrastructure and technology.

## What You Will Learn

- How to write unit tests for domain aggregates
- How to test application services using stubs
- How to write integration tests against a running application
- How to verify domain events and externally published messages
- How to apply the **Arrange–Act–Assert** pattern consistently

## Prerequisites

- Familiarity with [BookStore – Using a Repository](README.md)
- Basic knowledge of JUnit 5
- JDK 25 or later
- Maven 3.6 or later
- Approximately 45 minutes

## Testing Strategy

We recommend three complementary types of tests for applications built with Jexxa.

| Test type         | Focus                              | Dependencies                  | Purpose                                                 |
|-------------------|------------------------------------|-------------------------------|---------------------------------------------------------|
| Unit tests        | Domain model and aggregates        | JUnit                         | Verify business rules and invariants                    |
| Tests with stubs  | Application services and use cases | JUnit, Jexxa-Test             | Verify application behavior without real infrastructure |
| Integration tests | Running application                | Jexxa-Test, external services | Verify communication and infrastructure integration     |

The goal is not to test every behavior at every level. Instead, each test should provide confidence in a specific responsibility.

### 1. Unit Tests – Verify Business Behavior

Domain aggregates encapsulate business rules and protect invariants.

Their tests should be fast, deterministic, and independent of databases, messaging systems, application frameworks, or other infrastructure.

For example, a `Book` aggregate should be testable without starting Jexxa or configuring a repository.

Keeping business behavior directly testable also helps identify misplaced domain logic. If a business rule can only be tested through an application service, it may be worth reconsidering where that rule belongs.

### 2. Tests with Stubs – Verify Use Cases

Application services coordinate domain objects and interact with external resources through ports.

For example, `BookStoreService` may use a repository to load a book, invoke its business behavior, save the resulting state, and publish a domain event.

Jexxa-Test provides support for testing these interactions using stubs for supported driven-adapter strategies.

We generally prefer stubs over interaction-heavy mocks when testing use cases. Stubs allow tests to focus on observable outcomes rather than prescribing the internal sequence of method calls.

Mocks are still useful when verifying a specific interaction is itself a requirement.

### 3. Integration Tests – Verify System Integration

Integration tests exercise the running application through its public interfaces.

For BookStore, this includes invoking use cases through REST and verifying messages published through JMS.

These tests provide confidence that application configuration, adapters, serialization, and external communication work together.

They are typically slower than unit tests and tests with stubs, so they should complement rather than replace them.

## Arrange–Act–Assert

All examples in this tutorial follow the Arrange–Act–Assert pattern:

- **Arrange:** Prepare the object under test, its dependencies, and the initial state.
- **Act:** Execute the behavior being tested.
- **Assert:** Verify the expected observable outcome.

Each test should focus on one behavior, even when several assertions are needed to describe its result.

## Add the Test Dependency

Add `jexxa-test` to your Maven project when you want to use Jexxa's testing support.

```xml
<dependency>
    <groupId>io.jexxa.jexxatest</groupId>
    <artifactId>jexxa-test</artifactId>
    <version>9.1.10</version>
    <scope>test</scope>
</dependency>
```

The version above reflects the version used in this tutorial. Check Maven Central for the latest compatible release.

---

## Write Unit Tests for Aggregates

The `Book` aggregate is responsible for enforcing business rules related to books and their inventory.

One important scenario occurs when the last available book is sold.

The expected behavior is:

1. The sale succeeds.
2. The remaining stock becomes zero.
3. The aggregate produces a `BookSoldOut` domain event.

### Testing Domain Events

A domain event represents something meaningful that happened in the domain.

An aggregate should not need a message broker, an event dispatcher, or any other infrastructure component to express such an event.

In this example, the aggregate returns the event as part of the result of the business operation. The application service is responsible for publishing it.

```java
class BookTest
{
    @Test
    void sellLastBook()
    {
        // Arrange
        var book = newBook(ANY_BOOK);
        book.addToStock(1);

        // Act
        var domainEvent = book.sell();

        // Assert
        assertEquals(0, book.amountInStock());
        assertEquals(BookSoldOut.bookSoldOut(ANY_BOOK), domainEvent);
    }
}
```

This example illustrates an aggregate API that returns a domain event. If the actual `Book.sell()` method returns a different result type, adapt the assertion accordingly.

The important principle remains the same: **the domain behavior can be tested without an event dispatcher or infrastructure dependency.**

### Why Not Use a Domain Event Dispatcher in the Aggregate Test?

A dispatcher is responsible for distributing events to interested subscribers. It does not define the business behavior that produces those events.

Keeping event production separate from event distribution provides several benefits:

- Aggregate tests remain independent of messaging infrastructure.
- Business behavior is easier to understand and test.
- Application services can control when events are published.
- The same domain model can be reused with different event-distribution mechanisms.

For example, an application may use `InProcessPubSub` to distribute domain events within the same process or a messaging adapter to communicate with external systems.

Neither mechanism needs to be referenced by the aggregate itself.

---

## Write Tests Using Stubs

Unit tests verify individual domain objects. The next step is to test complete use cases implemented by application services.

In BookStore, `BookStoreService` coordinates the domain model and its outgoing dependencies.

Jexxa-Test helps create an isolated testing environment using stubs instead of real infrastructure.

### Initialize the Test Environment

Create a new `JexxaTest` instance before each test and obtain the objects required for execution and verification.

```java
class BookStoreServiceTest
{
    private static final ISBN13 ANY_BOOK =
            createISBN("978-3-86490-387-8");

    private BookStoreService objectUnderTest;
    private MessageRecorder publishedDomainEvents;
    private BookRepository bookRepository;

    @BeforeEach
    void initTest()
    {
        // Arrange: Create an isolated test environment
        JexxaTest jexxaTest = getJexxaTest(BookStore.class);

        // Obtain the application service under test
        objectUnderTest =
                jexxaTest.getInstanceOfPort(BookStoreService.class);

        // Obtain a recorder for published domain events
        publishedDomainEvents =
                jexxaTest.getMessageRecorder(DomainEventSender.class);

        // Obtain the repository stub
        bookRepository =
                jexxaTest.getRepository(BookRepository.class);
    }
}
```

The `DomainEventSender` and `MessageRecorder` names shown here reflect the existing testing approach. Their exact configuration must match the outbound port and event-publishing adapter used by the current BookStore implementation.

### Bootstrap Test Data Explicitly

If a test requires reference data, initialize that data through the application's bootstrap mechanism.

For example, BookStore may use a `ReferenceLibrary` bootstrap component to populate its initial collection of books.

This is infrastructure initialization, not domain behavior.

Tests should explicitly initialize only the data they require. Avoid bootstrapping every domain service merely to establish the initial state.

### Test: Sell the Last Book

The following test verifies the observable result of selling the last available book.

```java
@Test
void sellLastBook()
{
    // Arrange
    objectUnderTest.addToStock(ANY_BOOK, 1);

    // Act
    objectUnderTest.sell(ANY_BOOK);

    // Assert
    assertEquals(0, objectUnderTest.amountInStock(ANY_BOOK));

    assertEquals(1, publishedDomainEvents.size());

    assertEquals(
            BookSoldOut.bookSoldOut(ANY_BOOK),
            publishedDomainEvents.getMessage(BookSoldOut.class)
    );
}
```

The test verifies two outcomes:

- The inventory reflects the completed sale.
- The expected domain event was published.

It does not verify the internal order in which the application service accesses the repository, invokes the aggregate, or publishes the event.

That distinction is intentional. The test describes the expected behavior of the use case rather than its implementation details.

### What About InProcessPubSub?

`InProcessPubSub` is a reusable infrastructure component for synchronous, in-process message distribution.

It can be used by a driven adapter implementing the application's domain-event publishing port.

However, application-service tests should generally interact with the **application-defined port**, not with the concrete `InProcessPubSub` implementation.

This preserves the separation between application behavior and technical infrastructure.

---

## Write Integration Tests

Integration tests verify the running application through its externally accessible interfaces.

Unlike tests with stubs, these tests use real adapters and may require external services such as a message broker.

In the BookStore example, we use:

- **RESTBinding** to invoke application use cases through REST.
- **MessageBinding** to receive messages through JMS.

The examples assume that BookStore and its required infrastructure are running before the integration tests execute.

### Initialize and Shut Down the Test Environment

`JexxaIntegrationTest` provides convenient access to the application's external interfaces.

```java
class BookstoreIT
{
    private static JexxaIntegrationTest jexxaIntegrationTest;
    private static RESTBinding restBinding;
    private static MessageBinding messageBinding;

    @BeforeAll
    static void initBeforeAll()
    {
        jexxaIntegrationTest =
                new JexxaIntegrationTest(BookStore.class);

        restBinding =
                jexxaIntegrationTest.getRESTBinding();

        messageBinding =
                jexxaIntegrationTest.getMessageBinding();
    }

    // Integration tests go here

    @AfterAll
    static void shutDown()
    {
        jexxaIntegrationTest.shutDown();
    }
}
```

The test infrastructure is initialized once for the test class and released after all tests have completed.

### Test: Sell the Last Book

This test exercises BookStore through REST and verifies that selling the last book results in an externally published message.

```java
@Test
void testSellLastBook()
{
    // Arrange
    var bookStoreService =
            restBinding.getRESTHandler(BookStoreService.class);

    var messageListener = messageBinding.getMessageListener(
            "BookStoreTopic",
            JMSConfiguration.MessagingType.TOPIC
    );

    bookStoreService.postRequest(
            Void.class,
            addToStock,
            ANY_BOOK,
            5
    );

    var inStock = bookStoreService.postRequest(
            Integer.class,
            amountInStock,
            ANY_BOOK
    );

    // Act
    for (int i = 0; i < inStock; ++i)
    {
        bookStoreService.postRequest(
                Void.class,
                sell,
                ANY_BOOK
        );
    }

    // Assert
    var result = messageListener
            .awaitMessage(5, TimeUnit.SECONDS)
            .pop(BookSoldOut.class);

    assertEquals(
            BookSoldOut.bookSoldOut(ANY_BOOK),
            result
    );
}
```

The test verifies the interaction between multiple architectural components:

1. The REST adapter receives the request.
2. The application service executes the use case.
3. The domain model performs the business operation.
4. The application publishes the resulting domain event.
5. The configured messaging integration publishes a message.
6. The test receives and verifies that message through JMS.

### Domain Events vs. Integration Events

A domain event describes something that happened within the domain.

An integration event communicates information to external systems.

These events may contain similar information, but they have different responsibilities and may require different contracts.

In a simple tutorial, `BookSoldOut` may be published directly through JMS to keep the example easy to follow.

In larger applications, it is often preferable to explicitly select or transform domain events into integration events.

This keeps internal domain contracts independent of externally published message formats.

### A Note on Reliable Message Publication

Receiving a JMS message confirms that the messaging integration worked for the tested scenario.

It does not, by itself, guarantee reliable delivery across failures.

Applications requiring reliable event publication may use patterns such as the **Transactional Outbox** to coordinate state changes and outgoing messages.

Likewise, consumers may require idempotent processing to handle duplicate deliveries.

These concerns belong to the infrastructure and integration design, not to the aggregate's business logic.

---

## Summary

The three test levels complement each other:

| Level                               | Main question                                                 |
|-------------------------------------|---------------------------------------------------------------|
| Aggregate unit test                 | Does the domain model behave correctly?                       |
| Application-service test with stubs | Does the use case produce the expected result?                |
| Integration test                    | Do the application and its external interfaces work together? |

The underlying architectural principle is consistent across all three levels:

**Test business behavior as close to the domain as possible, and introduce infrastructure only where it is relevant to the responsibility being tested.**

This keeps tests understandable, maintainable, and resilient to implementation changes.