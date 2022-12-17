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
package org.jallaby.execution;

import java.util.List;

/**
 * A group of {@link TransitionAction}s to be executed during a {@link Transition},
 * either concurrently or sequentially.
 * 
 * @author Matthias Rothe
 */
public interface TransitionActionGroup {
	
	/**
	 * @return {@code true} if the {@link TransitionAction}s of this group are to be executed
	 * concurrently, {@code false} otherwise
	 */
	boolean isConcurrent();
	
	/**
	 * @return {@code true} if the {@link TransitionAction}s of this group are to be executed
	 * sequentially, {@code false} otherwise
	 */
	boolean isSequential();
	
	/**
	 * @return the list of {@link TransitionAction}s belonging to this group
	 */
	List<TransitionAction> getTransitionActions();
}
