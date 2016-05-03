package com.gurella.studio.editor;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IPathEditorInput;

import com.gurella.engine.application.ApplicationConfig;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.utils.Reflection;
import com.gurella.studio.editor.scene.SceneEditorView;

public class GurellaEditorContext {
	public final IPathEditorInput editorInput;
	public final IResource editorInputResource;
	public final IWorkspace workspace;
	public final IProject project;
	public final IJavaProject javaProject;

	public final URLClassLoader classLoader;

	private EditorMessageSignal signal = new EditorMessageSignal();
	private List<SceneEditorView> registeredViews = new ArrayList<SceneEditorView>();

	private ApplicationConfig applicationConfig;
	private Scene scene;

	private IContainer rootAssetsContainer;

	public GurellaEditorContext(IPathEditorInput editorInput) {
		this.editorInput = editorInput;
		editorInputResource = editorInput.getAdapter(IResource.class);
		workspace = editorInputResource.getWorkspace();
		project = editorInputResource.getProject();
		javaProject = JavaCore.create(project);
		classLoader = DynamicURLClassLoader.newInstance(javaProject);
		Reflection.classResolver = classLoader::loadClass;
	}

	public void addEditorMessageListener(EditorMessageListener listener) {
		signal.addListener(listener);
	}

	public void removeEditorMessageListener(EditorMessageListener listener) {
		signal.removeListener(listener);
	}
	
	public void postMessage(Object source, Object message) {
		signal.dispatch(source, message);
	}
	
	void dispose() {
		signal.clear();
		if (javaProject != null) {
			try {
				javaProject.close();
			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}