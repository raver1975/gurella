package com.gurella.studio.editor.engine.ui;

import java.util.Arrays;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.gurella.engine.editor.ui.EditorBaseComposite;
import com.gurella.engine.editor.ui.EditorControl;
import com.gurella.studio.GurellaStudioPlugin;

public abstract class SwtEditorBaseComposite<T extends Composite> extends SwtEditorScrollable<T>
		implements EditorBaseComposite {
	boolean paintFormBorders;

	SwtEditorBaseComposite(T composite) {
		super(composite);
	}

	@Override
	public SwtEditorControl<?>[] getChildren() {
		return Arrays.<Control> stream(widget.getChildren()).map(c -> (EditorControl) instances.get(c))
				.toArray(i -> new SwtEditorControl<?>[i]);
	}

	@Override
	public void layout() {
		widget.layout(true, true);
	}

	@Override
	public EditorControl[] getTabList() {
		return Arrays.stream(widget.getTabList()).map(c -> getEditorWidget(c)).filter(ec -> ec != null)
				.toArray(i -> new EditorControl[i]);
	}

	@Override
	public void setTabList(EditorControl[] tabList) {
		widget.setTabList(
				Arrays.stream(tabList).map(c -> ((SwtEditorControl<?>) c).widget).toArray(i -> new Control[i]));
	}

	void paintBorders() {
		if (!paintFormBorders) {
			GurellaStudioPlugin.getToolkit().paintBordersFor(widget);
		}
	}
}
