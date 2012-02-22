Spring Batch CloudFoundry
=========================
https://github.com/gordonad/BatchCloudFoundry


This project is a Spring Maven project demonstrating configuring an application with partitioned processes running on CloudFoundry.

CloudFoundry
------------
http://www.cloudfoundry.com/
CloudFoundry Java Client: https://github.com/cloudfoundry/vcap-java


Spring Batch
------------
http://www.springsource.org/spring-batch

Spring Batch provides features allowing applications to process and manage large data sets, with retry and recovery processing.  This application intends to demonstrate remote partitioning of batch jobs on CloudFoundry.



Spring Integration
------------------
http://www.springsource.org/spring-integration


Spring Integration provides configurable integration between systems via Enterprise Integration Patterns (http://www.eaipatterns.com).

Spring Integration is used in this application to marshall & unmarshall data between local and remote processes.



Spring Batch Admin Console
--------------------------
https://github.com/SpringSource/spring-batch-admin

Console application for managing batch jobs.  Also contains the Spring-Batch-Integration sub-project.



Spring 3.1
----------

This project uses Spring Framework 3.1.  Environment abstraction provides features allowing this application to auto detect the environment that it is running within.  If the application discovers it is deployed on CloudFoundry, it will configure resources accordingly.  If not, it assumes local resources.

3.1 Features
- JavaConfig, no xml configuration
- Profile support with environment auto-detection



Spring Data-JPA
---------------
http://www.springsource.org/spring-data/jpa

Data management and configuration established via Spring Data.  This application is setup to use JPA2 with Hibernate as the provider.



Versions
--------
Spring: 3.1.0
Spring Data-JPA 1.0.2
CloudFoundry 0.8.1
Hibernate: 3.6.9
Logback 1.0.0
SLF4j 1.6.4
JUnit: 4.10



Installation
------------

### From source:

Execute:

`mvn clean install`



Contact Me
----------
http://technophile.gordondickens.com
@gdickens
gordon@gordondickens.com

