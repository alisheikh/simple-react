package com.aol.simple.react.stream.traits;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.QueueFactory;

public interface EagerToQueue<U> extends ToQueue<U> {

	abstract QueueFactory<U> getQueueFactory();

	abstract <T, R> SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn);
	

	abstract <R> SimpleReactStream<R> thenSync(final Function<U, R> fn);
	
	

	
	/**
	 * Convert the current Stream to a SimpleReact Queue
	 * 
	 * @return Queue populated asynchrnously by this Stream
	 */
	default Queue<U> toQueue() {
		Queue<U> queue = this.getQueueFactory().build();

		 thenSync(it -> queue.offer(it)).allOf(it ->queue.close());

		return queue;
	}
	/* 
	 * Convert the current Stream to a simple-react Queue.
	 * The supplied function can be used to determine properties of the Queue to be used
	 * 
	 *  @param fn Function to be applied to default Queue. Returned Queue will be used to conver this Stream to a Queue
	 *	@return This stream converted to a Queue
	 * @see com.aol.simple.react.stream.traits.ToQueue#toQueue(java.util.function.Function)
	 */
	default Queue<U> toQueue(Function<Queue,Queue> modifier){
		  Queue<U> queue = modifier.apply(this.getQueueFactory().build());
		  thenSync(it -> queue.offer(it)).allOf(it ->queue.close());

			return queue;
	}
	/* 
	 * Populate provided queues with the sharded data from this Stream.
	 * 
	 *	@param shards Map of key to Queue shards
	 *	@param sharder Sharding function, element to key converter
	 * @see com.aol.simple.react.stream.traits.ToQueue#toQueue(java.util.Map, java.util.function.Function)
	 */
	default <K> void toQueue(Map<K, Queue<U>> shards, Function<U, K> sharder) {

		thenSync(
				it -> shards.get(sharder.apply(it)).offer(it)).allOf(
				data -> {
					shards.values().forEach(it -> it.close());
					return true;
				});

	}
}
