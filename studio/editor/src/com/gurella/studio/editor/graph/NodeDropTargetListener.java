package com.gurella.studio.editor.graph;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.gurella.engine.scene.SceneNode2;
import com.gurella.studio.editor.SceneEditorContext;

class NodeDropTargetListener extends DropTargetAdapter {
	private static final LocalSelectionTransfer localTransfer = LocalSelectionTransfer.getTransfer();
	private final Tree graph;
	private final SceneEditorContext context;

	NodeDropTargetListener(Tree graph, SceneEditorContext context) {
		this.graph = graph;
		this.context = context;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		if ((event.operations & DND.DROP_MOVE) == 0 || getTransferNode() == null) {
			event.detail = DND.DROP_NONE;
			return;
		}

		event.detail = DND.DROP_MOVE;
	}

	private static SceneNode2 getTransferNode() {
		ISelection selection = localTransfer.getSelection();
		if (selection instanceof NodeSelection) {
			return ((NodeSelection) selection).getNode();
		} else {
			return null;
		}
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;

		if (event.item == null) {
			event.detail = DND.DROP_NONE;
			return;
		}

		TreeItem item = (TreeItem) event.item;
		Object data = item.getData();
		if (!(data instanceof SceneNode2)) {
			event.detail = DND.DROP_NONE;
			return;
		}

		SceneNode2 eventNode = (SceneNode2) data;
		SceneNode2 node = getTransferNode();
		if (node == eventNode) {
			event.detail = DND.DROP_NONE;
			return;
		}

		Point point = event.display.map(null, graph, event.x, event.y);
		Rectangle bounds = item.getBounds();

		if (point.y < bounds.y + bounds.height / 3) {
			event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
			event.detail = DND.DROP_MOVE;
		} else if (point.y > bounds.y + 2 * bounds.height / 3) {
			event.feedback |= DND.FEEDBACK_INSERT_AFTER;
			event.detail = DND.DROP_MOVE;
		} else if (eventNode == node.getParentNode()) {
			event.detail = DND.DROP_NONE;
		} else {
			event.feedback |= DND.FEEDBACK_SELECT;
			event.detail = DND.DROP_MOVE;
		}
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
		event.detail = DND.DROP_MOVE;
	}

	@Override
	public void drop(DropTargetEvent event) {
		if (event.item == null) {
			event.detail = DND.DROP_NONE;
			return;
		}

		TreeItem item = (TreeItem) event.item;
		Object data = item.getData();
		if (!(data instanceof SceneNode2)) {
			event.detail = DND.DROP_NONE;
			return;
		}

		SceneNode2 eventNode = (SceneNode2) data;
		SceneNode2 node = getTransferNode();
		if (node == eventNode) {
			event.detail = DND.DROP_NONE;
			return;
		}

		Point point = event.display.map(null, graph, event.x, event.y);
		Rectangle bounds = item.getBounds();
		SceneNode2 parentNode = node.getParentNode();
		int oldIndex = parentNode == null ? context.getScene().getNodeIndex(node) : parentNode.getChildNodeIndex(node);

		if (point.y < bounds.y + bounds.height / 3) {
			if (node.getParent() == eventNode.getParent()) {
				SceneNode2 parent = eventNode.getParentNode();
				int newIndex = parent == null ? context.getScene().getNodeIndex(eventNode)
						: parent.getChildNodeIndex(eventNode);
				newIndex = oldIndex < newIndex ? newIndex - 1 : newIndex;
				reindexNode(node, oldIndex, newIndex);
			} else {
				SceneNode2 parent = eventNode.getParentNode();
				int newIndex = parent == null ? context.getScene().getNodeIndex(eventNode)
						: parent.getChildNodeIndex(eventNode);
				newIndex = oldIndex < newIndex ? newIndex - 1 : newIndex;
				reparentNode(node, parent, newIndex);
			}
		} else if (point.y > bounds.y + 2 * bounds.height / 3) {
			if (node.getParent() == eventNode.getParent()) {
				SceneNode2 parent = eventNode.getParentNode();
				int newIndex = parent == null ? context.getScene().getNodeIndex(eventNode)
						: parent.getChildNodeIndex(eventNode);
				newIndex = oldIndex < newIndex ? newIndex : newIndex + 1;
				reindexNode(node, oldIndex, newIndex);
			} else {
				SceneNode2 parent = eventNode.getParentNode();
				int newIndex = parent == null ? context.getScene().getNodeIndex(eventNode)
						: parent.getChildNodeIndex(eventNode);
				newIndex = oldIndex < newIndex ? newIndex : newIndex + 1;
				reparentNode(node, parent, newIndex);
			}
		} else if (eventNode != node.getParentNode()) {
			reparentNode(node, eventNode, eventNode.childNodes.size());
		}
	}

	private void reindexNode(SceneNode2 node, int oldIndex, int newIndex) {
		int editorId = context.editorId;
		String errorMsg = "Error while repositioning node";
		context.executeOperation(new ReindexNodeOperation(editorId, node, oldIndex, newIndex), errorMsg);
	}

	private void reparentNode(SceneNode2 node, SceneNode2 newParent, int newIndex) {
		int editorId = context.editorId;
		String errorMsg = "Error while repositioning node";
		context.executeOperation(new ReparentNodeOperation(editorId, node, newParent, newIndex), errorMsg);
	}
}