package com.gurella.engine.base.metamodel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.gurella.engine.base.metamodel.DefaultArrayModels.BooleanArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.ByteArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.CharArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.DoubleArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.FloatArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.IntArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.LongArrayModel;
import com.gurella.engine.base.metamodel.DefaultArrayModels.ShortArrayModel;
import com.gurella.engine.base.metamodel.DefaultModels.BigDecimalModel;
import com.gurella.engine.base.metamodel.DefaultModels.BigIntegerModel;
import com.gurella.engine.base.metamodel.DefaultModels.BooleanModel;
import com.gurella.engine.base.metamodel.DefaultModels.BooleanPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.ByteModel;
import com.gurella.engine.base.metamodel.DefaultModels.BytePrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.CharModel;
import com.gurella.engine.base.metamodel.DefaultModels.CharPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.ClassModel;
import com.gurella.engine.base.metamodel.DefaultModels.DateModel;
import com.gurella.engine.base.metamodel.DefaultModels.DoubleModel;
import com.gurella.engine.base.metamodel.DefaultModels.DoublePrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.FloatModel;
import com.gurella.engine.base.metamodel.DefaultModels.FloatPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.IntegerModel;
import com.gurella.engine.base.metamodel.DefaultModels.IntegerPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.LongModel;
import com.gurella.engine.base.metamodel.DefaultModels.LongPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.ShortModel;
import com.gurella.engine.base.metamodel.DefaultModels.ShortPrimitiveModel;
import com.gurella.engine.base.metamodel.DefaultModels.StringModel;
import com.gurella.engine.base.metamodel.DefaultModels.VoidModel;
import com.gurella.engine.utils.ImmutableArray;
import com.gurella.engine.utils.ReflectionUtils;

public class Models {
	private static final ObjectMap<Class<?>, Metamodel<?>> resolvedModels = new ObjectMap<Class<?>, Metamodel<?>>();
	private static final Array<ModelFactory> modelFactories = new Array<ModelFactory>();

	static {
		resolvedModels.put(int.class, IntegerPrimitiveModel.instance);
		resolvedModels.put(long.class, LongPrimitiveModel.instance);
		resolvedModels.put(short.class, ShortPrimitiveModel.instance);
		resolvedModels.put(byte.class, BytePrimitiveModel.instance);
		resolvedModels.put(char.class, CharPrimitiveModel.instance);
		resolvedModels.put(boolean.class, BooleanPrimitiveModel.instance);
		resolvedModels.put(double.class, DoublePrimitiveModel.instance);
		resolvedModels.put(float.class, FloatPrimitiveModel.instance);
		resolvedModels.put(void.class, VoidModel.instance);
		resolvedModels.put(Void.class, VoidModel.instance);
		resolvedModels.put(Integer.class, IntegerModel.instance);
		resolvedModels.put(Long.class, LongModel.instance);
		resolvedModels.put(Short.class, ShortModel.instance);
		resolvedModels.put(Byte.class, ByteModel.instance);
		resolvedModels.put(Character.class, CharModel.instance);
		resolvedModels.put(Boolean.class, BooleanModel.instance);
		resolvedModels.put(Double.class, DoubleModel.instance);
		resolvedModels.put(Float.class, FloatModel.instance);
		resolvedModels.put(String.class, StringModel.instance);
		resolvedModels.put(BigInteger.class, BigIntegerModel.instance);
		resolvedModels.put(BigDecimal.class, BigDecimalModel.instance);
		resolvedModels.put(Class.class, ClassModel.instance);
		resolvedModels.put(Date.class, DateModel.instance);
		resolvedModels.put(Locale.class, LocaleModel.instance);
		resolvedModels.put(int[].class, IntArrayModel.instance);
		resolvedModels.put(long[].class, LongArrayModel.instance);
		resolvedModels.put(short[].class, ShortArrayModel.instance);
		resolvedModels.put(byte[].class, ByteArrayModel.instance);
		resolvedModels.put(char[].class, CharArrayModel.instance);
		resolvedModels.put(boolean[].class, BooleanArrayModel.instance);
		resolvedModels.put(double[].class, DoubleArrayModel.instance);
		resolvedModels.put(float[].class, FloatArrayModel.instance);

		modelFactories.add(ObjectArrayModelFactory.instance);
		modelFactories.add(EnumModelFactory.instance);
		modelFactories.add(GdxArrayModelFactory.instance);
		modelFactories.add(CollectionModelFactory.instance);
		modelFactories.add(MapModelFactory.instance);
	}

	private Models() {
	}

	public static <T> Metamodel<T> getModel(T object) {
		@SuppressWarnings("unchecked")
		Class<T> casted = (Class<T>) object.getClass();
		return getModel(casted);
	}

