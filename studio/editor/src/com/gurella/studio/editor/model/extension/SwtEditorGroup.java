package com.gurella.studio.editor.model.extension;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.gurella.engine.editor.ui.EditorGroup;

public class SwtEditorGroup extends SwtEditorBaseComposite<Group> implements EditorGroup {
	public SwtEditorGroup(SwtEditorBaseComposite<?> parent, int style) {
		super(parent, style);
	}

	@Override
	public String getText() {
		return widget.getText();
	}

	@Override
	public void setText(String string) {
		widget.setText(string);
	}

	@Override
	Group createWidget(Composite parent, int style) {
		return new Group(parent, style);
	}
}
