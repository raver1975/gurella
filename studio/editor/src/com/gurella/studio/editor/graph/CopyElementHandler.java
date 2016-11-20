package com.gurella.studio.editor.graph;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;

import com.gurella.engine.scene.SceneElement2;

class CopyElementHandler extends AbstractHandler {
	private final SceneGraphView view;
	private final Clipboard clipboard;

	public CopyElementHandler(SceneGraphView view) {
		this.view = view;
		this.clipboard = view.clipboard;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Optional<SceneElement2> selected = view.getFirstSelectedElement();
		if (selected.isPresent()) {
			LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
			SceneElement2 element = selected.get();
			transfer.setSelection(new CopyElementSelection(element));
			clipboard.setContents(new Object[] { element }, new Transfer[] { transfer });
		}
		return null;
	}
}