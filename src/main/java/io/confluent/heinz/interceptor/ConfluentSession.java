/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.confluent.heinz.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import io.confluent.kafka.schemaregistry.json.JsonSchemaUtils;


import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;

import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializerConfig;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializerConfig;
//import io.opentelemetry.api.OpenTelemetry;
//import io.opentelemetry.api.common.Attributes;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.TracerProvider;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.TracerProvider;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
//import io.opentelemetry.api.trace.TracerProvider;
//import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
//import io.opentelemetry.context.propagation.ContextPropagators;
//import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
//import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
//import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
//import io.opentelemetry.instrumentation.api.internal.InstrumenterUtil;
//import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingConsumerInterceptor;
//import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
//import io.opentelemetry.sdk.OpenTelemetrySdk;
//import io.opentelemetry.sdk.metrics.SdkMeterProvider;
//import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
//import io.opentelemetry.sdk.resources.Resource;
//import io.opentelemetry.sdk.trace.SdkTracerProvider;
//import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
//import io.opentelemetry.sdk.trace.samplers.Sampler;
//import io.opentelemetry.semconv.ResourceAttributes;
//import io.opentracing.Tracer;
//import io.opentracing.*;
//import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingConsumerInterceptor;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
//import io.opentracing.Tracer;
//import io.opentracing.contrib.kafka.TracingProducerInterceptor;

import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
//import io.opentracing.util.GlobalTracer;
//import io.opentracing.propagation.Format;
//import io.opentracing.util.GlobalTracer;
//import io.opentracing.util.GlobalTracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

//import static io.opentelemetry.semconv.ServiceAttributes.SERVICE_NAME;

@Component
public class ConfluentSession{
    private final Log logger = LogFactory.getLog(ConfluentSession.class);
    //private KafkaProducer<avroMsgK, ObjectNode> producerCfltCe;
    private KafkaProducer producer;
    private String topic = "";
    private int counter = 0;
    private Properties props;
    //private JsonSchema jsonSchema1 = null;
    private Environment env = null;

    //private Map<String, JsonNode> resolvedRefsNodes;
    //private List<SchemaReference> sReferences;

    //public Tracer tracer;
    Tracer tracer = GlobalOpenTelemetry.get().getTracer("ServerApp");
    Context parentContext = Context.current();

    //private JaegerGrpcSpanExporter jaegerExporter;

    //Constructor
    public ConfluentSession(Environment env) {


        createConfluentSession(env);
        createConfluentProducer();

    }




    public void createConfluentSession(Environment env) {
        this.env = env;
        props = new Properties();

        Tracer tracer = GlobalOpenTelemetry.get().getTracer("ServerApp");
        Context parentContext = Context.current();

        topic = env.getProperty("topic");

        props.setProperty("bootstrap.servers", env.getProperty("bootstrap.servers"));
        props.setProperty("schema.registry.url", env.getProperty("schema.registry.url"));
        props.setProperty("schema.registry.basic.auth.user.info",
                env.getProperty("schema.registry.basic.auth.user.info"));
        props.setProperty("basic.auth.credentials.source", env.getProperty("basic.auth.credentials.source"));
        props.setProperty("sasl.mechanism", env.getProperty("sasl.mechanism"));
        props.setProperty("sasl.jaas.config", env.getProperty("sasl.jaas.config"));
        props.setProperty("security.protocol", env.getProperty("security.protocol"));
        props.setProperty("client.dns.lookup", env.getProperty("client.dns.lookup"));
        props.setProperty("acks", "all");
        props.setProperty("auto.create.topics.enable", "false");
        props.setProperty("topic.creation.default.partitions", "3");
        props.setProperty("auto.register.schema", "false");
        props.setProperty("json.fail.invalid.schema", "true");
        props.setProperty("enable.idempotence", env.getProperty("enable.idempotence"));

        //Properties for interceptor
        props.setProperty(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingProducerInterceptor.class.getName());
        //props.setProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());
        //TracingProducerInterceptor tracingProducerInterceptor = new TracingProducerInterceptor<>();

    }

