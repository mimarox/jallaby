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

package org.jallaby.beans.xml.sourcing;

import java.util.Objects;

/**
 * A single state machine validation error.
 * 
 * @author Matthias Rothe
 */
public class StateMachineValidationError {
	public enum ValidationSection {
		STATE_MACHINE  ("state-machine"),
		EVENT          ("event________"),
		EVENT_PROPERTY ("property_____"),
		STATE          ("state________"),
		TRANSITION     ("transition___"),
		EVENT_REF      ("event-ref____");
		
		private String caption;
		
		ValidationSection(String caption) {
			this.caption = caption;
		}
	}
	
	private ValidationSection section;
	private String errorMessage;
	
	/**
	 * Ctor.
	 * 
	 * @param section the validation section
	 * @param errorMessage the error message
	 */
	public StateMachineValidationError(ValidationSection section, String errorMessage) {
		Objects.requireNonNull(section, "section must not be null");
		Objects.requireNonNull(errorMessage, "errorMessage must not be null");
		
		this.section = section;
		this.errorMessage = errorMessage;
	}

	/**
	 * @return the section
	 */
	public ValidationSection getSection() {
		return section;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(errorMessage, section);
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
		StateMachineValidationError other = (StateMachineValidationError) obj;
		return Objects.equals(errorMessage, other.errorMessage) && section == other.section;
	}
	
	@Override
	public String toString() {
		return String.format("[%s]: %s", section.caption, errorMessage);
	}
}
