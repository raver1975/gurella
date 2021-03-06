package com.gurella.studio.editor.ui.bean;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;

import com.gurella.engine.event.Listener1;
import com.gurella.studio.GurellaStudioPlugin;
import com.gurella.studio.editor.ui.bean.BeanEditorContext.PropertyValueChangedEvent;
import com.gurella.studio.editor.ui.property.PropertyEditor;

public abstract class BeanEditor<T> extends Composite {
	BeanEditorContext<T> context;

	private List<PropertyEditor<?>> hoverEditors = new ArrayList<PropertyEditor<?>>();
	private List<PropertyEditor<?>> hoverEditorsTemp = new ArrayList<PropertyEditor<?>>();

	public BeanEditor(Composite parent, int gdxContextId, T bean) {
		this(parent, new BeanEditorContext<>(gdxContextId, bean));
	}

	public BeanEditor(Composite parent, BeanEditorContext<T> context) {
		super(parent, SWT.NONE);
		this.context = context;
		GurellaStudioPlugin.getToolkit().adapt(this);
		Listener mouseMovedListener = e -> mouseMoved();
		Display display = getDisplay();
		display.addFilter(SWT.MouseMove, mouseMovedListener);
		addListener(SWT.Dispose, (e) -> display.removeFilter(SWT.MouseMove, mouseMovedListener));
	}

	private void mouseMoved() {
		extractHoveredEditors();

		hoverEditors.stream().filter(e -> !hoverEditorsTemp.contains(e)).forEach(e -> e.setMenuVisible(false));
		hoverEditorsTemp.stream().filter(e -> !hoverEditors.contains(e)).forEach(e -> e.setMenuVisible(true));

		hoverEditors.clear();
		List<PropertyEditor<?>> temp = hoverEditorsTemp;
		hoverEditorsTemp = hoverEditors;
		hoverEditors = temp;
	}

	private void extractHoveredEditors() {
		Control cursorControl = getDisplay().getCursorControl();
		if (cursorControl == null) {
			return;
		}

		Composite parent = cursorControl.getParent();

		while (parent != null && parent != this) {
			PropertyEditor<?> editor = (PropertyEditor<?>) parent.getData(PropertyEditor.class.getName());
			if (editor != null) {
				hoverEditorsTemp.add(editor);
			}

			parent = parent.getParent();
		}

		if (this != parent) {
			hoverEditorsTemp.clear();
		}
	}

	public BeanEditorContext<T> getContext() {
		return context;
	}

	public boolean addPropertiesListener(Listener1<PropertyValueChangedEvent> listener) {
		return context.propertiesSignal.addListener(listener);
	}

	public boolean removePropertyListener(Listener1<PropertyValueChangedEvent> listener) {
		return context.propertiesSignal.removeListener(listener);
	}
}
