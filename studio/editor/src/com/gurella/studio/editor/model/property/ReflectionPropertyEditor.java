package com.gurella.studio.editor.model.property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.gurella.engine.utils.Values;
import com.gurella.studio.editor.model.ModelEditorContainer;

public class ReflectionPropertyEditor<P> extends ComplexPropertyEditor<P> {
	private ModelEditorContainer<P> objectPropertiesContainer;

	public ReflectionPropertyEditor(Composite parent, PropertyEditorContext<?, P> context) {
		super(parent, context);

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);

		objectPropertiesContainer = new ModelEditorContainer<P>(this, getValueContext());
		objectPropertiesContainer.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
	}

	private ModelEditorContext<P> getValueContext() {
		P value = getValue();
		if (value == null) {
			return new ModelEditorContext<>(context, Values.cast(new Object()));
		} else {
			return new ModelEditorContext<>(context, value);
		}
	}
}