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

package org.jallaby;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.jallaby.event.Event;
import org.jallaby.event.EventResult;
import org.jallaby.execution.State;
import org.jallaby.execution.StateInfo;
import org.jallaby.execution.StateMachine;
import org.jallaby.execution.Transition;
import org.testng.annotations.Test;

public class JallabyTest {
	
	@Test
	public void shouldReceiveEvent() throws Exception {
		Jallaby jallaby = new Jallaby();
		
		String stateMachineName = "jallaby-one";
		String instanceId = UUID.randomUUID().toString();
		String eventName = "start";
		Map<String, Object> payload = new HashMap<>();
		Event event = new Event(stateMachineName, instanceId, eventName, payload);
		
		State initialState = buildInitialState(event);
		
		JallabyRegistry registry = JallabyRegistry.getInstance();
		registry.register(new StateMachine(stateMachineName, initialState, e -> true));
		
		EventResult actualResult = jallaby.receiveEvent(event);
		
		EventResult expectedResult = new EventResult(stateMachineName, instanceId, "started");
		
		assertEquals(actualResult, expectedResult);
	}
	
	private State buildInitialState(final Event event) throws Exception {
		State initialState = mock(State.class);
		State startedState = mock(State.class);
		Transition initialToStartedTransition = mock(Transition.class);
		StateInfo stateInfo = mock(StateInfo.class);
		
		Deque<State> targetStates = new LinkedList<>();
		targetStates.add(startedState);
		
		when(initialState.getName()).thenReturn("initial");
		when(initialState.offerEvent(event)).thenReturn(initialToStartedTransition);
		
		when(initialToStartedTransition.getTransitionActionGroups())
		.thenReturn(new ArrayList<>());
		
		when(initialToStartedTransition.getTransitionGuards())
		.thenReturn(new ArrayList<>());
		
		when(initialToStartedTransition.getTargetStateInfo()).thenReturn(stateInfo);
		
		when(stateInfo.getStatesToExit()).thenReturn(1);
		when(stateInfo.getStates()).thenReturn(targetStates);
		
		when(startedState.getName()).thenReturn("started");
		
		return initialState;
	}
}
