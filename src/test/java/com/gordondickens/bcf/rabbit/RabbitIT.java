package com.gordondickens.bcf.rabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gordondickens.bcf.config.BatchInfrastructureConfig;
import com.gordondickens.bcf.services.Env;

@ContextConfiguration
//@ContextConfiguration(locations = { "RabbitIT-context.xml" }, classes = { BatchInfrastructureConfig.class })
// , loader = AnnotationConfigContextLoader.class
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = Env.LOCAL)
public class RabbitIT {
	private static final String NULL_VAL = "NULL - d'oh";

	private static final Logger logger = LoggerFactory
			.getLogger(RabbitIT.class);

	@Autowired
	AmqpTemplate rabbitTemplate;

	@Autowired
	RabbitGateway rabbitGateway;

	@Autowired
	ApplicationContext applicationContext;

	@Value("${bcf.jobRequestQueue}")
	String requestQueue;

	@Value("${bcf.jobResponseQueue}")
	String responseQueue;

	@Before
	public void beforeClass() {
		logger.debug("Request Queue '{}' - Response Queue '{}'", requestQueue,
				responseQueue);
		assertTrue("Request Queue MUST have a value", requestQueue != null);
		assertTrue("Response Queue MUST have a value", responseQueue != null);
	}

	@Test
	public void sendTestMessage() {
		try {
			String sendMsg = "Hello Wabbit";
			logger.debug("Sending message through Gateway {}", sendMsg);
			rabbitGateway.send(sendMsg);

			Object recvMsg = rabbitTemplate.receiveAndConvert(requestQueue);
			Object recvMsg2 = rabbitTemplate.receiveAndConvert(responseQueue);
			log("Received From Request Queue", (recvMsg == null ? NULL_VAL : recvMsg));
			log("Received From Response Queue", (recvMsg2 == null ? NULL_VAL : recvMsg2));

			assertNotNull("Received Message MUST exist", recvMsg);
			log("Received Message", recvMsg);
			assertEquals("Send and Receive values MUST match", sendMsg, recvMsg);
		} catch (Exception e) {
			logger.error("Rabbit Exception {}", e.getMessage(), e);
			fail("Rabbit Exception");
		}
	}

	private void log(String message, Object object) {
        Object safeObject = object == null ? NULL_VAL
                : object.toString();
		logger.debug("{} {}", message, safeObject);
		logger.debug("[{} T({})]", safeObject.getClass().getName());
	}
}
