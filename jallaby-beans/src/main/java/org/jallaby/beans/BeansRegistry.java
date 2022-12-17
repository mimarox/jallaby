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

package org.jallaby.beans;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jallaby.beans.metamodel.LifecycleBean;
import org.jallaby.beans.metamodel.MetaState;
import org.jallaby.beans.metamodel.MetaTransition;

public class BeansRegistry {
	private final Set<MetaState> states = new CopyOnWriteArraySet<>();
	private final Set<MetaTransition> transitions = new CopyOnWriteArraySet<>();
	
	public synchronized Set<LifecycleBean> getAllLifecycleBeans() {
		HashSet<LifecycleBean> beans = new HashSet<>();
		
		beans.addAll(states);
		beans.addAll(transitions);
		
		return beans;
	}
	
	public synchronized void registerState(final MetaState state) {
		Objects.requireNonNull(state, "state must not be null");
		states.add(state);
	}
	
	public synchronized void registerTransition(final MetaTransition transition) {
		Objects.requireNonNull(transition, "transition must not be null");
		transitions.add(transition);
	}

	public synchronized MetaState getState(String name) {
		return states.stream().filter(state ->
		Objects.equals(state.getName(), name)).findFirst().orElse(null);
	}

	public synchronized MetaTransition getTransition(final String fromState, String toState) {
		return transitions.stream().filter(transition ->
		Objects.equals(transition.fromState(), fromState) &&
		Objects.equals(transition.toState(), toState)).findFirst().orElse(null);
	}
}
