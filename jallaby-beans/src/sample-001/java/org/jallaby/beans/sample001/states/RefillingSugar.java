package org.jallaby.beans.sample001.states;

import java.util.Map;

import org.jallaby.beans.annotations.EntryAction;
import org.jallaby.beans.annotations.ExitAction;
import org.jallaby.beans.annotations.State;
import org.jallaby.beans.sample001.business.CoffeeMachine;

import com.google.inject.Inject;

@State(name = "RefillingSugar")
public class RefillingSugar {

	@Inject
	private CoffeeMachine coffeeMachine;
	
	@EntryAction
	public void entryAction(final Map<String, Map<String, Object>> eventData) {
		coffeeMachine.openSugarTray();
	}
	
	@ExitAction
	public void exitAction(final Map<String, Map<String, Object>> eventData) {
		coffeeMachine.closeSugarTray();
	}
}
