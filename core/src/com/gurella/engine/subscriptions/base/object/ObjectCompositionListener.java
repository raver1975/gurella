package com.gurella.engine.subscriptions.base.object;

import com.gurella.engine.base.object.ManagedObject;
import com.gurella.engine.subscriptions.application.ApplicationEventSubscription;

public interface ObjectCompositionListener extends ApplicationEventSubscription {
	void childAdded(ManagedObject child);

	void childRemoved(ManagedObject child);
}