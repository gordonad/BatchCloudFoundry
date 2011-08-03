package com.gordondickens.bcf.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;

/**
 * Listener that logs entry operations to the configured logger.
 */
public class CacheLogger extends CacheListenerAdapter<Object, Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(CacheLogger.class);

	@Override
	public void afterCreate(EntryEvent<Object, Object> event) {
		logger.info("Added " + messageLog(event) + " to the cache");
	}

	@Override
	public void afterDestroy(EntryEvent<Object, Object> event) {
		logger.info("Removed " + messageLog(event) + " from the cache");
	}

	@Override
	public void afterUpdate(EntryEvent<Object, Object> event) {
		logger.info("Updated " + messageLog(event) + " in the cache");
	}

	private String messageLog(EntryEvent<Object, Object> event) {
		Object key = event.getKey();
		Object value = event.getNewValue();

		if (event.getOperation().isUpdate()) {
			return "[" + key + "] from [" + event.getOldValue() + "] to ["
					+ event.getNewValue() + "]";
		}
		return "[" + key + "=" + value + "]";
	}
}
