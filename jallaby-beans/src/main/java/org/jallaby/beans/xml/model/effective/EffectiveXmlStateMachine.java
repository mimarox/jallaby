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

package org.jallaby.beans.xml.model.effective;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlStateInfo;
import org.jallaby.beans.xml.tree.XmlStateTreePool;
import org.jallaby.util.Assert;

/**
 * @author Matthias Rothe
 * @since RedRoo 1.0
 */
public class EffectiveXmlStateMachine {
	private final String name;
	private final String initialState;
	private final Set<EffectiveXmlEvent> events;
	private final Set<EffectiveXmlState> states;
	private final XmlStateTreePool stateTreePool;
	/**
	 * @param name the name of the state machine
	 * @param initialState the initial state
	 * @param events the declared events
	 * @param states the declared states
	 * @param stateTreePool the state tree pool
	 */
	public EffectiveXmlStateMachine(final String name, final String initialState,
			final Set<EffectiveXmlEvent> events, final Set<EffectiveXmlState> states,
			final XmlStateTreePool stateTreePool) {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(initialState, "initialState must not be null");
		Assert.notEmpty(events, IllegalArgumentException.class, "events must not be null or empty");
		Assert.notEmpty(states, IllegalArgumentException.class, "states must not be null or empty");
		Objects.requireNonNull(stateTreePool, "stateTreePool must not be null");
		
		this.name = name;
		this.initialState = initialState;
		this.events = events;
		this.states = states;
		this.stateTreePool = stateTreePool;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the initialState
	 */
	public String getInitialState() {
		return initialState;
	}

	/**
	 * @return the eventDeclarations
	 */
	public Set<EffectiveXmlEvent> getEvents() {
		return events;
	}

	/**
	 * @return the states
	 */
	public Set<EffectiveXmlState> getStates() {
		return states;
	}

	public EffectiveXmlState getStateByName(final String stateName) {
		Optional<EffectiveXmlState> optionalState = states.stream().filter(
				state -> state.getName().equals(stateName)).findFirst();
		
		if (optionalState.isPresent()) {
			return optionalState.get();
		} else {
			throw new NoSuchElementException(String.format("The state [%s] is not"
					+ " present in this state machine", stateName));
		}
	}

	public XmlStateInfo getXmlTargetStateInfo(final String fromState, final String toState) {
		return stateTreePool.getXmlTargetStateInfo(fromState, toState);
	}
}