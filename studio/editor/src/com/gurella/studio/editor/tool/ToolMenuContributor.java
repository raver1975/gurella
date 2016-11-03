package com.gurella.studio.editor.tool;

import static com.gurella.studio.editor.tool.ToolType.none;
import static com.gurella.studio.editor.tool.ToolType.rotate;
import static com.gurella.studio.editor.tool.ToolType.scale;
import static com.gurella.studio.editor.tool.ToolType.translate;

import com.gurella.engine.event.EventService;
import com.gurella.studio.editor.menu.ContextMenuActions;
import com.gurella.studio.editor.subscription.EditorContextMenuContributor;
import com.gurella.studio.editor.subscription.EditorPreCloseListener;

public class ToolMenuContributor implements EditorPreCloseListener, EditorContextMenuContributor {
	private static final String toolMenuGroupName = "&Tool";

	private final int editorId;
	private final ToolManager manager;

	public ToolMenuContributor(int editorId, ToolManager manager) {
		this.editorId = editorId;
		this.manager = manager;
		EventService.subscribe(editorId, this);
	}

	@Override
	public void contribute(ContextMenuActions actions) {
		actions.addGroup(toolMenuGroupName, -500);
		ToolType type = manager.getSelectedToolType();
		boolean selected = type == translate;
		actions.addCheckAction(toolMenuGroupName, "&Translate\tT", 100, !selected, selected, () -> select(translate));
		selected = type == rotate;
		actions.addCheckAction(toolMenuGroupName, "&Rotate\tR", 100, !selected, selected, () -> select(rotate));
		selected = type == scale;
		actions.addCheckAction(toolMenuGroupName, "&Scale\tS", 100, !selected, selected, () -> select(scale));
		selected = type == none;
		actions.addCheckAction(toolMenuGroupName, "&None\tN", 100, !selected, selected, () -> select(none));
	}

	private void select(ToolType type) {
		manager.selectTool(type);
	}

	@Override
	public void onEditorPreClose() {
		EventService.unsubscribe(editorId, this);
	}
}