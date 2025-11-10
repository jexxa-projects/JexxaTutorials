# Contract Management—Using an ObjectStore 

## What You Learn

*   [When to use Repository or ObjectStore](#Repository-vs-ObjectStore)
*   [How to use Jexxa's ObjectStore](#Example-ContractManagement)

## What you need

*   Understand tutorial [`BookStore - Using a Repository`](../BookStore/README.md) because we explain only new aspects
*   30 minutes
*   JDK 25 (or higher) installed
*   Maven 3.6 (or higher) installed
*   curl to trigger the application
*   (Optional) A postgres DB 

##  Repository vs. ObjectStore

When developing a business application, you should focus on the business domain and how to represent
it within your application. Technical aspects such as the database schema should be hidden as good as possible. 
Within Jexxa, we support this by providing different strategies for implementing a Repository.   

### Driven Adapter Strategy: `IRepository`  

The `IRepository` interface is very limited regarding querying a managed object. Either you query an object by its 
unique key. If you want to offer advanced querying mechanisms, you have to implement them by yourself by querying all 
objects in a first step. Even though this sounds very limiting choosing an `IRepository` for your object should be your 
first choice. Especially during the development phase of a new application or bounded context, it gives you the time to 
learn which query interface you really need from the application point of view. 

In general, you should use an `IRepository` in following scenarios: 

*   You query the managed objects only be their unique key.
*   In case you need more advanced query operations, the lifetime of the aggregates should be short, so that their 
    number is relative. This allows you to read all data from the database. 

Especially the second case happens quite often in production systems, especially in batch systems. Here, it is quite 
common that software controlling a specific manufacturing unit requires only knowing the batches that are currently 
processed. As soon as the processing step is finished, a corresponding `DomainEvent` is published and the aggregate can 
be removed from the repository.         
                                                                                      
Please do not underestimate this aspect because it supports you separating your production data from your archive data.  

### Driven Adapter Strategy: `IObjectStore`
                               
The `IObjectStore` provides more sophisticated interfaces to query managed objects by all kinds of data. 
Available strategies make explicit use of the underlying technology so that the performance depends 
on chosen technology stack. This kind of repository should be your second choice. As soon as you see that an 
`IRepository` is not sufficient, you should switch the implementation of the driven adapter to an `IObjectStore`. 
Please note that this step should be transparent to the application core because it uses a single interface which is 
not affected. Only the underlying strategy is changed.

In general, you should use an `IObjectStore` in following scenarios:

*   You need several ways to request managed objects and
*   the lifetime of the managed objects is high, so that the number of managed objects will continuously increase.
*   The metadata to find objects is fixed and will not change over time.  

At first thought, the last requirement sounds like a severe restriction. Especially this kind of change typically 
happens some time after the software is in production. But please keep in mind that your application core is protected 
by your application-specific interface. So changing the implementation will not affect the application core itself. 
If a change request occurs, you have a lot of knowledge based on production and other change requests which underlying
technology or database stack should be used. Now it is the right point in time to switch to a specific implementation
without using a specific strategy or to provide your own strategy using technologies such as liquibase for versioning 
your database schema.    

Typical use cases to select an `IObjectStore` are:
*   An archive of the domain events.
*   A bounded context managing objects with a very long lifetime, which mostly happens within a DomainService. 

### Strategies for `IRepository` and `IObjectStore`

At the moment, Jexxa provides driven adapter strategies for in-memory storage and JDBC. 
To query an `IRegistry` or `IObjectStore` you use the `RegistryManager` or `ObjectStoreManager` 
respectively.
A significant advantage of using these strategies is to write tests against your 
Repository without the need of a database.
This typically speeds up your tests significantly.   

By default, both manager classes select a strategy depending on your application configuration and the `Property` 
object passed to Jexxa as follows: 

1.  Check if the application defined a strategy for a specific object type is registered.
2.  Check if the application defined a default strategy for all kinds of objects. 
3.  Check if the `Property` object defines a JDBC driver. In this case the `JDBCKeyValueRepository` or `JDBCObjectStore` is used.
4.  Otherwise, an in memory strategy `IMDBKeyValueRepository` or `IMDBObjectStore` is used. 

## Example ContractManagement

This tutorial defines the following requirements: 
*   `IContractRepository`: Manage contracts with a very high lifetime and must be searched by different metadata.  
*   `IDomainEventStore`: Archive all domain events that must be searched by different metadata.    

Based on the requirements, both interfaces should be implemented using an `IObjectStore`. 
In the rest of this section we describe the implementation of `IContractRepositroy`. 
Since the implementation of `IDomainEventStore` is quite similar please refer to its source code.  

### Implementing `IContractRepositroy`

Using an ObjectStore is quite similar to a Repository. The main difference is in defining the metadata used to query 
objects. To ensure type safety, Jexxa requires that all metadata is defined as enum together with a `MetaTag` used for 
converting the value into a base type such as a numeric or string representation. In the following example, we define 
the three different values to query objects. Please note that the following code belongs to the infrastructure of your 
application which means that your application just sees the `IContractRepository` and not the schema specification:

```java
public class ContractRepositoryImpl implements ContractRepository
{
    /**
     * Here we define the values to query contracts. Apart from their key, elements should be queried by the following information: <br>
     * <ol>
     *    <li>Contract number</li>
     *    <li>Contract signed flag</li>
     *    <li>Advisor of the contract</li>
     * </ol>
     */
    enum ContractSchema implements MetadataSchema
    {
        /**
         * This MetaTag represents the contract number. Since it is a numeric value, we use a numberTag. As most
         * predefined {@link MetaTag} class, we just provide an accessor function to get the value from the managed object.
         */
        CONTRACT_NUMBER(numericTag(element -> element.getContractNumber().value())),

        /**
         * This MetaTag represents a boolean if the contract is signed or not.  Here, we use booleanTag together with the
         * corresponding accessor function.
         */
        CONTRACT_SIGNED(booleanTag(Contract::isSigned)),

        /**
         * This MetaTag allows for searching for the advisor of the contract. Here, we use stringTag together with the corresponding
         * accessor function.
         */
        ADVISOR(stringTag(Contract::getAdvisor));

        // The remaining code is always the same for all metadata specifications
        private final MetaTag<Contract, ?, ? > metaTag;

        ContractSchema(MetaTag<Contract,?, ?> metaTag)
        {
            this.metaTag = metaTag;
        }

        @Override
        @SuppressWarnings("unchecked")
        public MetaTag<Contract, ?, ?> getTag()
        {
            return metaTag;
        }
    }

    // ...
}
```

After defining the metadata, we can implement the interface.

```java
public class ContractRepositoryImpl  implements ContractRepository
{
    // ...

    private final IObjectStore<Contract, ContractNumber, ContractMetadata> objectStore;

    public ContractRepositoryImpl(Properties properties)
    {
        // To request an ObjectStore strategy, we need to pass the following information to the manager: 
        this.objectStore = ObjectStoreManager.getObjectStore(
                Contract.class,                // 1. Type information of the managed object
                Contract::getContractNumber,   // 2. Method to get the unique key
                ContractMetadata.class,        // 3. The previous defined metadata
                properties);                   // 4. Finally, the application-specific Property file
    }

    // We skip the implementation of the IRepository methods here and focus on the methods
    // using the extension in IObjectStore.
    // ...

    // Implementing the specific query methods is straight forward.     
    @Override
    public List<Contract> getByAdvisor(String advisor)
    {
        return objectStore
                .getStringQuery(ADVISOR, String.class)
                .isEqualTo(advisor);
    }
    
    @Override
    public List<Contract> getSignedContracts()
    {
        return objectStore
                .getNumericQuery(CONTRACT_SIGNED, Boolean.class)
                .isEqualTo(true);
    }

    @Override
    public List<Contract> getUnsignedContracts()
    {
        return objectStore
                .getNumericQuery(CONTRACT_SIGNED, Boolean.class)
                .isEqualTo(false);
    }

    @Override
    public Optional<Contract> getHighestContractNumber()
    {
        return objectStore
                .getNumericQuery(CONTRACT_NUMBER, Integer.class)
                .getDescending(1)
                .stream()
                .findFirst();
    }
}
```
## Run the application

### Use an in memory database

```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties" ./target/contractmanagement-jar-with-dependencies.jar```
You will see following (or similar) output
```console
[main] INFO io.jexxa.utils.JexxaBanner - Jexxa Version                  : VersionInfo[version=5.0.0-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/Jexxa.git/jexxa-core, projectName=Jexxa-Core, buildTimestamp=2022-06-06 07:08]
[main] INFO io.jexxa.utils.JexxaBanner - Context Version                : VersionInfo[version=1.0.16-SNAPSHOT, repository=scm:git:https://github.com/jexxa-projects/JexxaTutorials.git/contractmanagement, projectName=ContractManagement, buildTimestamp=2022-06-06 07:16]
[main] INFO io.jexxa.utils.JexxaBanner - Used Driving Adapter           : [RESTfulRPCAdapter]
[main] INFO io.jexxa.utils.JexxaBanner - Used ObjectStore Strategie     : IMDBObjectStore
...
```          

### Use a Postgres database

The properties file [jexxa-test.properties](src/test/resources/jexxa-test.properties) is configured to use a postgres
DB. So we have to enter following command


```console                                                          
mvn clean install
java -jar "-Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties" ./target/contractmanagement-jar-with-dependencies.jar
```
In contrast to the above output, Jexxa will state that you use JDBC persistence strategy now:
```console
[main] INFO io.jexxa.tutorials.contractmanagement.ContractManagement - Use persistence strategy: JDBCObjectStore 
```

### Execute some commands using curl

#### Create a new contract which is managed by Paul 

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '"Paul"' \
    http://localhost:7504/ContractService/createNewContract                 
```

Response: The number of the contract
```Console
{"value":1}
```

Note: This command can be repeated several times to create more contracts 

#### Query for a specific advisor 

Command:
```Console
curl -X POST -H "Content-Type: application/json" \
    -d '"Paul"' \
    http://localhost:7504/ContractService/getContractsByAdvisor                 
```
Response: The contract numbers of all contracts managed by Paul. Note that we called the previous method 9 times.  
```Console
[{"value":1},{"value":2},{"value":4},{"value":5},{"value":6},{"value":7},{"value":3},{"value":8},{"value":9}]

```