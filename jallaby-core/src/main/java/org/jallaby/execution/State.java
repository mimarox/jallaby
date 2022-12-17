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

import java.util.Map;

import org.jallaby.event.Event;
import org.jallaby.event.EventProcessingException;

/**
 * States of a {@link StateMachine} implement this interface.
 * 
 * @author Matthias Rothe
 */
public interface State {
	
	/**
	 * Offers the state an event, upon which the state might return a transition
	 * to another state.
	 * 
	 * @param event the event to offer
	 * @return the transition, if any
	 * @throws EventProcessingException if an exception occurs while processing the event
	 */
	Transition offerEvent(Event event) throws EventProcessingException;
	
	/**
	 * @return the event data of all successfully offered states
	 */
	Map<String, Map<String, Object>> getEventData();
	
	/**
	 * The entry action for the state.
	 * 
	 * @param eventData the event data of all successfully offered states that led to this state being entered
	 * @return either {@link FinishState#FINISHED} or {@link FinishState#ONGOING}
	 * @see FinishState
	 */
	FinishState performEntryAction(Map<String, Map<String, Object>> eventData);
	
	/**
	 * The exit action for the state.
	 * 
	 * @param eventData the event data of all successfully offered states that led to this state being exited
	 */
	void performExitAction(Map<String, Map<String, Object>> eventData);
	
	/**
	 * @return the name of the state
	 */
	String getName();
}
