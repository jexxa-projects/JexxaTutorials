# BookStore—Using a Repository

## What You Learn

*   [How to design and implement an application core](#1-Implementing-application-core)
*   [How to publish Domain Events within the application core](#2-publishing-domainevents)      
*   [How to implement a Repository](#3-implement-the-infrastructure)
*   [Changes to the main method](#4-implement-the-application)     

## What you need

*   Understand tutorial `HelloJexxa` and `TimeService` because we explain only new aspects 
*   60 minutes
*   JDK 25 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   Optional: A postgres DB   

## Requirements to the application core
This application core should provide the following super simplified functionality:

*   Manage available books in the store which means to add, sell, and query books

*   All books should be identified by their ISBN13

*   For each book we store the number of available copies in stock

*   Publish `DomainEvent` `BookSoldOut` if last copy of a book is sold

*   A service which gets the latest books from our reference library. For this tutorial, it is sufficient that: 
    *   Service provides a hardcoded list
    *   Service is triggered when starting the application     

## 1. Implementing application core

General note: There are several books, courses, tutorials available describing how to implement an application core using the patterns of DDD. 
The approach used in this tutorial should not be considered as reference. It serves only for demonstration purpose how to realize your decisions 
with Jexxa.       

### Mapping to DDD patterns 

First, we map the functionality of the application to DDD patterns   
<details>
  <summary>Show mapping to DDD patterns</summary>

*   `Domain`:
     *   `Book`: Is an `Aggregate` because it has a life-cycle that changes over time.
     *   `BookRepository`: Is a `Repository` that manages `Book` instances.  
     *   `ISBN13`: Is a `ValueObject` that is immutable and identifies a book     
     *   `BookSoldOut`: Is a `DomainEvent` that informs us if a book is no longer in stock   
     *   `BookNotInStockException`: Is a `BusinessException` in case we try to sell a book that is currently not available

*   `DomainService`:
     *   `DomainEventPublisher`: Is a `DomainService` that registers for all `DomainEvents` and forwards them to an external message bus.
     *   `ReferenceLibrary`: Is a `DomainService` that provides primary data for our bookstore and returns the latest books. 
     *   `IntegrationEventSender`: Is an `InfrastructureService` that sends `DomainEvents` to a message bus.

*   `ApplicationService`:
     *   `BookStoreService:` Is an `ApplicationService` that provides typical use cases such as selling a book. 
</details>

### Package structure

Based on the mapping to DDD patterns,
we derive the following package structure which is quite common in the DDD community: 

<details>
  <summary>Show package structure</summary>

*   `applicationservice`

*   `domainservice`

*   `domain` 
    *   `<use case 1>`
    *   `...`
    *   `<use case n>`

*   `infrastructure`
    *   `drivenadapter`
    *   `drivingadapter` 
</details>

Please note that a package for a specific use case includes all required domain classes.
As you can see in the examples, these are typically the corresponding of type `Aggregate`,`ValueObject`,
`DomainEvent`, `BusinessException`, and `Repository`.
The reason for this is
that you should apply the [Common Closure Principle](https://en.wikipedia.org/wiki/Package_principles)
so that changing classes within such a package is a change in the use case.
In addition, it should not affect any other use-cases.  

Structuring your domain-package this way provides the following benefits: 
*   Use cases are represented explicitly which allows a clear view into the application
*   Use cases remain strictly separated which simplifies adding, removing or changing use cases 
*   When designing your application, this approach supports discussing what kind of functionality belongs to a use case or not

As soon as your domain logic and thus the number of use cases grows, it will happen that ValueObjects will be used by multiple use cases. This is quite normal. The challenge is to group these ValueObjects and find a domain specific main term which is not `common` or something like this. As soon as you have this name, create a corresponding package within `domain`.   


### A note on implementing DDD patterns  

*   `ValueObject` and `DomainEvent`: Are implemented using Java records due to the following reasons:  
    *   They are immutable and compared based on their internal values.
    *   They must not have set-methods. So all fields are final. 
    *   They must provide a valid implementation of equals() and hashcode().
    *   They must not include any business logic, but they can validate their input data.
    *   Using records ensures that the canonical constructor is called, even if they are deserialized. So you can validate given values in constructor without considering serialization methods.   

*   `Aggregate`: Is identified by a unique `AggregateID` which is a `ValueObject`
    *   `Book` uses an `ISBN13` object     

*   `Repositroy` when defining any interface within the application core ensure that you use the domain language for all methods. Resist the temptation to use the language of the used technology stack that you use to implement this interface.     

As you can see in the source code, all classes are annotated with the pattern language of DDD. 
This is not required but strongly recommended. The explanation for this can be found in tutorial [pattern language](README-PatternLanguage.md). 

## 2. Publishing DomainEvents
When sending DomainEvents, we should distinguish between two separate scenarios.
*   `DomainEvent`: Used to inform the application core that something happened which is then typically handled by an `ApplicationService` or a `DomainService`. 
*   `IntegrationEvent`: Used to inform other bounded contexts that something important happened. These events are typically forwarded by an `InfrastructureService`. 

Within the DDD community, there are essentially three different approaches on how to implement sending DomainEvents. 
An overview and discussion of these approaches can be found [here](http://www.kamilgrzybek.com/design/how-to-publish-and-handle-domain-events/).

Jexxa itself supports all of these approaches.
In these tutorials, the approach with static methods is used because it 
is also used in the book ___Implementing Domain-Driven Design___ and described in great detail.
It therefore forms an exceptional basis for the initial 
implementation of a DDD application, from which teams can then work out for their own approach.

     
## 3. Implement the infrastructure

The implementation of interface `IntegrationEventSender` is quite similar to message sender in tutorial [TimeService](../TimeService/README.md).
So we don't discuss it here. 

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

Finally, we have to write our application. As you can see in the code below there is one difference compared to `HelloJexxa` and `TimeService`:

*   Add a bootstrap service which is directly called to initialize domain-specific aspects.   
   
```java
    
public final class BookStore
{
    static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                .bootstrap(ReferenceLibrary.class).and()       // Bootstrap latest books via ReferenceLibrary
                .bootstrap(IntegrationEventSender.class).with(sender -> subscribe(sender::publish)) // publish all DomainEvents as IntegrationEvents for other bounded contexts

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)        // Provide REST access to BookStoreService
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // Provide REST access to BoundedContext

                .run(); // Finally, run the application
    }
}
```

That's it. 

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
