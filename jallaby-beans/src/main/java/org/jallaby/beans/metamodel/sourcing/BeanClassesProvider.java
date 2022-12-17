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

package org.jallaby.beans.metamodel.sourcing;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jallaby.beans.annotations.State;
import org.jallaby.beans.annotations.Transition;
import org.jallaby.event.EventValidator;

import com.google.inject.Module;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;

/**
 * Provides the bean classes for the state machine.
 * 
 * @author Matthias Rothe
 */
public class BeanClassesProvider {
	private static final Object MUTEX = new Object();
	
	/**
	 * Provide the bean classes from the given URL and base packages.
	 * 
	 * @param url the URL to provide the bean classes from
	 * @param basePackages the base packages to provide the bean classes from
	 * @return the bean classes
	 */
	public BeanClasses provideFrom(URL url, String[] basePackages) {
		BeanClasses beanClasses = new BeanClasses();
		
		Set<Class<?>> states = new HashSet<>();
		Set<Class<?>> transitions = new HashSet<>();
		Set<Class<? extends EventValidator>> eventValidators = new HashSet<>();
		Set<Class<? extends Module>> modules = new HashSet<>();
		
		synchronized (MUTEX) {
			ComponentScanner scanner = new ComponentScanner();
			scanner.getClasses(new ComponentQuery() {
				
				@Override
				protected void query() {
					select().from(basePackages).within(url)
					.andStore(
						thoseAnnotatedWith(State.class).into(states),
						thoseAnnotatedWith(Transition.class).into(transitions),
						thoseImplementing(EventValidator.class).into(eventValidators),
						thoseImplementing(Module.class).into(modules)
					);
				}
			});
		}
				
		beanClasses.setStates(states);
		beanClasses.setTransitions(transitions);
		beanClasses.setEventValidators(eventValidators);
		beanClasses.setModules(modules);
		
		return beanClasses;
	}
}
