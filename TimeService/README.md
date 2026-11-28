# TimeService: Asynchronous Messaging with Jexxa

In about 60 minutes you build an application that publishes the current time as a message and receives it again. The point is not the clock. The point is that your business logic stays free of any messaging code, and you can prove it.

**What you build**

```
                    ┌──────────────── Application Core ────────────────┐
 curl ──REST──▶ RESTfulRPCAdapter ──▶ TimeApplicationService            │
                                          │            ▲                │
                      uses interfaces:    ▼            │                │
                                    TimePublisher   TimeDisplay         │
                    └─────────────────────┬────────────┬────────────────┘
                                          ▼            ▼
                                 TimePublisherImpl  TimeDisplayImpl
                                  (sends to JMS)     (writes to log)
                                          │
                                       JMS topic
                                          │
                                          ▼
 TimeListener (driving adapter) ──▶ TimeApplicationService.showReceivedTime(...)
```

**Two use cases**

1. Publish the current time (triggered via REST).
2. Receive the published time and display it (triggered by a JMS message).

**Requirements**

- You completed the **HelloJexxa** tutorial. Binding adapters and REST are not explained again here.
- JDK 25 or higher, Maven 3.6+, `curl`
- Optional: a running ActiveMQ (Artemis) broker, only for the last step

---

## The New Concepts

| Term                 | Meaning                                                                 | In this tutorial                                         |
|----------------------|-------------------------------------------------------------------------|----------------------------------------------------------|
| **Application core** | Use cases and the interfaces they need. No technology.                  | `TimeApplicationService`, `TimePublisher`, `TimeDisplay` |
| **Driven adapter**   | Technology that your core *calls* by implementing one of its interfaces | `TimePublisherImpl`, `TimeDisplayImpl`                   |
| **Driving adapter**  | Technology that *calls* your core                                       | `RESTfulRPCAdapter`, `TimeListener`                      |

The **dependency rule** is the most important sentence of this tutorial: infrastructure depends on the core, never the other way around. The core defines what it needs (`TimePublisher`), and the infrastructure fulfills it. Because of this, you can replace JMS by something else without touching a line of the core.

---

## Step 0: Project Layout

Create a Maven project as in HelloJexxa. The `pom.xml` is the same, plus the JMS dependency required by your broker. Then create this package structure:

```
io.jexxa.tutorials.timeservice
├── applicationservice          use cases
├── domainservice               interfaces the core needs
├── domain                      domain objects (empty in this tutorial)
├── infrastructure
│   ├── drivenadapter
│   │   ├── display             TimeDisplayImpl
│   │   └── messaging           TimePublisherImpl
│   └── drivingadapter
│       └── messaging           TimeListener
└── TimeService.java            composition of everything
```

Notice the split: everything outside `infrastructure` is the application core.

> Imports are omitted in the code below for brevity. Your IDE resolves them.

---

## Step 1: Write the Application Core

Start with the core, before any technology is chosen.

**The two interfaces** (package `domainservice`):

```java
public interface TimePublisher {
    void publish(LocalTime localTime);
}
```

```java
public interface TimeDisplay {
    void show(LocalTime localTime);
}
```

**The use cases** (package `applicationservice`):

```java
public class TimeApplicationService {
    private final TimePublisher timePublisher;
    private final TimeDisplay timeDisplay;

    public TimeApplicationService(TimePublisher timePublisher, TimeDisplay timeDisplay) {
        this.timePublisher = Objects.requireNonNull(timePublisher);
        this.timeDisplay = Objects.requireNonNull(timeDisplay);
    }

    // Use case 1
    public void publishTime() {
        timePublisher.publish(LocalTime.now());
    }

    // Use case 2
    public void showReceivedTime(LocalTime localTime) {
        timeDisplay.show(localTime);
    }
}
```

The service only knows the interfaces. It receives them through its constructor, and Jexxa provides matching implementations when it creates the service.

### Prove It: Test the Core Without Any Infrastructure

Both interfaces have a single method, so lambdas are enough as test doubles (JUnit 5 required):

```java
class TimeApplicationServiceTest {
    @Test
    void publishTimeSendsCurrentTime() {
        var published = new ArrayList<LocalTime>();
        var service = new TimeApplicationService(published::add, time -> {});

        service.publishTime();

        assertEquals(1, published.size());
    }
}
```

