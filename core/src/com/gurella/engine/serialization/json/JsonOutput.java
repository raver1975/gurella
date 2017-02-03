package com.gurella.engine.serialization.json;

import static com.gurella.engine.serialization.json.JsonSerialization.arrayType;
import static com.gurella.engine.serialization.json.JsonSerialization.arrayTypeTag;
import static com.gurella.engine.serialization.json.JsonSerialization.assetReferenceIndexTag;
import static com.gurella.engine.serialization.json.JsonSerialization.assetReferenceType;
import static com.gurella.engine.serialization.json.JsonSerialization.isSimpleType;
import static com.gurella.engine.serialization.json.JsonSerialization.resolveOutputType;
import static com.gurella.engine.serialization.json.JsonSerialization.serializeType;
import static com.gurella.engine.serialization.json.JsonSerialization.typeTag;
import static com.gurella.engine.serialization.json.JsonSerialization.valueTag;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.SerializationException;
import com.gurella.engine.asset.AssetService;
import com.gurella.engine.metatype.MetaType;
import com.gurella.engine.metatype.MetaTypes;
import com.gurella.engine.serialization.Output;
import com.gurella.engine.serialization.Reference;
import com.gurella.engine.serialization.Serializer;
import com.gurella.engine.utils.IdentityObjectIntMap;

public class JsonOutput implements Output, Serializer, Poolable {
	private String filePath;
	private JsonWriter writer;

	private int currentId;
	private IdentityObjectIntMap<Object> references = new IdentityObjectIntMap<Object>();
	private Array<ObjectInfo> objectsToSerialize = new Array<ObjectInfo>();
	private OrderedSet<String> externalDependencies = new OrderedSet<String>();

