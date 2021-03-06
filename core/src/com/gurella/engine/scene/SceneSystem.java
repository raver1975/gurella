package com.gurella.engine.scene;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.gurella.engine.event.EventService;
import com.gurella.engine.event.EventSubscription;
import com.gurella.engine.managedobject.ManagedObject;
import com.gurella.engine.metatype.TransientProperty;
import com.gurella.engine.subscriptions.scene.SceneEventSubscription;

public abstract class SceneSystem extends SceneElement {
	public final int baseSystemType;
	public final int systemType;

	public SceneSystem() {
		Class<? extends SceneSystem> type = getClass();
		baseSystemType = SystemType.getBaseType(type);
		systemType = SystemType.findType(type);
	}

	@Override
	protected final void validateReparent(ManagedObject newParent) {
		super.validateReparent(newParent);

		if (newParent == null) {
			return;
		}

		if (newParent.getClass() != Scene.class) {
			throw new GdxRuntimeException("System can only be added to Scene.");
		}
	}

	@Override
	protected final boolean isActivationAllowed() {
		return super.isActivationAllowed();
	}

	final void setParent(Scene scene) {
		super.setParent(scene);
	}

	@Override
	protected final void activated() {
		super.activated();
		scene._activeSystems.add(this);
		if (this instanceof SceneEventSubscription) {
			EventService.subscribe(scene.getInstanceId(), (EventSubscription) this);
		}
		systemActivated();
	}

	protected void systemActivated() {
	}

	@Override
	protected final void deactivated() {
		super.deactivated();
		scene._activeSystems.remove(this);
		if (this instanceof SceneEventSubscription) {
			EventService.unsubscribe(scene.getInstanceId(), (EventSubscription) this);
		}
		systemDeactivated();
	}

	protected void systemDeactivated() {
	}

	@TransientProperty
	public int getIndex() {
		Scene scene = getScene();
		return scene == null ? -1 : scene._systems.indexOf(this, true);
	}

	public void setIndex(int newIndex) {
		Scene scene = getScene();
		if (scene == null) {
			throw new GdxRuntimeException("System is not attached to graph.");
		}
		scene._systems.setIndex(newIndex, this, true);
	}
}
