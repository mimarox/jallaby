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
public class EffectiveXmlEvent {
	private String name;
	private Set<EffectiveXmlProperty> properties;
	
	/**
	 * @param name the name of the event
	 * @param properties the properties of the event
	 */
	public EffectiveXmlEvent(String name, Set<EffectiveXmlProperty> properties) {
		Objects.requireNonNull(name, "name must not be null");
		
		this.name = name;
		this.properties = properties;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the properties
	 */
	public Set<EffectiveXmlProperty> getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, properties);
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
		EffectiveXmlEvent other = (EffectiveXmlEvent) obj;
		return Objects.equals(name, other.name) && Objects.equals(properties, other.properties);
	}
}
