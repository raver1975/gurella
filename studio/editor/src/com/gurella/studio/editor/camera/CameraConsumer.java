package com.gurella.studio.editor.camera;

import com.badlogic.gdx.graphics.Camera;
import com.gurella.engine.utils.plugin.Plugin;

public interface CameraConsumer extends Plugin {
	void setCamera(Camera camera);
}
