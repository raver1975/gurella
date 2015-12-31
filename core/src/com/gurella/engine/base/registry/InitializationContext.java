package com.gurella.engine.base.registry;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool.Poolable;

public class InitializationContext<T> implements Poolable {
	public ObjectRegistry registry;
	public T initializingObject;
	public T template;
	public Json json;
	public JsonValue serializedValue;

	private IntMap<ManagedObject> instances = new IntMap<ManagedObject>();

	public <MO extends ManagedObject> MO findManagedObject(int objectId) {
		@SuppressWarnings("unchecked")
		MO instance = (MO) instances.get(objectId);
		if (instance == null) {
			instance = registry.getObject(objectId);
			if (instance == null) {
				MO template = registry.getTemplate(objectId);
				if (template == null) {
					throw new GdxRuntimeException("Can't find object by id: " + objectId);
				}
				instance = Objects.duplicate(template);
			}
			instances.put(objectId, instance);
		}
		return instance;
	}

	public <MO extends ManagedObject> MO getManagedObject(MO object) {
		int objectId = object.id;
		@SuppressWarnings("unchecked")
		MO instance = (MO) instances.get(objectId);
		if (instance == null) {
			instance = Objects.duplicate(object);
			instances.put(objectId, instance);
		}
		return instance;
	}

	@Override
	public void reset() {
		registry = null;
		initializingObject = null;
		serializedValue = null;
		template = null;
	}
}
