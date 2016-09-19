package com.gurella.engine.scene.ui;

import com.gurella.engine.base.object.ManagedObject;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.scene.SceneNodeComponent2;
import com.gurella.engine.scene.SceneService2;
import com.gurella.engine.subscriptions.base.object.ObjectsParentListener;
import com.gurella.engine.subscriptions.scene.ComponentActivityListener;

public class UiSystem extends SceneService2 implements ComponentActivityListener, ObjectsParentListener {
	private FocusManager focusManager = new FocusManager();

	public UiSystem(Scene scene) {
		super(scene);
	}

	@Override
	public void componentActivated(SceneNodeComponent2 component) {
		if (component instanceof UiComponent) {

		}
	}

	@Override
	public void componentDeactivated(SceneNodeComponent2 component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parentChanged(ManagedObject object, ManagedObject oldParent, ManagedObject newParent) {
		if (object instanceof UiComponent) {
			UiComponent component = (UiComponent) object;
			component.parent = findNewParentComposite(newParent);
		}
	}

	private Composite findNewParentComposite(ManagedObject newParent) {
		// TODO Auto-generated method stub
		return null;
	}
}
