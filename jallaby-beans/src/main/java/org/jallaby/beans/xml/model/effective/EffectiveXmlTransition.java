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

import org.jallaby.util.Assert;

/**
 * @author Matthias Rothe
 */
public class EffectiveXmlTransition {
	private String to;
	private Set<String> eventRefs;
	
	/**
	 * @param to the state to transition to
	 * @param eventRefs the event references
	 */
	public EffectiveXmlTransition(String to, Set<String> eventRefs) {
		Objects.requireNonNull(to, "to must not be null");
		Assert.notEmpty(eventRefs, IllegalArgumentException.class, "eventRefs must not be null or empty");
		
		this.to = to;
		this.eventRefs = eventRefs;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @return the events
	 */
	public Set<String> getEventRefs() {
		return eventRefs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(eventRefs, to);
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
		EffectiveXmlTransition other = (EffectiveXmlTransition) obj;
		return Objects.equals(eventRefs, other.eventRefs) && Objects.equals(to, other.to);
	}
}
