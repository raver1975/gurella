package com.gurella.engine.state;

import com.badlogic.gdx.utils.ObjectMap;
import com.gurella.engine.state.StateTransition.SimpleStateTransition;

public class FixedStateMachineContext<STATE> extends BaseStateContext<STATE> {
	private ObjectMap<STATE, ObjectMap<STATE, StateTransition<STATE>>> validTransitions = new ObjectMap<STATE, ObjectMap<STATE, StateTransition<STATE>>>();

	public FixedStateMachineContext(STATE initialState) {
		super(initialState);
	}

	public FixedStateMachineContext<STATE> put(STATE from, STATE to) {
		return put(new SimpleStateTransition<STATE>(from, to));
	}

	public FixedStateMachineContext<STATE> put(StateTransition<STATE> transition) {
		STATE source = transition.getSource();
		ObjectMap<STATE, StateTransition<STATE>> triggersMap = validTransitions.get(source);

		if (triggersMap == null) {
			triggersMap = new ObjectMap<STATE, StateTransition<STATE>>();
			validTransitions.put(source, triggersMap);
		}

		triggersMap.put(transition.getDestination(), transition);
		return this;
	}

	@Override
	public StateTransition<STATE> getStateTransition(STATE newState) {
		STATE currentState = getCurrentState();
		ObjectMap<STATE, StateTransition<STATE>> stateTransitions = validTransitions.get(currentState);
		return stateTransitions == null ? null : stateTransitions.get(newState);
	}
}