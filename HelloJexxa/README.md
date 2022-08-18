# HelloJexxa

## What You Learn

1.  [How to write a simple application using Jexxa](#1.-Write-the-application)
2.  [How to configure this application using the `jexxa-application.properties`](#2.-Configure-the-application)
3.  [How to define a simple CI/CD pipeline](#3.-A-simple-CI/CD-pipeline)   

## What you need

*   30 minutes
*   JDK 17 (or higher) installed 
*   Maven 3.6 (or higher) installed
*   A web browser
*   curl to access the web page via command line (optional)
*   Docker installed (optional)

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
You will see following (or similar) output
```console
[main] INFO io.jexxa.utils.JexxaBanner - Config Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.1.0, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2022-08-15 06:52]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=2.0.0, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/hellojexxa, projectName=HelloJexxa, buildTimestamp=2022-08-15 07:01]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used Properties Files          : [/jexxa-application.properties]
[main] INFO io.jexxa.utils.JexxaBanner - 
[main] INFO io.jexxa.utils.JexxaBanner - Access Information: 
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: http://0.0.0.0:7501
[main] INFO io.jexxa.utils.JexxaBanner - Listening on: https://0.0.0.0:8081
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: http://0.0.0.0:7501/swagger-docs
[main] INFO io.jexxa.utils.JexxaBanner - OpenAPI available at: https://0.0.0.0:8081/swagger-docs

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
        Hello Jexxa 
    ```

## 2. Configure the application 
All Jexxa applications are configured by using the `jexxa-application.properties`. 

### Configure HTTP(S) settings 
In this simple tutorial `jexxa-application.properties` includes only the two parameters for RESTFulRPCAdapter.
The most interesting one here is `io.jexxa.rest.port` that allows to define the used network port.

```properties                                                          
#Settings for RESTfulRPCAdapter
#Note: Setting host to 0.0.0.0 starts listening on all network devices 
io.jexxa.rest.host=0.0.0.0
io.jexxa.rest.port=7501
```

In addition, we also enabled HTTPS. In this case you have to pass a keystore including https certificate. 
In this tutorial we included a self-singed certificate for demonstration purpose, so you will
get a corresponding warning in your browser if you access it. 
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

Note: This directory is relative to the class path of the application. From a web client it is accessed without any 
other prefix. 

This tutorial provides a simple web page which performs the previous `GET`. The html page itself can be found 
[here](src/main/resources/public/index.html).

The web page can be accessed via following link [http://localhost:7501/index.html](http://localhost:7501/index.html) and looks as follows: 

![Webpage](images/Webpage.jpg)

## 3. A simple CI/CD pipeline
This CI/CD pipeline is only for demonstration purpose. Focus is to show how Jexxa supports your own CI/CD pipeline.  

### Continuous Integration (CI) 
Since we host this tutorial on GitHub, we use so called `GitHub Actions` to build our application and provide the 
artifacts via GitHub container registry (ghcr). 

* [mavenBuild.yml](../.github/workflows/mavenBuild.yml): Builds our tutorials and runs the tests with each commit to ensure that we did not break the build process. 
* [autoMerge.yml](../.github/workflows/newRelease.yml): Automatically merges dependency updates from dependabot.
* [newRelease.yml](../.github/workflows/newRelease.yml): Builds a new release including a docker image that is stored in GitHub's container registry (www.ghcr.io). Note: In our setup, this action script must be started manually via  be started manually to 
In order to build a docker image and a new release, we recommend to use the GitHub action [newRelease.yml](../.github/workflows/newRelease.yml). This action script can be executed directly on GitHub and uploads a docker image to the GitHub registry ghcr.io. 

### Continuous Deployment (CD)
For continuous deployment, we focus only on the following two aspects: 

* **Zero downtime deployment:** For zero downtime deployment we configure rolling updates for the application as you can see 
  in the [hellojexxa-compose.yml](../deploy/hellojexxa-compose.yml). The most interesting part here are the following lines: 
  ```yml  
  healthcheck:
     test: ["CMD-SHELL", "wget -nv -t1 --spider 'http://localhost:7501/BoundedContext/isRunning/'"]`
  ```
  The return value of this call is only true, if the application could be successfully started. This also includes all 
  connections to infrastructure components, such as requesting a REST port, or setting-up a connection to a database. In  
  case of a rolling update, a new release is only deployed, if this method returns true within the defined period of time.
  If this call fails, the old version remains active. 

* **Handling of secrets:** In general, Jexxa provides dedicated properties to read credentials from a file as described 
  [here](https://jexxa-projects.github.io/Jexxa/jexxa_reference.html#_secrets). This allows you to hand in credentials 
  such as a keystore, or a password into a docker container via files. The list of available properties can be seen 
  [here](https://github.com/jexxa-projects/Jexxa/blob/master/jexxa-web/src/test/resources/jexxa-application.properties). 
  Since the secrets must be defined in your orchestration tool, we define the secrets directly in 
  jexxa-application.properties which is not recommended for production use. 
              
