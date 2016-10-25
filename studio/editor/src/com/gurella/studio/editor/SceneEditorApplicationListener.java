package com.gurella.studio.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.InputEventQueue;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.gurella.engine.application.GurellaStateProvider;
import com.gurella.engine.disposable.DisposablesService;
import com.gurella.engine.event.Event;
import com.gurella.engine.event.EventService;
import com.gurella.engine.graphics.render.GenericBatch;
import com.gurella.engine.input.InputService;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.scene.SceneNode2;
import com.gurella.engine.scene.SceneNodeComponent2;
import com.gurella.engine.scene.debug.DebugRenderable.DebugRenderContext;
import com.gurella.engine.scene.renderable.RenderableComponent;
import com.gurella.engine.scene.spatial.Spatial;
import com.gurella.engine.subscriptions.application.ApplicationDebugUpdateListener;
import com.gurella.engine.subscriptions.application.ApplicationShutdownListener;
import com.gurella.studio.GurellaStudioPlugin;
import com.gurella.studio.editor.common.Compass;
import com.gurella.studio.editor.common.bean.BeanEditor;
import com.gurella.studio.editor.common.bean.BeanEditorContext;
import com.gurella.studio.editor.inspector.component.ComponentInspectable;
import com.gurella.studio.editor.inspector.node.NodeInspectable;
import com.gurella.studio.editor.render.GridModelInstance;
import com.gurella.studio.editor.render.SceneCameraInputController;
import com.gurella.studio.editor.render.SceneEditorRenderSystem;
import com.gurella.studio.editor.subscription.EditorContextMenuContributor;
import com.gurella.studio.editor.subscription.SceneEditorMouseListener;
import com.gurella.studio.editor.subscription.SceneLoadedListener;
import com.gurella.studio.editor.subscription.SelectionListener;

