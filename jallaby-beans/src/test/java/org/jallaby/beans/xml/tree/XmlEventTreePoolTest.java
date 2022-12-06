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

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlEvent;
import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlProperty;
import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlProperty;
import org.testng.annotations.Test;

/**
 * @author Matthias Rothe
 */
public class XmlEventTreePoolTest {

	@Test
	public void testCreateEffectiveEvents() {
		XmlEventTreePool pool = new XmlEventTreePool();

		// XML Properties
		XmlProperty timedProperty = new XmlProperty();
		timedProperty.setName("timed");
		timedProperty.setType("long");
		
		XmlProperty personProperty = new XmlProperty();
		personProperty.setName("person");
		personProperty.setType("String");
		
		XmlProperty placeProperty = new XmlProperty();
		placeProperty.setName("place");
		placeProperty.setType("String");
		
		// Raw XML Property Sets
		Set<XmlProperty> eventCProperties = new HashSet<>();
		eventCProperties.add(timedProperty);
		
		Set<XmlProperty> eventBProperties = new HashSet<>();
		eventBProperties.add(personProperty);

		Set<XmlProperty> eventDProperties = new HashSet<>();
		eventDProperties.add(placeProperty);

		Set<XmlProperty> eventAProperties = new HashSet<>();
		eventAProperties.add(placeProperty);

		Set<XmlProperty> event0Properties = new HashSet<>();
		event0Properties.add(timedProperty);
		
		// Raw XML Events
		XmlEvent eventC = new XmlEvent();
		eventC.setModifier(XmlModifier.xmlAbstract);
		eventC.setName("EventC");
		eventC.setProperties(eventCProperties);
		
		pool.add(eventC);
		
		
		XmlEvent eventB = new XmlEvent();
		eventB.setName("EventB");
		eventB.setXmlExtends("Event0");
		eventB.setProperties(eventBProperties);
		
		pool.add(eventB);
		
		
		XmlEvent eventD = new XmlEvent();
		eventD.setName("EventD");
		eventD.setXmlExtends("EventC");
		eventD.setProperties(eventDProperties);
		
		pool.add(eventD);
		
		
		XmlEvent eventA = new XmlEvent();
		eventA.setName("EventA");
		eventA.setXmlExtends("Event0");
		eventA.setProperties(eventAProperties);
		
		pool.add(eventA);
		

		XmlEvent event0 = new XmlEvent();
		event0.setName("Event0");
		event0.setModifier(XmlModifier.xmlAbstract);
		event0.setProperties(event0Properties);
		
		pool.add(event0);
		
		// Effective Events
		Set<EffectiveXmlEvent> expectedEffectiveEvents = new HashSet<>();
		
		Set<EffectiveXmlProperty> effectiveEventBProperties = new HashSet<>();
		effectiveEventBProperties.add(new EffectiveXmlProperty("timed", "long"));
		effectiveEventBProperties.add(new EffectiveXmlProperty("person", "String"));
		
		expectedEffectiveEvents.add(new EffectiveXmlEvent("EventB", effectiveEventBProperties));
		
		
		Set<EffectiveXmlProperty> effectiveEventDProperties = new HashSet<>();
		effectiveEventDProperties.add(new EffectiveXmlProperty("timed", "long"));
		effectiveEventDProperties.add(new EffectiveXmlProperty("place", "String"));
		
		expectedEffectiveEvents.add(new EffectiveXmlEvent("EventD", effectiveEventDProperties));
		
		
		Set<EffectiveXmlProperty> effectiveEventAProperties = new HashSet<>();
		effectiveEventAProperties.add(new EffectiveXmlProperty("timed", "long"));
		effectiveEventAProperties.add(new EffectiveXmlProperty("place", "String"));
		
		expectedEffectiveEvents.add(new EffectiveXmlEvent("EventA", effectiveEventAProperties));
		
		// Assert
		Set<EffectiveXmlEvent> actualEffectiveEvents = pool.calculateEffectiveEvents();
		
		assertEquals(actualEffectiveEvents, expectedEffectiveEvents);
	}
}
