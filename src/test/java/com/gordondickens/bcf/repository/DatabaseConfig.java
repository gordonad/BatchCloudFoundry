package com.gordondickens.bcf.repository;

import com.gordondickens.bcf.config.ApplicationConfigLocal;
import com.gordondickens.bcf.services.Env;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * User: gordondickens
 * Date: 1/31/12
 * Time: 6:49 PM
 */
@Configuration
@ActiveProfiles(profiles = Env.LOCAL)
@Import(ApplicationConfigLocal.class)
public class DatabaseConfig {


}