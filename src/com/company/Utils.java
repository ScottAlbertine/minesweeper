package com.company;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"unchecked", "JavaDoc"})
public enum Utils {
	;

	public static final int N_THREADS = Runtime.getRuntime().availableProcessors();
	public static final ThreadPoolExecutor MAIN_POOL = new ThreadPoolExecutor(N_THREADS, N_THREADS, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

	/**
	 * Picks one random item from an array.
	 *
	 * @param array duh
	 * @param <T>   duh
	 * @return duh
	 */
	public static <T> T pickOne(T... array) {
		if ((array == null) || (array.length == 0)) {
			return null;
		}
		return array[ThreadLocalRandom.current().nextInt(array.length)];
	}
}
