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

package integrationtests.common;

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
	 * @return the stateMachineName
	 */
	public String getStateMachineName() {
		return stateMachineName;
	}

	/**
	 * @param stateMachineName the stateMachineName to set
	 */
	public void setStateMachineName(String stateMachineName) {
		this.stateMachineName = stateMachineName;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the currentStateName
	 */
	public String getCurrentStateName() {
		return currentStateName;
	}

	/**
	 * @param currentStateName the currentStateName to set
	 */
	public void setCurrentStateName(String currentStateName) {
		this.currentStateName = currentStateName;
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