    private void createConfluentProducer() {
        AtomicBoolean running = new AtomicBoolean(true);
        if (producer == null) {
            logger.info("Creating new Kafka Producer");


            props.put("client.id", env.getProperty("producer.id"));
            props.setProperty("value.serializer", io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
            props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.setProperty("auto.create.topics.enable", "true");
            props.setProperty("topic.creation.default.partitions", "3");
            props.setProperty("auto.register.schema", "true");
            props.setProperty("json.fail.invalid.schema","true");
            props.setProperty("transactional.id", env.getProperty("transactional.id"));

            //Create the Confluent producer
            producer = new KafkaProducer<>(props);
            producer.initTransactions();

            logger.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=- created producer");

            Map<String, String> env_var = System.getenv();

            // Loop through all environment variables
            for (String envName : env_var.keySet()) {
                // Print environment variable name and value to console
                System.out.format("%s=%s", envName, env_var.get(envName));
                System.out.println();
            }

            //shutdown hook when process is interrupted
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Stopping Producer");
                producer.flush();
                producer.close();
                running.set(false);
            }));

        }
    }

    public void sendAvroMessage(JsonMsg jMsg) throws FileNotFoundException {
        String jsonKey = "";

        //toggle count up and down for 0/1
        if (counter == 0){
            counter++;
        } else {
            counter=0;
        }



        //Simpler for avro and automatically includes the schema as part of the transmission
        avroMsg avroRecord = new avroMsg();
        avroRecord.setFirstName(jMsg.getFirstName());
        avroRecord.setLastName(jMsg.getLastName());
        avroRecord.setCustomerId(jMsg.getCustomerId() + counter);

        // create producer record that includes JSON schema and payload that is understood by Confluent JSON Schema Serializer
        ProducerRecord producerRecord = new ProducerRecord<Object, avroMsg>(topic, String.valueOf(avroRecord.getCustomerId()), avroRecord);
        //TracingKafkaUtils.buildAndInjectSpan(producerRecord, GlobalTracer.get()).finish();

        /*
        Headers headers = producerRecord.headers();
        for (Header header : headers) {
            String key = header.key();
            String value = new String(header.value());
            System.out.println("Key: " + key + ", Value: " + value);
        }

         */

        //System.out.println("ProducerHeader: " + producerRecord.headers());

        //System.out.println("Schema: " + avroRecord.getSchema().toString(true));
        //System.out.println("First: " + avroRecord.get("first_name"));
        //System.out.println("Last: " + avroRecord.get("last_name"));
        //System.out.println("ID: " + avroRecord.get("customer_id"));

        try {
            producer.beginTransaction();
            producer.send(producerRecord, new MyProducerCallback());
            producer.commitTransaction();
        } catch(SerializationException e) {
            System.out.println("Error:");
            e.printStackTrace();
            producer.abortTransaction();
        }



    }

    class MyProducerCallback implements Callback {

        private final Log logger = LogFactory.getLog(MyProducerCallback.class);
        Tracer tracer = GlobalOpenTelemetry.get().getTracer("ServerApp");
        Context parentContext = Context.current();

        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            Span span = tracer.spanBuilder("PublishCallback")
                    .setParent(parentContext)
                    //.setAttribute("Servlet Path", httpRequest.getServletPath())
                    //.setAttribute("Customer ID", request.getCustomerId())
                    //.setAttribute("Protoocl", httpRequest.getProtocol())
                    //.setAttribute("Content Length", httpRequest.getContentLength())
                    .setAttribute("RecordMetaDataOffset", recordMetadata.offset())
                    .setAttribute("RecordMetaDataPartition", recordMetadata.partition())
                    .startSpan();
            try (Scope scope = span.makeCurrent()) {
                if (e != null)
                    logger.info("AsynchronousProducer failed with an exception");
                else {
                    logger.info("AsynchronousProducer call Success:" + "Sent to partition: " + recordMetadata.partition() + " and offset: " + recordMetadata.offset() + "\n");
                }
            } finally {
                span.end();
            }
        }
    }

}
