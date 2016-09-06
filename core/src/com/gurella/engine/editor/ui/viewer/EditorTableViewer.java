package com.gurella.engine.editor.ui.viewer;

import java.util.List;

public interface EditorTableViewer<ELEMENT> extends EditorColumnViewer<ELEMENT, List<ELEMENT>> {
	void setSelection(ELEMENT... selection);

	void setSelection(ELEMENT[] selection, boolean reveal);

	void add(ELEMENT element);

	void add(@SuppressWarnings("unchecked") ELEMENT... elements);

	void add(Iterable<ELEMENT> elements);

	void clear(int index);

	ELEMENT getElementAt(int index);

	void insert(ELEMENT element, int position);

	void remove(ELEMENT element);

	void remove(@SuppressWarnings("unchecked") ELEMENT... elements);

	void remove(Iterable<ELEMENT> elements);

	void replace(ELEMENT element, int index);

	void refresh(boolean updateLabels, boolean reveal);

	void refresh(ELEMENT element, boolean updateLabels, boolean reveal);
}