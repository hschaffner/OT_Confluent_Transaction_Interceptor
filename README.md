# Springboot Confluent Transaction Sample with OpenTelemetry Tracing and OpenTelemetry Interceptor

This project demonstrates the use of Confluent Kafka Transaction. The application is instrumented with OpenTelemetry to provide latency measurements that can be
monitored using Jaeger. The project can be run locally from the shell or in Kubernetes. The shell level is
supported using Maven for the local deployment or using Skaffold to automate the Kubernetes deployment.

The project is created with Intellij. The shell and Kubernetes deployment can also be run directly from Intellij (using the "Develop on Kubernetes" environment).

This is simply a sample program to demonstrate functionality and is, therefore, not a "polished" application (i.e. expect warning).

## Jaeger Installation

The current version of Jaeger supports direct consumption of OpenTelemetry feed data. Therefore, it is only necessary to install Jaeger as the OpenTelemetry collector.

For local shell application deployment simply install the Jaeger binaries on your host. Next, you can start the Jaeger binary wih the following arguments:

` jaeger-all-in-one --collector.grpc.tls.enabled=false --collector.otlp.grpc.tls.enabled=false `

The default setup for OpenTelemetry uses grpc for communication to Jaeger (the code also supports HTTP). Access to the GUI is on the default:

`localhost:16686`

When the sample application is deployed in Kubernetes, it is assumed that
the Jaeger Operator has been installed. A Jaeger object must be created in Kubernetes. A sample memory-based all-in-one object samples is found un the
"kube-setup" directory. To gain access, it is necessary to port forward the "my-jaeger-query" service for port 16686. This will allow access to the
Kubernetes Jaeger Query application via:

`localhost:16686`

Make sure there is no local Jaeger server running on localhost. If there is, make sure the export of port 16686 is directed to a different local port.

## Configuration of the Spring Application

The application is expected to make use of Confluent Cloud for the Kafka services. The default topic that was used was called "avroTopic.ot.intercept" and was created with 4 partitions. You can adjust these values to whatever you want, however, the changes need to be reflected in the code.



The project makes use of the Avro and JSON plugins to automatically create the schema POJO objects from the schemas. The project makes use of the POJOs directly since
the Confluent serializer/deserializer automatically works directly from the schema POJOs and working directly with the POJO without conversion back to JSON or AVRO makes the coding easier.

Packaging the project with Maven should automatically create the POJOs. If they do not exist, they can be created with:

` mvn generate-sources `

Most of the Confluent session configuration parameters are referenced to the "application.yaml" file. Some of the Confluent
session parameters are hard-coded directly in the "ConfluentSession" application.

OpenTelemetry is configured in the "initConfiguration" application, which is called from main() in the "OtSpringBootKafkaInterceptorApplication". 

A simple Kubernetes template is included to deploy the Spring application and another template creates the load-balancer and exposes the IP address to send a JSON message to the REST controller.

## Running the Application

As mentioned above, the application can be run from the shell level. It is necessary
ensure Jaeger collector is already running. At that point a simple command of:

` mvn spring-boot:run `

should start the project. At this point you can send a JSON message using CURL. A sample message and a CURL sample are
available in the "PostMessage.sh" file. The transaction producer and consumer are triggered when ther REST controller receives a JSON message.


It is also possible to automatically call mvn and automatically create the image and deploy the application to
Kubernetes using:

` skaffold dev `

Obviously, you must have installed and set up the "skaffold" command on your host. Make sure the
"~/.skaffold/config" file matches your required image repository and Kubernetes context.

You use the same JSON file and CURL command. However, the IP address of the CURL command must be the exposed load-balancer port that is
defined in the Kubernetes service that was created.

You can stop the skaffold deployment with a simple "control-c", which will automatically stop the Kubernetes Deployment and Load-Balancer service.


It is also possible to run Skaffold/JIB automatically from Intellij. Make sure the configuration file for the
"Develop on Kubernetes" dropdown has been properly configured to reflect your Kubernetes context and
image repository.

Jaeger will provide the tracing details and latencies. 


