package com.gurella.engine.scene.action;

public class ParalelAction extends CompositeAction {
	boolean complete = false;

	public ParalelAction() {
	}

	public ParalelAction(SceneAction action1) {
		super(action1);
	}

	public ParalelAction(SceneAction action1, SceneAction action2) {
		super(action1, action2);
	}

	public ParalelAction(SceneAction action1, SceneAction action2, SceneAction action3) {
		super(action1, action2, action3);
	}

	public ParalelAction(SceneAction action1, SceneAction action2, SceneAction action3, SceneAction action4) {
		super(action1, action2, action3, action4);
	}

	public ParalelAction(SceneAction action1, SceneAction action2, SceneAction action3, SceneAction action4,
			SceneAction action5) {
		super(action1, action2, action3, action4, action5);
	}

	@Override
	public boolean act() {
		if (complete) {
			return true;
		}

		for (int i = 0, n = actions.size; i < n; i++) {
			SceneAction currentAction = actions.get(i);
			if (!currentAction.isComplete()) {
				complete |= currentAction.act();
			}
		}
		return complete;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public void restart() {
		super.restart();
		complete = false;
	}
}