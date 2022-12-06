package org.jallaby.beans.sample001.modules;

import org.jallaby.beans.sample001.business.CoffeeMachine;

import com.google.inject.Binder;
import com.google.inject.Module;

public class DefaultModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(CoffeeMachine.class).asEagerSingleton();
	}
}
