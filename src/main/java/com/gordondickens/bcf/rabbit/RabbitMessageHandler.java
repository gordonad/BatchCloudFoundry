package com.gordondickens.bcf.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory
            .getLogger(RabbitMessageHandler.class);

    @Override
    public String handleMessage(String s) {
        log.debug("Received Message {}", s);
        return s;
    }

}
