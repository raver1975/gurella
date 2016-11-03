package com.gurella.studio.editor.tool;

import static com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse;
import static com.gurella.studio.editor.tool.HandleType.x;
import static com.gurella.studio.editor.tool.HandleType.y;
import static com.gurella.studio.editor.tool.HandleType.z;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.gurella.engine.graphics.render.GenericBatch;
import com.gurella.engine.scene.transform.TransformComponent;

public class RotateTool extends TransformTool {
	private RotateHandle xHandle;
	private RotateHandle yHandle;
	private RotateHandle zHandle;

	private Matrix4 shapeRenderMat = new Matrix4();

	private Vector3 temp0 = new Vector3();
	private Vector3 temp1 = new Vector3();
	private Quaternion tempQuat = new Quaternion();

	private ShapeRenderer shapeRenderer;

	private float lastRot = 0;

	public RotateTool() {
		this.shapeRenderer = new ShapeRenderer();
		xHandle = new RotateHandle(x, COLOR_X, torus(COLOR_X, 20, 1, 50, 50));
		yHandle = new RotateHandle(y, COLOR_Y, torus(COLOR_Y, 20, 1, 50, 50));
		zHandle = new RotateHandle(z, COLOR_Z, torus(COLOR_Z, 20, 1, 50, 50));
		handles = new RotateHandle[] { xHandle, yHandle, zHandle };
	}

	@Override
	ToolType getType() {
		return ToolType.rotate;
	}

	@Override
	public void render(Vector3 translation, Camera camera, GenericBatch batch) {
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

		if (state == HandleType.idle) {
			batch.begin(camera);
			xHandle.render(batch);
			yHandle.render(batch);
			zHandle.render(batch);
			batch.end();
		} else {
			temp0.set(translation);
			Vector3 pivot = camera.project(temp0);
			Graphics graphics = Gdx.graphics;

			shapeRenderMat.setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
			switch (state) {
			case x:
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.setProjectionMatrix(shapeRenderMat);
				shapeRenderer.rectLine(pivot.x, pivot.y, Gdx.input.getX(), graphics.getHeight() - Gdx.input.getY(), 2);
				shapeRenderer.setColor(COLOR_X);
				shapeRenderer.circle(pivot.x, pivot.y, 7);
				shapeRenderer.end();
				break;
			case y:
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.setProjectionMatrix(shapeRenderMat);
				shapeRenderer.rectLine(pivot.x, pivot.y, Gdx.input.getX(), graphics.getHeight() - Gdx.input.getY(), 2);
				shapeRenderer.setColor(COLOR_Y);
				shapeRenderer.circle(pivot.x, pivot.y, 7);
				shapeRenderer.end();
				break;
			case z:
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.setProjectionMatrix(shapeRenderMat);
				shapeRenderer.rectLine(pivot.x, pivot.y, Gdx.input.getX(), graphics.getHeight() - Gdx.input.getY(), 2);
				shapeRenderer.setColor(COLOR_Z);
				shapeRenderer.circle(pivot.x, pivot.y, 7);
				shapeRenderer.end();
				break;
			default:
				break;
			}
		}
	}

	@Override
	void update(Vector3 translation, Camera camera) {
		scaleHandles(translation, camera);
		rotateHandles();
		translateHandles(translation);
	}

	protected void rotateHandles() {
		xHandle.rotationEuler.set(0, 90, 0);
		xHandle.applyTransform();
		yHandle.rotationEuler.set(90, 0, 0);
		yHandle.applyTransform();
		zHandle.rotationEuler.set(0, 0, 0);
		zHandle.applyTransform();
	}

	protected void translateHandles(Vector3 translation) {
		xHandle.position.set(translation);
		xHandle.applyTransform();
		yHandle.position.set(translation);
		yHandle.applyTransform();
		zHandle.position.set(translation);
		zHandle.applyTransform();
	}

	protected void scaleHandles(Vector3 translation, Camera camera) {
		float scaleFactor = camera.position.dst(translation) * 0.005f;

		xHandle.scale.set(scaleFactor, scaleFactor, scaleFactor);
		xHandle.applyTransform();

		yHandle.scale.set(scaleFactor, scaleFactor, scaleFactor);
		yHandle.applyTransform();

		zHandle.scale.set(scaleFactor, scaleFactor, scaleFactor);
		zHandle.applyTransform();
	}

