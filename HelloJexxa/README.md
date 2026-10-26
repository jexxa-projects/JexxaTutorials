# HelloJexxa: Your First Jexxa Application

In about 30 minutes you build a small application, call it over REST, and put a web page in front of it. Along the way you meet the three ideas behind every Jexxa application.

**What you build**

```
Browser / curl ──HTTP──▶ RESTfulRPCAdapter ──▶ GreetingService
   (outside)              (driving adapter)     (your business logic)
```

Your business logic knows nothing about REST. Jexxa connects the two. That separation is the core idea of hexagonal architecture (Ports & Adapters), and this tutorial lets you see it in the smallest possible example.

**Requirements**

- JDK 25 or higher
- Maven 3.6+
- A web browser (and optionally `curl`)

---

## The Three Ideas

Read this once, then come back to it when the code feels magical.

| Term                | Meaning                                                | In this tutorial                    |
|---------------------|--------------------------------------------------------|-------------------------------------|
| **Business logic**  | Plain Java classes without framework code              | `GreetingService`                   |
| **Driving adapter** | Technology that lets the outside world call your logic | `RESTfulRPCAdapter`                 |
| **Binding**         | You tell Jexxa which adapter exposes which object      | `bind(...).to(...)` in `HelloJexxa` |

Jexxa exposes the **public methods** of a bound object as REST endpoints. You write no controllers, no annotations, no mapping code.

---

## Step 1: Set Up the Project

Create the following layout:

```
hellojexxa/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── GreetingService.java
    │   └── HelloJexxa.java
    └── resources/
        └── jexxa-application.properties
```

Use this complete `pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.jexxa.tutorials</groupId>
    <artifactId>hellojexxa</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Jexxa core + REST support -->
        <dependency>
            <groupId>io.jexxa</groupId>
            <artifactId>jexxa-web</artifactId>
            <version>9.1.1</version>
        </dependency>

        <!-- Log output to the console -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.20</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>hellojexxa</finalName>
        <plugins>
            <!-- Builds a single runnable jar: target/hellojexxa-jar-with-dependencies.jar -->
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.7.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>HelloJexxa</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

`jexxa-web` is the only Jexxa dependency you need here. It brings the core and the REST adapter. `slf4j-simple` just makes Jexxa's log output visible.

**Checkpoint:** `mvn clean compile` runs without errors.

---

## Step 2: Write the Business Logic

Create `GreetingService.java`:

```java
public final class GreetingService
{
    public String greetings()
    {
        return "Hello Jexxa";
    }
}
```

This is plain Java. There is no Jexxa import and no annotation. You could unit-test it, or expose it via messaging instead of REST, without changing a line.

---

## Step 3: Start Jexxa and Bind the Adapter

Create `HelloJexxa.java`:

```java
import io.jexxa.core.JexxaMain;
import io.jexxa.drivingadapter.rest.RESTfulRPCAdapter;

public final class HelloJexxa
{
    static void main(String[] args)
    {
        // 1. Create the Jexxa runtime for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
            // 2. Expose your business logic via REST
            .bind(RESTfulRPCAdapter.class).to(GreetingService.class)

            // 3. Expose the application's health status via REST
            .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

            // 4. Start and block until Ctrl+C
            .run();
    }
}
```

What happens here:

1. `JexxaMain` is the runtime. It creates your objects and manages the adapters.
2. `bind(adapter).to(object)` connects a driving adapter with the object it should expose. Jexxa creates the `GreetingService` instance for you.
3. The *bounded context* is Jexxa's built-in object for application-level information such as health. It is exposed the same way as your own logic.
4. `run()` starts all adapters.

---

## Step 4: Configure and Run

Create `src/main/resources/jexxa-application.properties`:

```properties
# IP address to listen on (0.0.0.0 = all interfaces)
io.jexxa.rest.host=0.0.0.0

