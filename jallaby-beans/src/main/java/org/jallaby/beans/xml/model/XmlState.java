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

package org.jallaby.beans.xml.model;

import java.io.Serializable;
import java.util.Set;

/**
 * JavaBean abstraction of an XML state declaration.
 * 
 * @author Matthias Rothe
 * @since RedRoo 1.0
 */
public class XmlState implements Serializable {
	private static final long serialVersionUID = -3465811338582709530L;
	private String name;
	private String xmlExtends;
	private XmlModifier modifier;
	private Set<XmlTransition> transitions;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the xmlExtends
	 */
	public String getXmlExtends() {
		return xmlExtends;
	}

	/**
	 * @param xmlExtends the xmlExtends to set
	 */
	public void setXmlExtends(String xmlExtends) {
		this.xmlExtends = xmlExtends;
	}

	/**
	 * @return the modifier
	 */
	public XmlModifier getModifier() {
		return modifier;
	}

	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(XmlModifier modifier) {
		this.modifier = modifier;
	}

	/**
	 * @return the transitions
	 */
	public Set<XmlTransition> getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions the transitions to set
	 */
	public void setTransitions(Set<XmlTransition> transitions) {
		this.transitions = transitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		XmlState other = (XmlState) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}