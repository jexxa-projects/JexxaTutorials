# HelloJexxa

## What You Learn

1.  [How to write a simple application using Jexxa](#1-Write-the-application)
2.  [How to develop a web frontend](#2-Develop-a-Web-Frontend)

## What you need

*   30 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   A web browser
*   curl to access the web page via command line (optional)

## 1. Write the application

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

### Compile & Start the Application
```console                                                          
mvn clean install
java -jar target/hellojexxa-jar-with-dependencies.jar
```
You should see following (or similar) output
```console
[2023-01-12T06:36Z] INFO JexxaBanner - Config Information: 
[2023-01-12T06:36Z] INFO JexxaBanner - Jexxa Version                  : VersionInfo[version=5.6.0, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2023-01-08T17:51:13+0000]
[2023-01-12T06:36Z] INFO JexxaBanner - Context Version                : VersionInfo[version=2.0.15-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/hellojexxa, projectName=HelloJexxa, buildTimestamp=2023-01-11 08:42]
[2023-01-12T06:36Z] INFO JexxaBanner - Used Driving Adapter           : [RESTfulRPCAdapter]
[2023-01-12T06:36Z] INFO JexxaBanner - Used Properties Files          : [/jexxa-application.properties]
[2023-01-12T06:36Z] INFO JexxaBanner - 
[2023-01-12T06:36Z] INFO JexxaBanner - Access Information: 
[2023-01-12T06:36Z] INFO JexxaBanner - Listening on: http://0.0.0.0:7501
[2023-01-12T06:36Z] INFO JexxaBanner - OpenAPI available at: http://0.0.0.0:7501/swagger-docs
[2023-01-12T06:36Z] INFO JexxaMain - BoundedContext 'HelloJexxa' successfully started in 1.568 seconds
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
### Access the Web Page
This tutorial provides also a simple web page, which uses the above REST commands. You can access it via following link [http://localhost:7501/index.html](http://localhost:7501/index.html). The result should look like this:

![Webpage](images/Webpage.jpg)

This web page can be used as starting point for developing your own web page that is described in the following section. 

## 2. Develop a Web Frontend

### Start Application in Developing Mode
For simplicity, our web page is included in the jar. We can also define an external location of the provided web pages so that we do not need to recompile the entire application if we change the web frontend. 

All Jexxa applications are configured by using the [`jexxa-application.properties`](src/main/resources/jexxa-application.properties). This config file is automatically loaded if available. 
In addition, we can also define configuration files which either extend, or overwrite the settings in the `jexxa-application.properties`. More information about this hierarchical approach can be found [here](https://jexxa-projects.github.io/Jexxa/jexxa_reference.html#_properties_files).

To do so, we define another configuration file called [`jexxa-test.properties`](src/test/resources/jexxa-test.properties) that changes our default configuration as follows.
```properties                                                          
#Changed Settings for RESTfulRPCAdapter
io.jexxa.rest.static_files_root=src/main/resources/public
io.jexxa.rest.static_files_external=true
```

Since this configuration file is also included in this tutorial, we can restart our application using following command: 
```console                                                          
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" target/hellojexxa-jar-with-dependencies.jar
```
The output messages states that two properties files are loaded: 
```console
...
[2023-01-12T07:22Z] INFO JexxaBanner - Used Properties Files          : [/jexxa-application.properties, ./src/test/resources/jexxa-test.properties]
...
```

### Change the Web Page 

To change the web page we first open the file [index.html](src/main/resources/public/index.html) in our favorite editor.  

For example, we can change the headings from: 
```html
<h1 id = "greetings">Greetings: </h1>
```
into 
```html
<h1 id = "greetings">Gentle people say: </h1>
```

After saving the file, you can reload the web page and should see following output:
![](images/ChangedWebPage.png)