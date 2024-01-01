
# Microservice Spring Boot Implementation

This repository contains a microservice Spring Boot implementation for the architecture shown in the figure below.

![alt Project Architecture](./architecture/Microservices-Spring-Boot-Architecture.png "Project Architecture")

## Overview

Explain briefly what this microservice implementation does and what purpose it serves. Highlight any key features or components.

## How to run the application using Docker
1.  Run  `mvn clean package -DskipTests`  to build the applications and create the docker image locally.
2.  Run  `docker-compose up -d`  to start the applications.

## How to run the application without Docker
1.  Run  `mvn clean verify -DskipTests`  by going inside each folder to build the applications.
2.  After that run  `mvn spring-boot:run`  by going inside each folder to start the applications.