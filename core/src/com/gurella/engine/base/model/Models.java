package com.gurella.engine.base.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.gurella.engine.base.model.DefaultArrayModels.BooleanArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.ByteArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.CharArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.DoubleArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.FloatArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.IntArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.LongArrayModel;
import com.gurella.engine.base.model.DefaultArrayModels.ShortArrayModel;
import com.gurella.engine.base.model.DefaultModels.BigDecimalModel;
import com.gurella.engine.base.model.DefaultModels.BigIntegerModel;
import com.gurella.engine.base.model.DefaultModels.BooleanModel;
import com.gurella.engine.base.model.DefaultModels.BooleanPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.ByteModel;
import com.gurella.engine.base.model.DefaultModels.BytePrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.CharModel;
import com.gurella.engine.base.model.DefaultModels.CharPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.ClassModel;
import com.gurella.engine.base.model.DefaultModels.DateModel;
import com.gurella.engine.base.model.DefaultModels.DoubleModel;
import com.gurella.engine.base.model.DefaultModels.DoublePrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.FloatModel;
import com.gurella.engine.base.model.DefaultModels.FloatPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.IntegerModel;
import com.gurella.engine.base.model.DefaultModels.IntegerPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.LongModel;
import com.gurella.engine.base.model.DefaultModels.LongPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.ShortModel;
import com.gurella.engine.base.model.DefaultModels.ShortPrimitiveModel;
import com.gurella.engine.base.model.DefaultModels.StringModel;
import com.gurella.engine.base.model.DefaultModels.VoidModel;
import com.gurella.engine.utils.ReflectionUtils;

public class Models {
	private static final ObjectMap<Class<?>, Model<?>> resolvedModels = new ObjectMap<Class<?>, Model<?>>();
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

	public static <T> Model<T> getModel(T object) {
		@SuppressWarnings("unchecked")
		Class<T> casted = (Class<T>) object.getClass();
		return getModel(casted);
	}

	public static <T> Model<T> getModel(Class<T> type) {
		synchronized (resolvedModels) {
			@SuppressWarnings("unchecked")
			Model<T> model = (Model<T>) resolvedModels.get(type);
			if (model == null) {
				model = resolveModel(type);
				resolvedModels.put(type, model);
			}
			return model;
		}
	}

	private static <T> Model<T> resolveModel(Class<T> type) {
		Model<T> model = resolveModelByDescriptor(type);
		if (model != null) {
			return model;
		}

		model = resolveCustomModel(type);
		return model == null ? ReflectionModel.<T> getInstance(type) : model;
	}

	private static <T> Model<T> resolveModelByDescriptor(Class<T> type) {
		ModelDescriptor descriptor = ReflectionUtils.getDeclaredAnnotation(type, ModelDescriptor.class);
		if (descriptor == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		Class<Model<T>> modelType = (Class<Model<T>>) descriptor.model();
		if (modelType == null) {
			return null;
		}

		if (ReflectionModel.class.equals(modelType)) {
			return ReflectionModel.<T> getInstance(type);
		} else {
			Model<T> model = instantiateModelByFactoryMethod(modelType);
			return model == null ? ReflectionUtils.newInstance(modelType) : model;
		}
	}

	private static <T> Model<T> instantiateModelByFactoryMethod(Class<Model<T>> modelType) {
		// TODO should be annotation based
		Method factoryMethod = ReflectionUtils.getDeclaredMethodSilently(modelType, "getInstance");
		if (isValidFactoryMethod(modelType, factoryMethod)) {
			try {
				@SuppressWarnings("unchecked")
				Model<T> casted = (Model<T>) factoryMethod.invoke(null);
				return casted;
			} catch (@SuppressWarnings("unused") ReflectionException e) {
				return null;
			}
		}

		// TODO should be annotation based
		factoryMethod = ReflectionUtils.getDeclaredMethodSilently(modelType, "getInstance", Class.class);
		if (isValidFactoryMethod(modelType, factoryMethod)) {
			try {
				@SuppressWarnings("unchecked")
				Model<T> casted = (Model<T>) factoryMethod.invoke(modelType);
				return casted;
			} catch (@SuppressWarnings("unused") ReflectionException e) {
				return null;
			}
		}

		return null;
	}

	private static <T> boolean isValidFactoryMethod(Class<Model<T>> modelClass, Method factoryMethod) {
		return factoryMethod != null && factoryMethod.isPublic() && factoryMethod.getReturnType() == modelClass
				&& factoryMethod.isStatic();
	}

	private static <T> Model<T> resolveCustomModel(Class<T> type) {
		for (int i = 0; i < modelFactories.size; i++) {
			Model<T> model = modelFactories.get(i).create(type);
			if (model != null) {
				return model;
			}
		}

		return null;
	}
}
