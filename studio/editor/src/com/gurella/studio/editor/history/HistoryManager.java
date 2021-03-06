package com.gurella.studio.editor.history;

import static com.gurella.studio.GurellaStudioPlugin.log;
import static com.gurella.studio.GurellaStudioPlugin.showError;

import java.util.Optional;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import com.gurella.engine.utils.plugin.Workbench;
import com.gurella.studio.editor.SceneEditor;
import com.gurella.studio.editor.menu.ContextMenuActions;
import com.gurella.studio.editor.menu.EditorContextMenuContributor;
import com.gurella.studio.editor.subscription.EditorCloseListener;
import com.gurella.studio.editor.utils.Try;
import com.gurella.studio.gdx.GdxContext;

public class HistoryManager extends UndoContext
		implements HistoryService, EditorCloseListener, EditorContextMenuContributor {
	private final int editorId;

	private final IOperationHistory operationHistory;
	private final UndoActionHandler undoAction;
	private final RedoActionHandler redoAction;
	private final IStatusLineManager statusLineManager;
	private final UndoRedoActionGroup historyActionGroup;

	private final HistoryContributorRegistry registry;

	public HistoryManager(SceneEditor editor) {
		editorId = editor.id;

		IEditorSite site = editor.getEditorSite();
		undoAction = new UndoActionHandler(site, this);
		redoAction = new RedoActionHandler(site, this);

		statusLineManager = site.getActionBars().getStatusLineManager();

		IWorkbench workbench = site.getWorkbenchWindow().getWorkbench();
		operationHistory = workbench.getOperationSupport().getOperationHistory();
		historyActionGroup = new UndoRedoActionGroup(site, this, true);
		historyActionGroup.fillActionBars(site.getActionBars());

		registry = new HistoryContributorRegistry(this);

		GdxContext.subscribe(editorId, editorId, this);
		Workbench.addListener(editorId, registry);
		Workbench.activate(editorId, this);
	}

	@Override
	public void onEditorClose() {
		Workbench.deactivate(editorId, this);
		Workbench.removeListener(editorId, registry);
		GdxContext.unsubscribe(editorId, editorId, this);
		historyActionGroup.dispose();
		operationHistory.dispose(this, true, true, true);
		redoAction.dispose();
		undoAction.dispose();
	}

	@Override
	public void executeOperation(IUndoableOperation operation, String errorMsg) {
		operation.addContext(this);
		IProgressMonitor monitor = statusLineManager.getProgressMonitor();
		Try.ofFailable(() -> operationHistory.execute(operation, monitor, null)).onFailure(e -> showError(e, errorMsg));
	}

	private boolean canUndo() {
		return operationHistory.canUndo(this);
	}

	private void undo() {
		if (canUndo()) {
			IProgressMonitor monitor = statusLineManager.getProgressMonitor();
			String msg = "Error while executing undo.";
			Try.ofFailable(() -> operationHistory.undo(this, monitor, null)).onFailure(e -> log(e, msg));
		}
	}

	private boolean canRedo() {
		return operationHistory.canRedo(this);
	}

	private void redo() {
		if (canRedo()) {
			IProgressMonitor monitor = statusLineManager.getProgressMonitor();
			String msg = "Error while executing redo.";
			Try.ofFailable(() -> operationHistory.redo(this, monitor, null)).onFailure(e -> log(e, msg));
		}
	}

	@Override
	public void contribute(float x, float y, ContextMenuActions actions) {
		IUndoableOperation undoOperation = operationHistory.getUndoOperation(this);
		String undo = Optional.ofNullable(undoOperation).map(o -> "&Undo " + o.getLabel()).orElse("&Undo");
		actions.addAction(undo, -1000, canUndo(), this::undo);

		IUndoableOperation redoOperation = operationHistory.getRedoOperation(this);
		String redo = Optional.ofNullable(redoOperation).map(o -> "&Redo " + o.getLabel()).orElse("&Redo");
		actions.addAction(redo, -900, canRedo(), this::redo);
	}
}
