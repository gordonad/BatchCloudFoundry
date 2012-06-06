package com.gordondickens.bcf.services;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Check the environment and switch the configuration based on an environment determinant.
 *
 * For Heroku, the "Procfile" at the root project level requires setting the "-Dspring.profiles.active=heroku"
 */
public class CloudApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext> {
	private static final Logger logger = LoggerFactory
			.getLogger(CloudApplicationContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		CloudEnvironment env = new CloudEnvironment();
		if (env.getInstanceInfo() != null) {
			logger.info("Application running in cloud with API URL '{}'",
					env.getCloudApiUri());
			// System.out.println("cloud API: " + env.getCloudApiUri());
			applicationContext.getEnvironment().setActiveProfiles(Env.CLOUDFOUNDRY);
        } else if (applicationContext.getEnvironment().getActiveProfiles().toString().toLowerCase().contains(Env.HEROKU)) {
            logger.info("Application running in cloud with API URL '{}'",
                    env.getCloudApiUri());
            // System.out.println("cloud API: " + env.getCloudApiUri());
            applicationContext.getEnvironment().setActiveProfiles(Env.HEROKU);
        } else {
			logger.info("Application running local");
			applicationContext.getEnvironment().setActiveProfiles(Env.LOCAL);
        }
        applicationContext.refresh();
    }
}
