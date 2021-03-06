package com.gurella.studio.editor.ui.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import com.gurella.engine.editor.property.PropertyEditorDescriptor;
import com.gurella.engine.editor.property.PropertyEditorDescriptor.EditorType;
import com.gurella.engine.editor.property.PropertyEditorFactory;
import com.gurella.engine.metatype.Property;
import com.gurella.engine.metatype.ReflectionProperty;
import com.gurella.engine.utils.Reflection;
import com.gurella.engine.utils.Values;
import com.gurella.studio.editor.ui.bean.BeanEditorContext;
import com.gurella.studio.editor.utils.Try;

public class PropertyEditorData {
	private static final Map<EditorPropertyKey, PropertyEditorData> editorProperties = new HashMap<>();

	public final EditorType type;
	public final String customFactoryClass;
	public final int index;
	public final String group;
	public final String descriptiveName;
	public final String description;
	public final List<String> genericTypes;

	private PropertyEditorData(EditorType type, String customFactoryClass, int index, String group,
			String descriptiveName, String description, List<String> genericTypes) {
		this.type = type;
		this.customFactoryClass = customFactoryClass;
		this.index = index;
		this.group = group;
		this.descriptiveName = descriptiveName;
		this.description = description;
		this.genericTypes = Collections.unmodifiableList(genericTypes);
	}

	public static int compare(BeanEditorContext<?> context, Property<?> p1, Property<?> p2) {
		return Integer.compare(getIndex(context, p1), getIndex(context, p2));
	}

	public static int getIndex(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return getIndex(context.javaProject, beanType, property);
	}

	public static int getIndex(BeanEditorContext<?> context, Property<?> property) {
		Class<?> beanType = context.bean.getClass();
		return getIndex(context.javaProject, beanType, property);
	}

	public static int getIndex(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		PropertyEditorData propertyEditorData = get(javaProject, beanType, property);
		return propertyEditorData == null ? 0 : propertyEditorData.index;
	}

	public static String getGroup(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return getGroup(context.javaProject, beanType, property);
	}

	public static String getGroup(BeanEditorContext<?> context, Property<?> property) {
		Class<?> beanType = context.bean.getClass();
		return getGroup(context.javaProject, beanType, property);
	}

	public static String getGroup(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		PropertyEditorData propertyEditorData = get(javaProject, beanType, property);
		return propertyEditorData == null || propertyEditorData.group == null ? "" : propertyEditorData.group;
	}

	public static String getDescription(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return getDescription(context.javaProject, beanType, property);
	}

	public static String getDescription(BeanEditorContext<?> context, Property<?> property) {
		Class<?> beanType = context.bean.getClass();
		return getDescription(context.javaProject, beanType, property);
	}

	public static String getDescription(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		PropertyEditorData propertyEditorData = get(javaProject, beanType, property);
		return propertyEditorData == null ? null : propertyEditorData.description;
	}

	public static String getDescriptiveName(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return getDescriptiveName(context.javaProject, beanType, property);
	}

	public static String getDescriptiveName(BeanEditorContext<?> context, Property<?> property) {
		Class<?> beanType = context.bean.getClass();
		return getDescriptiveName(context.javaProject, beanType, property);
	}

	public static String getDescriptiveName(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		PropertyEditorData propertyEditorData = get(javaProject, beanType, property);
		String descriptiveName = propertyEditorData == null ? null : propertyEditorData.descriptiveName;
		return Values.isBlank(descriptiveName) ? property.getName() : propertyEditorData.descriptiveName;
	}

	public static List<String> getGenericTypes(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return getGenericTypes(context.javaProject, beanType, property);
	}

	public static <T> Optional<Class<T>> getGenericType(PropertyEditorContext<?, ?> context, int index) {
		List<String> genericTypes = getGenericTypes(context);
		if (genericTypes == null || genericTypes.size() <= index) {
			return Optional.empty();
		}

		return Optional.ofNullable(Try.ignored(() -> Reflection.forName(genericTypes.get(0)), null));
	}

	public static List<String> getGenericTypes(BeanEditorContext<?> context, Property<?> property) {
		Class<?> beanType = context.bean.getClass();
		return getGenericTypes(context.javaProject, beanType, property);
	}

	public static List<String> getGenericTypes(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		PropertyEditorData propertyEditorData = get(javaProject, beanType, property);
		return propertyEditorData == null ? Collections.emptyList() : propertyEditorData.genericTypes;
	}

	public static PropertyEditorData get(PropertyEditorContext<?, ?> context) {
		Class<?> beanType = context.bean.getClass();
		Property<?> property = context.property;
		return PropertyEditorData.get(context.javaProject, beanType, property);
	}

	public static PropertyEditorData get(IJavaProject javaProject, Class<?> beanType, Property<?> property) {
		try {
			return getSafely(javaProject, beanType, property);
		} catch (Exception e) {
			editorProperties.put(new EditorPropertyKey(beanType, property.getName()), null);
			return null;
		}
	}

