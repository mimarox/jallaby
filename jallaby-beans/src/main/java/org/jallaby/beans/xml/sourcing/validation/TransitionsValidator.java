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

package org.jallaby.beans.xml.sourcing.validation;

import java.util.List;
import java.util.Set;

import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.model.effective.EffectiveXmlTransition;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError.ValidationSection;

/**
 * @author Matthias Rothe
 */
public class TransitionsValidator implements StateMachineValidator {

	/* (non-Javadoc)
	 * @see org.jallaby.beans.xml.sourcing.validation.StateMachineValidator#validate(
	 * org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine, java.util.List)
	 */
	@Override
	public void validate(EffectiveXmlStateMachine stateMachine, List<StateMachineValidationError> validationErrors) {
		Set<EffectiveXmlState> states = stateMachine.getStates();
		
		for (EffectiveXmlState fromState : states) {
			for (EffectiveXmlTransition transition : fromState.getTransitions()) {
				if (!states.stream().anyMatch(toState ->
				toState.getName().equalsIgnoreCase(transition.getTo()))) {
					validationErrors.add(new StateMachineValidationError(ValidationSection.TRANSITION,
							"The transition of state [" + fromState.getName() + "] refering to state ["
							+ transition.getTo() + "] is invalid as the state referred to doesn't exist."
							+ " It may be defined as abstract."));
				}
				
				if (refersToAnyParent(transition.getTo(), fromState.getParent())) {
					validationErrors.add(new StateMachineValidationError(ValidationSection.TRANSITION,
							String.format("The transition of state [%1$s] refering to state [%2$s] "
							+ "is invalid as the state referred to is a parent state of the state [%1$s].",
							fromState.getName(), transition.getTo())));
				}
			}
		}
	}

	private boolean refersToAnyParent(String to, EffectiveXmlState parent) {
		if (parent != null) {
			if (parent.getName().equalsIgnoreCase(to)) {
				return true;
			} else {
				return refersToAnyParent(to, parent.getParent());
			}
		} else {
			return false;
		}
	}
}
