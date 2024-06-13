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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

//import io.opentelemetry.exporter.logging.LoggingMetricExporter;
//import io.opentelemetry.exporter.logging.LoggingSpanExporter;
//import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;


@SpringBootApplication
@Configuration
public class OtSpringBootKafkaInterceptorApplication {
    private static Tracer tracer;
    private static Span spanParent;
    private static OpenTelemetry openTelemetry;

    private static final Log logger = LogFactory.getLog(OtSpringBootKafkaInterceptorApplication.class);

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        logger.info("Starting OpenTelemetry Interceptor application ");
        //openTelemetry = initConfiguration.initializeOpentelemtryHttp();
        openTelemetry = initConfiguration.initializeOpentelemtryGrpc();
        logger.info("Opentelemetry is initialized");


        applicationContext = SpringApplication.run(OtSpringBootKafkaInterceptorApplication.class, args);

        //Create Confluent Consumer -- Producer and Rest Controller are automatically started with Spring
        ConfluentTransConsumer confluentConsumer = new ConfluentTransConsumer(applicationContext.getEnvironment());

    }
}
