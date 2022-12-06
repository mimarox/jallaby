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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jallaby.beans.util.StringUtils;
import org.jallaby.beans.xml.model.XmlEvent;
import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlProperty;
import org.jallaby.beans.xml.model.XmlState;
import org.jallaby.beans.xml.model.XmlTransition;
import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError.ValidationSection;
import org.jallaby.beans.xml.sourcing.validation.AllStatesReachableValidator;
import org.jallaby.beans.xml.sourcing.validation.StateMachineValidator;
import org.jallaby.beans.xml.sourcing.validation.TransitionsValidator;
import org.jallaby.beans.xml.tree.XmlEventTreePool;
import org.jallaby.beans.xml.tree.XmlStateTreePool;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

/**
 * This class reads the state-machine.xml file from an InputStream and parses its
 * contents into an {@link EffectiveXmlStateMachine} object.
 * 
 * @author Matthias Rothe
 */
public class XmlDeclarationProvider {

	/**
	 * Reads the state-machine.xml file from the given InputStream and
	 * parses its contents into an {@link EffectiveXmlStateMachine} object.
	 * 
	 * @param in the InputStream to read from
	 * @return the {@link EffectiveXmlStateMachine}
	 * @throws InvalidStateMachineException if the state machine to be provided is invalid
	 * @throws IOException if an I/O error occurs
	 * @throws JDOMException if an error occurs with the XML state machine declaration
	 */
	public EffectiveXmlStateMachine provide(InputStream in)
			throws InvalidStateMachineException, IOException, JDOMException {
		SAXBuilder builder = new SAXBuilder(XMLReaders.DTDVALIDATING);
		Element rootElement = builder.build(in).detachRootElement();
		
		return provideFromRootElement(rootElement);
	}
	
