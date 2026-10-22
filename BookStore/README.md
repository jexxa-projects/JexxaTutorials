# BookStore—Using a Repository

## What You Learn

- [How to model the application core with DDD](#1-implementing-the-application-core)
- [How to distinguish Domain Events from Integration Events](#2-publishing-domain-events)
- [How to implement a repository and messaging adapter](#3-implement-the-infrastructure)
- [How to wire adapters and event subscriptions in the composition root](#4-implement-the-application)
- [How to run the application](#run-the-application)

## What you need

*   Understand tutorial `HelloJexxa` and `TimeService` because we explain only new aspects
*   60 minutes
*   JDK 25 (or higher) installed
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   Optional: A postgres DB

## Requirements for the application core
This application core should provide the following super simplified functionality:

*   Manage available books in the store which means to add, sell, and query books

*   All books should be identified by their ISBN13

*   For each book we store the number of available copies in stock

*   Produce a `BookSoldOut` domain event when the last copy of a book is sold

*   A bootstrap component that provides sample books. For this tutorial, it is sufficient that:
    *   It provides a hardcoded list
    *   It is triggered when starting the application

## 1. Implementing the application core

This tutorial illustrates one possible combination of Domain-Driven Design (DDD) and Ports & Adapters using Jexxa. It is deliberately small and should not be treated as a mandatory reference architecture.

### Mapping to DDD and Ports & Adapters

| Component                 | Responsibility                                                                  | Classification                              |
|---------------------------|---------------------------------------------------------------------------------|---------------------------------------------|
| `Book`                    | Enforces book stock rules and produces `BookSoldOut` when the last copy is sold | Domain aggregate                            |
| `ISBN13`                  | Identifies a book                                                               | Domain value object                         |
| `BookSoldOut`             | Describes a meaningful occurrence in the bookstore domain                       | Domain event                                |
| `BookNotInStockException` | Signals a domain rule violation                                                 | Domain exception                            |
| `BookRepository`          | Provides domain-oriented access to books                                        | Repository interface in the domain          |
| `BookStoreService`        | Coordinates use cases such as adding stock and selling books                    | Application service                         |
| `IDomainEventDispatcher`  | Allows application code to publish and subscribe to domain events               | Application port (outbound for publication) |
| `DomainEventDispatcher`   | Distributes domain events synchronously to registered subscribers               | Infrastructure driven adapter               |
| `IntegrationEventSender`  | Sends events through the configured messaging infrastructure                    | Infrastructure driven adapter               |
| `ReferenceLibrary`        | Seeds a hardcoded list of books at startup                                      | Bootstrap / sample-data component           |

A **Domain Service** is appropriate only when genuine domain behavior does not naturally belong to an aggregate or value object. This example does not require one. In particular, a dispatcher is not a Domain Service simply because it handles Domain Events.

### Package structure

The following structure separates domain rules, use-case coordination, ports, and technical adapters:

```text
io.jexxa.tutorials.bookstore
├── application
│   ├── service
│   │   └── BookStoreService
│   └── port
│       └── outbound
│           └── IDomainEventDispatcher
├── domain
│   └── book
│       ├── Book
│       ├── ISBN13
│       ├── BookSoldOut
│       ├── BookNotInStockException
│       └── BookRepository
└── infrastructure
    ├── bootstrap
    │   └── ReferenceLibrary
    ├── drivingadapter
    │   └── (REST adapter supplied by Jexxa)
    └── drivenadapter
        ├── persistence
        │   └── BookRepositoryImpl
        └── messaging
            ├── DomainEventDispatcher
            └── IntegrationEventSender
```

The package structure is illustrative: the concrete source tree and Jexxa bootstrap registration must be kept in sync when refactoring existing code.

The `domain.book` package groups concepts that change together around the book model. It is not necessary to create a new domain package for every application-service method. Apply the [Common Closure Principle](https://en.wikipedia.org/wiki/Package_principles) to identify meaningful domain boundaries rather than creating generic `common` packages.

The domain defines `BookRepository` because it is a domain-oriented repository abstraction. `IDomainEventDispatcher` belongs to the application boundary because `BookStoreService` uses it to publish events; its implementation belongs to infrastructure.

### A note on implementing DDD patterns

*   `ValueObject` and `DomainEvent`: Are implemented using Java records due to the following reasons:
    *   They are immutable and compared based on their internal values.
    *   They must not have set-methods. So all fields are final.
    *   They must provide a valid implementation of equals() and hashcode().
    *   Value objects may also contain domain behavior; records are suitable when value semantics and immutability are appropriate.
    *   Using records ensures that the canonical constructor is called, even if they are deserialized. So you can validate given values in constructor without considering serialization methods.

*   `Aggregate`: Is identified by a unique `AggregateID` which is a `ValueObject`
    *   `Book` uses an `ISBN13` object

*   `Repository`: when defining any interface within the application core ensure that you use the domain language for all methods. Resist the temptation to use the language of the used technology stack that you use to implement this interface.

As you can see in the source code, all classes are annotated with the pattern language of DDD.
This is not required but strongly recommended. The explanation for this can be found in tutorial [pattern language](README-PatternLanguage.md).

## 2. Publishing Domain Events

**Domain Events** describe facts within a bounded context. **Integration Events** are deliberately exposed contracts for other bounded contexts. They are related, but they are not necessarily identical: external contracts may require selection, mapping, versioning, and reliable delivery.

In this tutorial, `Book.sell()` returns a domain event when the last copy is sold. The application service persists the updated aggregate and then publishes the event:

```java
var bookEvent = book.sell();
bookRepository.update(book);
domainEventDispatcher.publish(bookEvent);
```

This keeps event distribution out of the aggregate. The dispatcher is an in-memory, synchronous implementation of `IDomainEventDispatcher`, located in `infrastructure.drivenadapter.messaging` and annotated `@DrivenAdapter`.

For the tutorial, the composition root subscribes the integration sender to the `BookSoldOut` event type:

```java
domainEventDispatcher.subscribe(BookSoldOut.class, sender::publish);
```

This is **wiring**, not a separate application use case. There is no need for an additional `ApplicationService` merely to connect two components. If a domain event must be transformed into a stable external event contract, a dedicated application-layer event handler can perform that mapping while the composition root still registers the subscription.

> **Scope and limitations:** The current dispatcher calls subscribers synchronously, in the publisher's thread. It does not provide durable delivery, retries, subscriber failure isolation, or a transaction spanning repository updates and message publication. Production integration may require explicit event mapping and a transactional outbox or equivalent mechanism. Only forward a domain event directly as an integration event when that simplified contract is intentional.

## 3. Implement the infrastructure

The `IntegrationEventSender` is a concrete messaging adapter, similar to the message sender in tutorial [TimeService](../TimeService/README.md). The event dispatcher is another driven adapter, implementing `IDomainEventDispatcher`. Both are technical mechanisms rather than Domain Services.

### Implement the repository
To simplify the implementation of a `Repository` Jexxa provides strategies that can be used. For example, the `IRepository`
interface provides typical CRUD operations and is especially designed to handle an `Aggregate` as you can see below:

```java
  
@SuppressWarnings("unused")
public class BookRepositoryImpl implements BookRepository
{
    private final IRepository<Book, ISBN13> repository;

    public BookRepositoryImpl(Properties properties)
    {
        // Request a strategy to implement our repository    
        this.repository = getRepository(Book.class, Book::getISBN13, properties);
    }

    @Override
    public void add(Book book)                  { repository.add(book); }

    @Override
    public void update(Book book)               { repository.update(book); }

    public void removeFromStock(ISBN13 isbn13)  { bookRepository.remove(isbn13); }
    
    @Override
    public boolean isRegistered(ISBN13 isbn13)  { return search(isbn13).isPresent(); }

    @Override
    public Book get(ISBN13 isbn13)              { return repository.get(isbn13).orElseThrow(); }

    @Override
    public Optional<Book> search(ISBN13 isbn13) { return repository.get(isbn13); }
    
    @Override
    public List<Book> getAll()                  { return repository.get(); }
}
```

**Important**:
As you can see, the implementation of a repository and a message sender is straight forward. So it is a good starting point for junior developers. See [here](https://jexxa-projects.github.io/Jexxa/jexxa_architecture.html#_strategy_pattern_for_driven_adapters) how you can use it to develop your junior developers.

## 4. Implement the application

`BookStore.main()` is the **composition root**. It creates the Jexxa application, registers startup initialization, connects event subscribers to publishers, binds REST adapters, and runs the application.

```java
public final class BookStore
{
    static void main()
    {
        var jexxaMain = new JexxaMain(BookStore.class);
        var domainEventDispatcher =
                jexxaMain.getInstanceOfPort(IDomainEventDispatcher.class);

        jexxaMain
                .bootstrap(ReferenceLibrary.class).and()
                .bootstrap(IntegrationEventSender.class)
                    .with(sender -> domainEventDispatcher.subscribe(
                            BookSoldOut.class, sender::publish))

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }
}
```

`ReferenceLibrary` initializes tutorial data at startup; it is not a Domain Service. Its initialization should be explicit rather than performed as an unexpected constructor side effect. The shown bootstrap call assumes the corresponding Jexxa bootstrap registration is configured.

The event subscription forwards only `BookSoldOut`, rather than publishing every internal Domain Event externally. The sample intentionally uses direct forwarding; a production system should define its external event contract explicitly.

## Run the application

### Use an in memory database

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties" \
          ./target/bookstore-jar-with-dependencies.jar
```
You will see the following (or similar) output
```console
[main] INFO io.jexxa.utils.JexxaBanner - Config Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.0.0-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2022-06-16 15:39]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=1.0.16-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/bookstore, projectName=BookStore, buildTimestamp=2022-06-16 18:07]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-local.properties]
[main] INFO io.jexxa.utils.JexxaBanner - Used Repository Strategie      : [IMDBRepository]
[main] INFO io.jexxa.utils.JexxaBanner - Used Message Sender Strategie  : [MessageLogger]
[main] INFO io.jexxa.utils.JexxaBanner - 
[main] INFO io.jexxa.utils.JexxaBanner - Access Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7505
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: http://0.0.0.0:7505/swagger-docs
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'BookStore' successfully started in 1.280 seconds

```          

### Use a postgres database
The properties file [jexxa-test.properties](src/test/resources/jexxa-test.properties) is configured to use a postgres
DB. So we have to enter following command

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" ./target/bookstore-jar-with-dependencies.jar
```
In contrast to the above output, Jexxa will state that you use JDBC persistence strategy now:
```console
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-test.properties]
[main] INFO io.jexxa.utils.JexxaBanner - Used Repository Strategie      : [JDBCKeyValueRepository]
[main] INFO io.jexxa.utils.JexxaBanner - Used Message Sender Strategie  : [JMSSender]
```

Note: In case you want to use a difference database, you have to:

1.  Add the corresponding jdbc driver to [pom.xml](pom.xml) to dependencies-section.
2.  Adjust the section `#Settings for JDBCConnection to postgres DB` in [jexxa-test.properties](src/test/resources/jexxa-test.properties).

### Execute some commands using curl

#### Get a list of all books

Command:
```Console
curl -X GET  http://localhost:7503/BookStoreService/getBooks
```

Response:
```Console
[
 {"isbn13":"978-1-60309-322-4"},{"isbn13":"978-1-891830-85-3"},
 {"isbn13":"978-1-60309-047-6"},{"isbn13":"978-1-60309-025-4"},
 {"isbn13":"978-1-60309-016-2"},{"isbn13":"978-1-60309-265-4"}
]
```

#### Query available books
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d '{isbn13:"978-1-891830-85-3"}' \
     http://localhost:7503/BookStoreService/inStock       
```

Response:
```Console
false
```

#### Add some books
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d "[{isbn13: "978-1-891830-85-3"}, 5]" \
     http://localhost:7503/BookStoreService/addToStock
```

Response: No output
```Console
```

#### Ask again if a specific book is in stock
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d '{isbn13:"978-1-891830-85-3"}' \
     http://localhost:7503/BookStoreService/inStock       
```

Response:
```Console
true
```