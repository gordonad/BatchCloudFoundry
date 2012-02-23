package com.gordondickens.bcf.config;

import com.gordondickens.bcf.services.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:META-INF/spring/database-heroku.properties")
@Profile(Env.HEROKU)
@EnableTransactionManagement
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)})
public class ApplicationConfigHeroku extends ApplicationConfigCommon {
    private static final Logger logger = LoggerFactory
            .getLogger(ApplicationConfigHeroku.class);

    @Bean
    public Properties serviceProperties() {
        Properties props = new Properties();
        props.put("url", getUrl());
        props.put("name", getDatabaseName());
        props.put("hostname", getHost());
        props.put("port", getPort());
        props.put("username", getUser());
        props.put("password", getPassword());
        props.put("driver", getDriverClassName());
        return props;
    }


    @Override
    protected DatabasePopulator databasePopulator(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
//        populator.addScript(new ClassPathResource("org/springframework/batch/core/batch-mysql-ddl.sql"));
        populator.addScript(new ClassPathResource("batch-postgres-ddl.sql"));
        try {
            populator.populate(dataSource.getConnection());
        } catch (SQLException e) {
            logger.error("Exception Populating Database", e);
        }
        return populator;
    }

}