	EffectiveXmlStateMachine provideFromRootElement(Element rootElement)
			throws InvalidStateMachineException {
		List<StateMachineValidationError> validationErrors = new ArrayList<>();

		try {
			String name = rootElement.getAttributeValue("name");
			String initialState = rootElement.getAttributeValue("initial-state");
			Set<EffectiveXmlEvent> events = buildEffectiveXmlEvents(rootElement, validationErrors);
			
			XmlStateTreePool stateTreePool = new XmlStateTreePool();
			Set<EffectiveXmlState> states = buildEffectiveXmlStates(rootElement, events,
					validationErrors, stateTreePool);
			
			boolean throwAfterPrevalidation = false;
			
			if (StringUtils.isBlank(name)) {
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
						"The state machine's name must not be null, empty or only whitespace."));
				throwAfterPrevalidation = true;
			}
			
			
			if (StringUtils.isBlank(initialState)) {
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
						"The state machine's initial-state must not be null,"
						+ " empty or only whitespace."));
				throwAfterPrevalidation = true;
			}
			
			if (events.isEmpty()) {
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
						"The state machine definition contains no instantiable"
						+ " events and is therefore invalid."));
				throwAfterPrevalidation = true;
			}
			
			if (states.isEmpty()) {
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
						"The state machine definition contains no instantiable"
						+ " states and is therefore invalid."));
				throwAfterPrevalidation = true;
			}

			if (throwAfterPrevalidation) {
				throw new InvalidStateMachineException(validationErrors);
			}
			
			if (validationErrors.isEmpty()) {
				EffectiveXmlStateMachine stateMachine =
						new EffectiveXmlStateMachine(name, initialState, events, states, stateTreePool);
				
				validationErrors.addAll(validate(stateMachine));
				
				if (validationErrors.isEmpty()) {
					return stateMachine;
				} else {
					throw new InvalidStateMachineException(validationErrors);
				}
			} else {
				throw new InvalidStateMachineException(validationErrors);
			}
		} catch (Exception e) {
			if (e instanceof InvalidStateMachineException) {
				throw e;
			} else {
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
						"Invalid state machine."));
				
				throw new InvalidStateMachineException(validationErrors, e);
			}
		}
	}

	private Set<EffectiveXmlEvent> buildEffectiveXmlEvents(Element rootElement,
			List<StateMachineValidationError> validationErrors) {
		int numberOfAbstractEvents = 0;
		int numberOfTotalEvents = 0;
		
		XmlEventTreePool eventPool = new XmlEventTreePool();
		
		for (Element eventElement : rootElement.getChildren("event")) {
			boolean addEvent = true;
			numberOfTotalEvents++;
			
			XmlEvent event = new XmlEvent();
			
			String name = eventElement.getAttributeValue("name");
			
			if (StringUtils.isBlank(name)) {
				addEvent = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT,
						"The state machine definition contains an event with the name set"
						+ " to null, an empty string or a string that only contains whitespace."
						+ " This event is invalid."));
			} else {
				event.setName(name);
			}
			
			String xmlExtends = eventElement.getAttributeValue("extends");
			
			if (xmlExtends == null || !StringUtils.isBlank(xmlExtends)) {
				event.setXmlExtends(xmlExtends);
			} else {
				addEvent = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT,
						String.format("The event [%s] has the extends attribute set"
						+ " to an empty string or a string that only contains whitespace."
						+ " This event is invalid.", event.getName())));
			}
			
			String modifier = eventElement.getAttributeValue("modifier");
			
			if (modifier != null) {
				XmlModifier xmlModifier = XmlModifier.by(modifier);
				
				if (xmlModifier != null) {
					event.setModifier(xmlModifier);
					
					if (xmlModifier == XmlModifier.xmlAbstract) {
						numberOfAbstractEvents++;
					}
				} else {
					addEvent = false;
					validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT,
							String.format("The event [%s] has the modifier attribute set"
							+ " to [%s] but only the values 'abstract' and 'final' are"
							+ " allowed. This event is invalid.", event.getName(), modifier)));
				}
			}
			
			event.setProperties(buildXmlProperties(event, eventElement, validationErrors));
			
			if (addEvent) {
				eventPool.add(event);
			}
		}
		
		if (numberOfTotalEvents == 0) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT,
					"There are no events defined. Please define at least one"
					+ " non-abstract event for the state machine to be able to function."));
			
			return new HashSet<>();
		} else if (numberOfAbstractEvents == numberOfTotalEvents) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT,
					"There are only abstract events defined. Please define at least one"
					+ " non-abstract event for the state machine to be able to function."));
			
			return new HashSet<>();
		} else {
			return eventPool.calculateEffectiveEvents();
		}
	}

	private Set<XmlProperty> buildXmlProperties(XmlEvent event, Element eventElement,
			List<StateMachineValidationError> validationErrors) {
		Set<XmlProperty> properties = new HashSet<>();
		
		for (Element propertyElement : eventElement.getChildren("property")) {
			boolean addProperty = true;
			XmlProperty property = new XmlProperty();
			
			String name = propertyElement.getAttributeValue("name");
			
			if (StringUtils.isBlank(name)) {
				addProperty = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.EVENT_PROPERTY,
						String.format("The event [%s] contains a property with the name set"
						+ " to null, an empty string or a string that only contains whitespace."
						+ " This property is invalid.", event.getName())));
			} else {
				property.setName(name);
			}

			String visibility = propertyElement.getAttributeValue("visibility");

			if (visibility != null) {
				if (visibility.equalsIgnoreCase("private")) {
					if (event.getModifier() == XmlModifier.xmlAbstract) {
						addProperty = false;
						validationErrors.add(new StateMachineValidationError(
								ValidationSection.EVENT_PROPERTY,
								String.format("Private properties are not allowed"
										+ " for abstract events. Abstract event: [%s],"
										+ " Disallowed property: [%s]",  event.getName(),
										property.getName())));
					} else {
						property.setXmlPrivate(true);
					}
				} else {
					addProperty = false;
					validationErrors.add(new StateMachineValidationError(
							ValidationSection.EVENT_PROPERTY,
							String.format("The property [%s] of the event [%s] has its"
									+ " visibility attribute set to [%s]. This renders"
									+ " the property invalid. The only valid value for"
									+ " that attribute is 'private'.", property.getName(),
									event.getName(), visibility)));
				}
			}
			
			String type = propertyElement.getAttributeValue("type");
			
			if (isValidPropertyType(type)) {
				property.setType(type);
			} else {
				addProperty = false;
				validationErrors.add(
						new StateMachineValidationError(ValidationSection.EVENT_PROPERTY,
								String.format("The property [%s] of the event [%s] has the"
										+ " type attribute set to [%s]. This renders the"
										+ " property invalid. The allowed values for this"
										+ " attribute are ['int', 'long', 'double', 'boolean',"
										+ " 'string', 'list', 'map'].", property.getName(),
										event.getName(), type)));
			}
			
			if (addProperty) {
				properties.add(property);
			}
		}
		
		return properties;
	}

	private boolean isValidPropertyType(String type) {
		if (StringUtils.isBlank(type)) {
			return false;
		}
		
		switch (type) {
		//fall-through
		case "int":
		case "long":
		case "double":
		case "boolean":
		case "string":
		case "list":
		case "map":
			return true;
		//else
		default:
			return false;
		}
	}

	private Set<EffectiveXmlState> buildEffectiveXmlStates(final Element rootElement,
			final Set<EffectiveXmlEvent> events,
			final List<StateMachineValidationError> validationErrors,
			final XmlStateTreePool stateTreePool) {
		int numberOfAbstractStates = 0;
		int numberOfTotalStates = 0;
		
		for (Element stateElement : rootElement.getChildren("state")) {
			boolean addState = true;
			numberOfTotalStates++;
			
			XmlState state = new XmlState();
			
			String name = stateElement.getAttributeValue("name");
			
			if (StringUtils.isBlank(name)) {
				addState = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
						"The state machine definition contains a state with the name set"
						+ " to null, an empty string or a string that only contains whitespace."
						+ " This state is invalid."));
			} else {
				state.setName(name);
			}
			
			String xmlExtends = stateElement.getAttributeValue("extends");
			
			if (xmlExtends == null || !StringUtils.isBlank(xmlExtends)) {
				state.setXmlExtends(xmlExtends);
			} else {
				addState = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
						String.format("The state [%s] has the extends attribute set"
						+ " to an empty string or a string that only contains whitespace."
						+ " This state is invalid.", state.getName())));
			}
			
			String modifier = stateElement.getAttributeValue("modifier");
			
			if (modifier != null) {
				XmlModifier xmlModifier = XmlModifier.by(modifier);
				
				if (xmlModifier != null) {
					state.setModifier(xmlModifier);
					
					if (xmlModifier == XmlModifier.xmlAbstract) {
						numberOfAbstractStates++;
					}
				} else {
					addState = false;
					validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
							String.format("The state [%s] has the modifier attribute set"
							+ " to [%s] but only the values 'abstract' and 'final' are"
							+ " allowed. This state is invalid.", state.getName(), modifier)));
				}
			}
			
			state.setTransitions(buildXmlTransitions(state, stateElement, events, validationErrors));
			
			if (addState) {
				stateTreePool.add(state);
			}
		}
		
		if (numberOfTotalStates < 2) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
					"There are less than two states defined. Please define at least two"
					+ " non-abstract states for the state machine to be able to function."));
			
			return new HashSet<>();
		} else if (numberOfAbstractStates == numberOfTotalStates) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
					"There are only abstract states defined. Please define at least two"
					+ " non-abstract states for the state machine to be able to function."));
			
			return new HashSet<>();
		} else if (numberOfAbstractStates == numberOfTotalStates - 1) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
					"There is only one non-abstract state defined. Please define at least two"
					+ " non-abstract states for the state machine to be able to function."));
			
			return new HashSet<>();
		} else {
			return stateTreePool.calculateEffectiveStates();
		}
	}

	private Set<XmlTransition> buildXmlTransitions(XmlState state, Element stateElement,
			Set<EffectiveXmlEvent> events, List<StateMachineValidationError> validationErrors) {
		
		Set<XmlTransition> transitions = new HashSet<>();
		
		for (Element transitionElement : stateElement.getChildren("transition")) {
			boolean addTransition = true;
			XmlTransition transition = new XmlTransition();
			
			String visibility = transitionElement.getAttributeValue("visibility");

			if (visibility != null) {
				if (visibility.equalsIgnoreCase("private")) {
					if (state.getModifier() == XmlModifier.xmlAbstract) {
						addTransition = false;
						validationErrors.add(
								new StateMachineValidationError(ValidationSection.TRANSITION,
										"Private transitions are not allowed for abstract states. "
										+ "Abstract state: " + state.getName()));
					} else {
						transition.setXmlPrivate(true);
					}
				} else {
					addTransition = false;
					validationErrors.add(new StateMachineValidationError(
							ValidationSection.TRANSITION,
							String.format("The transition to [%s] of the state [%s] has its"
									+ " visibility attribute set to [%s]. This renders"
									+ " the transition invalid. The only valid value for"
									+ " that attribute is 'private'.", transition.getTo(),
									state.getName(), visibility)));
				}
			}
			
			String to = transitionElement.getAttributeValue("to");
			
			if (StringUtils.isBlank(to)) {
				addTransition = false;
				validationErrors.add(new StateMachineValidationError(ValidationSection.TRANSITION,
						String.format("The state [%s] contains a transition with the 'to'"
						+ " attribute set to null, an empty string or a string that only"
						+ " contains whitespace. This transition is invalid.", state.getName())));
			} else {
				transition.setTo(to);
			}

			transition.setEvents(buildTransitionEvents(
					state, transition, transitionElement, events, validationErrors));
			
			if (addTransition) {
				transitions.add(transition);
			}
		}
		
		return transitions;
	}

	private Set<String> buildTransitionEvents(XmlState state, XmlTransition transition,
			Element transitionElement, Set<EffectiveXmlEvent> events,
			List<StateMachineValidationError> validationErrors) {
		Set<String> eventRefs = new HashSet<>();
		
		for (Element eventRefElement : transitionElement.getChildren("event-ref")) {
			String eventRef = eventRefElement.getAttributeValue("name");
			
			if (eventsContainEventRef(events, eventRef)) {
				eventRefs.add(eventRef);
			} else {
				validationErrors.add(new StateMachineValidationError(
						ValidationSection.EVENT_REF,
						String.format("Event reference [%s] for transition to"
								+ " [%s] on state [%s] does not reference an existing event.",
								eventRef, transition.getTo(), state.getName())));
			}
		}
		
		return eventRefs;
	}

	private boolean eventsContainEventRef(Set<EffectiveXmlEvent> events, String eventRef) {
		for (EffectiveXmlEvent event : events) {
			if (event.getName().equalsIgnoreCase(eventRef)) {
				return true;
			}
		}
		return false;
	}

	private List<StateMachineValidationError> validate(EffectiveXmlStateMachine stateMachine) {
		List<StateMachineValidationError> validationErrors = new ArrayList<>();
		
		// validate
		// - that the two required states are not parent and child and that there is more
		//   variety than just one single chain of direct inheritance (child-parent-grandparent etc)
		// - there is no event that extends from any of its children
		//   (write at least a test case, as the XmlEventTree class might already assure that)
		// - there is no state that extends from any of its children
		//   (write at least a test case, as the XmlStateTree class might already assure that)
		
		List<StateMachineValidator> validators = new ArrayList<StateMachineValidator>();
		validators.add(new TransitionsValidator());
		validators.add(new AllStatesReachableValidator()); // must be the last validator

		validators.forEach(validator -> validator.validate(stateMachine, validationErrors));
				
		return validationErrors;
	}
}
