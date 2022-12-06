package org.jallaby.beans.sample001.transitions;

import org.jallaby.beans.annotations.Transition;
import org.jallaby.beans.annotations.TransitionGuard;
import org.jallaby.beans.sample001.business.CoffeeMachine;

import com.google.inject.Inject;

@Transition(fromState = "SwitchedOff", toState = "SwitchedOn")
public class SwitchedOffToSwitchedOnTransition {
	
	@Inject
	private CoffeeMachine coffeeMachine;
	
	@TransitionGuard
	public boolean enoughPower() {
		return coffeeMachine.hasPower();
	}
}
