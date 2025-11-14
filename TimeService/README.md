# TimeService - Asynchronous Messaging with Jexxa

This tutorial demonstrates how to build an application with a **clean
separation between domain logic and technology stacks** using Jexxa.
We implement two simple use cases:

-   **Use Case 1:** Publish the current time.
-   **Use Case 2:** Receive and display the published time.

------------------------------------------------------------------------

## What You Will Learn

-   [How to implement an application core defining the use
    cases](#1-implement-the-application-core)
-   [How to implement required driven
    adapters](#2-implement-the-infrastructure)
-   [How to implement required driving
    adapters](#3-receive-localtime-again-driving-adapter)
-   [How to compose the full application with
    Jexxa](#4-implement-the-application)

------------------------------------------------------------------------

## Requirements

Before starting, ensure that you:

-   Understand the `HelloJexxa` tutorial (this tutorial focuses on new
    concepts)
-   Have approx. *60 minutes*
-   Installed **JDK 25** or higher
-   Installed **Maven 3.6** or higher
-   Have a running **ActiveMQ** instance (for running with real
    infrastructure)
-   Installed **curl** (for triggering the application)

------------------------------------------------------------------------

## Motivation

This tutorial illustrates how to structure your application following
Domain-Driven Design (DDD) principles and how Jexxa helps to keep your
domain logic clean and technology‑agnostic.

We deliberately follow two small use cases that demonstrate:

1.  Publishing the current local time
2.  Receiving the published time and displaying it

------------------------------------------------------------------------

## 0. Project Setup

To organize the code following DDD, we use the following package
structure:

-   `applicationservice` - Declares application services for each use
    case
-   `domainservice` - Contains domain-specific services that do not
    belong to a single entity
-   `domain` - Holds core domain objects (not required for this
    tutorial)
-   `infrastructure` - Implements driven and driving adapters

Inside `infrastructure`, we create:

-   `drivenadapter/display`
-   `drivenadapter/messaging`
-   `drivingadapter/messaging`

Example structure:

    (io.jexxa.tutorials.timeservice)
        applicationservice
        domain
        domainservice
        infrastructure
        |    drivenadapter
        |    |    display
        |    |    messaging
        |    drivingadapter
        |    |    messaging
        Main.java

------------------------------------------------------------------------

## 1. Implement the Application Core

The application core stays free from any technology‑specific details.

### `TimePublisher` interface

Located in `domainservice`.\
Responsible for publishing a `LocalTime`.

``` java
public interface TimePublisher {
    void publish(LocalTime localTime);
}
```

### `TimeDisplay` interface

Located in `domainservice`.\
Responsible for displaying a received `LocalTime`.

``` java
public interface TimeDisplay {
    void show(LocalTime localTime);
}
```

### `TimeApplicationService`

Implements both use cases by delegating to the interfaces above.

``` java
public class TimeApplicationService {
    private final TimePublisher timePublisher;
    private final TimeDisplay timeDisplay;

    public TimeApplicationService(TimePublisher timePublisher, TimeDisplay timeDisplay) {
        this.timePublisher = Objects.requireNonNull(timePublisher);
        this.timeDisplay = Objects.requireNonNull(timeDisplay);
    }

    public void publishTime() {
        timePublisher.publish(LocalTime.now());
    }

    public void showReceivedTime(LocalTime localTime) {
        timeDisplay.show(localTime);
    }
}
```

------------------------------------------------------------------------

## 2. Implement the Infrastructure

### `TimeDisplayImpl` (Driven Adapter)

Logs the received time to the console.

``` java
public class TimeDisplayImpl implements TimeDisplay {
    public void show(LocalTime localTime) {
        var msg = "New time received: " + localTime.format(DateTimeFormatter.ISO_TIME);
        SLF4jLogger.getLogger(TimeDisplayImpl.class).info(msg);
    }
}
```

### `TimePublisherImpl` (Driven Adapter)

Publishes times to a JMS topic (`TimeService`).

``` java
public class TimePublisherImpl implements TimePublisher {
    public static final String TIME_TOPIC = "TimeService";
    private final MessageSender messageSender;

    public TimePublisherImpl(Properties properties) {
        this.messageSender = getMessageSender(TimePublisher.class, properties);
    }

    @Override
    public void publish(LocalTime localTime) {
        messageSender
                .send(localTime)
                .toTopic(TIME_TOPIC)
                .asJson();
    }
}
```

Property configuration (`jexxa-application.properties`):

``` properties
java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory
java.naming.provider.url=tcp://localhost:61616
java.naming.user=admin
java.naming.password=admin
```

------------------------------------------------------------------------

## 3. Receive LocalTime Again (Driving Adapter)

We implement a JMS listener that calls the Application Service once a
message is received.

``` java
public final class TimeListener extends TypedMessageListener<LocalTime> {
    private final TimeApplicationService timeApplicationService;
    private static final String TIME_TOPIC = "TimeService";

    public TimeListener(TimeApplicationService timeApplicationService) {
        super(LocalTime.class);
        this.timeApplicationService = timeApplicationService;
    }

    @Override
    @JMSConfiguration(destination = TIME_TOPIC, messagingType = TOPIC)
    public void onMessage(LocalTime localTime) {
        timeApplicationService.showReceivedTime(localTime);
    }
}
```

------------------------------------------------------------------------

## 4. Implement the Application

Finally, we bootstrap Jexxa and bind all required adapters.

``` java
public final class TimeService {
    public static void main(String[] args) {
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())
                .bind(JMSAdapter.class).to(TimeListener.class)
                .run();
    }
}
```

------------------------------------------------------------------------

## Running the Application (Console Mode)

``` bash
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties"      ./target/timeservice-jar-with-dependencies.jar
```

You can publish a time using:

``` bash
curl -X POST http://localhost:7502/TimeApplicationService/publishTime
```

------------------------------------------------------------------------

## Running the Application with JMS

``` bash
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties"      ./target/timeservice-jar-with-dependencies.jar
```

Publishing a time via curl:

``` bash
curl -X POST http://localhost:7502/TimeApplicationService/publishTime
```

------------------------------------------------------------------------

## Summary

You have successfully implemented:

-   A clean Application Core
-   Driven Adapters (JMS publisher, console display)
-   A Driving Adapter (JMS listener)
-   An assembled application using Jexxa

This example demonstrates how Jexxa enforces clear architecture
boundaries and simplifies working with infrastructure components such as
JMS.
