# BookStore - Using a Repository

## What You Learn

*   How to provide an implementation of a specific outbound-port which is called `Repository` in terms of DDD using a database  
*   How to initialize master data into a Repository      
*   Default package structure for more complex applications based on DDD
*   How to test your business logic using Jexxa     

## What you need

*   Understand tutorial `HelloJexxa` and `TimeService` because we explain only new aspects 
*   60 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   Optional: A postgres DB   

## Requirements to the application core
This application core should provide following super simplified functionality:

*   Manage available books in store which means to add, sell, and query books

*   All books should be identified by their ISBN13

*   For each book the store the umber of available copies

*   Publish `DomainEvent` `BookSoldOut` if last copy of a book is sold

*   A service which gets the latest books from our reference library. For this tutorial it is sufficient that: 
    *   Service provides a hardcoded list
    *   Service is triggered when starting the application     

## Implementing application core 

General note: There are several books, courses, tutorials available describing how to implement an application core using the patterns of DDD. 
The approach used in this tutorial should not be considered as reference. It serves only for demonstration purpose how to realize your decisions 
with Jexxa.       

### 1. Mapping to DDD patterns 

First we map the functionality of the application to DDD patterns   

*   `Aggregate:` Elements that have a life-cycle and change over time and include our business logic 
    *   `Book` which manages available copies of a book.       

*   `Repository`: Provide access to repositories 
   *    `BookRepository:` Interface to manage `Book` instances. Since the implementation requires a technology stack we can only define an interface.  

*   `ValueObject:` Elements that represent a state and are immutable
    *   `ISBN13` which identifies a book     

*   `DomainEvent:` Business events that happened in the past 
    *   `BookSoldOut` when copies of a book are no longer in stock   

*   `DomainService:` 
    *   `DomainEventPublisher:` We need to publish our domain events in some way. Since the implementation requires a technology stack we can only define an interface.   
    *   `ReferenceLibrary:` Return latest books. For simplicity, we assume that it is a service which does not relate to our domain core directly.             

*   `BusinessException:`
    *   `BookNotInStockException:` In case we try to sell a book that is currently not available   
          
### Package structure

In our tutorials we use following package structure. Please note that this package structure is just a recommendation but Jexxa offers some convenience methods if you use it. That's why we recommend to start with this structure: 

*   applicationservice

*   domainservice

*   domain 
    *   <use case 1>
    *   ...
    *   \<use case n>

*   infrastructure
    *   drivenadapter
    *   drivingadapter 