	public void init(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public <T> void serialize(Class<T> expectedType, T rootObject) {
		serialize(expectedType, rootObject, null);
	}

	@Override
	public <T> void serialize(Class<T> expectedType, T rootObject, Object template) {
		serialize(expectedType, rootObject, template, new StringWriter());
	}

	private <T> void serialize(Class<T> expectedType, T rootObject, Object template, Writer buffer) {
		writer = new JsonWriter(buffer);

		object();
		addReference(expectedType, template, rootObject);

		while (objectsToSerialize.size > 0) {
			ObjectInfo objectInfo = objectsToSerialize.removeIndex(0);
			name(Integer.toString(objectInfo.ordinal));
			serializeObject(objectInfo.expectedType, objectInfo.object, objectInfo.template);
			objectInfo.free();
		}

		int externalDependenciesSize = externalDependencies.size;
		if (externalDependenciesSize > 0) {
			name("d");
			array();
			for (String dependency : externalDependencies) {
				value(dependency);
			}
			pop();
		}

		pop();
		reset();
	}

	public <T> String serialize(FileHandle file, Class<T> expectedType, T rootObject) {
		return serialize(file, expectedType, rootObject, null);
	}

	public <T> String serialize(FileHandle file, Class<T> expectedType, T rootObject, Object template) {
		return serialize(file.path(), expectedType, rootObject, template);
	}

	public <T> String serialize(String filePath, Class<T> expectedType, T rootObject, Object template) {
		this.filePath = filePath;
		StringWriter buffer = new StringWriter();
		serialize(expectedType, rootObject, template, buffer);
		return buffer.toString();
	}

	private void writeReference(Class<?> expectedType, Object object, Object template) {
		int ordinal = references.get(object, -1);
		if (ordinal < 0) {
			writeInt(addReference(expectedType, template, object));
		} else {
			writeInt(ordinal);
		}
	}

	private int addReference(Class<?> expectedType, Object template, Object object) {
		references.put(object, currentId);
		objectsToSerialize.add(ObjectInfo.obtain(currentId, expectedType, template, object));
		return currentId++;
	}

	private void serializeObject(Class<?> expectedType, Object object, Object template) {
		if (object == null) {
			writeNull();
		} else if (object.getClass().isArray()) {
			array();
			Class<? extends Object> actualType = object.getClass();
			if (actualType != expectedType) {
				object();
				writeStringProperty(typeTag, arrayType);
				writeStringProperty(arrayTypeTag, serializeType(actualType));
				pop();
			}

			MetaType<Object> metaType = MetaTypes.getMetaType(object);
			metaType.serialize(object, template, this);
			pop();
		} else {
			object();
			Class<? extends Object> actualType = object.getClass();
			if (expectedType != actualType) {
				type(actualType);
			}
			MetaType<Object> metaType = MetaTypes.getMetaType(object);
			metaType.serialize(object, template, this);
			pop();
		}
	}

	@Override
	public void writeNull() {
		value(null);
	}

	@Override
	public void writeInt(int value) {
		value(Integer.valueOf(value));
	}

	@Override
	public void writeLong(long value) {
		value(Long.valueOf(value));
	}

	@Override
	public void writeShort(short value) {
		value(Short.valueOf(value));
	}

	@Override
	public void writeByte(byte value) {
		value(Byte.valueOf(value));
	}

	@Override
	public void writeChar(char value) {
		value(Character.valueOf(value));
	}

	@Override
	public void writeBoolean(boolean value) {
		value(Boolean.valueOf(value));
	}

	@Override
	public void writeDouble(double value) {
		value(Double.valueOf(value));
	}

	@Override
	public void writeFloat(float value) {
		value(Float.valueOf(value));
	}

	@Override
	public void writeInt(Integer value) {
		value(value);
	}

	@Override
	public void writeLong(Long value) {
		value(value);
	}

	@Override
	public void writeShort(Short value) {
		value(value);
	}

	@Override
	public void writeByte(Byte value) {
		value(value);
	}

	@Override
	public void writeChar(Character value) {
		value(value);
	}

	@Override
	public void writeBoolean(Boolean value) {
		value(value);
	}

	@Override
	public void writeDouble(Double value) {
		value(value);
	}

	@Override
	public void writeFloat(Float value) {
		value(value);
	}

	@Override
	public void writeString(String value) {
		value(value);
	}

	@Override
	public void writeObject(Class<?> expectedType, Object value, Object template) {
		writeObject(expectedType, false, value, template);
	}

	@Override
	public void writeObject(Class<?> expectedType, boolean flat, Object value, Object template) {
		if (value == null) {
			writeNull();
		} else if (expectedType != null && expectedType.isPrimitive()) {
			@SuppressWarnings("unchecked")
			MetaType<Object> metaType = (MetaType<Object>) MetaTypes.getMetaType(expectedType);
			metaType.serialize(value, null, this);
		} else if (isSimpleType(value)) {
			addReferenceDependency(value);
			MetaType<Object> metaType = MetaTypes.getMetaType(value);
			Class<?> actualType = value.getClass();
			if (equalType(expectedType, actualType)) {
				metaType.serialize(value, null, this);
			} else {
				object();
				type(actualType);
				name(valueTag);
				metaType.serialize(value, null, this);
				pop();
			}
		} else if (flat) {
			addReferenceDependency(value);
			serializeObject(expectedType, value, template);
		} else {
			String assetLocation = getAssetLocation(value);
			if (assetLocation == null) {
				addReferenceDependency(value);
				writeReference(expectedType, value, template);
			} else {
				writeAsset(value, assetLocation);
			}
		}
	}

	private void addReferenceDependency(Object value) {
		if (value instanceof Reference) {
			Reference reference = (Reference) value;
			externalDependencies.add(reference.getValueType().getName() + " " + reference.getFileName());
		}
	}

	private static boolean equalType(Class<?> expectedType, Class<?> actualType) {
		if (expectedType == actualType) {
			return true;
		} else if (expectedType == null || actualType == null) {
			return false;
		} else if (expectedType.isEnum() && actualType.getEnumConstants() == null) {
			return expectedType == actualType.getSuperclass();
		} else {
			return false;
		}
	}

	private String getAssetLocation(Object object) {
		if (object instanceof Reference) {
			return null;
		} else {
			String fileName = AssetService.getFileName(object);
			return fileName == null || fileName.equals(filePath) ? null : fileName;
		}
	}

	private void writeAsset(Object asset, String assetLocation) {
		object();
		writeStringProperty(typeTag, assetReferenceType);
		writeIntProperty(assetReferenceIndexTag, getAssetId(asset, assetLocation));
		pop();
	}

	private int getAssetId(Object asset, String assetLocation) {
		String dependency = serializeType(asset.getClass()) + " " + assetLocation;
		if (externalDependencies.add(dependency)) {
			externalDependencies.add(dependency);
			return externalDependencies.orderedItems().indexOf(dependency, false);
		} else {
			return externalDependencies.size - 1;
		}
	}

	@Override
	public void writeNullProperty(String name) {
		name(name);
		value(null);
	}

	@Override
	public void writeIntProperty(String name, int value) {
		name(name);
		value(Integer.valueOf(value));
	}

	@Override
	public void writeLongProperty(String name, long value) {
		name(name);
		value(Long.valueOf(value));
	}

	@Override
	public void writeShortProperty(String name, short value) {
		name(name);
		value(Short.valueOf(value));
	}

	@Override
	public void writeByteProperty(String name, byte value) {
		name(name);
		value(Byte.valueOf(value));
	}

	@Override
	public void writeCharProperty(String name, char value) {
		name(name);
		value(Character.valueOf(value));
	}

	@Override
	public void writeBooleanProperty(String name, boolean value) {
		name(name);
		value(Boolean.valueOf(value));
	}

	@Override
	public void writeDoubleProperty(String name, double value) {
		name(name);
		value(Double.valueOf(value));
	}

	@Override
	public void writeFloatProperty(String name, float value) {
		name(name);
		value(Float.valueOf(value));
	}

	@Override
	public void writeIntProperty(String name, Integer value) {
		name(name);
		value(value);
	}

	@Override
	public void writeLongProperty(String name, Long value) {
		name(name);
		value(value);
	}

	@Override
	public void writeShortProperty(String name, Short value) {
		name(name);
		value(value);
	}

	@Override
	public void writeByteProperty(String name, Byte value) {
		name(name);
		value(value);
	}

	@Override
	public void writeCharProperty(String name, Character value) {
		name(name);
		value(value);
	}

	@Override
	public void writeBooleanProperty(String name, Boolean value) {
		name(name);
		value(value);
	}

	@Override
	public void writeDoubleProperty(String name, Double value) {
		name(name);
		value(value);
	}

	@Override
	public void writeFloatProperty(String name, Float value) {
		name(name);
		value(value);
	}

	@Override
	public void writeStringProperty(String name, String value) {
		name(name);
		value(value);
	}

	@Override
	public void writeObjectProperty(String name, Class<?> expectedType, Object value, Object template) {
		name(name);
		writeObject(expectedType, false, value, template);
	}

	@Override
	public void writeObjectProperty(String name, Class<?> expectedType, boolean flat, Object value, Object template) {
		name(name);
		writeObject(expectedType, flat, value, template);
	}

	private void value(Object value) {
		try {
			writer.value(value);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void object() {
		try {
			writer.object();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void type(Class<?> type) {
		try {
			writer.set(typeTag, serializeType(resolveOutputType(type)));
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void pop() {
		try {
			writer.pop();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void array() {
		try {
			writer.array();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	private void name(String name) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	@Override
	public void reset() {
		filePath = null;
		writer = null;
		currentId = 0;
		references.clear();
		objectsToSerialize.clear();
		externalDependencies.clear();
	}
}
