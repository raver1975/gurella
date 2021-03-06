package com.gurella.studio.editor;

import static com.gurella.studio.GurellaStudioPlugin.showError;
import static com.gurella.studio.common.AssetsFolderLocator.getAssetsFolder;
import static com.gurella.studio.common.AssetsFolderLocator.getAssetsRelativePath;

import java.util.function.Consumer;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.gurella.engine.asset.AssetId;
import com.gurella.engine.asset.AssetService;
import com.gurella.engine.async.AsyncCallbackAdapter;
import com.gurella.engine.event.Event;
import com.gurella.engine.event.EventService;
import com.gurella.engine.event.EventSubscription;
import com.gurella.engine.metatype.serialization.json.JsonOutput;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.utils.Sequence;
import com.gurella.engine.utils.plugin.Workbench;
import com.gurella.studio.GurellaStudioPlugin;
import com.gurella.studio.editor.control.Dock;
import com.gurella.studio.editor.control.ViewRegistry;
import com.gurella.studio.editor.dnd.DndAssetPlacementManager;
import com.gurella.studio.editor.history.HistoryManager;
import com.gurella.studio.editor.launch.LaunchManager;
import com.gurella.studio.editor.preferences.PreferencesManager;
import com.gurella.studio.editor.subscription.EditorCloseListener;
import com.gurella.studio.editor.subscription.SceneDirtyListener;
import com.gurella.studio.editor.swtgdx.SwtLwjglApplication;
import com.gurella.studio.editor.ui.ErrorComposite;
import com.gurella.studio.editor.utils.Try;
import com.gurella.studio.editor.utils.UiUtils;
import com.gurella.studio.gdx.GdxContext;

public class SceneEditor extends EditorPart implements SceneDirtyListener, EditorCloseListener {
	public final int id = Sequence.next();
	private final Workbench workbench = Workbench.newInstance(id);

	private Composite content;
	private Dock dock;

	SceneEditorContext sceneContext;
	ViewRegistry viewRegistry;
	private SceneProvider sceneProvider;
	private DndAssetPlacementManager dndAssetPlacementManager;
	private HistoryManager historyManager;
	private LaunchManager launchManager;
	private PreferencesManager preferencesManager;

	private SwtLwjglApplication application;

	private boolean dirty;

	@Override
	public void doSave(IProgressMonitor monitor) {
		GdxContext.run(id, () -> Try.run(() -> save(monitor), e -> showError(e, "Error saving scene")));
	}

	private void save(IProgressMonitor monitor) throws CoreException {
		Scene scene = sceneContext.getScene();
		if (scene == null) {
			return;
		}

		IFileEditorInput input = (IFileEditorInput) getEditorInput();
		IFile file = input.getFile();
		IPath path = file.getFullPath();

		monitor.beginTask("Saving scene", 2000);
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		manager.connect(path, LocationKind.IFILE, monitor);

		JsonOutput output = new JsonOutput();
		String relativeFileName = getAssetsRelativePath(file).toString();
		AssetId assetId = new AssetId().set(relativeFileName, FileType.Internal, Scene.class);
		String serialized = output.serialize(assetId, Scene.class, scene);
		String pretty = new JsonReader().parse(serialized).prettyPrint(OutputType.minimal, 120);

		ITextFileBuffer buffer = ITextFileBufferManager.DEFAULT.getTextFileBuffer(path, LocationKind.IFILE);
		buffer.getDocument().set(pretty);
		buffer.commit(monitor, true);

		sceneContext.persist(monitor);
		manager.disconnect(path, LocationKind.IFILE, monitor);

		dirty = false;
		firePropertyChange(PROP_DIRTY);

		monitor.done();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);

		IPathEditorInput pathEditorInput = (IPathEditorInput) input;
		String[] segments = pathEditorInput.getPath().segments();
		setPartName(segments[segments.length - 1]);
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void createPartControl(Composite parent) {
		this.content = parent;
		parent.setLayout(new GridLayout());

		dock = new Dock(parent, id);
		dock.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		IResource resource = getEditorInput().getAdapter(IResource.class);
		String internalAssetsPath = getAssetsFolder(resource).getLocation().toString();
		application = new SwtLwjglApplication(id, dock.getCenter(), internalAssetsPath);
		SceneEditorRegistry.put(this, parent, application, JavaCore.create(resource.getProject()));

		GdxContext.run(id, this::initGdxData);
	}

