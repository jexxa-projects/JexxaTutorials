# TimeService — Flow of Control

## What You Will Learn

* [Overview of Ports & Adapters building blocks](#1-building-blocks-of-ports--adapters)
* [How to follow the flow of control through your application](#2-navigate-through-your-application)
* [An initial understanding of the Dependency Inversion Principle](#3-summary)

---

## Prerequisites

* Completed the tutorials `HelloJexxa` and `TimeService`
* ~45 minutes

---

## 1. Building Blocks of Ports & Adapters

In a **Ports & Adapters (Hexagonal) architecture**, your application is divided into **core logic** and **infrastructure**, connected by clearly defined interfaces.

| Component | Description |
|-----------|-------------|
| **Driving Adapter** | Belongs to the infrastructure. Receives external requests (REST, JMS, RMI, etc.) and forwards them to an **Inbound Port**, driving domain logic. |
| **Inbound Port** | Part of the application core. Represents use cases that can be invoked via a Driving Adapter. |
| **Outbound Port** | Interface in the application core describing services required from the infrastructure (e.g., logging, databases). |
| **Driven Adapter** | Part of the infrastructure. Implements an Outbound Port using a concrete technology stack. It is *driven* by the application core. |

For more details, see [Explicit Architecture – Ports and Adapters](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/).

---

## 2. Navigate Through Your Application

### Traditional Navigation

Reading code line by line works for small projects but **does not scale** for larger applications. For maintainable software, understanding the **flow of control** through your architecture is crucial.

In a Ports & Adapters application, the flow of an incoming command is:

```
Driving Adapter → Inbound Port → Outbound Port → Driven Adapter
```

And the response travels back in reverse:

```
Driving Adapter ← Inbound Port ← Outbound Port ← Driven Adapter
```

### Main Method — Entry Point

Every Java application starts with `main()`. In Jexxa, the main class explicitly **binds Driving Adapters to Inbound Ports**.

```java
public final class TimeService {
    void main(String[] args) {
        jexxaMain
            // Bind RESTfulRPCAdapter and JMXAdapter to TimeApplicationService
            .bind(RESTfulRPCAdapter.class).to(TimeApplicationService.class)
            .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

            // Bind JMSAdapter to listener
            .bind(JMSAdapter.class).to(TimeListener.class);
    }
}
```

**From this code, we know:**

* **Driving Adapters**: `RESTfulRPCAdapter`, `JMSAdapter`
* **Inbound Ports**: `TimeApplicationService`, `BoundedContext`

> Even frameworks like Spring often hide these bindings, but it’s worth understanding them: *code is read many times, written once*.

### Enter the Application Core

Selecting an **Inbound Port** (e.g., `TimeApplicationService`) reveals its constructor:

```java
public class TimeApplicationService {
    public TimeApplicationService(TimePublisher timePublisher, MessageDisplay timeDisplay) {
        // ...
    }
}
```

**Key points:**

* The constructor only takes **Outbound Ports** as parameters.
* Outbound Ports are **interfaces** defining required methods for the core.
* This represents the flow: `Inbound Port → Outbound Port`.

For our example:

* **Inbound Port**: `TimeApplicationService`
* **Required Outbound Ports**: `TimePublisher`, `MessageDisplay`

From here, you can either:

* Dive into `TimeApplicationService` implementation
* Navigate to an Outbound Port (`TimePublisher` / `MessageDisplay`) to follow `Outbound Port → Driven Adapter`

### Leave the Application Core

Example Outbound Port:

```java
public interface MessageDisplay {
    void show(String message);
}
```

* Defines methods from a **domain perspective**
* Must not depend on infrastructure
* Dependency inversion principle applies: `Outbound Port ← Driven Adapter`

Implementation in infrastructure:

```java
public class MessageDisplayImpl implements MessageDisplay {
    @Override
    public void show(String message) {
        JexxaLogger.getLogger(MessageDisplay.class).info(message);
    }
}
```

### Handling Exceptions in Flow

Sometimes an additional layer is needed for mapping, e.g., **asynchronous messages** or **custom REST representation**:

```
Driving Adapter → Port Adapter → Inbound Port → Outbound Port → Driven Adapter
```

Example:

```java
public final class TimeService {
    void main(String[] args) {
        jexxaMain
            .bind(JMSAdapter.class).to(PublishTimeListener.class);
    }
}
```

`PublishTimeListener` constructor:

```java
public class PublishTimeListener {
    public PublishTimeListener(TimeService timeApplicationService) {
        // ...
    }
}
```

Here, the **Port Adapter** maps the JMS message to the Inbound Port without coupling the core to infrastructure.

---

## 3. Summary

* Ports & Adapters separate **core logic** from **infrastructure**
* Flow of control: `Driving Adapter → Inbound Port → Outbound Port → Driven Adapter`
* Dependency inversion principle ensures **Outbound Ports do not depend on implementation**
* Main method bindings make navigation explicit and scalable
* Port Adapters handle exceptions in mapping external requests

This approach allows you to **navigate complex applications efficiently** and **understand the architecture** from both infrastructure and domain perspectives.
