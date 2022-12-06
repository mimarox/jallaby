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

package org.jallaby.beans.util;

/**
 * @author Matthias Rothe
 */
public class StringUtils {

	private StringUtils() {}
	
	/**
	 * Returns whether the given string is null, the empty string or the empty
	 * string after removing all leading and trailing whitespace.
	 * 
	 * @param string the string to test for being blank
	 * @return <code>true</code>, if and only if the given string is null, the empty
	 * string or the empty string after removing all leading and trailing whitespace,
	 * <code>false</code> otherwise
	 */
	public static boolean isBlank(String string) {
		return string == null || string.trim().isEmpty();
	}
}
