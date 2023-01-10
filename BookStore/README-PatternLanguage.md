# BookStore - Pattern Language 

## What You Learn

*   How and why to use annotations in an application core 
*   How to handle cross-cutting concerns within the application core        

## What you need

*   Understand tutorial [`BookStore - Using a Repository`](README.md) because we explain only new aspects 
*   60 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   Optional: A postgres DB   

## A pattern language for your application core 
Most developers are aware of design pattern and use them when developing software. A pattern language 
goes one step further. It describes which design patterns are allowed and how they may interact with 
each other. Much like a grammar for a language, a developer uses a pattern language to navigate through code even if 
he or she has never read it before.

__Example:__ If an application uses the pattern language of DDD the execution of a typical use case looks as follows: 
*   A command, represented as `ValueObject`, is received by an `ApplicationServcice`
*   The `ApplicationService` requests the required `Aggregate` containing the business logic from the corresponding `Repository`
*   The `ApplicationService` executes the command ont the `Aggregate` 
*   The `Aggregate` executes the business logic, creates and publishes `DomainEvent` 
*   Finally, the `ApplicationService` returns the `Aggregate` to Repository again 

Ideally, a pattern language achieves 100% pattern consistency, so that all classes of an application and their 
relationships can be clearly mapped to the pattern language.

## Using a Pattern Language

To make these patterns explicit, I strongly recommend annotating all classes within the application core 
with their corresponding element of the pattern language. Classes that cannot be assigned to a specific element 
typically violate some design principles such as the single responsibility principle. In case of a durable software 
system, you will get the following advantages: 

*   You document the patterns directly in the code which ensures that all developers will read it
*   You establish a common understanding of the used patterns within the development team 
*   A developer will get a guideline how to integrate a new feature 
*   You speed up relearning of and navigating within the source code   
*   You speed up and simplify the initial training of new employees
*   You can use them for code reviews and refactorings      
*   You can automatically validate your architecture as part of your unit-tests(see [here](README-ArchitectureValidation.md))

