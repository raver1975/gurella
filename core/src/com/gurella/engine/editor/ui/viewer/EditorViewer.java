package com.gurella.engine.editor.ui.viewer;

import com.gurella.engine.editor.ui.EditorImage;
import com.gurella.engine.editor.ui.EditorItem;

public interface EditorViewer<INPUT, ELEMENT, SELECTION> {
	INPUT getInput();

	SELECTION getSelection();

	EditorItem scrollDown(int x, int y);

	EditorItem scrollUp(int x, int y);

	void setInput(INPUT input);

	void setSelection(SELECTION selection);

	void setSelection(SELECTION selection, boolean reveal);

	void setSelection(ELEMENT... selection);

	void setSelection(ELEMENT[] selection, boolean reveal);

	void refresh();

	void refresh(boolean updateLabels);

	void refresh(ELEMENT element);

	void refresh(ELEMENT element, boolean updateLabels);

	void reveal(ELEMENT element);

	void update(ELEMENT[] elements, String[] properties);

	void update(ELEMENT element, String... properties);

	LabelProvider<ELEMENT> getLabelProvider();

	void setLabelProvider(LabelProvider<ELEMENT> labelProvider);

	public interface LabelProvider<ELEMENT> {
		EditorImage getImage(ELEMENT element);

		String getText(ELEMENT element);
	}
}
