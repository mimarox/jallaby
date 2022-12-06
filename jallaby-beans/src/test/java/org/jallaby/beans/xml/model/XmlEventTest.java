package org.jallaby.beans.xml.model;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

public class XmlEventTest {

	@Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp=
			"Cannot add a private property to an abstract event.")
	public void shouldNotAddPrivatePropertyToAbstractEvent() {
		XmlEvent event = new XmlEvent();
		event.setModifier(XmlModifier.xmlAbstract);
		
		XmlProperty property = new XmlProperty();
		property.setXmlPrivate(true);
		
		Set<XmlProperty> properties = new HashSet<>();
		properties.add(property);
		
		event.setProperties(properties);
	}
	
	@Test
	public void shouldAddPrivatePropertyToNonAbstractEvent() {
		XmlEvent event = new XmlEvent();
		
		XmlProperty property = new XmlProperty();
		property.setXmlPrivate(true);
		
		Set<XmlProperty> properties = new HashSet<>();
		properties.add(property);
		
		event.setProperties(properties);
		
		assertEquals(event.getProperties(), properties);
	}
	
	@Test
	public void shouldAddNonPrivatePropertyToNonAbstractEvent() {
		XmlEvent event = new XmlEvent();
		
		XmlProperty property = new XmlProperty();
		
		Set<XmlProperty> properties = new HashSet<>();
		properties.add(property);
		
		event.setProperties(properties);
		
		assertEquals(event.getProperties(), properties);
	}	
}
