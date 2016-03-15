package com.gurella.engine.pool;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import com.gurella.engine.utils.Values;

public class ObjectArrayPool<T> {
	public final Class<T> type;
	public final int max;
	private final Array<T[]> freeObjects;

	public ObjectArrayPool(Class<T> type) {
		this(type, 64, Integer.MAX_VALUE);
	}

	public ObjectArrayPool(Class<T> type, int initialCapacity) {
		this(type, initialCapacity, Integer.MAX_VALUE);
	}

	public ObjectArrayPool(Class<T> type, int initialCapacity, int max) {
		this.type = type;
		this.max = max;
		freeObjects = new Array<T[]>(initialCapacity);
	}

	public T[] obtain(int length, int maxLength) {
		T[] array = find(length, maxLength);
		return array == null ? Values.<T[]> cast(ArrayReflection.newInstance(type, length)) : array;
	}

	private T[] find(int length, int maxLength) {
		int low = 0;
		int high = freeObjects.size - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			T[] midVal = freeObjects.get(mid);

			if (midVal.length < length) {
				low = mid + 1;
			} else if (midVal.length > length) {
				high = mid - 1;
				if (high >= 0) {
					T[] temp = freeObjects.get(high);
					if (temp.length < length && midVal.length <= maxLength) {
						return freeObjects.removeIndex(high);
					}
				}
			} else {
				return freeObjects.removeIndex(mid);
			}
		}

		return null;
	}

	public void free(T[] object) {
		if (object == null) {
			throw new IllegalArgumentException("object cannot be null.");
		}

		if (freeObjects.size < max) {
			freeObjects.add(object);
			freeObjects.sort(ArrayComparable.<T> getInstance());
		}
	}

	public void freeAll(Array<T[]> objects) {
		if (objects == null) {
			throw new IllegalArgumentException("object cannot be null.");
		}

		if (freeObjects.size >= max) {
			return;
		}

		for (int i = 0; i < objects.size && freeObjects.size >= max; i++) {
			T[] object = objects.get(i);
			if (object != null) {
				freeObjects.add(object);
			}
		}

		freeObjects.sort(ArrayComparable.<T> getInstance());
	}

	public void clear() {
		freeObjects.clear();
	}

	public int getFree() {
		return freeObjects.size;
	}

	private static class ArrayComparable<T> implements Comparator<T[]> {
		private static final ArrayComparable<Object> instance = new ArrayComparable<Object>();

		public static <T> ArrayComparable<T> getInstance() {
			return Values.cast(instance);
		}

		@Override
		public int compare(T[] o1, T[] o2) {
			return Values.compare(o1.length, o2.length);
		}
	}
}
