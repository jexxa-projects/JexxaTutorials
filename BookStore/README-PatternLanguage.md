# BookStoreJ - Pattern Language 

## What You Learn

*   How and why to use annotations in an application core 
*   How to handle cross-cutting concerns within the application core        

## What you need

*   Understand tutorial `BookStore` because we explain only new aspects 
*   60 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   curl or jconsole to trigger the application
*   A postgres DB (if you start the application with option `-jdbc')  

## A pattern language for your application core 
In the [architecture of Jexxa](https://jexxa-projects.github.io/Jexxa/jexxa_architecture.html) we describe that Jexxa does not require any special annotations. Main reason is that framework related annotations can tightly couple your application core to a specific technology stack. Therefore, framework specific annotations should not be used within the application core.

Instead, define and use your own annotations that are specific for your application. These annotations can then be used by a framework.  

### Framework-agnostic annotations 
Annotations as pure meta-information can be used your developing teams to make a so-called __pattern language__ explicit. The pattern language is part of the micro architecture of an application and allows your developers to quickly navigate through the source code. Instead of reading and understanding the source code again and again, a developer can navigate through the code based on these patterns. For example if the application uses the pattern language of DDD, and you have to change the business logic of your application core the corresponding code must be within an `Aggregate`. So you can directly navigate to the `Aggregate` and skip all remaining elements. 

Even if you not use the pattern language of DDD, the developers typically used some patterns to implement the application. To make these patterns explicit, I strongly recommend annotating all classes within the application core with their corresponding element of the pattern language. Classes that cannot be assigned to a specific element typically violate some design principles such as the single responsibility principle. In case of a durable software system, you will get the following advantages: 

*   You document the patterns directly in the code which ensures that all developers will read it
*   You establish a common understanding of the used patterns within the development team 
*   A developer will get a guideline how to integrate a new feature 
*   You speed up relearning of and navigating within the source code   
*   You speed up and simplify the initial training of new employees
*   You can use them for code reviews and refactorings      

For the pattern langauge of DDD we recommend project [Addend](https://addend.jexxa.io/).     

The following shows the annotation of an `Aggregate`. Apart from the obvious annotation, it also uses two other annotations: 
*   `AggregateID` to explicitly document the unique key
*   `AggregateFactory` to explicitly document the factory method for the `Aggregate`

```java

@Aggregate
public final class Book
{
    private final ISBN13 isbn13;
    private final int amountInStock = 0;

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

When implementing a business application using DDD one of the most important aspects is to provide a semantically elegant and consistent
solution for implementing the DDD pattern elements. 

One of the major changes in Java 16 is the official support for Java [records](https://openjdk.java.net/jeps/359). They are especially designed for classes holding immutable data. Apart from a compact syntax they also provide valid implementations of `equals()`, `hashCode()`, and `toString()`.
Therefore, they are suitable for the following DDD elements.

*   `ValueObject`
*   `DomainEvent`

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

### `IDomainEventPublisher`: Considerations on interface definition

In large applications it is quite common that you have multiple domain events that have to published to other applications.
To solve this issue at least following solutions exist:

*   Method overloading: Provide a specific method for each type of DomainEvent in `IDomainEventPublisher`. On the one side, this ensures static type
    safety but could flood your interface if the number of domain events is quite large. Unfortunately, I've learned that this could also lead to
    implementations in a `DrivenAdapter` in which each domain event is treated in a slightly different way.

*   Abstract `DomainEvent` class: This allows to ensure type safety in `IDomainEventPublisher` and also providing only a single method that is
    implemented in a generic way. This seems to solve all issues from method overloading. The problem with this approach is that you introduce an
    interface that must be implemented by all kind of domain events for technical reason. At first glance, this seems to be a slightly esoteric
    problem. In the long run, I've learned that such classes can be a gate opener, allowing technology aspects to enter the application core.

*   Publishing an `Object`: An alternative solution is to provide a method accepting a domain event of type `Object`. This prevents entering
    technology aspects into the application core. The obvious drawback is that you loose type safety. In case you annotated all your classes you can
    double-check if the domain event is annotated with `DomainEvent`. This prevents publishing arbitrary objects, but this check is performed only
    during runtime.

Of course, you can also combine the approaches. For example, you can use method overloading, and the implementation uses an internal method accepting
an `Object`. Anyway, the most important aspects are:
*   The outbound port is an interface to ensure the separation of your application core from a technology stack.
*   To avoid entering technology aspects into the application core, or vice versa, you should provide a clean guideline how to handle this.

Please do not underestimate such aspects if your application runs for several decades and is maintained by different developer teams. So you should
discuss and document such aspects with your colleagues and/or software architects.

Finally, the following code shows how to use the annotation to add a runtime test.

```java
@DrivenAdapter
public class DomainEventPublisher implements IDomainEventPublisher
{
private final MessageSender messageSender;

    public DomainEventPublisher(Properties properties)
    {
        messageSender = MessageSenderManager.getMessageSender(properties);
    }

    @Override
    public void publish(Object domainEvent)
    {
        validateDomainEvent(domainEvent);
        messageSender
                .send(domainEvent)
                .toTopic("BookStoreTopic")
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
public final class BookStoreJ
{
    public static void main(String[] args)
    {
        // Define the default strategies.
        // In this tutorial the Repository is either an IMDB database or a JDBC based repository.
        // In case of JDBC we use a simple key value approach which stores the key and the value as json strings.
        // Using json strings might be very inconvenient if you come from typical relational databases but in terms
        // of DDD our aggregate is responsible to ensure consistency of our data and not the database.
        RepositoryManager.setDefaultStrategy(getRepositoryStrategy(args));
        // The message sender is either a simple MessageLogger or a JMS sender.
        MessageSenderManager.setDefaultStrategy(getMessagingStrategy(args));

        var jexxaMain = new JexxaMain(BookStoreJ.class);

        jexxaMain
                //Define the default packages for inbound and outbound ports
                .addDDDPackages(BookStoreJ.class)

                //Get the latest books when starting the application
                .bootstrap(ReferenceLibrary.class).with(ReferenceLibrary::addLatestBooks)

                // In case you annotate your domain core with your pattern language,
                // You can also bind DrivingAdapter to annotated classes.
                .bind(RESTfulRPCAdapter.class).toAnnotation(ApplicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

                .start()

                .waitForShutdown()

                .stop();
    }

}  
```

## Run the application  

### Use an in memory database

```console                                                          
mvn clean install
java -jar target/bookstorej-jar-with-dependencies.jar 
```
You will see following (or similar) output
```console
[main] INFO io.jexxa.tutorials.bookstorej.BookStoreJ - Use persistence strategy: IMDBRepository 
[main] INFO io.jexxa.core.JexxaMain - Start BoundedContext 'BookStoreJ' with 2 Driving Adapter 
[main] INFO org.eclipse.jetty.util.log - Logging initialized @474ms to org.eclipse.jetty.util.log.Slf4jLog
[main] INFO io.javalin.Javalin - Starting Javalin ...
[main] INFO io.javalin.Javalin - Listening on http://localhost:7504/
[main] INFO io.javalin.Javalin - Javalin started in 148ms \o/
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'BookStoreJ' successfully started in 0.484 seconds
```          

### Use a Postgres database

You can run this application using a Postgres database because the corresponding driver is included in the pom file. The 
configured username and password is `admin`/`admin`. You can change it in the [jexxa-application.properties](../BookStoreJ/src/main/resources/jexxa-application.properties) 
file if required.       

```console                                                          
mvn clean install
java -jar target/bookstorej-jar-with-dependencies.jar -jdbc 
```
In contrast to the above output Jexxa will state that you use JDBC persistence strategy now:
```console
[main] INFO io.jexxa.tutorials.bookstorej.BookStoreApplication - Use persistence strategy: JDBCKeyValueRepository 
```

Note: In case you want to use a difference database, you have to: 

1.  Add the corresponding jdbc driver to [pom.xml](../BookStoreJ/pom.xml) to dependencies section.
2.  Adjust the section `#Settings for JDBCConnection to postgres DB` in [jexxa-application.properties](../BookStoreJ/src/main/resources/jexxa-application.properties).

### Execute some commands using curl 

#### Get list of books

Command: 
```Console
curl -X GET  http://localhost:7504/BookStoreService/getBooks
```

Response: 
```Console
[{"value":"978-1-891830-85-3"},{"value":"978-1-60309-025-4"},{"value":"978-1-60309-016-2"},{"value":"978-1-60309-265-4"},{"value":"978-1-60309-047-6"},{"value":"978-1-60309-322-4"}]
```

#### Ask if a specific book is in stock**

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '"978-1-891830-85-3"' \
    http://localhost:7504/BookStoreService/inStock                 
```

Response: 
```Console
false
```

#### Add some books

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '["978-1-891830-85-3", 5]' \
    http://localhost:7504/BookStoreService/addToStock                 
```
Response: No output  
```Console
```

#### Ask again if a specific book is in stock

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '"978-1-891830-85-3"' \
    http://localhost:7504/BookStoreService/inStock                 
```

Response: 
```Console
true
```
