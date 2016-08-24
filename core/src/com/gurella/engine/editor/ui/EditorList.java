package com.gurella.engine.editor.ui;

public interface EditorList extends EditorScrollable {
	void add(String string);

	void add(String string, int index);

	void deselect(int index);

	void deselect(int[] indices);

	void deselect(int start, int end);

	void deselectAll();

	int getFocusIndex();

	String getItem(int index);

	int getItemCount();

	int getItemHeight();

	String[] getItems();

	String[] getSelection();

	int getSelectionCount();

	int getSelectionIndex();

	int[] getSelectionIndices();

	int getTopIndex();

	int indexOf(String string);

	int indexOf(String string, int start);

	boolean isSelected(int index);

	void remove(int index);

	void remove(int[] indices);

	void remove(int start, int end);

	void remove(String string);

	void removeAll();

	void select(int index);

	void select(int[] indices);

	void select(int start, int end);

	void selectAll();

	void setItem(int index, String string);

	void setItems(String... items);

	void setSelection(int index);

	void setSelection(int[] indices);

	void setSelection(int start, int end);

	void setSelection(String[] items);

	void setTopIndex(int index);

	void showSelection();
}