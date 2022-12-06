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

import java.util.List;

import org.jallaby.util.Assert;

/**
 * @author Matthias Rothe
 */
public class InvalidStateMachineException extends Exception {
	private static final long serialVersionUID = 5983514671626161934L;
	
	private final List<StateMachineValidationError> validationErrors;
	
	public InvalidStateMachineException(List<StateMachineValidationError> validationErrors) {
		this(validationErrors, null);
	}

	public InvalidStateMachineException(List<StateMachineValidationError> validationErrors,
			Exception cause) {
		super(cause);
		Assert.notEmpty(validationErrors, IllegalArgumentException.class,
				"validationErrors must not be null or empty");
		
		this.validationErrors = validationErrors;
	}

	public List<StateMachineValidationError> getValidationErrors() {
		return validationErrors;
	}
	
	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\n");
		
		for (StateMachineValidationError error : validationErrors) {
			builder.append(error).append("\n");
		}
		
		return builder.toString();
	}
}
