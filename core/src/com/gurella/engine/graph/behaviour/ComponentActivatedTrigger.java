package com.gurella.engine.graph.behaviour;

import static com.gurella.engine.graph.behaviour.BehaviourEvents.componentActivated;

import com.gurella.engine.graph.SceneNode;
import com.gurella.engine.graph.SceneNodeComponent;
import com.gurella.engine.graph.event.EventTrigger;
import com.gurella.engine.signal.Listener1;

public class ComponentActivatedTrigger extends EventTrigger implements Listener1<SceneNodeComponent> {
	@Override
	protected void activated() {
		eventSystem.getGraph().componentActivatedSignal.addListener(this);
	}

	@Override
	protected void deactivated() {
		eventSystem.getGraph().componentActivatedSignal.removeListener(this);
	}

	@Override
	public void handle(SceneNodeComponent component) {
		SceneNode node = component.getNode();
		for (BehaviourComponent behaviourComponent : eventSystem.getListeners(componentActivated)) {
			behaviourComponent.componentActivated(node, component);
		}
	}
}
