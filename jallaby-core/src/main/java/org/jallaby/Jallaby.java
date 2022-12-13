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

package org.jallaby;

import org.jallaby.event.Event;
import org.jallaby.event.EventError;
import org.jallaby.event.EventProcessingException;
import org.jallaby.event.EventResult;
import org.jallaby.execution.StateMachine;

/**
 * The central class for Jallaby.
 * 
 * @author Matthias Rothe
 */
public class Jallaby {
	private JallabyRegistry registry = JallabyRegistry.getInstance();

	/**
	 * Receives an event for processing.
	 * 
	 * @param event The event to be received
	 * @return the result of processing the event
	 * @throws EventProcessingException in case of an error while processing the received event
	 */
	public EventResult receiveEvent(final Event event) throws EventProcessingException {
		StateMachine stateMachine = registry.get(
				event.getStateMachineName(), event.getInstanceId());
		
		if (stateMachine != null) {
			if (stateMachine.isValidEvent(event)) {
				return stateMachine.processEvent(event);
			} else {
				throw new EventProcessingException(eventInvalidError(event));
			}
		} else {
			throw new EventProcessingException(stateMachineUnknownError(event));
		}
	}

	private EventError eventInvalidError(final Event event) {
		return new EventError(
				event.getStateMachineName(),
				event.getInstanceId().toString(),
				event.getEventName(),
				"The given event is invalid for the given state machine.",
				100);
	}

	private EventError stateMachineUnknownError(final Event event) {
		return new EventError(
				event.getStateMachineName(),
				event.getInstanceId().toString(),
				event.getEventName(),
				"The given state machine is unknown.",
				200);
	}
}
