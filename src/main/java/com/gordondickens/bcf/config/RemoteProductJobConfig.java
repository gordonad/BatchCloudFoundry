package com.gordondickens.bcf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:META-INF/spring/amqp.properties")
@Configuration
public class RemoteProductJobConfig {
    private static final String IMPORT_PRODUCTS = "importProducts";

    private static final Logger logger = LoggerFactory.getLogger(RemoteProductJobConfig.class);


// TODO - PartitionHandler
//    @Bean
//    public MessageChannelPartitionHandler partitionHandler() {
//        MessageChannelPartitionHandler messageChannelPartitionHandler = new MessageChannelPartitionHandler();
//        MessagingTemplate messagingTemplate = new MessagingTemplate();
//        messagingTemplate.setDefaultChannel(new PollableAmqpChannel("requests"));
//    }
    /*

    <bean id="partitionHandler" class="org.springframework.batch.integration.partition.MessageChannelPartitionHandler">
  <property name="messagingOperations">
    <bean class="org.springframework.integration.core.MessagingTemplate">
      <property name="defaultChannel" ref="requests"/>
      <property name="receiveTimeout" value="10000"/>
    </bean>
  </property>
  <property name="replyChannel" ref="replies"/>
  <property name="stepName" value="importProductsStep"/>
  <property name="gridSize" value="2"/>
</bean>

     */


    //TODO - Partitioned Job
    /*

    <bean id="partitionHandler" class="org.springframework.batch.integration.partition.MessageChannelPartitionHandler">
  <property name="messagingOperations">
    <bean class="org.springframework.integration.core.MessagingTemplate">
      <property name="defaultChannel" ref="requests"/>
      <property name="receiveTimeout" value="10000"/>
    </bean>
  </property>
  <property name="replyChannel" ref="replies"/>
  <property name="stepName" value="importProductsStep"/>
  <property name="gridSize" value="2"/>
</bean>

     */

/* *****************************
       REMOTE SLAVE CONFIG
 */

//TODO - Service Activator for Slave
    /*
<int:service-activator
         ref="stepExecutionRequestHandler"
         input-channel="requests"
         output-channel="replies">
  <poller>
    <interval-trigger interval="10" />
  </poller>
</service-activator>


     */

    //TODO - Configure Step Exeution Request Handler


    // Step Locator - Slave process requires for step location
    @Bean
    public BeanFactoryStepLocator beanFactoryStepLocator() {
        return new BeanFactoryStepLocator();
    }


}
