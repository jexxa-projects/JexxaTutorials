# TimeService - Async Messaging

## What You Learn

*   [How to write an application core providing some use cases](#1-implement-the-application-core) 
*   [How to implement required driven adapter](#2-implement-the-infrastructure)
*   [How to implement required driving adapter](#3-receive-localtime-again)
*   [How to implement the application using application specific driving and driven adapter](#4-implement-the-application)

## What you need

*   Understand tutorial `HelloJexxa` because we explain only new aspects 
*   60 minutes
*   JDK 17  (or higher) installed 
*   Maven 3.6 (or higher) installed
*   A running ActiveMQ instance (at least if you start the application with infrastructure)
*   curl to trigger the application  

## Motivation

This application shows the strict separation from domain logic and technology stacks by following two use cases:  

* Use case 1: A user can publish the current time in some way. 
* Use case 2: A published time is shown to the user in some way. 

## 0. Setup

In order to organize our code we will base our package structure on the concepts of Domain Driven Design.
Those concepts are as follows:

* `applicationservice`: Provides interfaces per application scenario.
* `domainservice`: Provides specialized domain logic that cannot be attributed to a single entity or value object within the domain.
* `domain`: Provides the core domain typically grouped by use cases.
* `infrastructure`: Provides implementations to the ports and adapters defined by the interfaces in the applicationservice.

Because we do not have an extensive domain, we can ignore it at the moment.
In order to complete the tutorial you will have to create the following package structure:

First create the packages `applicationservice`, `domainservice` and `infrastructure` in the same folder as your Main-file.
Then create the packages `drivenadapter` and `drivingadapter` in the `infrastructure` package.
Finally you will need to create the packages `display` and `messaging` in the `drivenadapter` package and the package `messaging` in the `drivingadapter` package.

Your package structure should now look similar to this:
``` 
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

```


## 1. Implement the Application Core

In a first step, we ignore the technology stacks and focus on the domain logic which rests in our application core.

*   [`TimeApplicationService:`](src/main/java/io/jexxa/tutorials/timeservice/applicationservice/TimeApplicationService.java) This class implements both use cases because they belong to the same user. So in terms of DDD this class is an `ApplicationService`.
*   [`TimePublisher:`](src/main/java/io/jexxa/tutorials/timeservice/domainservice/TimePublisher.java) Is responsible for publishing the time from the first used case. Since we do not know which technology is used, we can only declare an interface providing the required methods. So in terms of DDD, this is an `InfrastructureService` which is a special type of  `DomainService` because its implementation can only be done within the infrastructure.
*   [`TimeDisplay:`](src/main/java/io/jexxa/tutorials/timeservice/domainservice/TimeDisplay.java) Is responsible for showing a received time to the user. Since we do not know which technology is used, we can only declare an interface providing the required methods. So in terms of DDD, this is an `InfrastructureService` which is a special type of  `DomainService` because its implementation can only be done within the infrastructure.

The important aspect here is that we use the concept of an interface to separate our application core from technology specific details.
So lets start programming...

### Interface `TimePublisher` 

Declare the interface `TimePublisher` in sub-package `domainservice` that allows to publish a `LocalTime`.

```java
public interface TimePublisher
{
    void publish(LocalTime localTime);
}
```                 

### Interface `TimeDisplay`
Declare the interface `TimeDisplay` in sub-package `domainservice` that allows to show a `LocalTime`.

```java
public interface TimeDisplay
{
    void show(LocalTime localTime);
}
```      
  
### Implement class `TimeApplicationService`

Implement `TimeApplicationService` in sub-package `applicationservice` that uses the previous declared interfaces to realize our use cases.  


```java
public class TimeApplicationService
{
    private final TimePublisher timePublisher;
    private final TimeDisplay timeDisplay;

    /**
     * This class need a {@link TimePublisher} and {@link TimeDisplay} for proper working. Therefore, we must
     * declare all required interfaces in the constructor.
     *
     * @param timePublisher used to publish time.
     * @param timeDisplay used to show a received time 
     */
    public TimeApplicationService(TimePublisher timePublisher, TimeDisplay timeDisplay)
    {
        this.timePublisher = Objects.requireNonNull(timePublisher);
        this.timeDisplay = Objects.requireNonNull(timeDisplay);
    }

    /**
     * Implements use case 1: publish current time
     */
    public void publishTime()
    {
        timePublisher.publish(LocalTime.now());
    }

    /**
     * Implements use case 2 : Shows the previously published time.
     * @param localTime the previously published time
     */
    public void showReceivedTime(LocalTime localTime)
    {
        timeDisplay.show(localTime);
    }
}
```                  

## 2. Implement the Infrastructure

### `TimeDisplayImpl` implements `TimeDisplay`   
[`TimeDisplayImpl`](src/main/java/io/jexxa/tutorials/timeservice/infrastructure/drivenadapter/display/TimeDisplayImpl.java) is located in package `infrastructure/drivenadapter/display` and logs received time to console. So the implementation is quite simple.    

```java
public class TimeDisplayImpl implements TimeDisplay
{
    public void show(LocalTime localTime)
    {
        var messageWithPublishedTime = "New Time was published, time: " + localTime.format(DateTimeFormatter.ISO_TIME);
        SLF4jLogger.getLogger(TimeDisplayImpl.class).info(messageWithPublishedTime);
    }
}
```

### `TimePublisherImpl` implements `TimePublisher`
[`TimePublisherImpl`](src/main/java/io/jexxa/tutorials/timeservice/infrastructure/drivenadapter/messaging/TimePublisherImpl.java) is located in package `infrastructure/drivenadapter/messaging` and sends the time to topic `TimeService` of a JMS broker.

Jexxa provides infrastructure components for various Java-APIs such as JMS. When using these components the
implementation of a driven adapter is quite easy.

```java
public class TimePublisherImpl implements TimePublisher
{
    public static final String TIME_TOPIC = "TimeService";

    private final MessageSender messageSender;

    /**
     * Creates a TimePublisher sending LocalTime to a JMS broker 
     *
     * @param properties contains all required configuration information of our JMS broker
     */
    public TimePublisherImpl(Properties properties)
    {
        //Request a message sender for the implemented interface TimePublisher 
        this.messageSender = getMessageSender(TimePublisher.class, properties);
    }

    @Override
    public void publish(LocalTime localTime)
    {
        // For most integrated standard APIs, Jexxa provides a fluent API to improve readability
        // and to emphasize the purpose of the code
        messageSender
                .send(localTime)
                .toTopic(TIME_TOPIC)
                .asJson();
    }
}
```

In order to configure your application for a specific message broker, we define all required information in [`jexxa-application.properties`](src/main/resources/jexxa-application.properties): 

```properties
#suppress inspection "UnusedProperty" for whole file
#Settings for JMSAdapter and JMSSender
java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory
java.naming.provider.url=tcp://localhost:61616
java.naming.user=admin
java.naming.password=admin
```                       

## 3. Receive LocalTime again
Now, we have to implement the driving adapter `TimeListener` which receives published time information.  
It is located in package `infrastructure/drivingadapter/messaging`.

### Implement TimeListener
When receiving asynchronous messages we have to:
1.  Know and declare the object from our application core processing received data
2.  Convert received data into business data
3.  Define the connection information how to receive the data
4.  Forward it to a specific method within the application core. 
 
Implementing a port adapter for JMS using Jexxa is quite easy.

```java
/**
 * 1. Within the constructor we define our class from the application core that will be called. Jexxa automatically
 * injects this object when creating the port adapter. By convention, this is the only object defined in the
 * constructor.
 * <p>
 * 2. In case of JMS we have to implement the JMS specific `MessageListener` interface. To facilitate this, Jexxa offers
 * convenience classes such as TypedMessageListener which perform JSON deserialization into a defined type.
 * <p>
 * 3. The JMS specific connection information is defined as annotation at the onMessage method. 
 * <p>
 * 4. Finally, the implementation of this method just forwards received data to the application service.
 */
public final class TimeListener extends TypedMessageListener<LocalTime> {
    private final TimeApplicationService timeApplicationService;
    private static final String TIME_TOPIC = "TimeService";

    public TimeListener(TimeApplicationService timeApplicationService) {
        super(LocalTime.class);
        this.timeApplicationService = timeApplicationService;
    }

    @Override
    // The JMS specific configuration is defined via annotation.
    @JMSConfiguration(destination = TIME_TOPIC,  messagingType = TOPIC)
    public void onMessage(LocalTime localTime) {
        // Forward this information to corresponding application service.
        timeApplicationService.showReceivedTime(localTime);
    }
}
```

## 4. Implement the Application ##

Finally, we have to write our application. As you can see in the code below, the only difference compared to `HelloJexxa`
is that we bind a JMSAdapter to our TimeListener.    
   
```java
public final class TimeService
{
    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                // Bind RESTfulRPCAdapter and JMXAdapter to TimeService class so that we can invoke its method
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())
                
                // Bind the JMSAdapter to our TimeListener
                .bind(JMSAdapter.class).to(TimeListener.class)

                .run();
    }
}
```  

That's it. 

## Run the Application with console output ##

Disabling of all infrastructure components can be done by property files. By convention, Jexxa tries to find a real implementation of infrastructure components such as a database or messaging system. If they are not configured, Jexxa falls back to dummy implementation that are suitable for local testing.    

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties" ./target/timeservice-jar-with-dependencies.jar
```
You will see following (or similar) output
```console
[main] INFO io.jexxa.utils.JexxaBanner - Config Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.0.1-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2022-06-24 05:10]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=1.0.20-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/timeservice, projectName=TimeService, buildTimestamp=2022-06-24 16:53]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [JMSAdapter, RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-local.properties]
[main] INFO io.jexxa.utils.JexxaBanner - Used Message Sender Strategie  : [MessageLogger]
[main] INFO io.jexxa.utils.JexxaBanner - 
[main] INFO io.jexxa.utils.JexxaBanner - Access Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7502
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: http://0.0.0.0:7502/swagger-docs
[main] INFO io.jexxa.utils.JexxaBanner - JMS Listening on  : tcp://ActiveMQ:61616
[main] INFO io.jexxa.utils.JexxaBanner -    * JMS-Topics   : []
[main] INFO io.jexxa.utils.JexxaBanner -    * JMS-Queues   : []
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'TimeService' successfully started in 1.964 seconds


```          

### Publish the time  with console output

You can use curl to publish the time.  
```Console
curl -X POST http://localhost:7502/TimeApplicationService/publishTime
```

Each time you execute curl you should see following output on the console: 

```console                                                          
[qtp380242442-31] INFO io.jexxa.infrastructure.messaging.logging.MessageLogger - Begin> Send message
[qtp380242442-31] INFO io.jexxa.infrastructure.messaging.logging.MessageLogger - Message           : {"hour":17,"minute":12,"second":34,"nano":873658000}
[qtp380242442-31] INFO io.jexxa.infrastructure.messaging.logging.MessageLogger - Destination       : TimeService
[qtp380242442-31] INFO io.jexxa.infrastructure.messaging.logging.MessageLogger - Destination-Type  : TOPIC
[qtp380242442-31] INFO io.jexxa.infrastructure.messaging.logging.MessageLogger - End> Send message
```

## Run the Application with JMS
Running the application with a locally messaging system is typically required for testing and developing purpose. Therefore, we use the file [jexxa-test.properties](src/test/resources/jexxa-test.properties). 

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" ./target/timeservice-jar-with-dependencies.jar
```
You will see following (or similar) output
```console
...
[main] INFO io.jexxa.utils.JexxaBanner - Config Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.0.1-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2022-06-24 05:10]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=1.0.20-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/timeservice, projectName=TimeService, buildTimestamp=2022-06-24 16:53]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [JMSAdapter, RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-test.properties]
[main] INFO io.jexxa.utils.JexxaBanner - Used Message Sender Strategie  : [JMSSender]
[main] INFO io.jexxa.utils.JexxaBanner - 
[main] INFO io.jexxa.utils.JexxaBanner - Access Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7502
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: http://0.0.0.0:7502/swagger-docs
[main] INFO io.jexxa.utils.JexxaBanner - JMS Listening on  : tcp://localhost:61616
[main] INFO io.jexxa.utils.JexxaBanner -    * JMS-Topics   : [TimeService]
[main] INFO io.jexxa.utils.JexxaBanner -    * JMS-Queues   : []
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'TimeService' successfully started in 2.223 seconds
... 
```          

As you can see in the last two lines, we now use the `JMSSender` which is listening on Topic TimeService. 

### Publish the time with JMS ###
 
You can use curl to publish the time.  
```Console
curl -X POST http://localhost:7502/TimeApplicationService/publishTime
```

Each time you execute curl you should see following output on the console: 

```console                                                          
[ActiveMQ Session Task-1] INFO io.jexxa.tutorials.timeservice.infrastructure.drivenadapter.display.TimeDisplayImpl - New Time was published, time: 17:15:18.743772
```
