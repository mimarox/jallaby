/*
 * Copyright 2022, The Jallaby Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jallaby.execution;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jallaby.event.Event;
import org.jallaby.event.EventError;
import org.jallaby.event.EventProcessingException;
import org.jallaby.event.EventResult;
import org.jallaby.event.EventValidator;
import org.jallaby.util.Stack;

public class StateMachine {
	private static final String STATE_PENDING = "STATE PENDING";
	
	private final String name;
	private final State initialState;
	private final EventValidator validator;
	
	private Stack<State> currentStates = new Stack<>();
	private boolean initialized;
	
	public StateMachine(String name, State initialState, EventValidator validator) {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(initialState, "initialState must not be null");
		Objects.requireNonNull(validator, "validator must not be null");
		
		this.name = name;
		this.initialState = initialState;
		this.validator = validator;
	}
	
	public String getName() {
		return name;
	}	

	public StateMachine newInstance() {
		return new StateMachine(name, initialState, validator);
	}
	
	public boolean isValidEvent(Event event) {
		return validator.isValidEvent(event);
	}

	public synchronized EventResult processEvent(Event event) throws EventProcessingException {
		Objects.requireNonNull(event, "event must not be null");
		
		if (!initialized) {
			init();
		}

		State currentState = currentStates.peek();
		Transition transition = currentState.offerEvent(event);
		
		if (transition != null) {
			return performTransition(currentState, transition, event);
		} else {
			return new EventResult(
					event.getStateMachineName(),
					event.getInstanceId().toString(),
					STATE_PENDING);
		}
	}

	private void init() {
		currentStates.push(initialState);
		currentStates.peek().performEntryAction(null);
		initialized = true;
	}
	
	private EventResult performTransition(State state, Transition transition, Event event)
	throws EventProcessingException {
		if (canProceed(transition.getTransitionGuards())) {
			Map<String, Map<String, Object>> eventData = state.getEventData();
			
			StateInfo stateInfo = transition.getTargetStateInfo();
			String nameOfNewState = stateInfo.getStates().peekLast().getName();
			int statesToExit = stateInfo.getStatesToExit();
			
			if (statesToExit >= 1) {
				exitStates(statesToExit, event, eventData);
			}
			
			performTransitionActions(transition.getTransitionActionGroups());
			enterNewStates(stateInfo, eventData);
			
			return new EventResult(
					event.getStateMachineName(),
					event.getInstanceId().toString(),
					nameOfNewState);
		} else {
			throw causedByTransitionGuard(event);
		}
	}

	private boolean canProceed(List<TransitionGuard> transitionGuards) {
		for (TransitionGuard guard : transitionGuards) {
			if (!guard.canProceed()) {
				return false;
			}
		}
		
		return true;
	}

	private EventProcessingException causedByTransitionGuard(Event event) {
		return new EventProcessingException(
				new EventError(
						event.getStateMachineName(),
						event.getInstanceId().toString(),
						event.getEventName(),
						"A transition guard prevented the event from being processed.",
						202));
	}

	private void exitStates(int statesToExit, Event event,
			Map<String, Map<String, Object>> eventData)
	throws EventProcessingException {
		if (statesToExit > currentStates.size()) {
			throw new EventProcessingException(new EventError(
					event.getStateMachineName(),
					event.getInstanceId().toString(),
					event.getEventName(),
					"Cannot exit " + statesToExit + " states, as there are only "
					+ currentStates.size() + " states available.",
					300));
		}
		
		for (int i = 0; i < statesToExit; i++) {
			currentStates.pop().performExitAction(eventData);
		}
	}

	private void performTransitionActions(List<TransitionActionGroup> transitionActionGroups) {
		for (TransitionActionGroup group : transitionActionGroups) {
			if (group.isConcurrent()) {
				performTransitionActionsConcurrently(group);
			} else if (group.isSequential()) {
				performTransitionActionsSequentially(group);
			}
		}
	}

	private void performTransitionActionsConcurrently(TransitionActionGroup group) {
		ExecutorService service = Executors.newCachedThreadPool();
		
		for (TransitionAction action : group.getTransitionActions()) {
			service.execute(action);
		}
		
		service.shutdown();
	}

	private void performTransitionActionsSequentially(TransitionActionGroup group) {
		for (TransitionAction action : group.getTransitionActions()) {
			action.run();
		}
	}

	private void enterNewStates(StateInfo stateInfo,
			Map<String, Map<String, Object>> eventData) {
		for (State state : stateInfo.getStates()) {
			currentStates.push(state);
			state.performEntryAction(eventData);
		}
	}
}
