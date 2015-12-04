package com.gurella.engine.graph.input;

import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragEnd;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragEndGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragMove;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragMoveGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragOverEnd;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragOverEndGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragOverMove;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragOverMoveGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragOverStartGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragStart;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.onDragStartGlobal;
import static com.gurella.engine.graph.behaviour.BehaviourEvents.touchDragged;

import com.badlogic.gdx.utils.IntMap;
import com.gurella.engine.graph.SceneNode;
import com.gurella.engine.graph.behaviour.BehaviourComponent;
import com.gurella.engine.graph.renderable.RenderableComponent;

public class DragInputProcessor implements PointerActivityListener {
	private final IntMap<SceneNode> dragStartNodes = new IntMap<SceneNode>(10);
	private final IntMap<SceneNode> dragOverNodes = new IntMap<SceneNode>(10);

	private final TouchEvent touchEvent = new TouchEvent();
	private final IntersectionTouchEvent intersectionTouchEvent = new IntersectionTouchEvent();

	private InputSystem inputSystem;

	public DragInputProcessor(InputSystem inputSystem) {
		this.inputSystem = inputSystem;
	}

	@Override
	public void onPointerActivity(int pointer, int button, PointerTrack pointerTrack) {
		int key = pointer + button * 100;
		switch (pointerTrack.getPhase()) {
		case begin:
			begin(key, pointer, button, pointerTrack);
			break;
		case move:
			move(key, pointer, button, pointerTrack);
			break;
		case end:
			end(key, pointer, button, pointerTrack);
			break;
		default:
			break;
		}
	}

	private void begin(int key, int pointer, int button, PointerTrack pointerTrack) {
		int screenX = pointerTrack.getScreenX(0);
		int screenY = pointerTrack.getScreenY(0);
		touchEvent.set(pointer, button, screenX, screenY);
		for (BehaviourComponent behaviourComponent : inputSystem.getListeners(touchDragged)) {
			behaviourComponent.touchDragged(touchEvent);
		}

		SceneNode node = pointerTrack.getNode(0);
		if (node != null) {
			intersectionTouchEvent.set(pointer, button, screenX, screenY, pointerTrack, 0);
			dragStartNodes.put(key, node);
			RenderableComponent renderableComponent = node.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragStartGlobal)) {
				behaviourComponent.onDragStart(renderableComponent, intersectionTouchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(node, onDragStart)) {
				behaviourComponent.onDragStart(intersectionTouchEvent);
			}

			dragOverNodes.put(key, node);
			renderableComponent = node.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragOverStartGlobal)) {
				behaviourComponent.onDragOverStart(renderableComponent, intersectionTouchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(node, onDragOverEnd)) {
				behaviourComponent.onDragOverStart(intersectionTouchEvent);
			}
		}
	}

	private void move(int key, int pointer, int button, PointerTrack pointerTrack) {
		int last = pointerTrack.getSize() - 1;
		int screenX = pointerTrack.getScreenX(last);
		int screenY = pointerTrack.getScreenY(last);
		touchEvent.set(pointer, button, screenX, screenY);
		for (BehaviourComponent behaviourComponent : inputSystem.getListeners(touchDragged)) {
			behaviourComponent.touchDragged(touchEvent);
		}

		SceneNode dragStartNode = dragStartNodes.get(key);
		if (dragStartNode != null) {
			RenderableComponent renderableComponent = dragStartNode.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragMoveGlobal)) {
				behaviourComponent.onDragMove(renderableComponent, touchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(dragStartNode, onDragMove)) {
				behaviourComponent.onDragMove(touchEvent);
			}
		}

		SceneNode pointerNode = pointerTrack.getNode(last);
		SceneNode dragOverNode = dragOverNodes.get(key);
		intersectionTouchEvent.set(pointer, button, screenX, screenY, pointerTrack, last);
		if (dragOverNode != null) {
			RenderableComponent renderableComponent = dragOverNode.getComponent(RenderableComponent.class);
			if (pointerNode == dragOverNode) {
				for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragOverMoveGlobal)) {
					behaviourComponent.onDragOverMove(renderableComponent, intersectionTouchEvent);
				}

				for (BehaviourComponent behaviourComponent : inputSystem.getListeners(dragOverNode, onDragOverMove)) {
					behaviourComponent.onDragOverMove(intersectionTouchEvent);
				}
			} else {
				for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragOverEndGlobal)) {
					behaviourComponent.onDragOverEnd(renderableComponent, touchEvent);
				}

				for (BehaviourComponent behaviourComponent : inputSystem.getListeners(dragOverNode, onDragOverEnd)) {
					behaviourComponent.onDragOverEnd(touchEvent);
				}

				dragOverNodes.remove(key);
			}
		}

		if (pointerNode != null && pointerNode != dragOverNode) {
			dragOverNodes.put(key, pointerNode);
			RenderableComponent renderableComponent = pointerNode.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragOverStartGlobal)) {
				behaviourComponent.onDragOverStart(renderableComponent, intersectionTouchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(pointerNode, onDragOverEnd)) {
				behaviourComponent.onDragOverStart(intersectionTouchEvent);
			}
		}
	}

	private void end(int key, int pointer, int button, PointerTrack pointerTrack) {
		int last = pointerTrack.getSize() - 1;
		int screenX = pointerTrack.getScreenX(last);
		int screenY = pointerTrack.getScreenY(last);
		touchEvent.set(pointer, button, screenX, screenY);

		SceneNode dragStartNode = dragStartNodes.remove(key);
		if (dragStartNode != null) {
			RenderableComponent renderableComponent = dragStartNode.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragEndGlobal)) {
				behaviourComponent.onDragEnd(renderableComponent, touchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(dragStartNode, onDragEnd)) {
				behaviourComponent.onDragEnd(touchEvent);
			}
		}

		SceneNode dragOverNode = dragOverNodes.remove(key);
		if (dragOverNode != null) {
			RenderableComponent renderableComponent = dragOverNode.getComponent(RenderableComponent.class);
			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(onDragOverEndGlobal)) {
				behaviourComponent.onDragOverEnd(renderableComponent, touchEvent);
			}

			for (BehaviourComponent behaviourComponent : inputSystem.getListeners(dragOverNode, onDragOverEnd)) {
				behaviourComponent.onDragOverEnd(touchEvent);
			}
		}
	}

	@Override
	public void reset() {
		dragStartNodes.clear();
		dragOverNodes.clear();
	}
}
