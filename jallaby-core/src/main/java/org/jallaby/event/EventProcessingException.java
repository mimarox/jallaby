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
 * The exception to be thrown in case of an error during event processing.
 * 
 * @author Matthias Rothe
 */
public class EventProcessingException extends Exception {
	private static final long serialVersionUID = -1062251828891743277L;

	private final EventError error;
	
	/**
	 * Ctor taking an EventError object containing error information.
	 * 
	 * @param error The error object
	 */
	public EventProcessingException(final EventError error) {
		this(error, null);
	}
	
	/**
	 * Ctor taking an EventError object containing error information and a causing
	 * Throwable.
	 * 
	 * @param error The error object
	 * @param cause The cause of this exception
	 */	
	public EventProcessingException(final EventError error, final Throwable cause) {
		super(cause);
		
		Objects.requireNonNull(error, "error must not be null");
		this.error = error;
	}
	
	/**
	 * @return The error object
	 */
	public EventError getError() {
		return error;
	}
}
