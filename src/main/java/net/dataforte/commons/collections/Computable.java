package net.dataforte.commons.collections;

import java.util.concurrent.ExecutionException;

public interface Computable<K,V> {
	 V compute(K argument) throws InterruptedException, ExecutionException;
}
