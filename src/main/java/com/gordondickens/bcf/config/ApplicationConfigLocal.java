package com.gordondickens.bcf.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@Profile(Env.LOCAL)
@PropertySource("classpath:META-INF/spring/database-local.properties")
public class ApplicationConfigLocal extends ApplicationConfigCommon {
	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationConfigLocal.class);

	@Override
	protected DatabasePopulator databasePopulator(DataSource dataSource) {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(true);
		populator.addScript(new ClassPathResource("batch-hsqldb-ddl.sql"));
		try {
			populator.populate(dataSource.getConnection());
		} catch (SQLException e) {
			logger.error("Exception Populating Database", e);
		}
		return populator;
	}

}
