package com.gordondickens.bcf.rabbit;

/**
 * @author gDickens
 * 
 *         String expected automatically marshalled into payload
 * 
 */
public interface RabbitGateway {
	public void send(String payload);
}
