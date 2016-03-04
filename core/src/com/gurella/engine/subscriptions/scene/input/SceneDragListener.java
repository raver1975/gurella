package com.gurella.engine.subscriptions.scene.input;

import com.gurella.engine.scene.input.TouchEvent;
import com.gurella.engine.subscriptions.scene.SceneEventSubscription;

public interface SceneDragListener extends SceneEventSubscription {
	void touchDragged(TouchEvent touchEvent);
}