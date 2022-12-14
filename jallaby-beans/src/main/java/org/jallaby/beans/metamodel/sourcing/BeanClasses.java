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

package org.jallaby.beans.metamodel.sourcing;

import java.util.Set;

import org.jallaby.event.EventValidator;

import com.google.inject.Module;

public class BeanClasses {
	private Set<Class<?>> states;
	private Set<Class<?>> transitions;
	private Set<Class<? extends EventValidator>> eventValidators;
	private Set<Class<? extends Module>> modules;
	
	/**
	 * @return the states
	 */
	public Set<Class<?>> getStates() {
		return states;
	}
	
	/**
	 * @param states the states to set
	 */
	public void setStates(Set<Class<?>> states) {
		this.states = states;
	}
	
	/**
	 * @return the transitions
	 */
	public Set<Class<?>> getTransitions() {
		return transitions;
	}
	
	/**
	 * @param transitions the transitions to set
	 */
	public void setTransitions(Set<Class<?>> transitions) {
		this.transitions = transitions;
	}
	
	/**
	 * @return the event validators
	 */
	public Set<Class<? extends EventValidator>> getEventValidators() {
		return eventValidators;
	}
	
	/**
	 * @param eventValidators the event validators to set
	 */
	public void setEventValidators(Set<Class<? extends EventValidator>> eventValidators) {
		this.eventValidators = eventValidators;
	}

	/**
	 * @return the modules
	 */
	public Set<Class<? extends Module>> getModules() {
		return modules;
	}

	/**
	 * @param modules the modules to set
	 */
	public void setModules(Set<Class<? extends Module>> modules) {
		this.modules = modules;
	}
}
