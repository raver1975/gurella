package com.gurella.engine.graphics.render.renderable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Renderable2d {
	private Texture texture;
	private Origin origin = Origin.center;
	private Color tint;
	private boolean flipX, flipY;
	private float[] textureCoords;
	private float[] vertices;

	public enum Origin {
		center, bottomLeft, topLeft;
	}
}
