package com.gurella.engine.scene.movement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gurella.engine.application.events.CommonUpdatePriority;
import com.gurella.engine.application.events.UpdateListener;
import com.gurella.engine.event.Listener0;
import com.gurella.engine.scene.SceneNode;
import com.gurella.engine.scene.SceneSystem;
import com.gurella.engine.scene.manager.ComponentBitsPredicate;
import com.gurella.engine.scene.manager.NodeManager;
import com.gurella.engine.scene.manager.NodeManager.SceneNodeFamily;
import com.gurella.engine.utils.ImmutableArray;

//TODO attach listeners
public class LinearVelocityProcessor extends SceneSystem implements UpdateListener {
	private static final SceneNodeFamily family = new SceneNodeFamily(
			ComponentBitsPredicate.all(true, TransformComponent.class, LinearVelocityComponent.class).build());

	private NodeManager nodeManager;
	private Vector3 tempTranslate = new Vector3();
	private Vector3 tempVelocity = new Vector3();

	public LinearVelocityProcessor() {
		activatedSignal.addListener(new ActivateListener());
		deactivatedSignal.addListener(new DeactivateListener());
	}

	@Override
	protected void activated() {
		nodeManager = getScene().nodeManager;
		nodeManager.registerFamily(family);
	}

	@Override
	protected void deactivated() {
		nodeManager.unregisterFamily(family);
		nodeManager = null;
	}

	@Override
	public void update() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		ImmutableArray<SceneNode> nodes = nodeManager.getNodes(family);
		for (int i = 0; i < nodes.size(); i++) {
			SceneNode node = nodes.get(i);
			LinearVelocityComponent linearVelocityComponent = node.getComponent(LinearVelocityComponent.class);
			node.getComponent(TransformComponent.class).getTranslation(tempTranslate);

			if (linearVelocityComponent.lastPosition.x == Float.NaN) {
				tempVelocity.setZero();
			} else {
				tempVelocity.set(tempTranslate).sub(linearVelocityComponent.lastPosition);
			}

			linearVelocityComponent.velocity.set(tempVelocity).scl(1.0f / deltaTime);
			linearVelocityComponent.lastPosition.set(tempTranslate);
		}
	}

	@Override
	public int getPriority() {
		return CommonUpdatePriority.THINK;
	}

	private class ActivateListener implements Listener0 {
		@Override
		public void handle() {

		}
	}

	private class DeactivateListener implements Listener0 {
		@Override
		public void handle() {
			nodeManager = null;
		}
	}
}
