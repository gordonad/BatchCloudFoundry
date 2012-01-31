package com.gordondickens.bcf.config;

import com.gordondickens.bcf.repository.ProductRepository;
import com.gordondickens.bcf.repository.ProductTrxRepository;
import com.gordondickens.bcf.web.ProductController;
import com.gordondickens.bcf.web.ProductTrxController;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.Serializable;
import java.util.Properties;

@Configuration
public abstract class ApplicationConfigCommon {

    @Inject
    private Environment environment;

    @SuppressWarnings("rawtypes")
    JpaEntityInformation entityMetadata;

    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setPassword(getPassword());
        ds.setUrl(getUrl());
        ds.setUsername(getUser());
        ds.setDriverClassName(getDriverClassName());
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
    public EntityManagerFactory containerEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
//        jpaVendorAdapter.setDatabase(Database.HSQL);
        jpaVendorAdapter.setDatabasePlatform(getDatabaseVendor());
        jpaVendorAdapter.setGenerateDdl(true);
        jpaVendorAdapter.setShowSql(true);

        entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        entityManagerFactoryBean.afterPropertiesSet();
        return entityManagerFactoryBean.getObject();
    }

    private Properties jpaProperties() {
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", getHbm2ddl());
//        jpaProperties.put("hibernate.ejb.naming_strategy", namingStrategy);
//        jpaProperties.put("hibernate.dialect", dialect);
        jpaProperties.put("hibernate.connection.charSet", getHibernateCharSet());
        return jpaProperties;
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
            }

            ;
        };
        return repository;
    }

    @Bean
    public EntityManager entityManager() throws Exception {
        return containerEntityManagerFactory().createEntityManager();
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
        EntityManagerFactory entityManagerFactory = containerEntityManagerFactory();
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

    public String getDatabaseName() {
        return environment.getProperty("database.name");
    }

    public String getHost() {
        return environment.getProperty("database.host");
    }

    public String getPort() {
        return environment.getProperty("database.port");
    }

    public String getUrl() {
        return environment.getProperty("database.url");
    }

    public String getUser() {
        return environment.getProperty("database.username");
    }

    public String getPassword() {
        return environment.getProperty("database.password");
    }

    public String getDriverClassName() {
        return environment.getProperty("database.driverClassName");
    }

    public String getDialect() {
        return environment.getProperty("database.dialect");
    }

    public String getDatabaseVendor() {
        return environment.getProperty("database.vendor");
    }

    public String getHbm2ddl() {
        return environment.getProperty("database.hbm2ddl", "update");
    }

    public String getHibernateCharSet() {
        return environment.getProperty("database.hibernateCharSet", "UTF-8");
    }

    @Override
    public String toString() {
        return "ApplicationConfigCommon{" +
                "entityMetadata=" + getEntityMetadata() +
                ", databaseName='" + getDatabaseName() + '\'' +
                ", host='" + getHost() + '\'' +
                ", port='" + getPort() + '\'' +
                ", url='" + getUrl() + '\'' +
                ", user='" + getUser() +
                ", password='" + getPassword() + '\'' +
                ", driverClassName='" + getDriverClassName() + '\'' +
                ", dialect='" + getDialect() + '\'' +
                ", databaseVendor='" + getDatabaseVendor() + '\'' +
                ", hbm2ddl='" + getHbm2ddl() + '\'' +
                ", hibernateCharSet='" + getHibernateCharSet() + '\'' +
                '}';
    }
}
