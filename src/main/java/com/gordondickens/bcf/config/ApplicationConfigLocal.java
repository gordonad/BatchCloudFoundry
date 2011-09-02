package com.gordondickens.bcf.config;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Profile(Env.LOCAL)
@PropertySource("classpath:META-INF/spring/database-local.properties")
@ComponentScan(excludeFilters = { @ComponentScan.Filter(Configuration.class) })
public class ApplicationConfigLocal {
	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationConfigLocal.class);

	@Value("${database.url}")
	private String url;

	@Value("${database.username}")
	private String user;

	@Value("${database.password}")
	private String password;

	@Value("${database.driverClassName}")
	private String driverClassName;
	// private Class<? extends Driver> driverClassName;

	@Value("${database.dialect}")
	private Class<? extends Dialect> dialect;

	@Value("${database.vendor}")
	private String databaseVendor;

	@Bean
	public DataSource dataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setPassword(password);
		ds.setUrl(url);
		ds.setUsername(user);
		ds.setDriverClassName(driverClassName);
		ds.setTestOnBorrow(true);
		ds.setTestOnReturn(true);
		ds.setTestWhileIdle(true);
		ds.setTimeBetweenEvictionRunsMillis(1800000);
		ds.setMinEvictableIdleTimeMillis(1800000);
		ds.setNumTestsPerEvictionRun(3);
		databasePopulator(ds);
		return ds;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory()
			throws Exception {
		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
		emfb.setDataSource(dataSource());

		HibernateJpaVendorAdapter jva = new HibernateJpaVendorAdapter();
		jva.setDatabasePlatform(databaseVendor);
		jva.setGenerateDdl(true);
		jva.setShowSql(true);
		emfb.setJpaVendorAdapter(jva);
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		jpaProperties.put("hibernate.ejb.naming_strategy",
				"org.hibernate.cfg.ImprovedNamingStrategy");
		jpaProperties.put("hibernate.connection.charSet", "UTF-8");
		emfb.setJpaProperties(jpaProperties);

		// look ma, no persistence.xml !
		return emfb;
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		EntityManagerFactory entityManagerFactory = entityManagerFactory()
				.getObject();
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	// sets up infrastructure and scope
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor()
			throws Exception {
		JobRegistryBeanPostProcessor jrbpp = new JobRegistryBeanPostProcessor();
		jrbpp.setJobRegistry(mapJobRegistry());
		return jrbpp;
	}

	@Bean
	public JobRepositoryFactoryBean jobRepository() throws Exception {
		JobRepositoryFactoryBean jrfb = new JobRepositoryFactoryBean();
		jrfb.setDataSource(dataSource());
		jrfb.setTransactionManager(transactionManager());
		return jrfb;
	}

	@Bean
	public MapJobRegistry mapJobRegistry() throws Exception {
		return new MapJobRegistry();
	}

	@Bean
	public SimpleJobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
		simpleJobLauncher.setJobRepository((JobRepository) jobRepository()
				.getObject());
		return simpleJobLauncher;
	}

	private DatabasePopulator databasePopulator(DataSource dataSource) {
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
