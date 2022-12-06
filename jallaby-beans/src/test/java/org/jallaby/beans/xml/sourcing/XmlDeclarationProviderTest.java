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

package org.jallaby.beans.xml.sourcing;

import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jallaby.beans.xml.model.XmlModifier;
import org.jdom2.Element;
import org.testng.annotations.Test;

/**
 * @author Matthias Rothe
 */
public class XmlDeclarationProviderTest {

	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void shouldNotProvideFromEmptyRootElement() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test
	public void shouldProvideValidStateMachine() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		assertNotNull(provider.provideFromRootElement(root));
	}

	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void shouldNotProvideStateMachineWithUnknownInitialState()
			throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "Unknown");
		
		List<Element> events = buildEventElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void testEmptyEventExtends() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventWithEmptyExtendsElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}

	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void testEmptyEventName() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventWithEmptyNameElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test
	public void testEventWithAbstractModifier() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventWithAbstractModifierElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test
	public void testEventWithFinalModifier() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventWithFinalModifierElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void testEventWithInvalidModifier() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildEventWithInvalidModifierElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}
	
	@Test(expectedExceptions = InvalidStateMachineException.class)
	public void testStateMachineWithAbstractEventsOnly() throws InvalidStateMachineException {
		Element root = new Element("state-machine");
		
		root.setAttribute("name", "CoffeeMachine");
		root.setAttribute("initial-state", "SwitchedOff");
		
		List<Element> events = buildAbstractEventsOnlyElements();
		List<Element> states = buildStateElements();
		
		root.addContent(events);
		root.addContent(states);
		
		XmlDeclarationProvider provider = new XmlDeclarationProvider();
		provider.provideFromRootElement(root);
	}

	private List<Element> buildEventElements() {
		List<Element> events = new ArrayList<>();
		
		events.add(newEvent("switchOn"));
		events.add(newEventWithProperty("makeCoffee", "type", "string"));
		events.add(newEvent("waterEmpty"));
		events.add(newEvent("coffeeEmpty"));
		events.add(newEvent("milkEmpty"));
		events.add(newEvent("sugarEmpty"));
		events.add(newEvent("finished"));
		events.add(newEvent("switchOff"));

		return events;
	}

	private List<Element> buildEventWithEmptyExtendsElements() {
		List<Element> events = new ArrayList<>();
		events.add(newEventWithExtends("switchOn", ""));
		return events;
	}
	
	private List<Element> buildEventWithEmptyNameElements() {
		List<Element> events = new ArrayList<>();
		events.add(newEvent(""));
		return events;
	}
	
	private List<Element> buildEventWithAbstractModifierElements() {
		List<Element> events = new ArrayList<>();
		
		events.add(newEvent("switchOn"));
		events.add(newEventWithProperty("makeCoffee", "type", "string"));
		events.add(newEvent("waterEmpty"));
		events.add(newEvent("coffeeEmpty"));
		events.add(newEvent("milkEmpty"));
		events.add(newEvent("sugarEmpty"));
		events.add(newEvent("finished"));
		events.add(newEvent("switchOff"));
		events.add(newEventWithModifier("abstractEvent", "abstract"));
		
		return events;
	}
	
	private List<Element> buildEventWithFinalModifierElements() {
		List<Element> events = new ArrayList<>();
		
		events.add(newEventWithModifier("switchOn", "final"));
		events.add(newEventWithProperty("makeCoffee", "type", "string"));
		events.add(newEvent("waterEmpty"));
		events.add(newEvent("coffeeEmpty"));
		events.add(newEvent("milkEmpty"));
		events.add(newEvent("sugarEmpty"));
		events.add(newEvent("finished"));
		events.add(newEvent("switchOff"));
		
		return events;
	}
	
	private List<Element> buildAbstractEventsOnlyElements() {
		List<Element> events = new ArrayList<>();
		events.add(newEventWithModifier("abstractEvent", "abstract"));
		return events;
	}

	private List<Element> buildEventWithInvalidModifierElements() {
		List<Element> events = new ArrayList<>();
		events.add(newEventWithModifier("switchOn", "xyz"));
		return events;
	}

	private Element newEvent(String name) {
		Element event = new Element("event");
		event.setAttribute("name", name);
		return event;
	}

	private Element newEventWithExtends(String name, String xmlExtends) {
		Element event = newEvent(name);
		event.setAttribute("extends", xmlExtends);
		return event;
	}

	private Element newEventWithProperty(String name, String propertyName, String propertyType) {
		Element event = newEvent(name);
		
		Element property = new Element("property");
		property.setAttribute("name", propertyName);
		property.setAttribute("type", propertyType);
		
		event.addContent(property);
		
		return event;
	}

	private Element newEventWithModifier(String name, String xmlModifier) {
		Element event = newEvent(name);
		event.setAttribute("modifier", xmlModifier);
		return event;
	}

	private List<Element> buildStateElements() {
		List<Element> states = new ArrayList<>();
		List<Element> empty = new ArrayList<>();
		
		states.add(newState("SwitchedOff", buildSwitchedOffTransitions()));
		states.add(newState("SwitchedOn", empty));
		states.add(newState("Idle", "SwitchedOn", buildIdleTransitions()));
		states.add(newState("Finishable", "SwitchedOn", XmlModifier.xmlAbstract,
				buildFinishableTransitions()));
		states.add(newState("MakingCoffee", "Finishable", empty));
		states.add(newState("RefillingWater", "Finishable", empty));
		states.add(newState("RefillingCoffee", "Finishable", empty));
		states.add(newState("RefillingMilk", "Finishable", empty));
		states.add(newState("RefillingSugar", "Finishable", empty));

		return states;
	}

	private Element newState(String name, List<Element> transitions) {
		Element state = new Element("state");
		
		state.setAttribute("name", name);
		state.addContent(transitions);
		
		return state;
	}

	private Element newState(String name, String xmlExtends, List<Element> transitions) {
		Element state = newState(name, transitions);
		state.setAttribute("extends", xmlExtends);
		return state;
	}

	private Element newState(String name, String xmlExtends, XmlModifier xmlModifier,
			List<Element> transitions) {
		Element state = newState(name, xmlExtends, transitions);
		String modifier;
		
		switch (xmlModifier) {
		case xmlAbstract:
			modifier = "abstract";
			break;
		case xmlFinal:
			modifier = "final";
			break;
		default:
			modifier = null;
		}
		
		if (modifier != null) {
			state.setAttribute("modifier", modifier);
		}
		
		return state;
	}

	private List<Element> buildSwitchedOffTransitions() {
		List<Element> transitions = new ArrayList<>();
		transitions.add(newTransition("Idle", Arrays.asList(newEventRef("switchOn"))));
		return transitions;
	}

	private List<Element> buildIdleTransitions() {
		List<Element> transitions = new ArrayList<>();
		
		transitions.add(newTransition("MakingCoffee", Arrays.asList(newEventRef("makeCoffee"))));
		transitions.add(newTransition("RefillingWater", Arrays.asList(newEventRef("waterEmpty"))));
		transitions.add(newTransition("RefillingCoffee", Arrays.asList(newEventRef("coffeeEmpty"))));
		transitions.add(newTransition("RefillingMilk", Arrays.asList(newEventRef("milkEmpty"))));
		transitions.add(newTransition("RefillingSugar", Arrays.asList(newEventRef("sugarEmpty"))));
		transitions.add(newTransition("SwitchedOff", Arrays.asList(newEventRef("switchOff"))));
		
		return transitions;
	}

	private List<Element> buildFinishableTransitions() {
		List<Element> transitions = new ArrayList<>();
		transitions.add(newTransition("Idle", Arrays.asList(newEventRef("finished"))));
		return transitions;
	}

	private Element newTransition(String to, List<Element> eventRefs) {
		Element transition = new Element("transition");
		
		transition.setAttribute("to", to);
		transition.addContent(eventRefs);
		
		return transition;
	}
	
	private Element newEventRef(String name) {
		Element eventRef = new Element("event-ref");
		
		eventRef.setAttribute("name", name);
		
		return eventRef;
	}
}
