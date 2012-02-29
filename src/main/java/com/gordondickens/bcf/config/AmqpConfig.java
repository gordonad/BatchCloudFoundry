package com.gordondickens.bcf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:META-INF/spring/applicationContext-amqp.xml")
//@PropertySource("classpath:META-INF/spring/amqp.properties")
public class AmqpConfig {
    private static final Logger logger = LoggerFactory.getLogger(AmqpConfig.class);

//    @Inject
//    private AbstractEnvironment environment;
//
//    public String getRabbitUserName() {
//        return environment.getProperty("rabbit.username", "guest");
//    }
//    public String getRabbitPassword() {
//        return environment.getProperty("rabbit.password", "guest");
//    }
//    public String getRabbitHost() {
//        return environment.getProperty("rabbit.host", "localhost");
//    }
//    public String getRabbitPort() {
//        return environment.getProperty("rabbit.port", "5672");
//    }
//    public String getRabbitChannelCacheSize() {
//        return environment.getProperty("rabbit.channel.cache.size", "1");
//    }
//    public String getRabbitVirtualHost() {
//        return environment.getProperty("rabbit.virtual.host", "/");
//    }
//
//    // Queues
//    public String getJobRequestQueue() {
//        return environment.getProperty("bcf.jobRequestQueue", "bcf.request.queue");
//    }
//    public String getJobResponseQueue() {
//        return environment.getProperty("bcf.jobResponseQueue", "bcf.response.queue");
//    }
//    public String getJobErrorQueue() {
//        return environment.getProperty("bcf.jobErrorQueue", "bcf.error.queue");
//    }
//
//    // Queue Timeouts
//    public String getRequestTimeout() {
//        return environment.getProperty("bcf.requestTimeout", "10000");
//    }
//    public String getResponseTimeout() {
//        return environment.getProperty("bcf.responseTimeout", "10000");
//    }
//
//    //Exchanges
//    public String getDefaultMessageExchange() {
//        return environment.getProperty("bcf.message.exchange", "bcf.batch.exchange");
//    }
//
//    //Routing Keys
//    public String getDefaultRoutingKey() {
//        return environment.getProperty("bcf.message.routing.key", "bcf.batch.partition");
//    }
}
