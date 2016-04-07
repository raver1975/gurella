package com.gurella.studio.editor.scene;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gurella.engine.asset.AssetType;
import com.gurella.studio.editor.GurellaEditor;
import com.gurella.studio.editor.GurellaStudioPlugin;
import com.gurella.studio.editor.inspector.TexturePropertiesContainer;
import com.gurella.studio.editor.inspector.TexturePropertiesContainer.TextureResource;
import com.gurella.studio.editor.scene.InspectorView.Inspectable;
import com.gurella.studio.editor.scene.InspectorView.PropertiesContainer;

public class AssetsExplorerView extends SceneEditorView {
	private static final String GURELLA_PROJECT_FILE_EXTENSION = "gprj";

	private Tree tree;

	public AssetsExplorerView(GurellaEditor editor, int style) {
		super(editor, "Assets", GurellaStudioPlugin.createImage("icons/resource_persp.gif"), style);

		setLayout(new GridLayout());
		FormToolkit toolkit = GurellaStudioPlugin.getToolkit();
		toolkit.adapt(this);
		tree = toolkit.createTree(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setHeaderVisible(false);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		try {
			IPath assetsRoot = getAssetsRoot().makeRelativeTo(editor.getProject().getLocation());
			IResource resource = editor.getProject().findMember(assetsRoot);
			if (resource instanceof IContainer) {
				createItems(null, resource);
			}

			tree.addListener(SWT.Selection, (e) -> postMessage(new SelectionMessage(getInspectable())));
		} catch (CoreException e) {
			tree.dispose();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Inspectable<?> getInspectable() {
		TreeItem[] selection = tree.getSelection();
		if (selection.length > 0) {
			Object data = selection[0].getData();
			if (data instanceof IFile) {
				IFile file = (IFile) data;
				if (AssetType.texture.containsExtension(file.getFileExtension())) {
					return new TextureInspectable(file);
				}
			}
		}
		return null;
	}

	private IPath getAssetsRoot() throws CoreException {
		IPathEditorInput pathEditorInput = (IPathEditorInput) editor.getEditorInput();
		IPath projectPath = editor.getProject().getLocation().makeAbsolute();
		IPath scenePath = pathEditorInput.getPath().removeLastSegments(1).makeAbsolute();
		IPath temp = scenePath;
		while (projectPath.isPrefixOf(temp)) {
			IResource member = editor.getProject().findMember(temp);
			if (member instanceof IContainer && isProjectAssetsFolder((IContainer) member)) {
				return temp;
			}
			temp = temp.removeLastSegments(1);
		}

		return scenePath;
	}

	private static boolean isProjectAssetsFolder(IContainer container) throws CoreException {
		for (IResource member : container.members()) {
			if (member instanceof IFile && GURELLA_PROJECT_FILE_EXTENSION.equals(((IFile) member).getFileExtension())) {
				return true;
			}
		}
		return false;
	}

	private void createItems(TreeItem parentItem, IResource resource) throws CoreException {
		TreeItem nodeItem = parentItem == null ? new TreeItem(tree, 0) : new TreeItem(parentItem, 0);
		nodeItem.setText(resource.getName());
		nodeItem.setData(resource);

		if (resource instanceof IContainer) {
			nodeItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
			for (IResource member : ((IContainer) resource).members()) {
				createItems(nodeItem, member);
			}
		} else {
			IFile file = (IFile) resource;
			if (AssetType.texture.containsExtension(file.getFileExtension())) {
				nodeItem.setImage(GurellaStudioPlugin.createImage("icons/picture.png"));
			} else {
				nodeItem.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
			}
		}
	}

	private static class TextureInspectable implements Inspectable<TextureResource> {
		TextureResource target;

		public TextureInspectable(IFile file) {
			this.target = new TextureResource(file);
		}

		@Override
		public TextureResource getTarget() {
			return target;
		}

		@Override
		public PropertiesContainer<TextureResource> createPropertiesContainer(InspectorView parent,
				TextureResource target) {
			return new TexturePropertiesContainer(parent, target);
		}
	}
}