	private void initGdxData() {
		application.init();
		sceneProvider = new SceneProvider(id);
		preferencesManager = new PreferencesManager(this);
		historyManager = new HistoryManager(this);
		sceneContext = new SceneEditorContext(this);
		launchManager = new LaunchManager(sceneContext);
		viewRegistry = new ViewRegistry(sceneContext, dock);

		// TODO create canvas in editor and pass it to consumers
		GLCanvas glCanvas = application.getGraphics().getGlCanvas();
		dndAssetPlacementManager = new DndAssetPlacementManager(id, glCanvas);
		EventService.subscribe(id, this);

		String path = getAssetsRelativePath(getEditorInput().getAdapter(IResource.class)).toString();
		AssetService.loadAsync(new LoadSceneCallback(), path, Scene.class, 0);
	}

	private void presentException(Throwable exception) {
		UiUtils.disposeChildren(content);
		String message = "Error opening scene";
		IStatus status = GurellaStudioPlugin.log(exception, message);
		ErrorComposite errorComposite = new ErrorComposite(content, status, message);
		errorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.layout(true, true);
	}

	@Override
	public void setFocus() {
		dock.setFocus();
	}

	@Override
	public void onEditorClose() {
		GdxContext.unsubscribe(id, id, this);
	}

	@Override
	public void sceneDirty() {
		dirty = true;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public void dispose() {
		super.dispose();
		Workbench.close(workbench);
		SceneEditorRegistry.remove(this);
	}

	public SceneEditorContext getSceneContext() {
		return sceneContext;
	}

	public static int getEditorId(Control control) {
		return SceneEditorRegistry.getEditorId(control);
	}

	public static int getCurrentEditorId() {
		return SceneEditorRegistry.getCurrentEditorId();
	}

	public static SceneEditor getCurrentEditor() {
		return SceneEditorRegistry.getCurrentEditor();
	}

	public static void subscribeToCurrentEditor(EventSubscription subscriber) {
		int editorId = getCurrentEditorId();
		GdxContext.subscribe(editorId, editorId, subscriber);
	}

	public static <T extends Control & EventSubscription> void subscribeToControlEditor(T subscriber) {
		int editorId = getEditorId(subscriber);
		GdxContext.subscribe(editorId, editorId, subscriber);
	}

	public static void unsubscribeFromCurrentEditor(EventSubscription subscriber) {
		int editorId = getCurrentEditorId();
		GdxContext.unsubscribe(editorId, editorId, subscriber);
	}

	public static <T extends Control & EventSubscription> void unsubscribeFromControlEditor(T subscriber) {
		int editorId = getEditorId(subscriber);
		GdxContext.unsubscribe(editorId, editorId, subscriber);
	}

	public static <L extends EventSubscription> void postToControlEditor(Control source, Event<L> event) {
		int editorId = getEditorId(source);
		GdxContext.post(editorId, editorId, event);
	}

	public static <L extends EventSubscription> void postToControlEditor(Control source, Class<L> type,
			Consumer<L> dispatcher) {
		int editorId = getEditorId(source);
		GdxContext.post(editorId, editorId, type, l -> dispatcher.accept(l));
	}

	public static <L extends EventSubscription> void postToCurrentEditor(Event<L> event) {
		int editorId = getCurrentEditorId();
		GdxContext.post(editorId, editorId, event);
	}

	public static <L extends EventSubscription> void postToCurrentEditor(Class<L> type, Consumer<L> dispatcher) {
		int editorId = getCurrentEditorId();
		GdxContext.post(editorId, editorId, type, l -> dispatcher.accept(l));
	}

	// TODO move to SceneProvider
	private final class LoadSceneCallback extends AsyncCallbackAdapter<Scene> {
		private Display display;
		private Label progressLabel;

		public LoadSceneCallback() {
			GLCanvas glCanvas = application.getGraphics().getGlCanvas();
			glCanvas.setLayout(new GridLayout());

			display = glCanvas.getDisplay();

			progressLabel = new Label(glCanvas, SWT.DM_FILL_NONE);
			progressLabel.setBackground(glCanvas.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
			progressLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
			progressLabel.setText("Loading...");
		}

		@Override
		public void onProgress(float progress) {
			asyncExec(() -> updateProgress((int) (progress * 100)));
		}

		private void updateProgress(int progress) {
			if (!progressLabel.isDisposed()) {
				progressLabel.setText("Loading... " + progress);
			}
		}

		@Override
		public void onSuccess(Scene scene) {
			GdxContext.run(id, () -> sceneProvider.setScene(scene));
			asyncExec(() -> progressLabel.dispose());
		}

		@Override
		public void onException(Throwable exception) {
			asyncExec(() -> presentException(exception));
		}

		private void asyncExec(Runnable runnable) {
			display.asyncExec(runnable);
		}
	}
}