	private static PropertyEditorData getSafely(IJavaProject javaProject, Class<?> beanType, Property<?> property)
			throws Exception {
		if (!(property instanceof ReflectionProperty)) {
			return null;
		}

		String propertyName = property.getName();
		EditorPropertyKey key = new EditorPropertyKey(beanType, propertyName);
		PropertyEditorData data = editorProperties.get(key);
		if (data != null) {
			return data;
		}

		ReflectionProperty<?> reflectionProperty = (ReflectionProperty<?>) property;
		Class<?> declaringClass = reflectionProperty.getDeclaringClass();
		EditorPropertyKey declaredKey = new EditorPropertyKey(declaringClass, propertyName);

		if (reflectionProperty.getField() == null) {
			// TODO bean properties
			editorProperties.put(declaredKey, data);
			editorProperties.put(key, data);
			return data;
		} else {
			data = editorProperties.get(declaredKey);
			if (data != null) {
				editorProperties.put(key, data);
				return data;
			}

			IType type = javaProject.findType(declaringClass.getName());
			IField jdtField = type.getField(property.getName());
			for (IAnnotation annotation : jdtField.getAnnotations()) {
				Class<PropertyEditorDescriptor> annotationType = PropertyEditorDescriptor.class;
				String elementName = annotation.getElementName();
				if (elementName.equals(annotationType.getSimpleName())
						|| elementName.equals(annotationType.getName())) {
					data = parseAnnotation(type, annotation);
					editorProperties.put(declaredKey, data);
					editorProperties.put(key, data);
					return data;
				}
			}

			IAnnotation annotation = jdtField.getAnnotation(PropertyEditorDescriptor.class.getName());
			data = annotation == null ? null : parseAnnotation(type, annotation);
			editorProperties.put(declaredKey, data);
			editorProperties.put(key, data);
			return data;
		}
	}

	private static PropertyEditorData parseAnnotation(IType type, IAnnotation annotation) throws JavaModelException {
		IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
		String factoryName = null;
		EditorType editorType = EditorType.composite;
		int index = 0;
		String group = null;
		String description = null;
		String descriptiveName = null;
		List<String> generics = new ArrayList<>();

		for (IMemberValuePair memberValuePair : memberValuePairs) {
			if ("factory".equals(memberValuePair.getMemberName())) {
				factoryName = extractTypeName(type, (String) memberValuePair.getValue());
			} else if ("type".equals(memberValuePair.getMemberName())) {
				String editorTypeStr = memberValuePair.getValue().toString();
				if (editorTypeStr.contains(EditorType.simple.name())) {
					editorType = EditorType.simple;
				} else if (editorTypeStr.contains(EditorType.custom.name())) {
					editorType = EditorType.custom;
				}
			} else if ("index".equals(memberValuePair.getMemberName())) {
				index = Integer.parseInt(memberValuePair.getValue().toString());
			} else if ("group".equals(memberValuePair.getMemberName())) {
				group = memberValuePair.getValue().toString();
			} else if ("description".equals(memberValuePair.getMemberName())) {
				description = memberValuePair.getValue().toString();
			} else if ("descriptiveName".equals(memberValuePair.getMemberName())) {
				descriptiveName = memberValuePair.getValue().toString();
			} else if ("genericTypes".equals(memberValuePair.getMemberName())) {
				Object[] values = (Object[]) memberValuePair.getValue();
				for (Object value : values) {
					try {
						generics.add(extractTypeName(type, value.toString()));
					} catch (Exception e) {
						break;
					}
				}
			}
		}

		return new PropertyEditorData(editorType, factoryName, index, group, descriptiveName, description, generics);
	}

	private static String extractTypeName(IType type, String typeName) throws JavaModelException {
		String[][] resolveType = type.resolveType(typeName);
		if (resolveType.length == 1) {
			String[] path = resolveType[0];
			int last = path.length - 1;
			path[last] = path[last].replaceAll("\\.", "\\$");
			StringBuilder builder = new StringBuilder();
			for (String part : path) {
				if (builder.length() > 0) {
					builder.append(".");
				}
				builder.append(part);
			}
			return builder.toString();
		}
		return null;
	}

	private static class EditorPropertyKey {
		String typeName;
		String propertyName;

		public EditorPropertyKey(Class<?> type, String propertyName) {
			this.typeName = type.getName();
			this.propertyName = propertyName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + typeName.hashCode();
			result = prime * result + propertyName.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			EditorPropertyKey other = (EditorPropertyKey) obj;
			return typeName.equals(other.typeName) && propertyName.equals(other.propertyName);
		}
	}

	public boolean isValidFactoryClass() {
		return customFactoryClass != null && !PropertyEditorFactory.class.getName().equals(customFactoryClass);
	}
}
