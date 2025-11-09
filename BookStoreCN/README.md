# BookStoreCN — Using typical cloud native technology

# TODO: REWRITE 

## What You Learn

*   [How to use an S3 storage for repositories]()
*   [How to publish DomainEvents using an event streaming platform](#1-Implementing-application-core)
*   [How to receive (Domain)Events using an event streaming platform](#2-publishing-domainevents)      
(#3-implement-the-infrastructure)

## What you need

*   Understand tutorial `BookStore`
*   60 minutes
*   JDK 25 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   Optional: A postgres DB   

## How to use an S3 storage for repositories
The properties file [jexxa-application.properties](src/main/resources/jexxa-application.properties)

- remove all properties starting with io.jexxa.jdbc

- add the following properties 
# Settings for S3-Repository connection
io.jexxa.s3.endpoint=http://s3-storage:8100
io.jexxa.s3.bucket=bookstore-cn
io.jexxa.s3.access-key=minioadmin
io.jexxa.s3.secret-key=minioadmin
io.jexxa.s3.path-style-access=true


The properties file [jexxa-test.properties](src/test/resources/jexxa-test.properties) is configured to use a local s3.endpoint 
. So we have to enter the following command 

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" ./target/bookstore-jar-with-dependencies.jar
```
In contrast to the above output, Jexxa will state that you use JDBC persistence strategy now:
```console
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Context Version                : VersionInfo[version=2.0.50-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/bookstorecn, projectName=BookStoreCN, buildTimestamp=2025-11-03 06:42]
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Driving Adapter           : [KafkaAdapter, RESTfulRPCAdapter]
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-test.properties]
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Repository Strategie      : [S3KeyValueRepository]
[2025-11-09T13:00Z] INFO io.jexxa.common.facade.logger.ApplicationBanner - Used Message Sender Strategie  : [KafkaSender]
```

### Execute some commands using curl 

#### Get a list of all books

Command: 
```Console
curl -X GET  http://localhost:7506/BookStoreService/getBooks
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
     http://localhost:7506/BookStoreService/inStock       
```

Response: 
```Console
false
```

#### Add some books
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d "[{isbn13: "978-1-891830-85-3"}, 5]" \
     http://localhost:7506/BookStoreService/addToStock
```

Response: No output  
```Console
```

#### Ask again if a specific book is in stock
Command:
```Console
curl -X POST -H "Content-Type: application/json" -d '{isbn13:"978-1-891830-85-3"}' \
     http://localhost:7506/BookStoreService/inStock       
```

Response: 
```Console
true
```
