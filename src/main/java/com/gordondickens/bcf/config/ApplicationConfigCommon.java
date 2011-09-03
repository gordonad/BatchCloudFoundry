package com.gordondickens.bcf.config;

import java.io.Serializable;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.gordondickens.bcf.repository.ProductRepository;
import com.gordondickens.bcf.repository.ProductTrxRepository;
import com.gordondickens.bcf.web.ProductController;
import com.gordondickens.bcf.web.ProductTrxController;

@Configuration
@EnableTransactionManagement
@ComponentScan(excludeFilters = { @ComponentScan.Filter(Configuration.class) })
@Import(BatchInfrastructureConfig.class)
public abstract class ApplicationConfigCommon {

	@SuppressWarnings("rawtypes")
	JpaEntityInformation entityMetadata;

	@Value("${database.name}")
	protected String databaseName;

	@Value("${database.host}")
	protected String host;

	@Value("${database.port}")
	protected String port;

	@Value("${database.url}")
	protected String url;

	@Value("${database.username}")
	protected String user;

	@Value("${database.password}")
	protected String password;

	@Value("${database.driverClassName}")
	protected String driverClassName;
	// protected Class<? extends Driver> driverClassName;

	@Value("${database.dialect}")
	protected Class<? extends Dialect> dialect;

	@Value("${database.vendor}")
	protected String databaseVendor;

	@Value("${hibernate.hbm2ddl.auto:update}")
	protected String hbm2ddl;

	@Value("${hibernate.ejb.naming_strategy}")
	protected String namingStrategy;

	@Value("${hibernate.connection.charSet:UTF-8}")
	protected String hibernateCharSet;

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
	public LocalContainerEntityManagerFactoryBean containerEntityManagerFactory()
			throws Exception {

		LocalContainerEntityManagerFactoryBean containerEMFB = new LocalContainerEntityManagerFactoryBean();
		containerEMFB.setDataSource(dataSource());

		HibernateJpaVendorAdapter jva = new HibernateJpaVendorAdapter();
		jva.setDatabasePlatform(databaseVendor);
		jva.setGenerateDdl(true);
		jva.setShowSql(true);
		containerEMFB.setJpaVendorAdapter(jva);
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.hbm2ddl.auto", hbm2ddl);
		jpaProperties.put("hibernate.ejb.naming_strategy", namingStrategy);
		jpaProperties.put("hibernate.connection.charSet", hibernateCharSet);
		containerEMFB.setJpaProperties(jpaProperties);
		containerEMFB
				.setPackagesToScan(new String[] { "com.gordondickens.bcf.entity" });
		// ProductTrx.class.getPackage().getName()
		// look ma, no persistence.xml !
		return containerEMFB;
	}

	@Bean
	public JpaRepositoryFactory jpaRepository() throws Exception {
		JpaRepositoryFactory repository = new JpaRepositoryFactory(
				entityManager()) {
			@Override
			@SuppressWarnings("unchecked")
			public <T, ID extends Serializable> JpaEntityInformation<T, ID> getEntityInformation(
					Class<T> domainClass) {

				return entityMetadata;
			};
		};
		return repository;
	}

	@Bean
	public EntityManager entityManager() throws Exception {
		return containerEntityManagerFactory().getNativeEntityManagerFactory()
				.createEntityManager();
	}

	@Bean
	public ProductRepository productRepository() throws Exception {
		return jpaRepository().getRepository(ProductRepository.class);
	}

	@Bean
	public ProductTrxRepository productTrxRepository() throws Exception {
		return jpaRepository().getRepository(ProductTrxRepository.class);
	}

	@Bean
	public PlatformTransactionManager transactionManager() throws Exception {
		EntityManagerFactory entityManagerFactory = containerEntityManagerFactory()
				.getObject();
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public ProductController productController() {
		return new ProductController();
	}

	@Bean
	public ProductTrxController productTrxController() {
		return new ProductTrxController();
	}

	@SuppressWarnings("rawtypes")
	public JpaEntityInformation getEntityMetadata() {
		return entityMetadata;
	}

	protected abstract DatabasePopulator databasePopulator(DataSource dataSource);
}
