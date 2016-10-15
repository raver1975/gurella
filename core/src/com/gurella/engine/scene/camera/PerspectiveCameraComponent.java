package com.gurella.engine.scene.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.gurella.engine.base.model.ModelDescriptor;

@ModelDescriptor(descriptiveName = "Perspective Camera")
public class PerspectiveCameraComponent extends CameraComponent<PerspectiveCamera> {
	/** the field of view in degrees **/
	private float fieldOfView = 67;
	
	public boolean depthTest = true;
	public Color ambientLight;
	public Color fog;
	
	public PerspectiveCameraComponent() {
		near = 0.1f;
	}

	@Override
	PerspectiveCamera createCamera() {
		return new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	void initCamera() {
		super.initCamera();
		camera.fieldOfView = fieldOfView;
	}

	public float getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(float fieldOfView) {
		this.fieldOfView = fieldOfView;
		camera.fieldOfView = fieldOfView;
	}

	@Override
	public void reset() {
		super.reset();
		fieldOfView = 67;
	}
}
