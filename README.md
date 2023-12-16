[![Maven Test Build](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/mavenBuild.yml/badge.svg)](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/mavenBuild.yml)
[![New Release](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/newRelease.yml/badge.svg)](https://github.com/jexxa-projects/JexxaTutorials/actions/workflows/newRelease.yml)
# Tutorials 

## General notes

All tutorials focus on the usage of Jexxa. Therefore, the business logic in these tutorials is without any special 
meaning. Nevertheless, we tried to find typical scenarios of business applications that can be easily mapped to other applications.

In these tutorials, we assume that you have a basic understanding of: 
* Writing Java code and build your programs using maven.

* Ideally, first experience in the use of a database and message bus. 

* Ideally, an initial idea of ports and adapters architecture as described [here](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/).

Additional information: 
* To implement the tutorials by yourself, you should use the [`jexxa-blank-archetype`](https://github.com/jexxa-projects/JexxaArchetypes) to generate a project skeleton. 
* All tutorials run by default without any additional infrastructure services such as message bus or a database.

* In case you want to just run the tutorials, you can use: 
  * Docker images provided [here](https://github.com/orgs/jexxa-projects/packages?repo_name=JexxaTutorials). 
  * Docker stacks for all tutorials: [HelloJexxa](deploy/hellojexxa-compose.yml), [TimeService](deploy/timeservice-compose.yml), [BookStore](deploy/bookstore-compose.yml), [ContractManagement](deploy/contract-management-compose.yml)
  
* In case you want to build the tutorials, please ensure a locally running [developer stack](deploy/developerStack.yml) providing a Postgres database, ActiveMQ broker, and Swagger-UI to access these applications.

## HelloJexxa
See documentation [HelloJexxa](HelloJexxa/README.md)

## TimeService—Async Messaging
See documentation [TimeService](TimeService/README.md)

## TimeService—Flow of Control
See documentation [TimeService—Flow of Control](TimeService/README-FlowOfControl.md)

## BookStore—Using a Repository  
See documentation [BookStore](BookStore/README.md)

## BookStore—Pattern Language
See documentation [BookStore—Pattern Language](BookStore/README-PatternLanguage.md)

## BookStore—Architecture Validation 
See documentation [BookStore—Architecture Validation](BookStore/README-ArchitectureValidation.md)

## BookStore—Writing Tests 
See documentation [BookStore—Writing Tests](BookStore/README-JexxaTest.md)

## BookStore—OpenAPI Support 
See documentation [BookStore—With OpenAPI Support](BookStore/README-OPENAPI.md)

## ContractManagement—Using an ObjectStore  
See documentation [ContractManagement](ContractManagement/README.md)

## Copyright and license

Code and documentation copyright 2020–2023 Michael Repplinger. Code released under the [Unlicense](LICENSE). Docs released under [Creative Commons](https://creativecommons.org/licenses/by/3.0/).
