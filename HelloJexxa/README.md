# HelloJexxa — A First Steps Tutorial

Welcome to **HelloJexxa**, a beginner-friendly tutorial that guides you through building your first application using the **Jexxa Framework**.

Jexxa helps you structure your application with a strong focus on **hexagonal architecture (Ports & Adapters)**. This tutorial keeps things simple and shows how to:

1. Build a minimal Jexxa application with one business method
2. Expose it via REST
3. Add a basic web page that interacts with your backend

---

## ✅ What You Will Learn

By completing this tutorial, you will learn:

| Step | Topic                                         |
|------|-----------------------------------------------|
| 1    | Create and run a basic Jexxa application      |
| 2    | Expose business logic via REST                |
| 3    | Add a static web page that calls your backend |

---

## 🧰 Requirements

Before you start, ensure you have:

- ⏱ **~30 minutes**
- ☕ **JDK 25 or higher**
- 🧱 **Maven 3.6+**
- 🌐 A web browser
- 🧪 `curl` (optional, for testing endpoints)

---

## 1️⃣ Create & Run Your First Jexxa Application

### 1. Create a Maven Project

Create a new Maven project using your IDE or `mvn archetype:generate`.  
Add the following dependencies in your `pom.xml`:

```xml
<project>
    <!-- ... your existing Maven project configuration ... -->

    <dependencies>
        <!-- Jexxa REST support -->
        <dependency>
            <groupId>io.jexxa</groupId>
            <artifactId>jexxa-web</artifactId>
            <version>9.0.0</version>
        </dependency>

        <!-- Log output to the console -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.17</version>
        </dependency>
    </dependencies>
</project>
```

---

### 2. Write the Main Application

Create the class `HelloJexxa.java`:

```java
import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;

public final class HelloJexxa
{
    // Simple business logic
    public String greetings()
    {
        return "Hello Jexxa";
    }

    static void main(String[] args)
    {
        // Initialize Jexxa for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
            // Expose your application via REST
            .bind(RESTfulRPCAdapter.class).to(HelloJexxa.class)               // Call via: http://localhost:7501/HelloJexxa/greetings
            .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())  // Call via: http://localhost:7501/BoundedContext/isRunning

            // Start the application (stop using Ctrl+C)
            .run();
    }
}
```

---

### 3. Configure the Application

Add a file named `jexxa-application.properties` to:

```
src/main/resources/jexxa-application.properties
```

Add:

```properties
# IP address to listen on (0.0.0.0 = all interfaces)
io.jexxa.rest.host=0.0.0.0

# REST port
io.jexxa.rest.port=7501
```

---

### 4. Build & Run

Run:

```bash
mvn clean install
```

Start the application from your IDE or via:

```bash
java -jar target//hellojexxa-jar-with-dependencies.jar
```

Expected console output:

```console
INFO  ... JexxaBanner - Listening on: http://0.0.0.0:7501
```

---

### 5. Test Your Application

#### ✔ Check system health:

- URL: `http://localhost:7501/BoundedContext/isRunning`
- Response:
```json
true
```

#### 💬 Call the business method:

- URL: `http://localhost:7501/HelloJexxa/greetings`
- Response:
```json
"Hello Jexxa"
```

You now have a running Jexxa application!

---

## 2️⃣ Add a Web Page

Next, let's add a very simple frontend that calls your backend.

### 1. Add a Static Web Directory

Create:

```
src/main/resources/public
```

Update `jexxa-application.properties`:

```properties
# Serve static files from /public
io.jexxa.rest.static.files.root=src/main/resources/public
io.jexxa.rest.static.files.external=true
```

---

### 2. Add `index.html`

Create `src/main/resources/public/index.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <title>Hello Jexxa Web</title>
</head>

<body>
<h1 id="contextName">Context Name: </h1>

<script>
    const BASE_URL = location.href.substring(0, location.href.lastIndexOf('/'));

    const appendToDOM = (message) => {
        document.querySelector("#contextName").append(message);
    };

    const fetchContextName = () => {
        fetch(`${BASE_URL}/BoundedContext/contextName`)
            .then(response => {
                if (!response.ok) throw new Error(response.statusText);
                return response.json();
            })
            .then(data => appendToDOM(data))
            .catch(console.error);
    };

    fetchContextName();
</script>
</body>
</html>
```

---

### 3. Restart and Open the Web Page

Start the application again and visit:

👉 http://localhost:7501/index.html

You should see a page displaying your **BoundedContext name**.

---

## 🚀 Next Steps

You now know how to:

✅ Start a Jexxa application  
✅ Expose business logic via REST  
✅ Serve a static web page

To continue learning:

| Topic                                                      | Resource                                          |
|------------------------------------------------------------|---------------------------------------------------|
| Build a full hexagonal application                         | https://www.jexxa.io                              |
| Use Jexxa project template (recommended for real projects) | https://github.com/jexxa-projects/JexxaArchetypes |
| Add persistence, messaging, domain services                | Tutorial [TimeService](../TimeService/README.md)  |

---

## 📌 Final Notes

This tutorial focuses on the core concepts only.  
For production-ready applications, we recommend using the **JexxaArchetype**, which includes Maven plugins, structured project layout, and CI support.

Happy coding — and welcome to the Jexxa ecosystem! 🎉
