package com.gurella.engine.scene.velocity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.gurella.engine.scene.SceneNode;
import com.gurella.engine.scene.SceneSystem;
import com.gurella.engine.scene.manager.ComponentBitsPredicate;
import com.gurella.engine.scene.manager.NodeManager;
import com.gurella.engine.scene.manager.NodeManager.SceneNodeFamily;
import com.gurella.engine.scene.transform.TransformComponent;
import com.gurella.engine.subscriptions.scene.update.PreRenderUpdateListener;
import com.gurella.engine.utils.ImmutableArray;

public class LinearVelocitySystem extends SceneSystem implements PreRenderUpdateListener {
	@SuppressWarnings("unchecked")
	private static final SceneNodeFamily family = new SceneNodeFamily(
			ComponentBitsPredicate.all(TransformComponent.class, LinearVelocityComponent.class).build());

	private NodeManager nodeManager;
	private Vector3 tempTranslate = new Vector3();
	private Vector3 tempVelocity = new Vector3();

	@Override
	protected void systemActivated() {
		nodeManager = getScene().nodeManager;
		nodeManager.registerFamily(family);
	}

	@Override
	protected void systemDeactivated() {
		nodeManager.unregisterFamily(family);
		nodeManager = null;
	}

	@Override
	public void onPreRenderUpdate() {
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
}
