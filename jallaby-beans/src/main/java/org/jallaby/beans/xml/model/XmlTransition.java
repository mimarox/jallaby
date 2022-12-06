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
 * JavaBean abstraction of an XML state transition declaration.
 * 
 * @author Matthias Rothe
 * @since RedRoo 1.0
 */
public class XmlTransition implements Serializable {
	private static final long serialVersionUID = 1799676421383393237L;
	private String to;
	private boolean xmlPrivate;
	private Set<String> events;

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the xmlPrivate
	 */
	public boolean isXmlPrivate() {
		return xmlPrivate;
	}

	/**
	 * @param xmlPrivate the xmlPrivate to set
	 */
	public void setXmlPrivate(boolean xmlPrivate) {
		this.xmlPrivate = xmlPrivate;
	}

	/**
	 * @return the events
	 */
	public Set<String> getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	public void setEvents(Set<String> events) {
		this.events = events;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		XmlTransition other = (XmlTransition) obj;
		if (to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!to.equals(other.to)) {
			return false;
		}
		return true;
	}
}