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

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.UUID;

import org.jallaby.execution.State;
import org.jallaby.execution.StateMachine;
import org.testng.annotations.Test;

public class JallabyRegistryTest {
	private final String stateMachineName = "CoffeeMachine";
	private JallabyRegistry registry = JallabyRegistry.getInstance();

	@Test
	public void testRegisterStateMachine() {
		registry.register(new StateMachine(stateMachineName, mock(State.class), event -> true));
	}
	
	@Test(dependsOnMethods = "testRegisterStateMachine")
	public void testGetStateMachine() {
		UUID uuid = UUID.randomUUID();
		
		StateMachine stateMachine1 = registry.get(stateMachineName, uuid);
		StateMachine stateMachine2 = registry.get(stateMachineName, uuid);
		
		assertEquals(stateMachine1, stateMachine2);
		
		StateMachine stateMachine3 = registry.get(stateMachineName, UUID.randomUUID());
		
		assertNotEquals(stateMachine1, stateMachine3);
	}
	
	@Test(dependsOnMethods = "testGetStateMachine")
	public void testUnregisterStateMachine() {
		registry.unregister(stateMachineName);
		
		StateMachine stateMachine = registry.get(stateMachineName, UUID.randomUUID());
		
		assertNull(stateMachine);
	}
}
