package com.gurella.engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.gurella.engine.input.ButtonTrigger.ButtonType;
import com.gurella.engine.input.DragTrigger.DragDirection;
import com.gurella.engine.pool.PoolService;

class InputMapper implements InputProcessor {
	private Array<InputContext> activeContexts = new Array<InputContext>();

	void addInputContext(InputContext inputContext) {
		activeContexts.insert(0, inputContext);
		activeContexts.sort();
	}

	void removeInputContext(InputContext inputContext) {
		activeContexts.removeValue(inputContext, true);
	}

	@Override
	public boolean keyDown(int keycode) {
		ButtonTrigger buttonTrigger = PoolService.obtain(ButtonTrigger.class);
		buttonTrigger.buttonType = ButtonType.KEYBOARD;
		buttonTrigger.button = keycode;
		buttonTrigger.buttonState = ButtonState.PRESSED;
		handleButton(buttonTrigger);
		PoolService.free(buttonTrigger);
		return false;
	}

	private void handleButton(ButtonTrigger buttonTrigger) {
		for (int i = 0; i < activeContexts.size; i++) {
			InputContext inputContext = activeContexts.get(i);
			if (inputContext.handleButton(buttonTrigger)) {
				return;
			}
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		ButtonTrigger buttonTrigger = PoolService.obtain(ButtonTrigger.class);
		buttonTrigger.buttonType = ButtonType.KEYBOARD;
		buttonTrigger.button = keycode;
		buttonTrigger.buttonState = ButtonState.RELEASED;
		handleButton(buttonTrigger);
		PoolService.free(buttonTrigger);
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		for (int i = 0; i < activeContexts.size; i++) {
			InputContext inputContext = activeContexts.get(i);
			if (inputContext.handleKeyTyped(character)) {
				return false;
			}
		}

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		ButtonTrigger buttonTrigger = PoolService.obtain(ButtonTrigger.class);
		buttonTrigger.buttonType = ButtonType.MOUSE;
		buttonTrigger.button = button;
		buttonTrigger.buttonState = ButtonState.PRESSED;
		handleButton(buttonTrigger);
		PoolService.free(buttonTrigger);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		ButtonTrigger buttonTrigger = PoolService.obtain(ButtonTrigger.class);
		buttonTrigger.buttonType = ButtonType.MOUSE;
		buttonTrigger.button = button;
		buttonTrigger.buttonState = ButtonState.RELEASED;
		handleButton(buttonTrigger);
		PoolService.free(buttonTrigger);
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		int deltaX = Gdx.input.getDeltaX(pointer);
		if (deltaX != 0) {
			handleDragged(DragDirection.HORISONTAL, pointer, deltaX);
		}

		int deltaY = Gdx.input.getDeltaY(pointer);
		if (deltaY != 0) {
			handleDragged(DragDirection.VERTICAL, pointer, deltaY);
		}

		return false;
	}

	private void handleDragged(DragDirection direction, int pointer, int delta) {
		DragTrigger dragTrigger = PoolService.obtain(DragTrigger.class);
		dragTrigger.direction = direction;
		dragTrigger.pointer = pointer;
		handleDragged(delta, dragTrigger);
		PoolService.free(dragTrigger);
	}

	private void handleDragged(int delta, DragTrigger dragTrigger) {
		for (int i = 0; i < activeContexts.size; i++) {
			InputContext inputContext = activeContexts.get(i);
			if (inputContext.handleTouchMoved(dragTrigger, delta)) {
				return;
			}
		}
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;// TODO
	}

	@Override
	public boolean scrolled(int amount) {
		for (int i = 0; i < activeContexts.size; i++) {
			InputContext inputContext = activeContexts.get(i);
			if (inputContext.handleScroll(amount)) {
				return false;
			}
		}
		return false;
	}
}
