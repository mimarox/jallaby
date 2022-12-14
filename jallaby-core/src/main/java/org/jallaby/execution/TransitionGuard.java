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

/**
 * A guard checking whether the {@link Transition} it belongs to can proceed or not.
 * 
 * @author Matthias Rothe
 */
public interface TransitionGuard {
	
	/**
	 * @param eventData the event data of all events that led to this {@link Transition} being executed
	 * @return {@code true} if and only if the {@link Transition} can proceed, {@code false} otherwise
	 */
	boolean canProceed(Map<String, Map<String, Object>> eventData);
}
