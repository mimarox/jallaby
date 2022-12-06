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

package org.jallaby.beans.xml.model;

/**
 * Enum of modifiers used in XML state machine declarations.
 * 
 * @author Matthias Rothe
 * @since RedRoo 1.0
 */
public enum XmlModifier {
	/**
	 * The abstract modifier.
	 */
	xmlAbstract,
	
	/**
	 * The final modifier.
	 */
	xmlFinal;
	
	public static XmlModifier by(String modifier) {
		if (modifier == null) {
			return null;
		} else if (modifier.equalsIgnoreCase("abstract")) {
			return xmlAbstract;
		} else if (modifier.equalsIgnoreCase("final")) {
			return xmlFinal;
		} else {
			return null;
		}
	}
}