final class SceneEditorApplicationListener extends ApplicationAdapter
		implements GurellaStateProvider, SceneEditorMouseListener, SceneLoadedListener, SelectionListener {
	private static final DebugUpdateEvent debugUpdateEvent = new DebugUpdateEvent();

	private final int editorId;

	private Thread renderThread;

	private final InputEventQueue inputQueue = new InputEventQueue();

	private PerspectiveCamera perspectiveCamera;
	private SceneCameraInputController perspectiveCameraController;

	private OrthographicCamera orthographicCamera;
	private SceneCameraInputController orthographicCameraController;

	private Camera camera;
	private SceneCameraInputController inputController;

	private GenericBatch batch;
	private DebugRenderContext renderContext = new DebugRenderContext();
	private Environment environment;
	private Color backgroundColor = new Color(0.501960f, 0.501960f, 0.501960f, 1f);

	// TODO replace batches with generic
	private ModelBatch modelBatch;
	private ShapeRenderer shapeRenderer;

	private GridModelInstance gridModelInstance;
	private Compass compass;

	private Matrix4 infoProjection;
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private StringBuffer info = new StringBuffer();

	private Scene scene;
	private SceneEditorRenderSystem renderSystem;

	private final Ray pickRay = new Ray();
	private final Vector3 intersection = new Vector3();

	private SceneNode2 focusedNode;
	private SceneNodeComponent2 focusedComponent;
	private Control lastFocusControl;
	private boolean focusDataFromInspectable;

	private final Array<Spatial> spatials = new Array<>(64);

	public SceneEditorApplicationListener(int editorId) {
		this.editorId = editorId;
		EventService.subscribe(editorId, this);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void create() {
		renderThread = Thread.currentThread();
		renderSystem = new SceneEditorRenderSystem(editorId);

		Graphics graphics = Gdx.graphics;
		perspectiveCamera = new PerspectiveCamera(67, graphics.getWidth(), graphics.getHeight());
		perspectiveCamera.position.set(0f, 0f, 3f);
		perspectiveCamera.lookAt(0, 0, 0);
		perspectiveCamera.near = 0.1f;
		perspectiveCamera.far = 10000;
		perspectiveCamera.update();
		perspectiveCameraController = new SceneCameraInputController(perspectiveCamera, editorId);

		orthographicCamera = new OrthographicCamera(graphics.getWidth(), graphics.getHeight());
		orthographicCamera.far = 10000;
		orthographicCamera.update();
		orthographicCameraController = new SceneCameraInputController(orthographicCamera, editorId);

		camera = perspectiveCamera;
		inputController = perspectiveCameraController;

		batch = new GenericBatch();

		modelBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
		environment.set(new DepthTestAttribute());
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		gridModelInstance = new GridModelInstance();
		compass = new Compass(perspectiveCamera);
		shapeRenderer = new ShapeRenderer();

		infoProjection = new Matrix4().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
		spriteBatch = new SpriteBatch();
		spriteBatch.enableBlending();
		font = new BitmapFont();
		font.setColor(Color.WHITE);

		inputQueue.setProcessor(inputController);
		InputService.addInputProcessor(inputQueue);

		DefaultShader.defaultCullFace = 0;
	}

	@Override
	public void sceneLoaded(Scene scene) {
		this.scene = scene;
		debugUpdate();
	}

	@Override
	public void resize(int width, int height) {
		perspectiveCamera.viewportWidth = width;
		perspectiveCamera.viewportHeight = height;
		perspectiveCamera.update();
		infoProjection.setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void render() {
		debugUpdate();
		inputQueue.drain();
		renderScene();
	}

	static void debugUpdate() {
		EventService.post(debugUpdateEvent);
	}

	public void renderScene() {
		synchronized (GurellaStudioPlugin.glMutex) {
			inputController.update();
			Color color = backgroundColor;
			Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
			Gdx.gl.glClearDepthf(1);
			Gdx.gl.glClearStencil(0);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			modelBatch.begin(camera);
			modelBatch.render(gridModelInstance, environment);
			compass.render(modelBatch);
			modelBatch.end();

			updateFocusData();
			renderContext.batch = batch;
			renderContext.camera = camera;
			renderContext.focusedNode = focusedNode;
			renderContext.focusedComponent = focusedComponent;
			renderSystem.renderScene(renderContext);

			renderPickRay();
			renderInfo();
		}
	}

	private void renderInfo() {
		spriteBatch.setProjectionMatrix(infoProjection);
		spriteBatch.begin();

		Vector3 position = camera.position;
		info.append("X: ");
		info.append(position.x);
		info.append(" Y: ");
		info.append(position.y);
		info.append(" Z: ");
		info.append(position.z);
		font.draw(spriteBatch, info.toString(), 15, 20);
		info.setLength(0);

		spriteBatch.end();
	}

	private void renderPickRay() {
		shapeRenderer.setAutoShapeType(true);
		shapeRenderer.begin();
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.YELLOW);
		shapeRenderer.set(ShapeType.Line);
		shapeRenderer.line(pickRay.origin, new Vector3(pickRay.direction).scl(10).add(pickRay.origin));
		shapeRenderer.end();
	}

	@Override
	public boolean isInRenderThread() {
		return renderThread == Thread.currentThread();
	}

	private void updateFocusData() {
		Display current = Display.getCurrent();
		if (current == null) {
			return;
		}

		Control focusControl = current.getFocusControl();
		if (focusControl == lastFocusControl && focusDataFromInspectable) {
			return;
		}

		lastFocusControl = focusControl;
		focusDataFromInspectable = false;

		if (focusControl == null) {
			return;
		}

		Composite temp = focusControl instanceof Composite ? (Composite) focusControl : focusControl.getParent();
		while (temp != null) {
			if (temp instanceof BeanEditor) {
				BeanEditorContext<?> context = ((BeanEditor<?>) temp).getContext();
				Object modelInstance = context.modelInstance;
				if (modelInstance instanceof SceneNodeComponent2) {
					focusedComponent = (SceneNodeComponent2) modelInstance;
					focusedNode = focusedComponent == null ? null : focusedComponent.getNode();
					return;
				} else if (modelInstance instanceof SceneNode2) {
					focusedComponent = null;
					focusedNode = (SceneNode2) modelInstance;
					return;
				}
			}
			temp = temp.getParent();
		}
	}

	@Override
	public void onMouseMenu(float x, float y) {
		ContextMenuActions actions = new ContextMenuActions();
		SceneEditor.post(editorId, EditorContextMenuContributor.class, c -> c.contribute(actions));
		actions.showMenu();
	}

	@Override
	public void onMouseSelection(float x, float y) {
		if (scene == null) {
			return;
		}

		camera.update(true);
		pickRay.set(camera.getPickRay(x, y));
		scene.spatialSystem.getSpatials(pickRay, spatials, null);
		if (spatials.size == 0) {
			return;
		}

		Vector3 cameraPosition = camera.position;
		Spatial closestSpatial = null;
		float closestDistance = Float.MAX_VALUE;

		for (int i = 0; i < spatials.size; i++) {
			Spatial spatial = spatials.get(i);
			RenderableComponent renderableComponent = spatial.renderableComponent;
			if (renderableComponent.getIntersection(pickRay, intersection)) {
				float distance = intersection.dst2(cameraPosition);
				if (closestDistance > distance) {
					closestDistance = distance;
					closestSpatial = spatial;
					// TODO Z order of sprites
				}
			}
		}

		spatials.clear();
		if (closestSpatial != null) {
			focusDataFromInspectable = false;
			focusedNode = closestSpatial.renderableComponent.getNode();
		}
	}

	@Override
	public void selectionChanged(Object selection) {
		if (selection instanceof NodeInspectable) {
			focusedNode = ((NodeInspectable) selection).target;
			focusedComponent = null;
			focusDataFromInspectable = true;
		} else if (selection instanceof ComponentInspectable) {
			focusedComponent = ((ComponentInspectable) selection).target;
			focusedNode = focusedComponent == null ? null : focusedComponent.getNode();
			focusDataFromInspectable = true;
		} else {
			focusedComponent = null;
			focusedNode = null;
			focusDataFromInspectable = true;
		}
	}

	Camera getCamera() {
		return camera;
	}

	boolean is2d() {
		return camera == orthographicCamera;
	}

	void set2d() {
		camera = orthographicCamera;
		inputController = orthographicCameraController;
		inputQueue.setProcessor(inputController);
	}

	boolean is3d() {
		return camera == perspectiveCamera;
	}

	void set3d() {
		camera = perspectiveCamera;
		inputController = perspectiveCameraController;
		inputQueue.setProcessor(inputController);
	}

	@Override
	public void dispose() {
		SceneEditor.post(ApplicationShutdownListener.class, l -> l.shutdown());
		debugUpdate();
		EventService.unsubscribe(editorId, this);
		batch.dispose();
		modelBatch.dispose();
		spriteBatch.dispose();
		font.dispose();
		gridModelInstance.dispose();
		compass.dispose();
		//TODO DisposablesService.disposeAll();
	}

	private static class DebugUpdateEvent implements Event<ApplicationDebugUpdateListener> {
		@Override
		public Class<ApplicationDebugUpdateListener> getSubscriptionType() {
			return ApplicationDebugUpdateListener.class;
		}

		@Override
		public void dispatch(ApplicationDebugUpdateListener listener) {
			listener.debugUpdate();
		}
	}
}