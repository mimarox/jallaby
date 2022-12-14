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
 * Enum listing the concurrency modes used for {@link ActionGroup}s.
 * 
 * @author Matthias Rothe
 */
public enum Concurrency {
	
	/**
	 * The actions in the {@link ActionGroup} are executed concurrently.
	 */
	CONCURRENT,
	
	/**
	 * The actions in the {@link ActionGroup} are executed sequentially.
	 */
	SEQUENTIAL
}
