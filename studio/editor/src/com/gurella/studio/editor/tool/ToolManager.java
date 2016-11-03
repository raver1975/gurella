package com.gurella.studio.editor.tool;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gurella.engine.event.EventService;
import com.gurella.engine.graphics.render.GenericBatch;
import com.gurella.engine.math.ModelIntesector;
import com.gurella.engine.plugin.Plugin;
import com.gurella.engine.plugin.Workbench;
import com.gurella.engine.scene.SceneNode2;
import com.gurella.engine.scene.transform.TransformComponent;
import com.gurella.studio.editor.subscription.EditorActiveCameraProvider;
import com.gurella.studio.editor.subscription.EditorCameraSelectionListener;
import com.gurella.studio.editor.subscription.EditorFocusListener;
import com.gurella.studio.editor.subscription.EditorPreCloseListener;
import com.gurella.studio.editor.subscription.ToolSelectionListener;

public class ToolManager extends InputAdapter
		implements EditorPreCloseListener, EditorFocusListener, EditorCameraSelectionListener, Plugin {
	private final int editorId;

	@SuppressWarnings("unused")
	private ToolMenuContributor menuContributor;

	private final ScaleTool scaleTool = new ScaleTool();
	private final TranslateTool translateTool = new TranslateTool();
	private final RotateTool rotateTool = new RotateTool();
	private final Environment environment;

	private Camera camera;
	private TransformComponent transformComponent;

	private final Vector3 translation = new Vector3();
	private final Vector3 cameraPosition = new Vector3();
	private final Vector3 intersection = new Vector3();
	private final ModelIntesector intesector = new ModelIntesector();

	private TransformTool selected;
	private ToolHandle mouseOver;
	private ToolHandle active;

	public ToolManager(int editorId) {
		this.editorId = editorId;

		menuContributor = new ToolMenuContributor(editorId, this);

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.65f, 0.65f, 0.65f, 1f));
		environment.set(new DepthTestAttribute());
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		EventService.subscribe(editorId, this);
		Workbench.activate(this);
		EventService.post(editorId, EditorActiveCameraProvider.class, l -> camera = l.getActiveCamera());
	}

	@Override
	public void focusChanged(EditorFocusData focusData) {
		SceneNode2 node = focusData.focusedNode;
		transformComponent = node == null ? null : node.getComponent(TransformComponent.class);
	}

	@Override
	public void cameraChanged(Camera camera) {
		this.camera = camera;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.S) {
			selectTool(scaleTool);
			return true;
		} else if (keycode == Keys.T) {
			selectTool(translateTool);
			return true;
		} else if (keycode == Keys.R) {
			selectTool(rotateTool);
			return true;
		} else if (keycode == Keys.ESCAPE || keycode == Keys.N) {
			selectTool((TransformTool) null);
			return true;
		} else {
			return false;
		}
	}

	void selectTool(ToolType type) {
		switch (type) {
		case none:
			selectTool((TransformTool) null);
			break;
		case rotate:
			selectTool(rotateTool);
			break;
		case translate:
			selectTool(translateTool);
			break;
		case scale:
			selectTool(scaleTool);
			break;
		default:
			selectTool((TransformTool) null);
			break;
		}
	}

	ToolType getSelectedToolType() {
		return selected == null ? ToolType.none : selected.getType();
	}

	private void selectTool(TransformTool newSelection) {
		selected = newSelection;
		ToolType type = selected == null ? ToolType.none : selected.getType();
		EventService.post(editorId, ToolSelectionListener.class, l -> l.toolSelected(type));
	}

	public void render(GenericBatch batch) {
		if (selected != null && transformComponent != null) {
			transformComponent.getWorldTranslation(translation);
			batch.setEnvironment(environment);
			selected.update(translation, camera);
			selected.render(translation, camera, batch);
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (pointer == 0 && button == Buttons.LEFT && selected != null) {
			ToolHandle pick = pick(screenX, screenY);
			if (pick != null) {
				active = pick;
				selected.activated(active.type);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (active == null) {
			return false;
		} else {
			selected.deactivated();
			active = null;
			return true;
		}
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (active == null) {
			return false;
		} else {
			transformComponent.getWorldTranslation(translation);
			selected.mouseMoved(translation, camera, active, screenX, screenY);
			return true;
		}
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (selected == null) {
			return false;
		}

		ToolHandle pick = pick(screenX, screenY);

		if (mouseOver != pick) {
			if (mouseOver != null) {
				mouseOver.restoreColor();
			}

			if (pick != null) {
				pick.changeColor(Color.YELLOW);
			}

			mouseOver = pick;
		}
		return false;
	}

	protected ToolHandle pick(int screenX, int screenY) {
		selected.update(translation, camera);
		cameraPosition.set(camera.position);
		Ray pickRay = camera.getPickRay(screenX, screenY);
		ToolHandle[] handles = selected.handles;
		ToolHandle pick = null;
		Vector3 closestIntersection = new Vector3(Float.NaN, Float.NaN, Float.NaN);
		float closestDistance = Float.MAX_VALUE;

		for (ToolHandle toolHandle : handles) {
			ModelInstance instance = toolHandle.modelInstance;
			if (intesector.getIntersection(cameraPosition, pickRay, intersection, instance)) {
				float distance = intersection.dst2(cameraPosition);
				if (closestDistance > distance) {
					closestDistance = distance;
					closestIntersection.set(intersection);
					pick = toolHandle;
				}
			}
		}
		return pick;
	}

	@Override
	public void onEditorPreClose() {
		Workbench.deactivate(this);
		EventService.unsubscribe(editorId, this);
		scaleTool.dispose();
		translateTool.dispose();
		rotateTool.dispose();
	}

	@Override
	public void activate() {
	}

	@Override
	public void deactivate() {
	}
}