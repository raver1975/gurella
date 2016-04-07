package com.gurella.studio.editor.model.property;

import com.gurella.engine.base.model.Model;
import com.gurella.engine.base.model.Models;
import com.gurella.engine.base.model.Property;
import com.gurella.engine.event.Signal1;

public class ModelEditorContext<T> {
	public ModelEditorContext<?> parent;
	public Model<T> model;
	public T modelInstance;
	public final Signal1<PropertyValueChangedEvent> signal = new Signal1<>();

	public ModelEditorContext(T modelInstance) {
		this(null, Models.getModel(modelInstance), modelInstance);
	}

	public ModelEditorContext(Model<T> model, T modelInstance) {
		this(null, model, modelInstance);
	}

	public ModelEditorContext(ModelEditorContext<?> parent, T modelInstance) {
		this(parent, Models.getModel(modelInstance), modelInstance);
	}
	
	public ModelEditorContext(ModelEditorContext<?> parent, Model<T> model, T modelInstance) {
		this.parent = parent;
		this.model = model;
		this.modelInstance = modelInstance;
		
		if (parent != null) {
			signal.addListener(parent.signal::dispatch);
		}
	}

	public void propertyValueChanged(Property<?> property, Object oldValue, Object newValue) {
		signal.dispatch(new PropertyValueChangedEvent(model, property, modelInstance, oldValue, newValue));
	}

	public static final class PropertyValueChangedEvent {
		public final Model<?> model;
		public final Property<?> property;
		public final Object modelInstance;
		public final Object oldValue;
		public final Object newValue;

		public PropertyValueChangedEvent(Model<?> model, Property<?> property, Object modelInstance, Object oldValue,
				Object newValue) {
			this.model = model;
			this.property = property;
			this.modelInstance = modelInstance;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
	}
}