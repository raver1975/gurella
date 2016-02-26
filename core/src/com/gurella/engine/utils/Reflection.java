package com.gurella.engine.utils;

import java.lang.annotation.Annotation;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

//TODO caches + exceptions description
public class Reflection {
	private static final ObjectMap<String, Class<?>> classesByName = new ObjectMap<String, Class<?>>();
	private static final ObjectMap<Class<?>, Constructor> constructorsByClass = new ObjectMap<Class<?>, Constructor>();

	private Reflection() {
	}

	public static <T> Class<T> forName(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> resourceType = ClassReflection.forName(className);
			return resourceType;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> Class<T> forNameSilently(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> resourceType = ClassReflection.forName(className);
			return resourceType;
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T newInstance(Class<T> type) {
		try {
			Constructor constructor = ClassReflection.getDeclaredConstructor(type);
			constructor.setAccessible(true);
			@SuppressWarnings("unchecked")
			T instance = (T) constructor.newInstance();
			return instance;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> T newInstanceSilently(Class<T> type) {
		try {
			return newInstance(type);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isInnerClass(Class<?> type) {
		return !type.isPrimitive() && ClassReflection.isMemberClass(type) && !ClassReflection.isStaticClass(type);
	}

	public static <T> T newInnerClassInstance(Class<T> type, Object enclosingInstance) {
		try {
			Constructor constructor = findInnerClassDeclaredConstructor(type, enclosingInstance);
			constructor.setAccessible(true);
			@SuppressWarnings("unchecked")
			T instance = (T) constructor.newInstance(enclosingInstance);
			return instance;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Constructor findInnerClassDeclaredConstructor(Class<?> type, Object enclosingInstance) {
		Class<?> enclosingInstanceType = enclosingInstance.getClass();
		while (enclosingInstanceType != Object.class) {
			Constructor constructor = getInnerClassDeclaredConstructorSilently(type, enclosingInstanceType);
			if (constructor != null) {
				return constructor;
			}
			enclosingInstanceType = enclosingInstanceType.getSuperclass();
		}
		return null;
	}

	public static <T> T newInstance(String className) {
		return newInstance(Reflection.<T> forName(className));
	}

	public static <T> T newInstanceSilently(String className) {
		try {
			return newInstance(Reflection.<T> forName(className));
		} catch (Exception e) {
			return null;
		}
	}

	public static Constructor getDeclaredConstructor(Class<?> c, Class<?>... parameterTypes) {
		try {
			return ClassReflection.getDeclaredConstructor(c, parameterTypes);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Constructor getDeclaredConstructorSilently(Class<?> c, Class<?>... parameterTypes) {
		try {
			return getDeclaredConstructor(c, parameterTypes);
		} catch (Exception e) {
			return null;
		}
	}

	public static Constructor getInnerClassDeclaredConstructor(Class<?> c, Class<?> enclosingClass,
			Class<?>... parameterTypes) {
		try {
			if (parameterTypes == null) {
				return ClassReflection.getDeclaredConstructor(c, enclosingClass);
			} else {
				int length = parameterTypes.length;
				@SuppressWarnings("rawtypes")
				Class[] allParameterTypes = new Class[length + 1];
				allParameterTypes[0] = enclosingClass;
				System.arraycopy(parameterTypes, 0, allParameterTypes, 1, length);
				return ClassReflection.getDeclaredConstructor(c, allParameterTypes);
			}
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Constructor getInnerClassDeclaredConstructorSilently(Class<?> c, Class<?> enclosingClass,
			Class<?>... parameterTypes) {
		try {
			return getInnerClassDeclaredConstructor(c, enclosingClass, parameterTypes);
		} catch (Exception e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Method getDeclaredMethod(Class<?> c, String name, Class<?>... parameterTypes) {
		try {
			return ClassReflection.getDeclaredMethod(c, name, parameterTypes);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Method getDeclaredMethodSilently(Class<?> c, String name, Class<?>... parameterTypes) {
		try {
			return ClassReflection.getDeclaredMethod(c, name, parameterTypes);
		} catch (Exception e) {
			return null;
		}
	}

	public static Method getMethod(Class<?> c, String name, Class<?>... parameterTypes) {
		try {
			return ClassReflection.getMethod(c, name, parameterTypes);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Method getMethodSilently(Class<?> c, String name, Class<?>... parameterTypes) {
		try {
			return ClassReflection.getMethod(c, name, parameterTypes);
		} catch (Exception e) {
			return null;
		}
	}

	public static Field getDeclaredField(Class<?> c, String name) {
		try {
			return ClassReflection.getDeclaredField(c, name);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static Field getDeclaredFieldSilently(Class<?> c, String name) {
		try {
			return ClassReflection.getDeclaredField(c, name);
		} catch (Exception e) {
			return null;
		}
	}

	public static Field getField(Class<?> c, String name) {
		try {
			return ClassReflection.getField(c, name);
		} catch (ReflectionException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Field getFieldSilently(Class<?> c, String name) {
		try {
			return ClassReflection.getField(c, name);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T extends Annotation> T getDeclaredAnnotation(Class<?> owner, Class<T> annotationType) {
		com.badlogic.gdx.utils.reflect.Annotation annotation = ClassReflection.getDeclaredAnnotation(owner,
				annotationType);
		return annotation == null ? null : annotation.getAnnotation(annotationType);
	}

	public static <T extends Annotation> T getAnnotation(Class<?> owner, Class<T> annotationType) {
		Class<?> temp = owner;
		while (temp != null && !Object.class.equals(temp)) {
			T annotation = getDeclaredAnnotation(temp, annotationType);
			if (annotation != null) {
				return annotation;
			} else {
				temp = temp.getSuperclass();
			}
		}
		return null;
	}

	public static <T extends Annotation> T getDeclaredAnnotation(Field owner, Class<T> annotationType) {
		com.badlogic.gdx.utils.reflect.Annotation annotation = owner.getDeclaredAnnotation(annotationType);
		return annotation == null ? null : annotation.getAnnotation(annotationType);
	}

	public static <T extends Annotation> T getDeclaredAnnotation(Method owner, Class<T> annotationType) {
		com.badlogic.gdx.utils.reflect.Annotation annotation = owner.getDeclaredAnnotation(annotationType);
		return annotation == null ? null : annotation.getAnnotation(annotationType);
	}

	public static void setFieldValue(String fieldName, Object object, Object value) {
		setFieldValue(getField(object.getClass(), fieldName), object, value);
	}

	public static void setFieldValue(Field field, Object object, Object value) {
		try {
			field.set(object, value);
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> T getFieldValue(Field field, Object object) {
		try {
			@SuppressWarnings("unchecked")
			T casted = (T) field.get(object);
			return casted;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> T invokeMethod(Method method, Object object, Object... args) {
		try {
			@SuppressWarnings("unchecked")
			T casted = (T) method.invoke(object, args);
			return casted;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> T invokeMethodSilently(Method method, Object object, Object... args) {
		try {
			return invokeMethod(method, object, args);
		} catch (Exception e) {
			return null;
		}
	}

	public static <T> T invokeConstructor(Constructor constructor, Object... args) {
		try {
			@SuppressWarnings("unchecked")
			T casted = (T) constructor.newInstance(args);
			return casted;
		} catch (ReflectionException e) {
			throw new GdxRuntimeException(e);
		}
	}

	public static <T> T invokeConstructorSilently(Constructor constructor, Object... args) {
		try {
			return invokeConstructor(constructor, args);
		} catch (Exception e) {
			return null;
		}
	}
}