![Technophile Blog](imagesc)

Spring Batch CloudFoundry
================================
[BatchCloudFoundry Source Code](https://github.com/gordonad/BatchCloudFoundry)

-----


Introduction
------------

This project provides examples from [The Technophile Blog](http://technophile.gordondickens.com)


This project is a Spring Maven project demonstrating configuring an application with partitioned processes running on CloudFoundry.

CloudFoundry
------------
* [CloudFoundry](http://www.cloudfoundry.com/)
* [CloudFoundry Java Client](https://github.com/cloudfoundry/vcap-java)


Spring Batch
------------
* [Spring Batch](http://www.springsource.org/spring-batch)

Spring Batch provides features allowing applications to process and manage large data sets, with retry and recovery processing.  This application intends to demonstrate remote partitioning of batch jobs on CloudFoundry.



Spring Integration
------------------
* [Spring Integration](http://www.springsource.org/spring-integration)


Spring Integration provides configurable integration between systems via Enterprise Integration Patterns (http://www.eaipatterns.com).

Spring Integration is used in this application to marshall & unmarshall data between local and remote processes.



Spring Batch Admin Console
--------------------------
* [Spring Batch Admin Project](https://github.com/SpringSource/spring-batch-admin)

Console application for managing batch jobs.  Also contains the Spring-Batch-Integration sub-project.



Spring 3.2
----------

This project uses Spring Framework 3.2.  Environment abstraction provides features allowing this application to auto detect the environment that it is running within.  If the application discovers it is deployed on CloudFoundry, it will configure resources accordingly.  If not, it assumes local resources.

3.1 Features
- JavaConfig, no xml configuration
- Profile support with environment auto-detection



Spring Data-JPA
---------------
* [Spring Data JPA](http://www.springsource.org/spring-data/jpa)

Data management and configuration established via Spring Data.  This application is setup to use JPA2 with Hibernate as the provider.



Versions
--------
* Spring: 3.2.0
* Spring Data-JPA 1.2.0
* CloudFoundry 0.8.4
* Hibernate: 3.6.10
* Logback 1.0.9
* SLF4j 1.7.2
* JUnit: 4.11
* Hamcrest: 1.3


-----

Building
---------------------


Import this Maven project into Eclipse or IntelliJ

At the command line, you can always execute the following Maven command to regenerate the Eclipse project files.

    _mvn clean eclipse:clean eclipse:eclipse_


-----

Generating Reports
---------------------

    _mvn clean install site_



-----



Markdown References
-------------------

* [Markdown on Wikipedia](http://en.wikipedia.org/wiki/Markdown)

* [Original Markdown Project - Daring Fireball](http://daringfireball.net/projects/markdown/)

* [For a free Windows based Markdown file reader](http://www.markdownpad.com)

* [For Mac Markdown file reader](http://www.barebones.com/products/TextWrangler/)



Contact Me
----------
http://technophile.gordondickens.com
@gdickens
gordon@gordondickens.com
