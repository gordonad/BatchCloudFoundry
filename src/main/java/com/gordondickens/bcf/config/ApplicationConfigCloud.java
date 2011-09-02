package com.gordondickens.bcf.config;

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.cloudfoundry.runtime.env.ApplicationInstanceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.CloudEnvironmentPropertiesFactoryBean;
import org.cloudfoundry.runtime.service.relational.MysqlServiceCreator;
import org.hibernate.dialect.Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@PropertySource("classpath:META-INF/spring/database.properties")
@ComponentScan(excludeFilters = { @ComponentScan.Filter(Configuration.class) })
@Profile(Env.CLOUD)
public class ApplicationConfigCloud {
	private static final Logger logger = LoggerFactory
			.getLogger(ApplicationConfigCloud.class);

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

	@Value("${database.name}")
	private String databaseName;

	@Value("${database.host}")
	private String host;

	@Value("${database.port}")
	private String port;

	@Bean
	public Properties serviceProperties() {
		Properties props = new Properties();
		props.put("url", url);
		props.put("name", databaseName);
		props.put("hostname", host);
		props.put("port", port);
		props.put("username", user);
		props.put("password", password);
		props.put("driver", driverClassName);
		return props;
	}

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
	public Properties cloudEnvironment() {
		CloudEnvironmentPropertiesFactoryBean cloudProps = new CloudEnvironmentPropertiesFactoryBean();
		CloudEnvironment cloudEnvironment = new CloudEnvironment();
		cloudProps.setCloudEnv(cloudEnvironment);
		Properties props = null;
		try {
			ApplicationInstanceInfo appInfo = cloudEnvironment
					.getInstanceInfo();
			String[] appinfo = { appInfo.getHost(), appInfo.getName(),
					(new Integer(appInfo.getInstanceIndex())).toString(),
					(new Integer(appInfo.getPort())).toString(),
					appInfo.getUris().toString() };
			logger.debug(
					"Cloud Environment Info: \n\tHost '{}', \n\tName '{}', \n\tInstance '{}', \n\tPort '{}', \n\tURIs '{}'",
					appinfo);
			cloudProps.setCloudEnv(cloudEnvironment);
			MysqlServiceCreator mysql = new MysqlServiceCreator(
					cloudEnvironment);

			logger.debug("MySQL driver class '{}', Validation query '{}' ",
					mysql.getDriverClassName(), mysql.getValidationQuery());

			props = cloudProps.getObject();
		} catch (Exception e) {
			logger.error("Error getting Properties from Cloud Environment", e);
		}
		logger.debug("Returning Cloud Environment Properties '{}'", props);
		return props;
	}

	/*
	 * <cloud:service-properties id="serviceProperties" />
	 * 
	 * <!-- TODO: Verify if database.properties is necessary for Cloud --> <!--
	 * <context:property-placeholder
	 * location="classpath:META-INF/spring/database.properties" /> -->
	 * 
	 * <cloud:data-source id="dataSource" /> <!-- Sample reference to Cloud
	 * Service Properties --> <!-- <bean id="dataSource"
	 * class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
	 * <property name="driverClassName" value="com.mysql.jdbc.Driver" />
	 * <property name="url" value="jdbc:mysql://127.0.0.1:3306/test" />
	 * <property name="username" value="spring" /> <property name="password"
	 * value="spring" /> </bean>
	 * 
	 * <mongo:db-factory id="mongoDbFactory"
	 * dbname="#{serviceProperties['db.name']}"
	 * host="#{serviceProperties['db.hostname']}"
	 * port="#{serviceProperties['db.port']}"
	 * username="#{serviceProperties['db.username']}"
	 * password="#{serviceProperties['db.password']}"/> -->
	 * 
	 * <jpa:repositories base-package="com.gordondickens.bcf.repository"
	 * query-lookup-strategy="create-if-not-found"
	 * repository-impl-postfix="Impl"
	 * entity-manager-factory-ref="entityManagerFactory"
	 * transaction-manager-ref="transactionManager" />
	 */

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory()
			throws Exception {
		LocalContainerEntityManagerFactoryBean emfb = new LocalContainerEntityManagerFactoryBean();
		emfb.setDataSource(dataSource());

		HibernateJpaVendorAdapter jva = new HibernateJpaVendorAdapter();
		// jva.setDatabasePlatform(databaseVendor);
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

	private DatabasePopulator databasePopulator(DataSource dataSource) {
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setContinueOnError(true);
		populator.addScript(new ClassPathResource("batch-mysql-ddl.sql"));
		try {
			populator.populate(dataSource.getConnection());
		} catch (SQLException e) {
			logger.error("Exception Populating Database", e);
		}
		return populator;
	}

}
