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

package org.jallaby.beans.xml.sourcing.validation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.model.effective.EffectiveXmlTransition;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError.ValidationSection;
import org.jallaby.beans.xml.tree.XmlStateTreePool;
import org.testng.annotations.Test;

/**
 * @author Matthias Rothe
 */
public class TransitionValidatorTest {

	@Test
	public void testStateWithoutTransitions() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("start", null));
		
		Set<EffectiveXmlState> states = new HashSet<>();
		states.add(new EffectiveXmlState(null, "finished", new HashSet<>()));
		
		EffectiveXmlStateMachine stateMachine =
				new EffectiveXmlStateMachine("Sample", "initial", events, states, new XmlStateTreePool());
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		TransitionsValidator validator = new TransitionsValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 0);
	}
	
	@Test
	public void testValidTransitions() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("activate", null));
		events.add(new EffectiveXmlEvent("start", null));
		events.add(new EffectiveXmlEvent("finish", null));
		events.add(new EffectiveXmlEvent("deactivate", null));
		
		Set<EffectiveXmlTransition> initialTransitions = new HashSet<>();
		initialTransitions.add(new EffectiveXmlTransition("Idle", new HashSet<>(Arrays.asList("activate"))));
		
		Set<EffectiveXmlTransition> idleTransitions = new HashSet<>();
		idleTransitions.add(new EffectiveXmlTransition("Started", new HashSet<>(Arrays.asList("start"))));
		idleTransitions.add(new EffectiveXmlTransition("Initial", new HashSet<>(Arrays.asList("deactivate"))));
		
		Set<EffectiveXmlTransition> startedTransitions = new HashSet<>();
		startedTransitions.add(new EffectiveXmlTransition("Idle", new HashSet<>(Arrays.asList("finish"))));
		
		Set<EffectiveXmlState> states = new HashSet<>();
		states.add(new EffectiveXmlState(null, "Initial", initialTransitions));
		states.add(new EffectiveXmlState(null, "Idle", idleTransitions));
		states.add(new EffectiveXmlState(null, "Started", startedTransitions));
		
		EffectiveXmlStateMachine stateMachine =
				new EffectiveXmlStateMachine("Sample", "Initial", events, states, new XmlStateTreePool());
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		TransitionsValidator validator = new TransitionsValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 0);
	}
	
	@Test
	public void testTransitionsPointingNowhere() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("activate", null));
		events.add(new EffectiveXmlEvent("start", null));
		events.add(new EffectiveXmlEvent("finish", null));
		events.add(new EffectiveXmlEvent("deactivate", null));
		
		Set<EffectiveXmlTransition> initialTransitions = new HashSet<>();
		initialTransitions.add(new EffectiveXmlTransition("Idle", new HashSet<>(Arrays.asList("activate"))));
		initialTransitions.add(new EffectiveXmlTransition("Nowhere", new HashSet<>(Arrays.asList("nothing"))));
		
		Set<EffectiveXmlTransition> idleTransitions = new HashSet<>();
		idleTransitions.add(new EffectiveXmlTransition("Started", new HashSet<>(Arrays.asList("start"))));
		idleTransitions.add(new EffectiveXmlTransition("Initial", new HashSet<>(Arrays.asList("deactivate"))));
		idleTransitions.add(new EffectiveXmlTransition("Nowhere", new HashSet<>(Arrays.asList("nothing"))));
		
		Set<EffectiveXmlTransition> startedTransitions = new HashSet<>();
		startedTransitions.add(new EffectiveXmlTransition("Idle", new HashSet<>(Arrays.asList("finish"))));
		startedTransitions.add(new EffectiveXmlTransition("Nowhere", new HashSet<>(Arrays.asList("nothing"))));
		
		Set<EffectiveXmlState> states = new HashSet<>();
		states.add(new EffectiveXmlState(null, "Initial", initialTransitions));
		states.add(new EffectiveXmlState(null, "Idle", idleTransitions));
		states.add(new EffectiveXmlState(null, "Started", startedTransitions));
		
		EffectiveXmlStateMachine stateMachine =
				new EffectiveXmlStateMachine("Sample", "Initial", events, states, new XmlStateTreePool());
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		TransitionsValidator validator = new TransitionsValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 3);
		
		ValidationSection section = ValidationSection.TRANSITION;
		String errorMessagePattern = "The transition of state [%s] refering to state "
				+ "[%s] is invalid as the state referred to doesn't exist. "
				+ "It may be defined as abstract.";
		
		StateMachineValidationError initialNowhereError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Initial", "Nowhere"));
		
		StateMachineValidationError idleNowhereError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Idle", "Nowhere"));
		
		StateMachineValidationError startedNowhereError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Started", "Nowhere"));
		
		assertTrue(validationErrors.containsAll(
				Arrays.asList(initialNowhereError, idleNowhereError, startedNowhereError)));
	}
	
	@Test
	public void testTransitionsPointingToParentStates() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("start", null));
		events.add(new EffectiveXmlEvent("up", null));
		events.add(new EffectiveXmlEvent("finish", null));
		
		Set<EffectiveXmlTransition> initialTransitions = new HashSet<>();
		initialTransitions.add(new EffectiveXmlTransition("Child", new HashSet<>(Arrays.asList("start"))));
		
		Set<EffectiveXmlTransition> grandParentTransitions = new HashSet<>();
		grandParentTransitions.add(new EffectiveXmlTransition("Finished", new HashSet<>(Arrays.asList("finish"))));
		
		Set<EffectiveXmlTransition> parentTransitions = new HashSet<>();
		parentTransitions.add(new EffectiveXmlTransition("Finished", new HashSet<>(Arrays.asList("finish"))));
		parentTransitions.add(new EffectiveXmlTransition("GrandParent", new HashSet<>(Arrays.asList("up"))));

		Set<EffectiveXmlTransition> childTransitions = new HashSet<>();
		childTransitions.add(new EffectiveXmlTransition("Finished", new HashSet<>(Arrays.asList("finish"))));
		childTransitions.add(new EffectiveXmlTransition("GrandParent", new HashSet<>(Arrays.asList("up"))));
		childTransitions.add(new EffectiveXmlTransition("Parent", new HashSet<>(Arrays.asList("up"))));

		EffectiveXmlState grandParentState = new EffectiveXmlState(null, "GrandParent", grandParentTransitions);
		EffectiveXmlState parentState = new EffectiveXmlState(grandParentState, "Parent", parentTransitions);

		Set<EffectiveXmlState> states = new HashSet<>();
		states.add(new EffectiveXmlState(null, "Initial", initialTransitions));
		states.add(grandParentState);
		states.add(parentState);
		states.add(new EffectiveXmlState(parentState, "Child", childTransitions));
		states.add(new EffectiveXmlState(null, "Finished", new HashSet<>()));

		EffectiveXmlStateMachine stateMachine =
				new EffectiveXmlStateMachine("Sample", "Initial", events, states, new XmlStateTreePool());
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		TransitionsValidator validator = new TransitionsValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 3);
		
		ValidationSection section = ValidationSection.TRANSITION;
		String errorMessagePattern = "The transition of state [%1$s] refering to state [%2$s] "
				+ "is invalid as the state referred to is a parent state of the state [%1$s].";
		
		StateMachineValidationError parentGrandParentError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Parent", "GrandParent"));
		
		StateMachineValidationError childGrandParentError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Child", "GrandParent"));
		
		StateMachineValidationError childParentError = new StateMachineValidationError(section,
				String.format(errorMessagePattern, "Child", "Parent"));
		
		assertTrue(validationErrors.containsAll(
				Arrays.asList(parentGrandParentError, childGrandParentError, childParentError)));
	}
}
