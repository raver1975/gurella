package com.gurella.engine.graph.behaviour.trigger;

import static com.gurella.engine.graph.behaviour.BehaviourEvents.onCleanup;

import com.gurella.engine.application.CommonUpdateOrder;
import com.gurella.engine.application.UpdateEvent;
import com.gurella.engine.application.UpdateListener;
import com.gurella.engine.event.EventService;
import com.gurella.engine.graph.behaviour.BehaviourComponent;
import com.gurella.engine.graph.event.EventTrigger;

public class CleanupUpdateTrigger extends EventTrigger implements UpdateListener {
	@Override
	protected void activated() {
		EventService.addListener(UpdateEvent.class, this);
	}

	@Override
	protected void deactivated() {
		EventService.removeListener(UpdateEvent.class, this);
	}

	@Override
	public int getOrdinal() {
		return CommonUpdateOrder.CLEANUP;
	}

	@Override
	public void update() {
		for (BehaviourComponent behaviourComponent : eventManager.getListeners(onCleanup)) {
			behaviourComponent.onInput();
		}
	}
}
