package com.gordondickens.bcf.config;


import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.repository.ProductRepository;
import com.gordondickens.bcf.repository.ProductTrxRepository;
import com.gordondickens.bcf.services.AppEnvironment;
import com.gordondickens.bcf.web.ProductController;
import com.gordondickens.bcf.web.ProductTrxController;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.ArrayList;

@Configuration
@EnableJpaRepositories(basePackages = {"com.gordondickens.bcf.repository"})
@Import({BatchInfrastructureConfig.class, ProductJobConfig.class})
public abstract class ApplicationConfigCommon {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfigCommon.class);

    @Inject
    private AbstractEnvironment environment;

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
    public PlatformTransactionManager transactionManager() throws Exception {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(containerEntityManagerFactory().getObject());

        return jpaTransactionManager;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean containerEntityManagerFactory() throws Exception {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(dataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan(Product.class.getPackage().getName());

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.valueOf(getDatabaseVendor()));
        jpaVendorAdapter.setShowSql(true);
        jpaVendorAdapter.setGenerateDdl(true);

        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        // No persistence.xml - thanks to packagesToScan
        return localContainerEntityManagerFactoryBean;
    }


    @Bean
    public JpaRepositoryFactory jpaRepository() throws Exception {
        return new JpaRepositoryFactory(entityManager());
    }

    @Bean
    public EntityManager entityManager() throws Exception {
        if (containerEntityManagerFactory() == null) logger.debug("1. CEMF IS NULL");

        EntityManager entityManager = null;

        EntityManagerFactory entityManagerFactory = containerEntityManagerFactory().getObject();
        if (entityManagerFactory == null) {
            logger.error("2. EMF IS NULL");
            return null;
        } else {
            entityManager = entityManagerFactory.createEntityManager();
            if (entityManager == null) {
                logger.error("3. EM IS NULL");
                return null;
            }
        }

        return entityManager;
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
    public ProductController productController() {
        return new ProductController();
    }

    @Bean
    public ProductTrxController productTrxController() {
        return new ProductTrxController();
    }

    @Bean
    public ArrayList<String> fileTypes() {
        ArrayList<String> fileTypeMap = new ArrayList<String>();
        fileTypeMap.add("CSV");
        fileTypeMap.add("TXT");
        fileTypeMap.add("XLS");
        return fileTypeMap;
    }

    @Bean
    public AppEnvironment appEnvironment() {
        AppEnvironment appEnvironment = new AppEnvironment();
        appEnvironment.setEnvironment(environment);
        appEnvironment.setSystemEnvironment(environment.getSystemEnvironment());
        appEnvironment.setSystemProperties(environment.getSystemProperties());
        appEnvironment.afterInstantiation();
        return appEnvironment;
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
