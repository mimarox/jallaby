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
import org.jallaby.beans.xml.tree.XmlStateTreePool;
import org.testng.annotations.Test;

/**
 * @author Matthias Rothe
 */
public class AllStatesReachableValidatorTest {

	/*
	 * Tests
	 *   ->
	 * s1  s2
	 *   <-
	 */
	@Test
	public void shouldReachAllStates() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("switchOn", null));
		events.add(new EffectiveXmlEvent("switchOff", null));
		
		Set<EffectiveXmlState> states = new HashSet<>();
		
		Set<EffectiveXmlTransition> switchedOffTransitions = new HashSet<>();
		switchedOffTransitions.add(new EffectiveXmlTransition("SwitchedOn",
				new HashSet<>(Arrays.asList("switchOn"))));
		
		Set<EffectiveXmlTransition> switchedOnTransitions = new HashSet<>();
		switchedOnTransitions.add(new EffectiveXmlTransition("SwitchedOff",
				new HashSet<>(Arrays.asList("switchOff"))));
		
		states.add(new EffectiveXmlState(null, "SwitchedOff", switchedOffTransitions));
		states.add(new EffectiveXmlState(null, "SwitchedOn", switchedOnTransitions));
		
		EffectiveXmlStateMachine stateMachine = new EffectiveXmlStateMachine("SMX",
				"SwitchedOff", events, states, new XmlStateTreePool());
		
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		AllStatesReachableValidator validator = new AllStatesReachableValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 0);
	}
	
	@Test
	public void shouldDetectAnUnreachableState() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("switchOn", null));
		events.add(new EffectiveXmlEvent("switchOff", null));
		
		Set<EffectiveXmlState> states = new HashSet<>();
		
		Set<EffectiveXmlTransition> switchedOffTransitions = new HashSet<>();
		switchedOffTransitions.add(new EffectiveXmlTransition("SwitchedOn",
				new HashSet<>(Arrays.asList("switchOn"))));
		
		Set<EffectiveXmlTransition> switchedOnTransitions = new HashSet<>();
		switchedOnTransitions.add(new EffectiveXmlTransition("SwitchedOff",
				new HashSet<>(Arrays.asList("switchOff"))));
		
		states.add(new EffectiveXmlState(null, "SwitchedOff", switchedOffTransitions));
		states.add(new EffectiveXmlState(null, "SwitchedOn", switchedOnTransitions));
		states.add(new EffectiveXmlState(null, "Unreachable", new HashSet<>()));
		
		EffectiveXmlStateMachine stateMachine = new EffectiveXmlStateMachine("SMX",
				"SwitchedOff", events, states, new XmlStateTreePool());
		
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		AllStatesReachableValidator validator = new AllStatesReachableValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 1);
		
		String actualMessage = validationErrors.get(0).getErrorMessage();
		String expectedMessage = "State [Unreachable] is unreachable and should be removed.";
		
		assertEquals(actualMessage, expectedMessage);
	}
	
	@Test
	public void shouldReachAllStatesWithParent() {
		Set<EffectiveXmlEvent> events = new HashSet<>();
		events.add(new EffectiveXmlEvent("switchOn", null));
		events.add(new EffectiveXmlEvent("switchOff", null));
		events.add(new EffectiveXmlEvent("workIncoming", null));
		events.add(new EffectiveXmlEvent("finished", null));
		events.add(new EffectiveXmlEvent("specialize", null));

		Set<EffectiveXmlState> states = new HashSet<>();
		
		Set<EffectiveXmlTransition> switchedOffTransitions = new HashSet<>();
		switchedOffTransitions.add(new EffectiveXmlTransition("Idle",
				new HashSet<>(Arrays.asList("switchOn"))));

		Set<EffectiveXmlTransition> switchedOnTransitions = new HashSet<>();
		switchedOnTransitions.add(new EffectiveXmlTransition("Special",
				new HashSet<>(Arrays.asList("specialize"))));
		
		Set<EffectiveXmlTransition> specialTransitions = new HashSet<>();
		specialTransitions.add(new EffectiveXmlTransition("SwitchedOn",
				new HashSet<>(Arrays.asList("finished"))));
		
		Set<EffectiveXmlTransition> idleTransitions = new HashSet<>();
		idleTransitions.add(new EffectiveXmlTransition("SwitchedOff",
				new HashSet<>(Arrays.asList("switchOff"))));
		idleTransitions.add(new EffectiveXmlTransition("Working", 				
				new HashSet<>(Arrays.asList("workIncoming"))));
		
		Set<EffectiveXmlTransition> workingTransitions = new HashSet<>();
		workingTransitions.add(new EffectiveXmlTransition("Idle",
				new HashSet<>(Arrays.asList("finished"))));
		
		EffectiveXmlState switchedOnState = new EffectiveXmlState(null, "SwitchedOn",
				switchedOnTransitions);
		
		states.add(new EffectiveXmlState(null, "SwitchedOff", switchedOffTransitions));
		states.add(switchedOnState);
		states.add(new EffectiveXmlState(null, "Special", specialTransitions));
		states.add(new EffectiveXmlState(switchedOnState, "Idle", idleTransitions));
		states.add(new EffectiveXmlState(switchedOnState, "Working", workingTransitions));
		
		EffectiveXmlStateMachine stateMachine = new EffectiveXmlStateMachine("SMX",
				"SwitchedOff", events, states, new XmlStateTreePool());
		
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		AllStatesReachableValidator validator = new AllStatesReachableValidator();
		validator.validate(stateMachine, validationErrors);
		
		assertEquals(validationErrors.size(), 0);
	}
}
