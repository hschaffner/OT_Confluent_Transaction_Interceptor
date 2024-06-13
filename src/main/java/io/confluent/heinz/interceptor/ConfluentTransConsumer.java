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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import io.confluent.heinz.transactions.JsonMsgK;
//import io.confluent.heinz.transactions.avroMsg;

//import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

//@Component
public class ConfluentTransConsumer {
    private final Log logger = LogFactory.getLog(ConfluentTransConsumer.class);
    private KafkaProducer producer;
    private String topic;


    public ConfluentTransConsumer(Environment env){
        logger.info("Check for brokers: " + env.getProperty("bootstrap.servers"));
        topic = env.getProperty("topic");
        createKafkaSession(env);

    }

    public void createKafkaSession(Environment env) {

        AtomicBoolean running = new AtomicBoolean(true);

        ObjectMapper mapper = new ObjectMapper();

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", env.getProperty("bootstrap.servers"));
        props.setProperty("value.serializer", io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
        props.setProperty("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class.getName());
        //props.setProperty("value.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class.getName());
        //props.setProperty("key.deserializer", io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer.class.getName());
        props.setProperty("value.deserializer", io.confluent.kafka.serializers.KafkaAvroDeserializer.class.getName());
        props.setProperty("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        props.setProperty("schema.registry.url", env.getProperty("schema.registry.url"));
        props.setProperty("schema.registry.basic.auth.user.info", env.getProperty("schema.registry.basic.auth.user.info"));
        props.setProperty("basic.auth.credentials.source", env.getProperty("basic.auth.credentials.source"));
        props.setProperty("sasl.mechanism",env.getProperty("sasl.mechanism"));
        props.setProperty("sasl.jaas.config", env.getProperty("sasl.jaas.config"));
        props.setProperty("security.protocol", env.getProperty("security.protocol"));
        props.setProperty("client.dns.lookup", env.getProperty("client.dns.lookup"));
        props.setProperty("acks", "all");
        props.setProperty("auto.create.topics.enable", "true");
        props.setProperty("topic.creation.default.partitions", "3");
        props.setProperty("auto.register.schema", "true");
        //props.setProperty("json.fail.invalid.schema","true");
        //Required for Exactly Once
        props.setProperty("enable.idempotence", env.getProperty("enable.idempotence"));
        //Required for Exactly Once
        props.setProperty("transactional.id", env.getProperty("transactional.id")+"consume");
        //Required for Exactly Once
        props.setProperty("group.id", env.getProperty("consume.group.id"));
        //Required for Exactly Once
        props.setProperty("enable.auto.commit",env.getProperty("consume.enable.auto.commit"));
        //Required for Exactly Once
        props.setProperty("isolation.level", env.getProperty("consume.isolation.level"));
        //props.setProperty(KafkaJsonDeserializerConfig.JSON_VALUE_TYPE, JsonMsgK.class.getName());
        props.setProperty("specific.avro.reader", "true" );

        //Props for producer without interceptor
        Properties propsProducer = (Properties)props.clone();

        props.setProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, TracingConsumerInterceptor.class.getName());


        //initialize producer session and initialize transaction
        producer = new KafkaProducer<>(propsProducer);
        producer.initTransactions();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Consumer");
            running.set(false);
        }));


        //Example using GenericRecord
        /*
        try (final Consumer<JsonNode, GenericRecord> consumer = new KafkaConsumer<JsonNode, GenericRecord>(props)) {
            consumer.subscribe(Arrays.asList(group));

            while (running) {
                ConsumerRecords<JsonNode, GenericRecord> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<JsonNode, GenericRecord> record : records) {
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
                    JsonMsgK jKey = mapper.convertValue(record.key(), new TypeReference<JsonMsgK>(){});
                    String key = jKey.toString();
                    String value = record.value().toString();
                    System.out.println(
                            String.format("Consumed event from topic %s: key = %-10s value = %s", topic, key, value));
                    System.out.println("Value Schema: " + record.value().getSchema());
                }
            }



        }

         */

        //Sample using Specific Record, note you must enable Avro Specific Record in Configuration
        try (final Consumer<String, avroMsg> consumer = new KafkaConsumer<String, avroMsg>(props)) {
            consumer.subscribe(Arrays.asList(topic));

            while (running.get()) {
                producer.beginTransaction();
                ConsumerRecords<String, avroMsg> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, avroMsg> record : records) {
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++");
                    //JsonMsgK jKey = mapper.convertValue(record.key(), new TypeReference<JsonMsgK>(){});

                    String key = record.key();
                    String value = record.value().toString();
                    System.out.println(
                            String.format("Consumed event from topic %s: key = %-10s value = %s", topic, key, value));
                    System.out.println("Value Schema: " + record.value().getSchema());
                    System.out.println("++++++++++++++++++++++++++++++++++++++++++++++\n");

                }

                //Get Consumer Offset information to commit as part of Producer Transaction
                Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = new HashMap<>();
                for (TopicPartition partition : records.partitions()) {
                    System.out.println("Partitions: " + records.partitions());
                    List<ConsumerRecord<String, avroMsg>> partitionedRecords = records.records(partition);
                    long offset = partitionedRecords.get(partitionedRecords.size() - 1).offset();
                    offsetsToCommit.put(partition, new OffsetAndMetadata(offset + 1));
                    System.out.println("Offset to commit: " + new OffsetAndMetadata(offset + 1) + " Partition: " + partition + " Number of Records: " + partitionedRecords.size());
                }
                //Add the offset commit producer to the transaction
                producer.sendOffsetsToTransaction(offsetsToCommit, props.getProperty("group.id"));
                try {
                    producer.commitTransaction();
                } catch (Exception e) {
                    producer.abortTransaction();
                    logger.info("!!!!!!!!!!!!!! Transaction was aborted");
                }
            }

        }

    }
}
