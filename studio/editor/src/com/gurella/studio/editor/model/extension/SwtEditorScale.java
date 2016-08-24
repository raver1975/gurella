package com.gurella.studio.editor.model.extension;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gurella.engine.editor.ui.EditorScale;

public class SwtEditorScale extends SwtEditorControl<Scale> implements EditorScale {
	public SwtEditorScale(SwtEditorBaseComposite<?> parent, FormToolkit toolkit) {
		super(parent);
	}

	@Override
	public int getIncrement() {
		return widget.getIncrement();
	}

	@Override
	public int getMaximum() {
		return widget.getMaximum();
	}

	@Override
	public int getMinimum() {
		return widget.getMinimum();
	}

	@Override
	public int getPageIncrement() {
		return widget.getPageIncrement();
	}

	@Override
	public int getSelection() {
		return widget.getSelection();
	}

	@Override
	public void setIncrement(int increment) {
		widget.setIncrement(increment);
	}

	@Override
	public void setMaximum(int value) {
		widget.setMaximum(value);
	}

	@Override
	public void setMinimum(int value) {
		widget.setMinimum(value);
	}

	@Override
	public void setPageIncrement(int pageIncrement) {
		widget.setPageIncrement(pageIncrement);
	}

	@Override
	public void setSelection(int value) {
		widget.setSelection(value);
	}

	@Override
	Scale createWidget(Composite parent) {
		return new Scale(parent, 0);
	}

}