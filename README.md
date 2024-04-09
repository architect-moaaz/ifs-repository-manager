# ifs-repomanager-svc Project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
mvn clean compile quarkus:dev -Ddebug=5005
```
or you can use the bash command available

```aidl
dev
```

## Local Server Setup

You can setup the service to run in your local system by following the below guide:

https://docs.google.com/document/d/1geUMswGMz1nOnSNVu6WxgU64sWlyN3SsEI8vpJZG_os

The dev mode properties can be updated in the application modifying the `application-dev.properties` file.
For the production mode `application.properties` files must be updated.

## Modeller Service

Initial part of the Intelliflow backend which exposes the primary App Designer API's. It also manages the 
various services on the available files in the application like the bpmn, dmn, forms and data models. 
CRUD operations, generation flows and mapping logics are available in the service. 

The REST API's for the Workspace, Application and Files are all exposed in the same.
# Dockerizing Ifs-Repository-Manager
 
 step 1: Build the docker image.
   ---
    docker build -f src/main/docker/Dockerfile.New.jvm -t quarkus/ifs-repomanager-svc-jvm --build-arg PROFILE=colo .
   ---
   step 2: Run the docker image.
   ----
    command used: docker run -i --rm -p 31510:31510 quarkus/ifs-repomanager-svc-jvm
     ---
     The above command starts the repository manager app image inside the container and exposes port 51510 inside container to port 51510 outside the container.
     ----

   step 3: Check the image created 
   ---
    command used: docker images
   ---
 step 4:Access the route on server using http://localhost:31510
