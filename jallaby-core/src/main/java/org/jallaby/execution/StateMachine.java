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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jallaby.event.Event;
import org.jallaby.event.EventError;
import org.jallaby.event.EventProcessingException;
import org.jallaby.event.EventResult;
import org.jallaby.event.EventValidator;
import org.jallaby.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state machine, consisting of {@link State}s and their {@link Transition}s.
 * 
 * @author Matthias Rothe
 */
public class StateMachine {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);
	private static final String STATE_PENDING = "STATE PENDING";
	
	private final String name;
	private final State initialState;
	private final EventValidator validator;
	
	private Stack<State> currentStates = new Stack<>();
	private boolean initialized;
	
	/**
	 * Ctor.
	 * 
	 * @param name the name of the state machine
	 * @param initialState the initial state of the state machine
	 * @param validator the event validator
	 */
	public StateMachine(final String name, final State initialState, final EventValidator validator) {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(initialState, "initialState must not be null");
		Objects.requireNonNull(validator, "validator must not be null");
		
		this.name = name;
		this.initialState = initialState;
		this.validator = validator;
	}
	
	/**
	 * @return the name of the state machine
	 */
	public String getName() {
		return name;
	}	

	/**
	 * @return a new instance of this state machine
	 */
	public StateMachine newInstance() {
		return new StateMachine(name, initialState, validator);
	}
	
	/**
	 * Checks whether the given event is valid for this state machine.
	 * 
	 * @param event the event to be checked
	 * @return {@code true} if and only if the given event is valid for this state machine,
	 * {@code false} otherwise
	 */
	public boolean isValidEvent(Event event) {
		return validator.isValidEvent(event);
	}

	/**
	 * Processes the given event.
	 * 
	 * @param event the event to process
	 * @return the result of processing the event
	 * @throws EventProcessingException in case an exception occurs while processing the event
	 */
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
		Map<String, Map<String, Object>> eventData = state.getEventData();
		
		if (canProceed(transition.getTransitionGuards(), eventData)) {
			StateInfo stateInfo = transition.getTargetStateInfo();
			String nameOfNewState = stateInfo.getStates().peekLast().getName();
			int statesToExit = stateInfo.getStatesToExit();
			
			if (statesToExit >= 1) {
				exitStates(statesToExit, event, eventData);
			}
			
			performTransitionActions(transition.getTransitionActionGroups(), eventData);
			EventResult result = enterNewStates(stateInfo, eventData, event.getInstanceId());
			
			if (result == null) {
				result =  new EventResult(
						getName(),
						event.getInstanceId().toString(),
						nameOfNewState);
			}
			
			return result;
		} else {
			throw causedByTransitionGuard(event);
		}
	}

	private boolean canProceed(final List<TransitionGuard> transitionGuards,
			final Map<String, Map<String, Object>> eventData) {
		for (TransitionGuard guard : transitionGuards) {
			if (!guard.canProceed(eventData)) {
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

	private void performTransitionActions(final List<TransitionActionGroup> transitionActionGroups,
			final Map<String, Map<String, Object>> eventData) {
		for (TransitionActionGroup group : transitionActionGroups) {
			if (group.isConcurrent()) {
				performTransitionActionsConcurrently(group, eventData);
			} else if (group.isSequential()) {
				performTransitionActionsSequentially(group, eventData);
			}
		}
	}

	private void performTransitionActionsConcurrently(final TransitionActionGroup group,
			Map<String, Map<String, Object>> eventData) {
		ExecutorService service = Executors.newCachedThreadPool();
		
		for (TransitionAction action : group.getTransitionActions()) {
			service.execute(() -> action.run(eventData));
		}
		
		service.shutdown();
	}

	private void performTransitionActionsSequentially(final TransitionActionGroup group,
			Map<String, Map<String, Object>> eventData) {
		for (TransitionAction action : group.getTransitionActions()) {
			action.run(eventData);
		}
	}

	private EventResult enterNewStates(StateInfo stateInfo,
			Map<String, Map<String, Object>> eventData, UUID instanceId) throws EventProcessingException {
		Iterator<State> stateIterator = stateInfo.getStates().iterator();
		
		while (stateIterator.hasNext()) {
			State state = stateIterator.next();
			currentStates.push(state);
			FinishState finishState = state.performEntryAction(eventData);
			
			if (finishState == FinishState.FINISHED) {
				if (!stateIterator.hasNext()) {
					return processEvent(new Event(getName(), instanceId.toString(),
							"finished", new HashMap<>()));
				} else {
					LOGGER.warn(String.format("State [%s] cannot be finished as"
							+ " there are more states to enter!", state.getName()));
				}
			}
		}
		
		return null;
	}
}
