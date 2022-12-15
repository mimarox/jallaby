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

package org.jallaby.beans.annotations;

/**
 * Annotation to be used solely as parameter values for {@link Transition#actionGroups()}.
 * It defines the action groups used within the transition where it is specified.
 * 
 * @author Matthias Rothe
 */
public @interface ActionGroup {
	
	/**
	 * @return the name of this action group.
	 */
	String name();
	
	/**
	 * @return the concurrency mode of this action group. It may either be
	 * {@link Concurrency#CONCURRENT} or {@link Concurrency#SEQUENTIAL}.
	 */
	Concurrency concurrency();
}
