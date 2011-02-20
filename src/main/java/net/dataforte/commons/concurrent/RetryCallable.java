package net.dataforte.commons.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import net.dataforte.commons.slf4j.LoggerFactory;

import org.slf4j.Logger;

/**
 * This class implements a {@link Callable} which can be retried a specified
 * number of times before failing
 * 
 * @author Tristan Tarrant
 *
 * @param <T>
 */
public class RetryCallable<T> implements Callable<T> {

	private final Callable<T> callable;

	private final int maxRetries;

	private final Logger log = LoggerFactory.make();

	public RetryCallable(Callable<T> callable, int retryCount) {
		this.callable = callable;
		this.maxRetries = retryCount;
	}

	public T call() throws Exception {
		int retry = 0;
		for(;;) {
			try {
				return callable.call();
			} catch (final InterruptedException e) {
				throw e;
			} catch (final CancellationException e) {
				throw e;
			} catch (final Exception e) {
				
				++retry;
				if (retry == maxRetries)
					throw e;
				log.warn(callable.toString()+" threw exception "+e.getMessage()+" on try "+retry+"/"+maxRetries);
			}
		}

	}

}
