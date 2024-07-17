package com.combatlogger.util;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * Double-ended queue (deque) with a fixed maximum capacity.
 * When new elements are added to the queue that exceed its capacity, the oldest elements in the queue are automatically removed.
 *
 * @param <E>
 */
public class BoundedQueue<E> extends LinkedBlockingDeque<E>
{
	public BoundedQueue(int capacity)
	{
		super(capacity);
	}

	@Override
	public boolean add(E e)
	{
		// Remove the oldest element if the queue is full
		if (remainingCapacity() == 0)
		{
			poll();
		}
		return super.add(e);
	}

	@Override
	public boolean offer(E e)
	{
		// Remove the oldest element if the queue is full
		if (remainingCapacity() == 0)
		{
			poll();
		}
		return super.offer(e);
	}
}
