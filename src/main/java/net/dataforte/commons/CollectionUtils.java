package net.dataforte.commons;

import java.util.Collection;
import java.util.Iterator;

public class CollectionUtils {
	
	/**
	 * Compares two collections returning the number of elements they have in common
	 * 
	 * @param <T>
	 * @param one
	 * @param two
	 * @return
	 */
	public static <T> int collectionCompare(Collection<T> one, Collection<T> two) {
		int count = 0;

		Iterator<T> e = one.iterator();
		while (e.hasNext()) {
			if (two.contains(e.next()))
				++count;
		}

		return count;
	}
}
