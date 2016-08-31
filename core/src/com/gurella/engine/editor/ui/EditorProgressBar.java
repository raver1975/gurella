package com.gurella.engine.editor.ui;

public interface EditorProgressBar extends EditorControl {
	int getMaximum();

	int getMinimum();

	int getSelection();

	int getState();

	void setMaximum(int value);

	void setMinimum(int value);

	void setSelection(int value);

	void setState(int state);

	public static class ProgressBarStyle extends ControlStyle {
		public boolean vertical;
		public boolean smooth;
		public boolean indeterminate;
	}
}
