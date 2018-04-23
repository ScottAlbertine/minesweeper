package com.company;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"unchecked", "JavaDoc"})
public enum Utils {
	;

	public static boolean[] bitfield(int n, int length){
		boolean[] bits = new boolean[length];
		for (int i = length - 1; i >= 0; i--) {
			bits[i] = (n & (1 << i)) != 0;
		}
		return bits;
	}

	public static boolean[] randomBitfield(int length) {
		boolean[] bits = new boolean[length];
		for (int i = 0; i < length; i++) {
			bits[i] = ThreadLocalRandom.current().nextBoolean();
		}
		return bits;
	}

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

	/**
	 * Picks one random item from a list.
	 *
	 * @param list duh
	 * @param <T>  duh
	 * @return duh
	 */
	public static <T> T pickOne(List<T> list) {
		if ((list == null) || list.isEmpty()) {
			return null;
		}
		return list.get(ThreadLocalRandom.current().nextInt(list.size()));
	}
}
