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

import java.util.Objects;
import java.util.Set;

/**
 * @author Matthias Rothe
 */
public class EffectiveXmlState {
	private EffectiveXmlState parent;
	private String name;
	private Set<EffectiveXmlTransition> transitions;
	
	/**
	 * @param parent the parent state
	 * @param name the name of the state
	 * @param transitions the transitions of the state, if any
	 */
	public EffectiveXmlState(EffectiveXmlState parent, String name, Set<EffectiveXmlTransition> transitions) {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(transitions, "transitions must not be null, however it can be empty");
		
		this.parent = parent;
		this.name = name;
		this.transitions = transitions;
	}

	/**
	 * @return the parent
	 */
	public EffectiveXmlState getParent() {
		return parent;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the transitions
	 */
	public Set<EffectiveXmlTransition> getTransitions() {
		return transitions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EffectiveXmlState other = (EffectiveXmlState) obj;
		return Objects.equals(parent, other.parent) && Objects.equals(name, other.name);
	}
}
