package com.gordondickens.bcf.config;

import com.gordondickens.bcf.services.Env;
import org.cloudfoundry.runtime.env.CloudPropertiesFactoryBean;
import org.cloudfoundry.runtime.env.MysqlServiceInfo;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@PropertySource("classpath:META-INF/spring/database-cloudfoundry.properties")
@Profile(Env.CLOUDFOUNDRY)
@EnableTransactionManagement
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)})
public class ApplicationConfigCloudFoundry extends ApplicationConfigCommon {
    private static final Logger logger = LoggerFactory
            .getLogger(ApplicationConfigCloudFoundry.class);

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

    @Bean
    public Properties cloudEnvironment() throws Exception {
        CloudPropertiesFactoryBean cloudPropertiesFactoryBean = new CloudPropertiesFactoryBean();
        Properties properties = cloudPropertiesFactoryBean.getObject();
        Map<String, Object> propertiesMap = new HashMap<String, Object>();

        try {
            logger.debug("Cloud Environment Info:");
            for (Object property : properties.keySet()) {
                propertiesMap.put(property.toString(), properties.get(property));
                logger.debug("\n\t'{}'='{}'",
                        property.toString(), properties.get(property).toString());
            }

            MysqlServiceInfo mysqlServiceInfo = new MysqlServiceInfo(propertiesMap);
            MysqlServiceCreator mysql = new MysqlServiceCreator();

            logger.debug("MySQL driver class '{}', Validation query '{}' ",
                    mysql.getDriverClassName(), mysql.getValidationQuery());

        } catch (Exception e) {
            logger.error("Error getting Properties from Cloud Environment", e);
        }
        logger.debug("Returning Cloud Environment Properties '{}'", properties);
        return properties;
    }

    /*
      * <cloud:service-properties id="serviceProperties" />
      *
      * <mongo:db-factory id="mongoDbFactory"
      * dbname="#{serviceProperties['db.name']}"
      * host="#{serviceProperties['db.hostname']}"
      * port="#{serviceProperties['db.port']}"
      * username="#{serviceProperties['db.username']}"
      * password="#{serviceProperties['db.password']}"/> -->
      */

    @Override
    protected DatabasePopulator databasePopulator(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
//        populator.addScript(new ClassPathResource("org/springframework/batch/core/batch-mysql-ddl.sql"));
        populator.addScript(new ClassPathResource("batch-mysql-ddl.sql"));
        try {
            populator.populate(dataSource.getConnection());
        } catch (SQLException e) {
            logger.error("Exception Populating Database", e);
        }
        return populator;
    }

}
