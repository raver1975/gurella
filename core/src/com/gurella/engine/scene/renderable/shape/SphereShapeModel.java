package com.gurella.engine.scene.renderable.shape;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;

public class SphereShapeModel extends ShapeModel {
	private float width = 1;
	private float height = 1;
	private float depth = 1;
	private int divisionsU = 10;
	private int divisionsV = 10;
	private float angleUFrom = 0;
	private float angleUTo = 360;
	private float angleVFrom = 0;
	private float angleVTo = 180;

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
		dirty = true;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
		dirty = true;
	}

	public float getDepth() {
		return depth;
	}

	public void setDepth(float depth) {
		this.depth = depth;
		dirty = true;
	}

	public int getDivisionsU() {
		return divisionsU;
	}

	public void setDivisionsU(int divisionsU) {
		this.divisionsU = divisionsU;
		dirty = true;
	}

	public int getDivisionsV() {
		return divisionsV;
	}

	public void setDivisionsV(int divisionsV) {
		this.divisionsV = divisionsV;
		dirty = true;
	}

	public float getAngleUFrom() {
		return angleUFrom;
	}

	public void setAngleUFrom(float angleUFrom) {
		this.angleUFrom = angleUFrom;
		dirty = true;
	}

	public float getAngleUTo() {
		return angleUTo;
	}

	public void setAngleUTo(float angleUTo) {
		this.angleUTo = angleUTo;
		dirty = true;
	}

	public float getAngleVFrom() {
		return angleVFrom;
	}

	public void setAngleVFrom(float angleVFrom) {
		this.angleVFrom = angleVFrom;
		dirty = true;
	}

	public float getAngleVTo() {
		return angleVTo;
	}

	public void setAngleVTo(float angleVTo) {
		this.angleVTo = angleVTo;
		dirty = true;
	}

	public void set(float width, float height, float depth) {
		this.width = width;
		this.height = height;
		this.depth = depth;
		dirty = true;
	}

	@Override
	protected void buildParts(ModelBuilder builder, Matrix4 parentTransform) {
		MeshPartBuilder part = builder.part("sphere", getGlPrimitiveType(), getVertexAttributes(), getMaterial());
		part.setVertexTransform(parentTransform);
		part.sphere(width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
	}
}
