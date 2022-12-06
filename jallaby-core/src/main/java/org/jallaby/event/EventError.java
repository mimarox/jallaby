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
 * This class encapsulates information pertaining to an event processing error.
 * 
 * @author Matthias Rothe
 */
public class EventError {
	private String stateMachineName;
	private String instanceId;
	private String eventName;
	private String errorDescription;
	private long errorCode;
	
	/**
	 * Ctor.
	 * 
	 * @param stateMachineName The name of the state machine
	 * @param instanceId The instance id
	 * @param eventName The name of the event which couldn't be processed properly
	 * @param errorDescription The textual description of the error
	 * @param errorCode The code number of the error
	 */
	public EventError(String stateMachineName, String instanceId, String eventName,
			String errorDescription, long errorCode) {
		Objects.requireNonNull(stateMachineName, "stateMachineName must not be null");
		Objects.requireNonNull(instanceId, "instanceId must not be null");
		Objects.requireNonNull(eventName, "eventName must not be null");
		Objects.requireNonNull(errorDescription, "errorDescription must not be null");
		
		this.stateMachineName = stateMachineName;
		this.instanceId = instanceId;
		this.eventName = eventName;
		this.errorDescription = errorDescription;
		this.errorCode = errorCode;
	}

	/**
	 * @return the stateMachineName
	 */
	public String getStateMachineName() {
		return stateMachineName;
	}

	/**
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return the errorDescription
	 */
	public String getErrorDescription() {
		return errorDescription;
	}

	/**
	 * @return the errorCode
	 */
	public long getErrorCode() {
		return errorCode;
	}
}
