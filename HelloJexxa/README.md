# HelloJexxa

## What You Learn

*   How to write a simple application using Jexxa
*   How to expose and access `HelloJexxa` class via RESTful-RPC   
*   How to use the `jexxa-application.properties` to configure the driving adapters
*   How to build a docker image with this tutorial    

## What you need

*   15 minutes
*   JDK 17  (or higher) installed 
*   Maven 3.6 (or higher) installed
*   A web browser
*   curl to access the web page via command line (optional)
*   Docker installed (optional)

## Write the application

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

## Compile & Start the Application
```console                                                          
mvn clean install
java -jar target/hellojexxa-jar-with-dependencies.jar
```
You will see following (or similar) output
```console
[main] INFO io.jexxa.core.JexxaMain - Start BoundedContext 'HelloJexxa' with 2 Driving Adapter 
[main] INFO org.eclipse.jetty.util.log - Logging initialized @446ms to org.eclipse.jetty.util.log.Slf4jLog
[main] INFO io.javalin.Javalin - Starting Javalin ...
[main] INFO io.javalin.Javalin - Listening on http://localhost:7501/
[main] INFO io.javalin.Javalin - Javalin started in 194ms \o/
[main] INFO io.jexxa.core.JexxaMain - BoundedContext 'HelloJexxa' successfully started in 0.549 seconds
```

## Access the application

### Using a web browser
*   Get name of the bounded context:
    *   URL: http://localhost:7501/HelloJexxa/greetings
    *   Result: 
    ```Json 
        Hello Jexxa 
    ```
*   Check if context is up and running:
    *   URL: http://localhost:7501/BoundedContext/isRunning
    *   Result:
    ```Json 
        true
    ```

## Configure the application 
All Jexxa applications are configure by using the `jexxa-application.properties`. 

### Set HTTP(S) settings 
In this simple tutorial `jexxa-application.properties` includes only the two parameters for RESTFulRPCAdapter.
The most interesting one here is `io.jexxa.rest.port` that allows to define the used network port.

```properties                                                          
#Settings for RESTfulRPCAdapter
#Note: Setting host to 0.0.0.0 starts listening on all network devices 
io.jexxa.rest.host=0.0.0.0
io.jexxa.rest.port=7501
```

In addition, we also enabled HTTPS. in this case you have to pass a keystore including https certificate. 
In this tutorial we included a self-singed certificate for demonstration purpose. 
```properties                                                          
io.jexxa.rest.https_port=8081
io.jexxa.rest.keystore=test.jks
io.jexxa.rest.keystore_password=test123
```


### Define location of exposed web pages
You can also define a path to static web pages in properties as follows. 
```properties                                                          
#Settings for RESTfulRPCAdapter
...
io.jexxa.rest.static_files_root=/public
```

Note: This directory is relative to the class path of the application. From a web client it is accessed without any other prefix. 

This tutorial provides a simple web page which performs the previous `GET`. The html page itself can be found [here](src/main/resources/public/index.html).

The web page can be accessed via following link [http://localhost:7501/index.html](http://localhost:7501/index.html) and looks as follows: 

![Webpage](images/Webpage.jpg)

## Build a docker image from release version
In order to build a docker image and a new release, we recommend to use the GitHub action [newRelease.yml](../.github/workflows/newRelease.yml). This action script can be executed directly on GitHub and uploads a docker image to the GitHub registry ghcr.io. 

## Build a docker image from snapshot release
In order to build a docker image with this tutorial we use the maven-jib-plugin. For the sake of simplicity we assume
that docker is installed on your local machine so that we do not need to configure any external docker registry.

Note: All tutorials can be build as docker image with the following steps.      

* Within a container, we have to define URLs to external infrastructure such as ActiveMQ or the database. As described in [reference guide](https://jexxa-projects.github.io/Jexxa/jexxa_reference.html#_application_configuration) you have to adjust either jexxa-application.properties, or you can use java system properties which can be set as JVM flags in [pom.xml](pom.xml) (see section `jvmFlags`). 

* Create the docker image with `maven` enter: 
    ```console                                                          
    mvn jib:dockerBuild -PlocalDockerImage
    ``` 

* Check available docker images:                                                
    ```console                                                          
    docker images
    ``` 
    You will see following (or similar) output
    ```console                                                          
    REPOSITORY                                    TAG                 IMAGE ID            CREATED             SIZE
    ...
    io.jexxa.tutorials/hellojexxa                 2.2.1-SNAPSHOT      18e39628a651        5 days ago          157MB
    ...
    ``` 

* In order to create a container from the image please refer [docker manual](https://docs.docker.com/)        

              
