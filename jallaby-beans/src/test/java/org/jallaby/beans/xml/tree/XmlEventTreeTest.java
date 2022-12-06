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

package org.jallaby.beans.xml.tree;

import org.jallaby.beans.xml.model.XmlEvent;
import org.jallaby.beans.xml.model.XmlModifier;
import org.testng.annotations.Test;

/**
 * @author Matthias Rothe
 */
public class XmlEventTreeTest {

	@Test(expectedExceptions = IllegalParentException.class)
	public void testExtendingFinalEventThrowsException() {
		XmlEventTree tree = new XmlEventTree();
		
		XmlEvent finalEvent = new XmlEvent();
		finalEvent.setName("final");
		finalEvent.setModifier(XmlModifier.xmlFinal);
		
		tree.add(finalEvent);
		
		XmlEvent extendingEvent = new XmlEvent();
		extendingEvent.setName("extending");
		extendingEvent.setXmlExtends("final");
		
		tree.add(extendingEvent);
	}
}