# REST port
io.jexxa.rest.port=7501
```

Build and start:

```bash
mvn clean install
java -jar target/hellojexxa-jar-with-dependencies.jar
```

You can also run `HelloJexxa` directly from your IDE.

**Checkpoint:** the console shows a line like this:

```console
INFO  ... JexxaBanner - Listening on: http://0.0.0.0:7501
```

---

## Step 5: Call Your Application

Open these URLs in your browser, or use `curl`:

| Purpose              | URL                                             | Response        |
|----------------------|-------------------------------------------------|-----------------|
| Health check         | http://localhost:7501/BoundedContext/isRunning  | `true`          |
| Your business method | http://localhost:7501/GreetingService/greetings | `"Hello Jexxa"` |

```bash
curl http://localhost:7501/GreetingService/greetings
```

Notice how the URL is built: `/<ClassName>/<methodName>`. No routing code was needed.

**Checkpoint:** you get `"Hello Jexxa"`. This is a complete Jexxa application.

---

## Step 6: Add a Web Page

Jexxa can also serve static files, so a frontend and its backend run in the same process.

Create the folder `src/main/resources/public` and add two lines to `jexxa-application.properties`:

```properties
# Serve static files from this directory
io.jexxa.rest.static.files.root=src/main/resources/public
io.jexxa.rest.static.files.external=true
```

> **Note:** this path is relative to the working directory. Start the application from the project root, otherwise the page is not found.

Create `src/main/resources/public/index.html`:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <title>Hello Jexxa Web</title>
</head>
<body>
    <h1 id="greeting">Loading ...</h1>
    <p>Context name: <span id="contextName">-</span></p>

    <script>
        // Relative URLs are resolved against the page's own address,
        // so no base URL is needed.
        async function callBackend(path) {
            const response = await fetch(path);
            if (!response.ok) throw new Error(response.statusText);
            return response.json();
        }

        callBackend("GreetingService/greetings")
            .then(text => document.querySelector("#greeting").textContent = text)
            .catch(console.error);

        callBackend("BoundedContext/contextName")
            .then(name => document.querySelector("#contextName").textContent = name)
            .catch(console.error);
    </script>
</body>
</html>
```

Restart the application and open http://localhost:7501/index.html.

**Checkpoint:** the page shows "Hello Jexxa" and the context name of your application. Both values come from your running backend, via the same REST endpoints you called in Step 5.

---

## What You Have Learned

- Business logic is plain Java and independent of any technology.
- A driving adapter exposes it, and `bind(...).to(...)` is the only place where the two meet.
- The public methods of a bound object become REST endpoints: `/<ClassName>/<methodName>`.
- A static web page can be served by the same application.

---

## Troubleshooting

| Problem                            | Likely cause and fix                                                                                  |
|------------------------------------|-------------------------------------------------------------------------------------------------------|
| `release version 25 not supported` | Maven runs on an older JDK. Check `java -version` and `mvn -version`.                                 |
| `Address already in use`           | Port 7501 is taken. Change `io.jexxa.rest.port` or stop the other process.                            |
| `Unable to access jarfile`         | The build did not run. Execute `mvn clean install` and check the file name in `target/`.              |
| `index.html` returns 404           | Start the application from the project root, and check the path in `io.jexxa.rest.static.files.root`. |

---

## Try It Yourself

1. Add a second method to `GreetingService` and call it via the browser. Do you need to change `HelloJexxa`?
2. Change the port to `8080` and verify that the web page still works. Why does it?
3. Open the browser's developer tools on `index.html` and watch the requests going to the backend.

---

## Next Steps

| Goal                                           | Resource                                                                                               |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| Add persistence, messaging and domain services | Tutorial [TimeService](../TimeService/README.md)                                                       |
| Start a real project                           | [JexxaArchetypes](https://github.com/jexxa-projects/JexxaArchetypes) (recommended for production code) |
| Learn the concepts in depth                    | https://www.jexxa.io                                                                                   |