	private static Model torus(Color color, float width, float height, int divisionsU, int divisionsV) {
		Material mat = new Material(createDiffuse(color));
		MeshPartBuilder.VertexInfo v0 = new MeshPartBuilder.VertexInfo();
		MeshPartBuilder.VertexInfo v1 = new MeshPartBuilder.VertexInfo();

		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("torus", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, mat);

		MeshPartBuilder.VertexInfo curr1 = v0.set(null, null, null, null);
		curr1.hasUV = false;
		curr1.hasNormal = true;
		curr1.hasPosition = true;

		MeshPartBuilder.VertexInfo curr2 = v1.set(null, null, null, null);
		curr2.hasUV = false;
		curr2.hasNormal = true;
		curr2.hasPosition = true;
		short i1, i2, i3 = 0, i4 = 0;

		int i, j, k;
		double s, t, twopi;
		twopi = 2 * Math.PI;

		for (i = 0; i < divisionsV; i++) {
			for (j = 0; j <= divisionsU; j++) {
				for (k = 1; k >= 0; k--) {
					s = (i + k) % divisionsV + 0.5;
					t = j % divisionsU;

					float u = j / (divisionsU - 1.0f);
					float v = i / (divisionsV - 1.0f);
					float theta = u * 2.0f * MathUtils.PI;
					float rho = v * 2.0f * MathUtils.PI;

					curr1.position.set(
							(float) ((width + height * Math.cos(s * twopi / divisionsV))
									* Math.cos(t * twopi / divisionsU)),
							(float) ((width + height * Math.cos(s * twopi / divisionsV))
									* Math.sin(t * twopi / divisionsU)),
							(float) (height * Math.sin(s * twopi / divisionsV)));

					float nx = MathUtils.cos(theta)*MathUtils.cos(rho);
					float ny = MathUtils.sin(theta)*MathUtils.cos(rho);
					float nz = MathUtils.sin(rho);
					curr1.normal.set(nx, ny, nz);
					

					k--;
					s = (i + k) % divisionsV + 0.5;
					curr2.position.set(
							(float) ((width + height * Math.cos(s * twopi / divisionsV))
									* Math.cos(t * twopi / divisionsU)),
							(float) ((width + height * Math.cos(s * twopi / divisionsV))
									* Math.sin(t * twopi / divisionsU)),
							(float) (height * Math.sin(s * twopi / divisionsV)));
					curr2.normal.set(nx, ny, nz);
					
					i1 = builder.vertex(curr1);
					i2 = builder.vertex(curr2);
					builder.rect(i4, i2, i1, i3);
					i4 = i2;
					i3 = i1;
				}
			}
		}

		return modelBuilder.end();
	}

	@Override
	void touchDragged(TransformComponent component, Vector3 translation, Camera camera, ToolHandle active, int screenX,
			int screenY) {
		translateHandles(translation);

		float angle = getCurrentAngle(translation, camera);
		float rot = angle - lastRot;

		boolean modified = false;
		switch (state) {
		case x:
			tempQuat.setEulerAngles(0, -rot, 0);
			component.rotate(tempQuat);
			modified = true;
			break;
		case y:
			tempQuat.setEulerAngles(-rot, 0, 0);
			component.rotate(tempQuat);
			modified = true;
			break;
		case z:
			tempQuat.setEulerAngles(0, 0, -rot);
			component.rotate(tempQuat);
			modified = true;
			break;
		default:
			break;
		}

		if (modified) {
			// gameObjectModifiedEvent.setGameObject(projectContext.currScene.currentSelection);
			// Mundus.INSTANCE.postEvent(gameObjectModifiedEvent);
		}

		lastRot = angle;
	}

	private float getCurrentAngle(Vector3 translation, Camera camera) {
		temp0.set(translation);
		Vector3 pivot = camera.project(temp0);
		Vector3 mouse = temp1.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), 0);
		return angle(pivot.x, pivot.y, mouse.x, mouse.y);
	}

	private static float angle(float x1, float y1, float x2, float y2) {
		return (float) Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
	}

	@Override
	public void dispose() {
		xHandle.dispose();
		yHandle.dispose();
		zHandle.dispose();
	}
}
