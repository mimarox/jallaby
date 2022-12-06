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

package org.jallaby.event;

import java.util.Objects;

/**
 * An object returning the current state of the given state machine after
 * processing an event.
 * 
 * @author Matthias Rothe
 */
public class EventResult {
	private String stateMachineName;
	private String instanceId;
	private String currentStateName;
	
	/**
	 * Ctor taking the current data.
	 * 
	 * @param stateMachineName The name of the state machine this result pertains to
	 * @param instanceId The instance id of the state machine
	 * @param currentStateName The name of the current state of the state machine
	 */
	public EventResult(String stateMachineName, String instanceId, String currentStateName) {
		Objects.requireNonNull(stateMachineName, "stateMachineName must not be null");
		Objects.requireNonNull(instanceId, "instanceId must not be null");
		Objects.requireNonNull(currentStateName, "currentStateName must not be null");
		
		this.stateMachineName = stateMachineName;
		this.instanceId = instanceId;
		this.currentStateName = currentStateName;
	}

	/**
	 * @return The name of the state machine
	 */
	public String getStateMachineName() {
		return stateMachineName;
	}

	/**
	 * @return The instance id of the state machine
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @return The name of the current state of the state machine
	 */
	public String getCurrentStateName() {
		return currentStateName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(currentStateName, instanceId, stateMachineName);
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
		EventResult other = (EventResult) obj;
		return Objects.equals(currentStateName, other.currentStateName)
				&& Objects.equals(instanceId, other.instanceId)
				&& Objects.equals(stateMachineName, other.stateMachineName);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventResult [stateMachineName=").append(stateMachineName).append(", instanceId=")
				.append(instanceId).append(", currentStateName=").append(currentStateName).append("]");
		return builder.toString();
	}
}