**Checkpoint:** the test passes. You tested a use case without a broker, a port or a framework.

---

## Step 2: Implement the Driven Adapters

### Display: writes to the log

```java
public class TimeDisplayImpl implements TimeDisplay {
    @Override
    public void show(LocalTime localTime) {
        var msg = "New time received: " + localTime.format(DateTimeFormatter.ISO_TIME);
        SLF4jLogger.getLogger(TimeDisplayImpl.class).info(msg);
    }
}
```

### Publisher: sends to a JMS topic

```java
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

The `Properties` constructor argument carries the connection settings from `jexxa-application.properties`. Your adapter never hard-codes them:

```properties
java.naming.factory.initial=org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory
java.naming.provider.url=tcp://localhost:61616
java.naming.user=admin
java.naming.password=admin
```

---

## Step 3: Implement the Driving Adapter

The listener is the mirror image of the publisher: it receives a message and calls the application service.

```java
public final class TimeListener extends TypedMessageListener<LocalTime> {
    private static final String TIME_TOPIC = "TimeService";
    private final TimeApplicationService timeApplicationService;

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

The listener contains no logic. It translates "a message arrived" into "call this use case".

> The topic name appears twice, in publisher and listener. Both adapters live in the infrastructure, and they must agree on it. Keeping the constant out of the core is intentional: the topic is a technical detail.

---

## Step 4: Compose the Application

```java
public final class TimeService {
    static void main(String[] args) {
        var jexxaMain = new JexxaMain(TimeService.class);

        jexxaMain
                .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)  // trigger use case 1
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext()) // health check
                .bind(JMSAdapter.class).to(TimeListener.class)                   // trigger use case 2
                .run();
    }
}
```

There are three bindings and no wiring code. Jexxa finds the implementations of `TimePublisher` and `TimeDisplay` in your packages and passes them to the `TimeApplicationService` constructor.

Compare this with HelloJexxa. Only the number of adapters has grown; the principle is the same.

---

## Step 5: Run It

The application can run in two modes. Which one you get is decided only by the properties file you import, not by code.

### Mode A: Local, without a broker

```bash
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties" \
     ./target/timeservice-jar-with-dependencies.jar
```

Trigger use case 1:

```bash
curl -X POST http://localhost:7502/TimeApplicationService/publishTime
```

**Checkpoint:** the console shows `New time received: ...`.

### Mode B: With a real ActiveMQ broker

Start a broker whose credentials match your properties (user `admin`, password `admin`). For example with Docker:

```bash
docker run --rm -p 61616:61616 -p 8161:8161 \
  -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin \
  apache/activemq-artemis:latest
```

Then start the application with the other configuration and repeat the `curl` call:

```bash
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" \
     ./target/timeservice-jar-with-dependencies.jar
```

**Checkpoint:** you get the same log output as in Mode A. The application code is identical, and only the configuration and the infrastructure behind it changed.

---

## What You Have Learned

- The core declares what it needs as interfaces, and adapters implement them.
- Driven adapters are called by the core, and driving adapters call the core.
- Jexxa composes the application with `bind(...).to(...)` and injects implementations through constructors.
- The same code runs against different infrastructure, selected by configuration.

---

## Troubleshooting

| Problem                                 | Likely cause and fix                                                                        |
|-----------------------------------------|---------------------------------------------------------------------------------------------|
| `Connection refused` on port 61616      | Mode B only: the broker is not running, or the URL in the properties is wrong.              |
| `curl` fails to connect on port 7502    | The application did not start, or the REST port in your properties file differs.            |
| Request succeeds, but nothing is logged | Check that `TimeListener` is bound and that publisher and listener use the same topic name. |
| `Unable to access jarfile`              | Run `mvn clean install` first and check the jar name in `target/`.                          |

---

## Try It Yourself

1. Write a second `TimeDisplay` implementation that prints to `System.out`. What do you have to change in the core? (Hint: nothing.)
2. Add a second use case, for example publishing a time on request with a given `LocalTime`. Which layers do you touch?
3. Replace the JMS publisher by a fake that stores times in a list. Which classes stay unchanged?

---

## Next Steps

| Goal                        | Resource                                                             |
|-----------------------------|----------------------------------------------------------------------|
| Start a real project        | [JexxaArchetypes](https://github.com/jexxa-projects/JexxaArchetypes) |
| Learn the concepts in depth | https://www.jexxa.io                                                 |