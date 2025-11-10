# BookStoreCN — Using Cloud-Native Technology with Jexxa

## What You Will Learn

In this tutorial, you will learn how to extend the BookStore example using cloud-native infrastructure components:

*   How to use S3-compatible object storage as a persistence layer for repositories
*   How to publish Domain Events using an event-streaming platform
*   How to consume Domain Events using an event-streaming platform

---

## Prerequisites

Before you begin, you should have:

*   Completed and understood the **BookStore** tutorial
*   ~60 minutes of time
*   JDK 25 (or higher) installed
*   Maven 3.6 (or higher) installed
*   `curl` to interact with the running application
*    A running S3-Storage

---

## Using S3 Storage for Repositories

To switch from JDBC-based persistence to an S3-compatible object store, open the file  
[`jexxa-application.properties`](src/main/resources/jexxa-application.properties) and update it as follows:

1. **Remove all properties starting with**:
   ```
   io.jexxa.jdbc
   ```

2. **Add the following S3 configuration and adjust values as needed**:

```properties
# Settings for S3-Repository connection
io.jexxa.s3.endpoint=http://s3-storage:8100
io.jexxa.s3.bucket=bookstore-cn
io.jexxa.s3.access-key=minioadmin
io.jexxa.s3.secret-key=minioadmin
io.jexxa.s3.path-style-access=true
```

### When Should You Use S3 Storage for DDD Aggregates?

S3-based storage is particularly useful when:

| ✅ Good Fit | ❌ Not Recommended |
|-------------|---------------------|
| Aggregates are relatively small but numerous | Aggregates need complex or cross-entity transactions |
| You need cheap, highly durable storage | Strong consistency guarantees are required |
| Your service runs in a cloud environment and persistence must scale horizontally | You need high-performance read-write operations with low latency |
| Event-sourced architecture or immutable document storage is preferred | You rely on relational queries or multi-aggregate joins |

**Rule of thumb:**  
Use S3 storage when your aggregates can be represented as independent documents that don’t require transactional consistency across entities.

---

## Publishing Domain Events Using an Event-Streaming Platform

Publishing Domain Events works similarly to sending messages.  
The only difference: instead of requesting a `MessageSender`, you request an `EventSender`.

```java
@DrivenAdapter
public class IntegrationEventSenderImpl implements IntegrationEventSender {
    private final EventSender eventSender;

    public IntegrationEventSenderImpl(Properties properties)
    {
        // Request an EventSender and configure it using the application's properties
        eventSender = createEventSender(IntegrationEventSender.class, properties);
    }

    @Override
    public void publish(BookSoldOut domainEvent)
    {
        // Publishing a DomainEvent in Jexxa is done via a fluent API
        eventSender
                .send(domainEvent)
                // For events, it is strongly recommended to use a key (usually the aggregateId)
                .toTopic("BookStore")
                .addHeader("Type", domainEvent.getClass().getSimpleName())
                .asJSON();
    }
}
```

---

## Receiving Domain Events Using an Event-Streaming Platform

Consuming Domain Events is equally straightforward.

### Step 1: Implement an Event Listener

```java
@DrivingAdapter
public class BookSoldOutListener extends TypedEventListener<ISBN13, BookSoldOut> {
    private final BookStoreService bookStoreService;

    public BookSoldOutListener(BookStoreService bookStoreService)
    {
        super(ISBN13.class, BookSoldOut.class);
        this.bookStoreService = bookStoreService;
    }

    @Override
    protected void onEvent(BookSoldOut value) {
        SLF4jLogger.getLogger(BookSoldOutListener.class)
                .warn("Book with ISBN {} is sold out", value.isbn13());
    }

    @Override
    public String topic() {
        return "BookStore";
    }
}
```

### Step 2: Register the Listener in the Application

```java
jexxaMain
        // ...
        .bind(KafkaAdapter.class).to(BookSoldOutListener.class)
        .run(); // Start the application
```

---

## Running the Example

The file  
[`jexxa-test.properties`](src/test/resources/jexxa-test.properties)  
is preconfigured to use:

* A local S3 endpoint (e.g., MinIO)
* A local Kafka instance

Run the following command to start the application:

```console
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" ./target/bookstore-jar-with-dependencies.jar
```

On startup, you should now see that Jexxa uses the **S3** persistence strategy:

```console
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Repository Strategy      : [S3KeyValueRepository]
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Message Sender Strategy  : [KafkaSender]
```

---

## Using the API via `curl`

### List all books

```console
curl -X GET http://localhost:7506/BookStoreService/getBooks
```

**Response:**
```json
[
 {"isbn13":"978-1-60309-322-4"},
 {"isbn13":"978-1-891830-85-3"},
 {"isbn13":"978-1-60309-047-6"},
 {"isbn13":"978-1-60309-025-4"},
 {"isbn13":"978-1-60309-016-2"},
 {"isbn13":"978-1-60309-265-4"}
]
```

### Check if a book is in stock

```console
curl -X POST -H "Content-Type: application/json"      -d '{"isbn13":"978-1-891830-85-3"}'      http://localhost:7506/BookStoreService/inStock
```

**Response:**
```json
false
```

### Add books to stock

```console
curl -X POST -H "Content-Type: application/json"      -d '[{"isbn13":"978-1-891830-85-3"}, 5]'      http://localhost:7506/BookStoreService/addToStock
```

(no response body)

### Check stock again

```console
curl -X POST -H "Content-Type: application/json"      -d '{"isbn13":"978-1-891830-85-3"}'      http://localhost:7506/BookStoreService/inStock
```

**Response:**
```json
true
```
