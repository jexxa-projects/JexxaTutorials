# HelloJexxa

## What You Learn

1.  [How to write a simple application using Jexxa](#1-Write-the-application)
2.  [How to change the web frontend](#2-Develop-a-Web-Frontend)

## What you need

*   30 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   A web browser
*   curl to access the web page via command line (optional)

## 1. Write the application

### Create a Maven project 

All IDEs such as IntelliJ or Eclipse provide a way to create a new maven project that you should to create your HelloJexxa program. Then add the following dependencies to the `pom.xml` to use Jexxa and to print log information to the console. 

```xml
<dependencies>
    <dependency>
       <groupId>io.jexxa</groupId>
       <artifactId>jexxa-web</artifactId>
       <version>5.6.0</version>
    </dependency>
    
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.6</version>
    </dependency>
</dependencies>
```

### Write the main method 
The source code of the main method is quite simple. Each line include comments to explain the meaning.  

```java     
public final class HelloJexxa
{
    @SuppressWarnings({"java:S3400", "unused"})
    // Our business logic ;-)
    public String greetings()
    {
        return "Hello Jexxa";
    }

    public static void main(String[] args)
    {
        //Create your jexxaMain for this application
        var jexxaMain = new JexxaMain(HelloJexxa.class);

        jexxaMain
                // Bind a REST adapter to expose parts of the application
                .bind(RESTfulRPCAdapter.class).to(HelloJexxa.class)               // Get greetings: http://localhost:7501/HelloJexxa/greetings
                .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())  // Get stats: http://localhost:7501/BoundedContext/isRunning

                // Run your application until Ctrl-C is pressed
                .run();
    }
}
```

### Configure your Application

Before we can run your application, we need to configure network port and address for HTTP access via a properties file. The default properties file used in jexxa
is named `jexxa-application.properties`. Add a file into the `resources` directory of your project and add the following
content:
```properties
#Settings for RESTfulRPCAdapter
io.jexxa.rest.host=0.0.0.0
io.jexxa.rest.port=7501
```

### Compile & Start the Application
Compile and start the program via your IDE. As soon as you start the application, you should see following (or similar) output

```console
[main] INFO io.jexxa.utils.JexxaBanner - Config Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.6.0, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2023-01-21T17:05:30+0000]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=, repository=, projectName=HelloJexxa, buildTimestamp=]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties]
[main] INFO io.jexxa.utils.JexxaBanner - 
[main] INFO io.jexxa.utils.JexxaBanner - Access Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7501
```

### Access the application
To access the application you can either use your favorite web browser, or a command line tool like 'curl'. 

*   Check if context is up and running:
    *   URL: http://localhost:7501/BoundedContext/isRunning
    *   Result:
    ```Json 
        true
    ```
    
*   Access our business function `greetings`:
    *   URL: http://localhost:7501/HelloJexxa/greetings
    *   Result: 
    ```Json 
        "Hello Jexxa" 
    ```
## 2. Develop a Web Frontend

### Add a Web Page

For simplicity, we add our web page into the same project as our backend by performing following steps: 

* Create directory `public` into our `resources` directory that will include our web page
* Extend `jexxa-application.propertoes` by following lines so that this directory is exposed as web directory 
  ```properties
  io.jexxa.rest.static_files_root=src/main/resources/public
  io.jexxa.rest.static_files_external=true
  ```
* Finally, add web page `index.html` with following content into `resources/public`:
    ```html
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title> Test page for Jexxa </title>
    </head>
    
    <body>
    <h1 id = "greetings">Greetings: </h1>
    
    <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
    
    <script>
        //Base URL is the URL of the browser together with the name of our application name HelloJexxa
        const BASE_URL = window.location.protocol + '//' + location.host +'/HelloJexxa';
    
        //Invoke method `greetings` on our backend (using axios framework)  
        const fetchGreetings = () => {
            axios.get(`${BASE_URL}/greetings`)
                .then(response => {
                    const greetings = response.data;
                    console.log(`GET greetings`, greetings);
                    // append to DOM
                    appendToDOM(greetings);
                })
                .catch(error => console.error(error));
        };
    
        //Append message behind id 'greetings'
        const appendToDOM = (message) => {
            document
                    .querySelector("#greetings")
                    .append(message);
        };
    
        //Call our method 
        fetchGreetings();
    </script>
    </body>
    </html>
    ```

### Access the Web Page

After restarting the application, you can access the web page via following link [http://localhost:7501/index.html](http://localhost:7501/index.html). The result should look like this:

![Webpage](images/Webpage.jpg)

This web page can be used as starting point for developing your own web page.


## Final notes
The steps described in this tutorial are written in a way that you can use it in any IDE. The source code we provide is based on the [JexxaTemplate](https://github.com/jexxa-projects/JexxaTutorials/) which uses additional maven plugins. As soon as you start developing a project for production use, you should use this template.    