	public static <T> Metamodel<T> getModel(Class<T> type) {
		synchronized (resolvedModels) {
			@SuppressWarnings("unchecked")
			Metamodel<T> model = (Metamodel<T>) resolvedModels.get(type);
			if (model == null) {
				model = resolveModel(type);
				resolvedModels.put(type, model);
			}
			return model;
		}
	}

	private static <T> Metamodel<T> resolveModel(Class<T> type) {
		Metamodel<T> model = resolveModelByDescriptor(type);
		if (model != null) {
			return model;
		}

		model = resolveCustomModel(type);
		return model == null ? ReflectionModel.<T> getInstance(type) : model;
	}

	private static <T> Metamodel<T> resolveModelByDescriptor(Class<T> type) {
		ModelDescriptor descriptor = ReflectionUtils.getDeclaredAnnotation(type, ModelDescriptor.class);
		if (descriptor == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Class<Metamodel<T>> modelType = (Class<Metamodel<T>>) descriptor.metamodel();
		if (modelType == null) {
			return null;
		}

		if (ReflectionModel.class.equals(modelType)) {
			return ReflectionModel.<T> getInstance(type);
		} else {
			Metamodel<T> model = instantiateModelByFactoryMethod(modelType);
			return model == null ? ReflectionUtils.newInstance(modelType) : model;
		}
	}

	private static <T> Metamodel<T> instantiateModelByFactoryMethod(Class<Metamodel<T>> modelType) {
		// TODO should be annotation based
		Method factoryMethod = ReflectionUtils.getDeclaredMethodSilently(modelType, "getInstance");
		if (isValidFactoryMethod(modelType, factoryMethod)) {
			try {
				@SuppressWarnings("unchecked")
				Metamodel<T> casted = (Metamodel<T>) factoryMethod.invoke(null);
				return casted;
			} catch (ReflectionException e) {
				return null;
			}
		}

		// TODO should be annotation based
		factoryMethod = ReflectionUtils.getDeclaredMethodSilently(modelType, "getInstance", Class.class);
		if (isValidFactoryMethod(modelType, factoryMethod)) {
			try {
				@SuppressWarnings("unchecked")
				Metamodel<T> casted = (Metamodel<T>) factoryMethod.invoke(modelType);
				return casted;
			} catch (ReflectionException e) {
				return null;
			}
		}

		return null;
	}

	private static <T> boolean isValidFactoryMethod(Class<Metamodel<T>> modelClass, Method factoryMethod) {
		return factoryMethod != null && factoryMethod.isPublic() && factoryMethod.getReturnType() == modelClass
				&& factoryMethod.isStatic();
	}

	private static <T> Metamodel<T> resolveCustomModel(Class<T> type) {
		for (int i = 0; i < modelFactories.size; i++) {
			Metamodel<T> model = modelFactories.get(i).create(type);
			if (model != null) {
				return model;
			}
		}

		return null;
	}

	public static boolean isEqual(Object first, Object second) {
		if (first == second) {
			return true;
		} else if (first == null || second == null) {
			return false;
		}

		Class<?> firstType = first.getClass();
		Class<?> secondType = second.getClass();
		if (firstType != secondType) {
			return false;
		} else if (firstType.isArray()) {
			if (first instanceof long[]) {
				return Arrays.equals((long[]) first, (long[]) second);
			} else if (first instanceof int[]) {
				return Arrays.equals((int[]) first, (int[]) second);
			} else if (first instanceof short[]) {
				return Arrays.equals((short[]) first, (short[]) second);
			} else if (first instanceof char[]) {
				return Arrays.equals((char[]) first, (char[]) second);
			} else if (first instanceof byte[]) {
				return Arrays.equals((byte[]) first, (byte[]) second);
			} else if (first instanceof double[]) {
				return Arrays.equals((double[]) first, (double[]) second);
			} else if (first instanceof float[]) {
				return Arrays.equals((float[]) first, (float[]) second);
			} else if (first instanceof boolean[]) {
				return Arrays.equals((boolean[]) first, (boolean[]) second);
			} else {
				Object[] firstArray = (Object[]) first;
				Object[] secondArray = (Object[]) second;
				if (firstArray.length != secondArray.length) {
					return false;
				}

				for (int i = 0; i < firstArray.length; ++i) {
					if (!isEqual(firstArray[i], secondArray[i])) {
						return false;
					}
				}

				return true;
			}
		} else {
			Metamodel<?> model = Models.getModel(first);
			ImmutableArray<Property<?>> properties = model.getProperties();
			if (properties.size() > 0) {
				for (int i = 0; i < properties.size(); i++) {
					Property<?> property = properties.get(i);
					if (!isEqual(property.getValue(first), property.getValue(second))) {
						return false;
					}
				}
			} else {
				return first.equals(second);
			}
		}

		return true;
	}
}