Please note that a package for a specific use case includes all required domain classes. As you can see in the examples these are typically the corresponding of type `Aggregate`,`ValueObject`, `DomainEvent`, `BusinessException`, and `Repository`. The reason for this is that you should apply the [Common Closure Principle](https://en.wikipedia.org/wiki/Package_principles) so that changing classes within such a package is a change in the use case. In addition, it should not affect any other use cases.  

Structuring your domain-package this way provides following benefits: 
*   Use cases are represented explicitly which allows a clear view into the application
*   Use cases remain strictly separated which simplifies adding, removing or changing use cases 
*   When designing your application this approach supports discussing what kind of functionality belongs to a use case or not

As soon as your domain logic and thus the number of use cases grows, it will happen that ValueObjects will be used by multiple use cases. This is quite normal. The challenge is to group these ValueObjects and find a domain specific main term which is not `common` or something like this. As soon as you have this name, create a corresponding package within `domain`.   

   

### A note on implementing DDD patterns  

*   `ValueObject` and `DomainEvent`: Are implemented using Java records due to following reasons.  
    *   They are immutable and compared based on their internal values.
    *   They must not have setter methods. So all fields should be final. 
    *   They must provide a valid implementation of equals() and hashcode().
    *   They must not include any business logic, but they can validate their input data.    

*   `Aggregate`: Is identified by a unique `AggregateID` which is a `ValueObject`
    *   `Book` uses an `ISBN13` object     

*   `Repositroy` when defining any interface within the application core ensure that you use the domain language for all methods. Resist the temptation to use the language of the used technology stack that you use to implement this interface.        

## 2. Sending DomainEvents
When sending DomainEvents we should distinguish between two separate scenarios.
*   `DomainEvent`: Used to inform the application core that something happened which is then typically handled by an `ApplicationService` or a `DomainEventService`. 
*   `IntegrationEvent`: Used to inform other contexts that something happened. These events are typically forwarded by an `InfrastructureService`. 

Within the DDD community, there are essentially three different approaches on how to implement sending DomainEvents. 
An overview and discussion of these approaches can be found [here](http://www.kamilgrzybek.com/design/how-to-publish-and-handle-domain-events/).

Jexxa itself supports all of these approaches. In these tutorials the approach with static methods is used, because it 
is also used in the book and described in great detail. It therefore forms a very good basis for the initial 
implementation of a DDD application, from which teams can then work out for their own approach.

     
## 3. Implement the infrastructure

Implementation of `DomainEventSender` just prints the `DomainEvent` to the console. So we can just use the implementation from tutorial `TimeService`.    

### Implement the repository 
When using Jexxa's `RepositoryManager` implementing a repository is just a mapping to the `IRepository` interface which provides typical CRUD operations.   
  
The requirements are: 

*   The managed object provides a so called key-function which returns a key to uniquely identify the object. In case of this tutorial it is the method `getISBN`.
*   The key itself must provide a valid implementation of method equals and hashcode to validate equality.     

The following source code shows a typical implementation of a `Repository`. Within the main function you can configure the `RepositoryManager` if required. 

For the sake of completeness we use a static factory method in this implementation instead of a public constructor. Here it is quite important to return the interface and not the concrete type.        

```java
  
@SuppressWarnings("unused")
public class BookRepositoryImpl implements BookRepository
{
    private final IRepository<Book, ISBN13> repository;

    public BookRepositoryImpl(Properties properties)
    {
        this.repository = getRepository(Book.class, Book::getISBN13, properties);
    }

    @Override
    public void add(Book book)                  { repository.add(book); }

    @Override
    public Book get(ISBN13 isbn13)              { return repository.get(isbn13).orElseThrow(); }

    @Override
    public boolean isRegistered(ISBN13 isbn13)  { return search(isbn13).isPresent(); }

    @Override
    public Optional<Book> search(ISBN13 isbn13) { return repository.get(isbn13); }

    @Override
    public void update(Book book)               { repository.update(book); }

    @Override
    public List<Book> getAll()                  { return repository.get(); }
}
```

## 3. Implement the application 

Finally, we have to write our application. As you can see in the code below there are two main differences compared to `HelloJexxa` and `TimeService`:

*   Define a default strategy for our Repositories.
*   Add a bootstrap service which is directly called to initialize domain-specific aspects.   
   
```java
    
public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                //Get the latest books when starting the application
                .bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks)

                .bind(RESTfulRPCAdapter.class).to(BookStoreService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }
    //...
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
You will see following (or similar) output
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

You can run this application using a Postgres database because the corresponding driver is included in the pom file. The 
configured username and password is `admin`/`admin`. You can change it in the [jexxa-test.properties](src/test/resources/jexxa-test.properties) 
file if required.       

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" \
          ./target/bookstore-jar-with-dependencies.jar
```
In contrast to the above output Jexxa will state that you use JDBC persistence strategy now:
```console
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-test.properties]
[main] INFO io.jexxa.utils.JexxaBanner - Used Repository Strategie      : [JDBCKeyValueRepository]
[main] INFO io.jexxa.utils.JexxaBanner - Used Message Sender Strategie  : [JMSSender]
```

Note: In case you want to use a difference database, you have to: 

1.  Add the corresponding jdbc driver to [pom.xml](pom.xml) to dependencies section.
2.  Adjust the section `#Settings for JDBCConnection to postgres DB` in [jexxa-test.properties](src/test/resources/jexxa-test.properties).

### Execute some commands using curl 

#### Get list of books

Command: 
```Console
curl -X GET  http://localhost:7503/BookStoreService/getBooks
```

Response: 
```Console
[{"value":"978-1-891830-85-3"},{"value":"978-1-60309-025-4"},{"value":"978-1-60309-016-2"},{"value":"978-1-60309-265-4"},{"value":"978-1-60309-047-6"},{"value":"978-1-60309-322-4"}]
```

#### Query available books
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d '"978-1-891830-85-3"' \
     http://localhost:7503/BookStoreService/inStock       
```

Response: 
```Console
false
```

#### Add some books
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d "["978-1-891830-85-3", 5]" \
     http://localhost:7503/BookStoreService/addToStock
```

Response: No output  
```Console
```

#### Ask again if a specific book is in stock
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d '"978-1-891830-85-3"' \
     http://localhost:7503/BookStoreService/inStock       
```

Response: 
```Console
true
```
