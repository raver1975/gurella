package com.gurella.studio.editor.scene.event;

import com.gurella.engine.event.Event;
import com.gurella.engine.scene.SceneNode2;
import com.gurella.engine.scene.SceneNodeComponent2;
import com.gurella.studio.editor.subscription.EditorSceneListener;

public class ComponentAddedEvent implements Event<EditorSceneListener> {
	final SceneNode2 node;
	final SceneNodeComponent2 component;

	public ComponentAddedEvent(SceneNode2 node, SceneNodeComponent2 component) {
		this.node = node;
		this.component = component;
	}

	@Override
	public Class<EditorSceneListener> getSubscriptionType() {
		return EditorSceneListener.class;
	}

	@Override
	public void dispatch(EditorSceneListener subscriber) {
		subscriber.componentAdded(node, component);
	}
}
