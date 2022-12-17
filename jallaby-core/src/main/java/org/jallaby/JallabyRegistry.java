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

package org.jallaby;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jallaby.execution.StateMachine;

/**
 * Holds the StateMachine instances by name and instance id.
 * 
 * @author Matthias Rothe
 */
public class JallabyRegistry {
	private static JallabyRegistry INSTANCE;
	
	private final Map<String, StateMachine> blueprints = new ConcurrentHashMap<>();
	private final Map<String, StateMachine> instances = new ConcurrentHashMap<>();
	
	private JallabyRegistry() {
	}

	public static JallabyRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new JallabyRegistry();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Returns the instance of a MetaStateMachine selected by the given state machine
	 * name and instance id.
	 * 
	 * @param stateMachineName The name of the state machine
	 * @param instanceId The instance id of the state machine
	 * @return the instance
	 */
	public StateMachine get(String stateMachineName, UUID instanceId) {
		String key = crateKey(stateMachineName, instanceId);
		
		StateMachine instance =	instances.get(key);
		
		if (instance == null) {
			StateMachine blueprint = blueprints.get(stateMachineName);
			
			if (blueprint != null) {
				instance = blueprint.newInstance();
				instances.put(key, instance);
			}
		}
		
		return instance;
	}

	/**
	 * Registers a new StateMachine.
	 * 
	 * @param stateMachine The instance to be registered
	 * @return the instance registered before with the given name and instance id, if any
	 */
	public StateMachine register(StateMachine stateMachine) {
		return blueprints.put(stateMachine.getName(), stateMachine);
	}
	
	private String crateKey(String stateMachineName, UUID instanceId) {
		return stateMachineName + "/" + instanceId;
	}

	/**
	 * Unregisters a StateMachine.
	 * 
	 * @param name The name of the state machine to be unregistered
	 */
	public void unregister(String name) {
		blueprints.remove(name);
		removeFromInstances(name);
	}
	
	private void removeFromInstances(final String name) {
		Iterator<String> iterator = instances.keySet().iterator();
		
		while (iterator.hasNext()) {
			String id = iterator.next();
			
			if (id.startsWith(name)) {
				iterator.remove();
			}
		}
	}
}
