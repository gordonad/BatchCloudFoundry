package com.gordondickens.bcf.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;

import javax.annotation.PostConstruct;
import java.util.Map;

public class AppEnvironment {
    private static final Logger logger = LoggerFactory.getLogger(AppEnvironment.class);

    @Value("#{ systemProperties }")
    private Map<String, Object> systemProperties;

    @Value("#{ systemEnvironment }")
    private Map<String, Object> systemEnvironment;

    @Value("#{ environment }")
    private AbstractEnvironment environment;

    @Override
    public String toString() {
        return "\n\n********************** AppEnvironment **********************\n["
                + "\n\tsystemProperties=" + formatMe(systemProperties.toString())
                + ", \n\n\tsystemEnvironment=" + formatMe(systemEnvironment.toString())
                + ", \n\n\tenvironment=" + formatMe(environment.toString()) + "]\n" +
                "********************** AppEnvironment **********************";
    }

    private static final String formatMe(String in) {
        String out = in;
        out = in.replace("{", "{\n\t\t");
        out = out.replace(", ", "\n\t\t");

        return out;
    }


    @PostConstruct
    public void afterInstantiation() {
        logger.trace(this.toString());
    }

    public void setSystemProperties(Map<String, Object> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public void setSystemEnvironment(Map<String, Object> systemEnvironment) {
        this.systemEnvironment = systemEnvironment;
    }

    public void setEnvironment(AbstractEnvironment environment) {
        this.environment = environment;
    }

}
