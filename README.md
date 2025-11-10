[![Maven Test Build](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/mavenBuild.yml/badge.svg)](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/mavenBuild.yml)
[![New Release](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/newRelease.yml/badge.svg)](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/newRelease.yml)
# Tutorials

## General Notes

These tutorials focus on the usage of **Jexxa**.  
The included business logic has no special meaning but is based on typical scenarios from business applications that can easily be transferred to other domains.

We assume you have a basic understanding of:
* **Java programming** and building applications with **Maven**
* (Optional) some initial experience with **databases** and **message buses**
* (Optional) a first idea of the **ports and adapters architecture** (see [this article](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/))

### Additional Information
* To implement the tutorials yourself, you can use the [`jexxa-blank-archetype`](https://github.com/jexxa-projects/JexxaArchetypes) to generate a project skeleton.
* All tutorials run by default without requiring additional infrastructure services such as a message bus or database.
* If you just want to **run the tutorials**, you can use:
    * Prebuilt Docker images available [here](https://github.com/orgs/jexxa-projects/packages?repo_name=JexxaTutorials)
    * Docker stacks for each tutorial:
        * [HelloJexxa](deploy/hellojexxa-compose.yml)
        * [TimeService](deploy/timeservice-compose.yml)
        * [BookStore](deploy/bookstore-compose.yml)
        * [ContractManagement](deploy/contract-management-compose.yml)
* If you want to **build the tutorials locally**, make sure you have a running [developer stack](deploy/developerStack.yml), which provides:
    * a Postgres database
    * an ActiveMQ broker
    * Swagger-UI to access these applications

---

## HelloJexxa
See documentation: [HelloJexxa](HelloJexxa/README.md)

## TimeService — Async Messaging
See documentation: [TimeService](TimeService/README.md)

## TimeService — Flow of Control
See documentation: [TimeService — Flow of Control](TimeService/README-FlowOfControl.md)

## BookStore — Using a Repository
See documentation: [BookStore](BookStore/README.md)

## BookStore — Pattern Language
See documentation: [BookStore — Pattern Language](BookStore/README-PatternLanguage.md)

## BookStore — Architecture Validation
See documentation: [BookStore — Architecture Validation](BookStore/README-ArchitectureValidation.md)

## BookStore — Writing Tests
See documentation: [BookStore — Writing Tests](BookStore/README-JexxaTest.md)

## BookStore — OpenAPI Support
See documentation: [BookStore — With OpenAPI Support](BookStore/README-OPENAPI.md)

## BookStoreCN — Using Cloud-Native Technology with Jexxa
See documentation: [BookStoreCN — Using Cloud-Native Technology with Jexxa](BookStoreCN/README.md)

## ContractManagement — Using an ObjectStore
See documentation: [ContractManagement](ContractManagement/README.md)

---

## Copyright and License

Code and documentation copyright © 2020–2025 Michael Repplinger.  
Code released under the [Unlicense](LICENSE).  
Documentation released under [Creative Commons](https://creativecommons.org/licenses/by/3.0/).