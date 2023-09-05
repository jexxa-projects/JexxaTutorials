# BookStore - Architecture Validation

## What You Learn
   
*   [How to validate the architecture of your application](#Validate-the-Architecture)

## What you need

*   Understand tutorial [`BookStore - Pattern Language`](README-PatternLanguage.md)
*   20 minutes
*   JDK 17 (or higher) installed
*   Maven 3.6 (or higher) installed

## Validate the Architecture


Even with a clear architecture, one can easily violate their rules by accident. So I 
strongly recommend to run some tests to validate the architecture of your application. 
Jexxa simplifies this especially if you use its conventions by providing architectural 
tests based on [ArchUnit](https://www.archunit.org). This ensures that the validation of the
architecture is port of your unit tests. 

Jexxa-test provides following architecture tests that. 
*   [Pattern Language](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validate the correct annotation of your application using project [Addend](http://addend.jexxa.io/)
*   [Ports&Adapters Architecture](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validates dependencies between packages of your application
*   [Usage of Aggregates](src/test/java/io/jexxa/tutorials/bookstore/architecture/ArchitectureTest.java) to validate that your business logic is not exposed

If you want to use the architecture tests provided by Jexxa, you need to add the following dependency.

```xml
    <dependency>
      <groupId>io.jexxa.jexxatest</groupId>
      <artifactId>jexxa-test</artifactId>
      <version>6.1.5</version>
      <scope>test</scope>
    </dependency>
```



### Validate Ports and Adapters

To validate the rules of a ports and adapter architecture is quite simple if you follow the conventions of Jexxa. As 
you can see below, you just have to declare the packages including the driven and driving adapter of the application.  

```java
class ArchitectureTest {

    @Test
    void validatePortsAndAdapters()
    {
        portsAndAdapters(BookStore.class)
                // Add all packages providing driven adapter  
                .addDrivenAdapterPackage("persistence")  
                .addDrivenAdapterPackage("messaging")

                // Add all packages providing driving adapter such as  
                // .addDrivingAdapterPackage("messaging")
                
                .validate();
    }
}
```

### Validate the Pattern Language
The following test validates that all classes of your application are annotated. In addition, it validates that all 
`ValueObject` and `DomainObject` classes are implemented as java-records. 

```java
class ArchitectureTest {
    @Test
    void validatePatternLanguage() {
        patternLanguage(BookStore.class).validate();
    }
}
```

### Validate the usage of Aggregates
Aggregates include the business logic and must not leave the application core. In addition, they must only be managed 
by a `Repository`. This leads to some additional rules that can be validated using following test: 

```java
class ArchitectureTest {
    @Test
    void validateAggregateRules()
    {
        aggregateRules(BookStore.class).validate();
    }
}
```