For the pattern language of DDD we recommend project [Addend](https://addend.jexxa.io/).     

The following shows the annotation of an `Aggregate`. Apart from the obvious annotation, it also uses two other 
annotations: 
*   `AggregateID` to explicitly document the unique key
*   `AggregateFactory` to explicitly document the factory method for the `Aggregate`

```java
@Aggregate
public final class Book
{
    private final ISBN13 isbn13;
    private int amountInStock = 0;

    private Book(ISBN13 isbn13)
    {
        this.isbn13 = isbn13;
    }

    @AggregateID
    public ISBN13 getISBN13()
    {
        return isbn13;
    }

    // ... 

    @AggregateFactory(Book.class)
    public static Book newBook(ISBN13 isbn13)
    {
        return new Book(isbn13);
    }
}
```

### Use of Java records to improve semantic meaning

When implementing a business application using DDD one of the most important aspects is to provide a semantically 
elegant and consistent solution for implementing the DDD pattern elements. 

One of the major changes in Java 16 is the official support for Java [records](https://openjdk.java.net/jeps/359). They 
are especially designed for classes holding immutable data. Apart from a compact syntax they also provide two vital 
features: 
*   Valid implementations of `equals()`, `hashCode()`, and `toString()`.
*   The canonical constructor must be called in any cases. This ensures that members of a record can be validated in all
cases even if they are deserialized.  

Therefore, they are suitable for the following DDD elements.
*   `ValueObject`
*   `DomainEvent`

### Implementing DomainEvent `BookSoldOut`
The following example shows the implementation of the domain event `BookSoldOut` using a record. Note that the static method `bookSoldOut` is not required but improves the read flow when creating the domain event.

```java 
@DomainEvent
public record BookSoldOut(ISBN13 isbn13)
{
    public static BookSoldOut bookSoldOut(ISBN13 isbn13)
    {
        return new BookSoldOut(isbn13);
    }
}
```

As you can see, all important information of a DomainEvent can be seen in thw following two lines.

*   `@DomainEvent`: Indicates the concrete type of the pattern element.
*   `public record BookSoldOut(ISBN13 isbn13)`: Indicates the type name `BookSoldOut` including the provided data which is `ISBN13`

### Implementing ValueObject `ISBN13`
The main challenge when implementing this class is to ensure that we get a valid string representation of an ISBN13
number. As long as the constructor is called, we can validate given string using private method `validateChecksum`.

Unfortunately, we typically have to (de-)serialize our ValueObjects to send or receive them over a network connection. 
In order to automate this, most frameworks use reflection or a default constructor to provide a generic approach. 
The main disadvantage is that these approaches can leverage the validation of the internal attributes, so that we could 
end up with an invalid ValueObject and finally with an invalid state in our application.   

Apart from providing valid `equals`, `hashCode`, and `toString` method, java records ensure that the so-called canonical
constructor is called in all cases. This is also true if a record is deserialized and ensures that we have a single 
point to validate our attributes, as you can see in the following:

```java 
@ValueObject
public record ISBN13(String isbn13)
{
    public ISBN13
    {
        // The canonical constructor must be called in all cases. 
        // So we put the validation of our attributes here.
        validateChecksum(isbn13);
    }

    @ValueObjectFactory(ISBN13.class)
    public static ISBN13 createISBN(String value)
    {
        return new ISBN13(value);
    }

    // implementaion of validateChecksum(String isbn13)

}
```

### `DomainEventSender`: Considerations on interface definition

In large applications it is quite common that you have multiple domain events that have to be published to other 
applications as so-called integration events. To solve this issue at least following solutions exist:

*   Method overloading: Provide a specific method for each type of DomainEvent in `DomainEventSender`. On the one side, 
    this ensures static type safety but could flood your interface if the number of domain events is quite large.

*   Abstract `DomainEvent` class: This allows to ensure type safety in `DomainEventSender` and also providing only a 
    single method that is implemented in a generic way. This seems to solve all issues from method overloading. The 
    problem with this approach is that you introduce an interface or abstract base class that must be implemented by all
    kind of domain events for technical reason. At first glance, this seems to be a slightly esoteric problem. In the 
    long run, I've learned that such classes can be a gate opener, allowing technology aspects to enter the application 
    core. Therefore, I can only recommend such an approach for teams who know how to avoid this. 

*   Publishing an `Object`: An alternative solution is to provide a method accepting a domain event of type `Object`. 
    This prevents entering technology aspects into the application core. The obvious drawback is that you loose type 
    safety. In case you annotated all your classes you can double-check if the domain event is annotated with 
    `DomainEvent`. This prevents publishing arbitrary objects, but this check is performed only during runtime.

Of course, you can also combine the approaches. For example, you can use method overloading, and the implementation uses
an internal method accepting an `Object`. Anyway, the most important aspects are:
*   The outbound port is an interface to ensure the separation of your application core from a technology stack.
*   To avoid entering technology aspects into the application core, or vice versa, you should provide a clean guideline 
    how to handle this.

Please do not underestimate such aspects if your application runs for several decades and is maintained by different 
developer teams. So you should discuss and document such aspects with your colleagues and/or software architects.

Finally, the following code shows how to use the annotation to add a runtime test.

```java
@DrivenAdapter
public class DomainEventSenderImpl implements DomainEventSender {
    private final MessageSender messageSender;

    public DomainEventSenderImpl(Properties properties)
    {
        // Request a MessageSender from the framework, so that we can configure it in our properties file
        messageSender = getMessageSender(DomainEventSender.class, properties);
    }

    @Override
    public void publish(Object domainEvent)
    {
        // We just allow sending DomainEvents
        validateDomainEvent(domainEvent);

        // For publishing a DomainEvent we use a fluent API in Jexxa
        messageSender
                .send(domainEvent)
                .toTopic("BookStore")
                .addHeader("Type", domainEvent.getClass().getSimpleName())
                .asJson();
    }

    private void validateDomainEvent(Object domainEvent)
    {
        Objects.requireNonNull(domainEvent);
        if ( domainEvent.getClass().getAnnotation(DomainEvent.class) == null )
        {
            throw new IllegalArgumentException("Given object is not annotated with @DomainEvent");
        }
    }
}

```
## Implement the Application

If your application core is annotated with your pattern language, you can use it together wih Jexxa. This requires to change the initial `BookStore` application as follows:
1.  You have to bind driving adapters using method `bindToAnnotation`. In this case alle inbound ports annotated with given annotation are bind to the driving adapter.    

```java 
public final class BookStore
{
    public static void main(String[] args)
    {
        var jexxaMain = new JexxaMain(BookStore.class);

        jexxaMain
                // Bootstrap all classes annotated with @DomainService. In this application this causes to get the 
                // latest books via ReferenceLibrary and forward DomainEvents to a message bus via DomainEventService
                .bootstrapAnnotation(DomainService.class)

                .bind(RESTfulRPCAdapter.class).toAnnotation(AppicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .run();
    }

}  
```

## Run the application  

### Use an in memory database

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties" target/bookstore-jar-with-dependencies.jar
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
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7503
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: http://0.0.0.0:7503/swagger-docs
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'BookStore' successfully started in 0.885 seconds

```          

### Use a Postgres database

You can run this application using a Postgres database because the corresponding driver is included in the pom file. The 
configured username and password is `admin`/`admin`. You can change it in the [jexxa-test.properties](src/test/resources/jexxa-test.properties) 
file if required.       

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" target/bookstore-jar-with-dependencies.jar
```
In contrast to the above output Jexxa will state that you use JDBC persistence strategy now:
```console
[main] INFO io.jexxa.utils.JexxaBanner - Used Repository Strategie      : [JDBCKeyValueRepository]
```

### Execute some commands using curl 

#### Get list of books

Command: 
```Console
curl -X GET  http://localhost:7503/BookStoreService/getBooks
```

Response: 
```Console
[{"isbn13":"978-1-60309-322-4"},{"isbn13":"978-1-891830-85-3"},{"isbn13":"978-1-60309-047-6"},{"isbn13":"978-1-60309-025-4"},{"isbn13":"978-1-60309-016-2"},{"isbn13":"978-1-60309-265-4"}]
```

#### Ask if a specific book is in stock**

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '{isbn13:"978-1-891830-85-3"}' \
    http://localhost:7503/BookStoreService/inStock                 
```

Response: 
```Console
false
```

#### Add some books

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '[{isbn13:"978-1-891830-85-3"}, 5]' \
    http://localhost:7503/BookStoreService/addToStock                 
```
Response: No output  
```Console
```

#### Ask again if a specific book is in stock

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '{isbn13:"978-1-891830-85-3"}' \
    http://localhost:7503/BookStoreService/inStock               
```

Response: 
```Console
true
```
