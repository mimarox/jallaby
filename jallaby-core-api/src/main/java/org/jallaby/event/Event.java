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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * This class encapsulates the information of an event.
 * 
 * @author Matthias Rothe
 */
public final class Event {
	private final String stateMachineName;
	private final UUID instanceId;
	private final String eventName;
	private final Map<String, Object> payload;
	
	/**
	 * Ctor.
	 * <p>
	 * Actual parameters must be given. Only the payload is nullable.
	 * 
	 * @param stateMachineName The name of the state machine
	 * @param instanceId The instance id
	 * @param eventName The name of the event
	 * @param payload The payload 
	 */
	public Event(final String stateMachineName, final String instanceId,
			final String eventName, final Map<String, Object> payload) {
		Objects.requireNonNull(stateMachineName, "stateMachineName must not be null");
		Objects.requireNonNull(instanceId, "instanceId must not be null");
		Objects.requireNonNull(eventName, "eventName must not be null");
		
		this.stateMachineName = stateMachineName;
		this.instanceId = UUID.fromString(instanceId);
		this.eventName = eventName;
		this.payload = payload;
	}

	/**
	 * @return the name of the state machine
	 */
	public String getStateMachineName() {
		return stateMachineName;
	}

	/**
	 * @return the instance id
	 */
	public UUID getInstanceId() {
		return instanceId;
	}

	/**
	 * @return the name of the event
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return the payload
	 */
	public Map<String, Object> getPayload() {
		return payload;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(eventName, instanceId, payload, stateMachineName);
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
		Event other = (Event) obj;
		return Objects.equals(eventName, other.eventName)
				&& Objects.equals(instanceId, other.instanceId)
				&& Objects.equals(payload, other.payload)
				&& Objects.equals(stateMachineName, other.stateMachineName);
	}
}
