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
//

//CHECKSTYLE:OFF

package org.jallaby.beans.util;

/**
* Fast String Utilities.
*
* These string utilities provide both convenience methods and
* performance improvements over most standard library versions. The
* main aim of the optimizations is to avoid object creation unless
* absolutely required.
*/
public class StringUtils
{
	 /**
	  * Test if a string is null or only has whitespace characters in it.
	  * <p>
	  * Note: uses codepoint version of {@link Character#isWhitespace(int)} to support Unicode better.
	  *
	  * <pre>
	  *   isBlank(null)   == true
	  *   isBlank("")     == true
	  *   isBlank("\r\n") == true
	  *   isBlank("\t")   == true
	  *   isBlank("   ")  == true
	  *   isBlank("a")    == false
	  *   isBlank(".")    == false
	  *   isBlank(";\n")  == false
	  * </pre>
	  *
	  * @param str the string to test.
	  * @return true if string is null or only whitespace characters, false if non-whitespace characters encountered.
	  */
	 public static boolean isBlank(String str)
	 {
	     if (str == null)
	     {
	         return true;
	     }
	     int len = str.length();
	     for (int i = 0; i < len; i++)
	     {
	         if (!Character.isWhitespace(str.codePointAt(i)))
	         {
	             // found a non-whitespace, we can stop searching  now
	             return false;
	         }
	     }
	     // only whitespace
	     return true;
	 }
}
//CHECKSTYLE:ON
