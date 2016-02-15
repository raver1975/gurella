package com.gurella.engine.application.events;

import com.gurella.engine.event.AbstractSignal;

public class ApplicationShutdownSignal extends AbstractSignal<ApplicationShutdownListener> {
	public void onShutdown() {
		ApplicationShutdownListener[] items = listeners.begin();
		for (int i = 0, n = listeners.size; i < n; i++) {
			items[i].onShutdown();
		}
		listeners.end();
	}
}