package com.gordondickens.bcf.rabbit;

import org.springframework.batch.core.StepExecution;


public interface RabbitGateway {
    public StepExecution send(String payload);
}
