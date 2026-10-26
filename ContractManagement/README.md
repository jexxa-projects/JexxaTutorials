# Contract Management – Using an ObjectStore

This tutorial shows how to use Jexxa's `IObjectStore` strategy when a domain repository needs more than lookup by a unique identifier. It also demonstrates how an application service can react to a domain event and maintain a queryable audit history.

## What You Will Learn

- [When to choose `IRepository` or `IObjectStore`](#repository-vs-objectstore)
- [How to define metadata for ObjectStore queries](#implementing-the-contract-repository)
- [How to separate contract management from audit-history queries](#application-services-and-domain-events)
- [How to bootstrap an event handler explicitly](#application-bootstrap)
- [How to run the example](#run-the-application)

## Prerequisites

- Familiarity with [BookStore – Using a Repository](../BookStore/README.md)
- JDK 25 or later
- Maven 3.6 or later
- `curl` for invoking the REST API
- Optional: a running PostgreSQL database
- Approximately 30 minutes

## Repository vs. ObjectStore

The Application Core should express persistence needs through **application-defined repository interfaces**, rather than database schemas or query APIs. JexxaAdapters offers reusable strategies that implement these interfaces in the infrastructure layer.

### `IRepository`: Start with the simplest strategy

`IRepository` is suitable when objects are primarily accessed by their unique identifiers. For less common queries, a small collection can also be retrieved and filtered in application code.

Prefer `IRepository` when:

- Most operations load an aggregate by its unique key.
- The number of objects to inspect is small or bounded.
- Query requirements are still evolving and do not yet justify a more specialized persistence strategy.

For example, a manufacturing application may only need to manage the batches currently being processed. Once processing finishes, a domain event can record the outcome and the completed batch can be removed from the active repository. This can help distinguish active operational data from historical records.

> **Design principle:** Start with the simplest repository strategy that satisfies the use cases. Do not introduce database-specific querying before it is needed.

### `IObjectStore`: Query objects by metadata

`IObjectStore` adds metadata-based queries, allowing infrastructure implementations to use capabilities of the selected storage technology.

Consider `IObjectStore` when:

- The application needs several query criteria beyond an object's unique key.
- The number of stored objects grows over time, making full scans undesirable.
- Queryable metadata can be defined explicitly.

The metadata schema is part of the **driven adapter**, not the domain model. The Application Core continues to depend on its own repository interface, so a persistence-strategy change need not affect domain code.

An event archive is one example: it may need queries by event type or time range. Long-lived contracts are another example: they may be queried by advisor, signing status, or contract number.

### Available persistence strategies

The tutorial uses Jexxa's in-memory and JDBC-backed strategies. The selection described in the existing example follows this precedence:

1. An explicitly registered strategy for the object type.
2. An application-wide default strategy.
3. A JDBC strategy when the supplied configuration specifies a JDBC driver.
4. Otherwise, an in-memory strategy (`IMDBKeyValueRepository` or `IMDBObjectStore`).

This makes it possible to run many tests without a database while using JDBC for integration scenarios. Strategy selection and exact configuration keys depend on the Jexxa version used by the project.

## Example: ContractManagement

The example has two distinct persistence needs:

| Application-defined port | Purpose                                              | Why `IObjectStore`?                                   |
|--------------------------|------------------------------------------------------|-------------------------------------------------------|
| `ContractRepository`     | Stores the current state of contracts                | Query by advisor, signing status, and contract number |
| `DomainEventStore`       | Stores `ContractSigned` events for the audit history | Query events by date range                            |

Both interfaces belong to the Application Core (or domain, where appropriate); their `IObjectStore`-based implementations belong to infrastructure.

### Architectural overview

```text
                         Application Core
  ┌────────────────────────────────────────────────────────┐
  │                                                        │
  │  ContractService                 ContractAuditService  │
  │  - createNewContract()           - handle(event)       │
  │  - signContract()                - getAllSigned...()   │
  │  - contract queries              - getSigned...()      │
  │         │                                  │           │
  │         │ uses                             │ uses      │
  │         ▼                                  ▼           │
  │  ContractRepository                 DomainEventStore   │
  │         │                                  ▲           │
  │         │ Contract.sign()                  │           │
  │         └── ContractSigned ── publish ─────┤           │
  │                        via DomainEventDispatcher       │
  └────────────────────────────────────────────────────────┘
           │                                  │
           ▼                                  ▼
  ContractRepositoryImpl              DomainEventStoreImpl
           │                                  │
           └────────── IObjectStore ──────────┘
                     JexxaAdapters
```

The diagram is conceptual: `ContractService` publishes the event through an application-defined `DomainEventDispatcher` port; `ContractAuditService` subscribes to that port and persists the event through `DomainEventStore`.

**This is not Event Sourcing.** The contract's current state is stored in `ContractRepository`; `DomainEventStore` maintains a separate history of signed contracts.

## Implementing the Contract Repository

The `ContractRepository` interface expresses the application's query needs. Its infrastructure implementation defines the metadata needed by `IObjectStore`.

### Define queryable metadata

For contracts, the queryable fields are:

- Contract number
- Signing status
- Advisor

The following excerpt shows the metadata concept used by the existing tutorial:

```java
public class ContractRepositoryImpl implements ContractRepository
{
    enum ContractMetadata implements MetadataSchema
    {
        CONTRACT_NUMBER(numericTag(contract -> contract.getContractNumber().value())),
        CONTRACT_SIGNED(booleanTag(Contract::isSigned)),
        ADVISOR(stringTag(Contract::getAdvisor));

        private final MetaTag<Contract, ?, ?> metaTag;

        ContractMetadata(MetaTag<Contract, ?, ?> metaTag)
        {
            this.metaTag = metaTag;
        }

        @Override
        public MetaTag<Contract, ?, ?> getTag()
        {
            return metaTag;
        }
    }

    // Repository implementation follows ...
}
```

The metadata describes how a stored `Contract` can be indexed and queried. It is **not** part of the aggregate's business rules.

> The original tutorial alternated between `ContractSchema` and `ContractMetadata`. This README consistently uses `ContractMetadata`; check the exact enum name and imports against the source before copying the excerpt.

### Obtain an ObjectStore

```java
private final IObjectStore<Contract, ContractNumber, ContractMetadata> objectStore;

public ContractRepositoryImpl(Properties properties)
{
    this.objectStore = ObjectStoreManager.getObjectStore(
            Contract.class,
            Contract::getContractNumber,
            ContractMetadata.class,
            properties);
}
```

The parameters identify the stored type, its unique key, its query metadata, and the application configuration.

### Implement application-specific queries

```java
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
```

These excerpts retain the query API used in the supplied tutorial. Verify the boolean-query method and generic types against the current JexxaAdapters version; they have not been compiled as part of this README revision.

`DomainEventStoreImpl` uses the same general approach to persist and query audit events; refer to its implementation for the event-specific metadata.

## Application Services and Domain Events

The example separates two application responsibilities without introducing an extra package:

- `ContractService` manages contracts and executes use cases such as `signContract()`.
- `ContractAuditService` handles `ContractSigned` events and provides queries over the audit history.

Both are application services. The audit service is **not a Domain Service**: it does not implement a business decision that belongs outside an aggregate. It acts as a **Domain Event Handler** and exposes audit-history queries.

### Sign a contract

```java
@ApplicationService
public class ContractService
{
    private final ContractRepository contractRepository;
    private final DomainEventDispatcher domainEventDispatcher;

    public ContractService(
            ContractRepository contractRepository,
            DomainEventDispatcher domainEventDispatcher)
    {
        this.contractRepository = requireNonNull(contractRepository);
        this.domainEventDispatcher = requireNonNull(domainEventDispatcher);
    }

    public void signContract(ContractNumber contractNumber)
    {
        var contract = contractRepository.get(contractNumber);
        var event = contract.sign();
        contractRepository.update(contract);
        domainEventDispatcher.publish(event);
    }

    // Other contract operations omitted ...
}
```

The aggregate performs the domain operation and returns `ContractSigned`. The application service persists the updated contract and publishes the event through its own outbound port. The aggregate itself does not know about the dispatcher.

### Maintain and query the audit history

```java
@ApplicationService
public class ContractAuditService
{
    private final DomainEventStore domainEventStore;
    private final DomainEventDispatcher domainEventDispatcher;

    public ContractAuditService(
            DomainEventDispatcher domainEventDispatcher,
            DomainEventStore domainEventStore)
    {
        this.domainEventStore = requireNonNull(domainEventStore);
        this.domainEventDispatcher = requireNonNull(domainEventDispatcher);
    }

    public void handle(ContractSigned event)
    {
        domainEventStore.add(event);
    }

    public List<ContractSigned> getAllSignedContracts()
    {
        return domainEventStore.get();
    }

    public List<ContractSigned> getSignedContracts(int month, int year)
    {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        return domainEventStore.get(
                startDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                endDate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC));
    }

    public void initialize()
    {
        domainEventDispatcher.subscribe(ContractSigned.class, this::handle);
    }
}
```

This service has one cohesive responsibility: maintaining and exposing the history of signed contracts. The constructor only receives dependencies; `initialize()` activates the subscription explicitly.

**Important:** The example uses UTC calendar boundaries. If the business defines months in another time zone, those boundaries must be calculated in that zone. The inclusive end-of-day range also follows the original example; a half-open range (`startInclusive`, `nextMonthStartExclusive`) is often preferable when the store supports it.

### Application bootstrap

The `main()` method wires the event handler before exposing application services through REST:

```java
void main()
{
    var jexxaMain = new JexxaMain(ContractManagement.class);

    jexxaMain
            .bootstrap(ContractAuditService.class)
                .with(ContractAuditService::initialize)

            .bind(RESTfulRPCAdapter.class).to(ContractService.class)
            .bind(RESTfulRPCAdapter.class).to(ContractAuditService.class)
            .bind(RESTfulRPCAdapter.class).to(jexxaMain.getBoundedContext())

            .run();
}
```

The explicit bootstrap makes the event subscription visible and ensures it is registered before the REST bindings start accepting requests, assuming the documented Jexxa lifecycle executes bootstrap steps before adapter startup.

> **REST exposure:** Check how `RESTfulRPCAdapter` selects operations. `handle(ContractSigned)` and `initialize()` are lifecycle/event-handling methods, not intended public REST operations. If all public methods are exposed automatically, restrict the binding or expose only the audit query API.

### Event delivery and consistency

The in-process dispatcher delivers events synchronously. This keeps the example simple, but it does **not** make the contract update and audit-history update one atomic operation. If publishing or persisting the audit event fails after the contract is updated, the two stores may diverge.

For a production application that requires reliable audit history, consider transaction boundaries and a mechanism such as the **Transactional Outbox Pattern**. This tutorial focuses on ObjectStore queries and application wiring rather than reliable event delivery.

## Run the Application

### Use in-memory persistence

From the ContractManagement project directory:

```shell
mvn clean install
java -Dio.jexxa.config.import=./src/test/resources/jexxa-local.properties \
     -jar ./target/contractmanagement-jar-with-dependencies.jar
```

The configuration should select the in-memory strategy, such as `IMDBObjectStore`. Check the startup log for the effective persistence strategy; exact log messages vary by version.

### Use PostgreSQL

Start a PostgreSQL database with the settings expected by the supplied configuration file, then run:

```shell
mvn clean install
java -Dio.jexxa.config.import=./src/test/resources/jexxa-test.properties \
     -jar ./target/contractmanagement-jar-with-dependencies.jar
```

The original tutorial uses `src/test/resources/jexxa-test.properties` for JDBC-backed persistence. Verify the database URL, credentials, and required schema before starting. The expected strategy is `JDBCObjectStore`.

### Invoke the REST API with curl

The following requests assume the application is available on port `7504` and the REST adapter exposes the shown service methods.

#### Create a contract for Paul

```shell
curl -X POST -H 'Content-Type: application/json' \
     -d '"Paul"' \
     http://localhost:7504/ContractService/createNewContract
```

Example response:

```json
{"value":1}
```

Repeat the request to create more contracts.

#### Query contracts by advisor

```shell
curl -X POST -H 'Content-Type: application/json' \
     -d '"Paul"' \
     http://localhost:7504/ContractService/getContractsByAdvisor
```

Example response after creating three contracts:

```json
[{"value":1},{"value":2},{"value":3}]
```

The order depends on the repository query; do not rely on it unless the application explicitly defines ordering.


```shell
curl -X POST -H 'Content-Type: application/json'      -d '{"value":1}'      http://localhost:7504/ContractService/signContract
```

#### Query the audit history

After signing contracts through the application's supported API, audit-history queries are available from `ContractAuditService` if those methods are exposed by the REST binding:

```text
ContractAuditService/getAllSignedContracts
ContractAuditService/getSignedContracts
```

```shell
curl   http://localhost:7504/ContractAuditService/getAllSignedContracts
```
Example response looks like:

```json
[{"contractNumber":{"value":1},"signatureDate":{"seconds":1793022377,"nanos":826240320}}]
```


The exact request body for `getSignedContracts(int month, int year)` depends on the REST adapter's parameter-serialization convention; consult the current REST binding tests before invoking it with `curl`.

## Summary

- **`IRepository`** is the first choice when lookup by unique key is sufficient.
- **`IObjectStore`** supports richer metadata-based queries while keeping storage details out of the Application Core.
- **`ContractService`** manages contracts; **`ContractAuditService`** maintains and queries the signed-contract audit history.
- **`ContractSigned`** is a Domain Event produced by the aggregate and published by the application service.
- **Bootstrap** activates the audit handler explicitly before REST adapters start.
- In-memory and JDBC-backed strategies can be selected without changing the application-defined repository interfaces.