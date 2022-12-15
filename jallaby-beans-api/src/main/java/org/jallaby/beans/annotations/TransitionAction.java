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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Method level annotation to be used inside classes annotated with the {@link Transition}
 * annotation. Methods annotated with this annotation SHOULD  have a <code>void</code>
 * return type and MUST NOT take any arguments.
 * <p>
 * There is no limitation on the number of methods annotated with this annotation inside
 * a {@link Transition} annotated class.
 * 
 * @author Matthias Rothe
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface TransitionAction {
	
	/**
	 * @return the name of the {@link ActionGroup} this transition action belongs to.
	 */
	String group();
	
	/**
	 * @return the order in which sequentially executed transition actions will be executed
	 * within their group. This parameter is ignored for concurrently executed transition actions.
	 * @see ActionGroup
	 */
	int order() default 0;
}
