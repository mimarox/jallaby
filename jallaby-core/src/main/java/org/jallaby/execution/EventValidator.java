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

package org.jallaby.execution;

import org.jallaby.event.Event;

/**
 * Interface for validating events.
 * 
 * @author Matthias Rothe
 */
public interface EventValidator {
	
	/**
	 * Checks whether the given event is valid for this state machine.
	 * 
	 * @param event the event to check
	 * @return <code>true</code> if and only if the given event is valid, <code>false</code> otherwise.
	 */
	boolean isValidEvent(Event event);
}
