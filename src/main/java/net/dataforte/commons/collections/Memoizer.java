package net.dataforte.commons.collections;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Memoizer<K, V> implements Computable<K, V> {
    private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<K, Future<V>>();
    private final Computable<K, V> computable;
 
    public Memoizer(final Computable<K, V> computable) {
        this.computable = computable;
    }
 
    public V compute(final K argument) throws ExecutionException, InterruptedException {
        while (true) {
            Future<V> future = cache.get(argument);
            if (future == null) {
                final Callable<V> callable = new Callable<V>() {
                    public V call() throws ExecutionException, InterruptedException {
                        return computable.compute(argument);
                    }
                };
                final FutureTask<V> futureTask = new FutureTask<V>(callable);
                future = cache.putIfAbsent(argument, futureTask);
                if (future == null) {
                    future = futureTask;
                    futureTask.run();
                }
            }
            try {
                return future.get();
            } catch (final CancellationException e) {
                cache.remove(argument, future);
            }
        }
    }
}