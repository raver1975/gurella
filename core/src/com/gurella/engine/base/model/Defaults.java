package com.gurella.engine.base.model;

import com.badlogic.gdx.utils.ObjectMap;
import com.gurella.engine.utils.Reflection;

class Defaults {
	private static final ObjectMap<Class<?>, Object> defaults = new ObjectMap<Class<?>, Object>();

	static <T> T getDefault(Class<T> type) {
		if (defaults.containsKey(type)) {
			@SuppressWarnings("unchecked")
			T casted = (T) defaults.get(type);
			return casted;
		}

		T defaultValue = Reflection.newInstanceSilently(type);
		defaults.put(type, defaultValue);
		return defaultValue;
	}
}
