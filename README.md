# Customer Onboarding Process - Extended Example

*Extended process solution example for customer onboarding, based on the simplified example used in the OReilly book [Practical Process Automation](https://processautomationbook.com/).*

![Customer Onboarding](docs/customer-onboarding-extended.png)

This following stack is used:

* Camunda Platform 8
* Java
* Spring Boot

There are some open todos for this example:

* A custom form for the user task to approve customer orders (the generic form in the tasklist is used instead)


# Simple Process

This extended process is based on a simpler example, that is meant to get started with process automation, workflow engines and BPMN:

![Customer Onboarding](docs/customer-onboarding-simple.png)

You can find it here: https://github.com/berndruecker/customer-onboarding-camunda-8-springboot

The simple process model contains three tasks:

* A service task that executes Java Code to score customers (using the stateless Camunda DMN engine)
* A user task so that humans can approve customer orders (or not)
* A service task that executes glue code to call the REST API of a CRM system


# Extended Process

![Customer Onboarding](docs/customer-onboarding-extended.png)

The extended process model adds some more tasks in the process:

* A DMN decision task that decides, if a customer order can be automatically processed or not (replacing the manual approval above)
* Scoring the customer will now be done via an external scoring service, that has an AMQP (messaging) API. The technical details around communication via AMQP are extracted in a seperate subprocess:

![Scoring](docs/customer-scoring.png)


The process solution is a Maven project and contains:

* The process models as BPMN
* Source code to provide the REST API for clients (using Spring Boot)
* Java code to do the customer scoring
* Glue code to implement the REST call to the CRM system
* Glue code for AMQP communication
* Fakes for CRM system and AMQP


# How To Run

There is a walk through video available for the simple version of this process:

<a href="http://www.youtube.com/watch?feature=player_embedded&v=QUB0dSBBMPM" target="_blank"><img src="http://img.youtube.com/vi/QUB0dSBBMPM/0.jpg" alt="Walkthrough" width="240" height="180" border="10" /></a>

## Create Camunda 8 SaaS Cluster

* Login to https://camunda.io/ (you can create an account on the fly)
* Create a new cluster
* Create a new set of API client credentials
* Copy the client credentials into `src/main/resources/application.properties`

## Run RabbitMQ locally

```
docker run -p 15672:15672 -p 5672:5672 rabbitmq:3-management
```

* http://localhost:15672/#/queues/
* User: guest
* Password: guest

## Run Spring Boot Java Application

The application will deploy the process model during startup

`mvn package exec:java`


## Play

You can easily use the application by requesting a new customer onboarding posting a PUT REST request to 

`curl -X PUT http://localhost:8080/customer`

You can now see the process instance in Camunda Operate - linked via the Cloud Console.

You can work on the user task using Camunda Tasklist, also linked via the Cloud Console.

