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

package org.jallaby.beans.metamodel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jallaby.beans.BeansRegistry;
import org.jallaby.beans.annotations.EntryAction;
import org.jallaby.beans.annotations.ExitAction;
import org.jallaby.beans.metamodel.sourcing.BeanClasses;
import org.jallaby.beans.util.TypeToken;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.model.effective.EffectiveXmlTransition;
import org.jallaby.event.Event;
import org.jallaby.event.EventProcessingException;
import org.jallaby.event.InvalidEventException;
import org.jallaby.execution.FinishState;
import org.jallaby.execution.State;
import org.jallaby.execution.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * A state instance that binds XML data to bean classes.
 * 
 * @author Matthias Rothe
 */
public class MetaState implements LifecycleBean, State {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetaState.class);
	
	private final String name;
	private final BeanClasses beanClasses;
	private final EffectiveXmlStateMachine xmlStateMachine;
	private final BeansRegistry beansRegistry;
	private final Injector injector;
	
	private final List<Event> offeredEvents = new ArrayList<>();
	private List<EffectiveXmlTransition> candidateXmlTransitions = new ArrayList<>();
	
	private EffectiveXmlState xmlState;
	private Method preDestroyMethod;
	private Method entryActionMethod;
	private Method exitActionMethod;
	private Object instance;
	
	/**
	 * Ctor.
	 * 
	 * @param name the name of the state
	 * @param beanClasses the bean classes
	 * @param xmlStateMachine the XML state machine
	 * @param beansRegistry the beans registry
	 * @param injector the injector
	 */
	public MetaState(final String name, final BeanClasses beanClasses,
			final EffectiveXmlStateMachine xmlStateMachine, final BeansRegistry beansRegistry,
			final Injector injector) {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(beanClasses, "beanClasses must not be null");
		Objects.requireNonNull(xmlStateMachine, "xmlStateMachine must not be null");
		Objects.requireNonNull(beansRegistry, "beansRegistry must not be null");
		Objects.requireNonNull(injector, "injector must not be null");
		
		this.name = name;
		this.beanClasses = beanClasses;
		this.xmlStateMachine = xmlStateMachine;
		this.beansRegistry = beansRegistry;
		this.injector = injector;
		
		postConstruct();
	}

	@Override
	public void postConstruct() {
		beansRegistry.registerState(this);
		xmlState = xmlStateMachine.getStateByName(name);
		
		Class<?> stateClass = findStateClass();
		instance = injector.getInstance(stateClass);
		Method postConstructMethod = findPostConstructMethod(stateClass);
		preDestroyMethod = findPreDestroyMethod(stateClass);
		entryActionMethod = findEntryActionMethod(stateClass);
		exitActionMethod = findExitActionMethod(stateClass);
		
		try {
			if (postConstructMethod != null) {
				postConstructMethod.invoke(instance, (Object[]) null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Unable to execute post construct method", e);
		}
	}

	private Class<?> findStateClass() {
		Optional<Class<?>> optionalStateClass = beanClasses.getStates().stream().filter(clazz -> {
			org.jallaby.beans.annotations.State stateAnnotation =
					clazz.getAnnotation(org.jallaby.beans.annotations.State.class);
			
			return stateAnnotation != null && stateAnnotation.name().equals(name);
		}).findFirst();
		
		if (optionalStateClass.isPresent()) {
			return optionalStateClass.get();
		} else {
			throw new NoSuchElementException(String.format(
					"No @State annotated class with state name [%s] could be found", name));
		}
	}

	private Method findPostConstructMethod(Class<?> stateClass) {
		Method[] methods = stateClass.getMethods();
		
		for (Method method : methods) {
			PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
			
			if (postConstruct != null && method.getParameterCount() == 0) {
				return method;
			}
		}
		
		return null;
	}

	private Method findPreDestroyMethod(Class<?> stateClass) {
		Method[] methods = stateClass.getMethods();
		
		for (Method method : methods) {
			PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
			
			if (preDestroy != null && method.getParameterCount() == 0) {
				return method;
			}
		}
		
		return null;
	}

	private Method findEntryActionMethod(Class<?> stateClass) {
		Method[] methods = stateClass.getMethods();
		
		for (Method method : methods) {
			EntryAction entryAction = method.getAnnotation(EntryAction.class);
			
			if (entryAction != null && takesEventDataAsOnlyParameter(method)) {
				return method;
			}
		}
		
		return null;
	}

	private Method findExitActionMethod(Class<?> stateClass) {
		Method[] methods = stateClass.getMethods();
		
		for (Method method : methods) {
			ExitAction exitAction = method.getAnnotation(ExitAction.class);
			
			if (exitAction != null && takesEventDataAsOnlyParameter(method)) {
				return method;
			}
		}
		
		return null;
	}

	private boolean takesEventDataAsOnlyParameter(Method method) {
		TypeToken<Map<String, Map<String, Object>>> token =
				new TypeToken<Map<String, Map<String, Object>>>(){};
		
		Type[] parameterTypes = method.getGenericParameterTypes();
		
		return parameterTypes.length == 1 && parameterTypes[0].equals(token.getType());
	}

	@Override
	public Transition offerEvent(Event event) throws EventProcessingException {
		Set<EffectiveXmlTransition> xmlTransitions;
		
		if (this.candidateXmlTransitions.isEmpty()) {
			xmlTransitions = xmlState.getTransitions();
		} else {
			xmlTransitions = new HashSet<>(this.candidateXmlTransitions);
		}
		
		List<EffectiveXmlTransition> candidateXmlTransitions =
		xmlTransitions.stream()
		.filter(xmlTransition -> xmlTransition.getEventRefs().contains(event.getEventName()))
		.collect(Collectors.toList());
		
		if (candidateXmlTransitions.isEmpty()) {
			throw new InvalidEventException("There is no transition referencing the event"
					+ " named " + event.getEventName());
		}
		
		offeredEvents.add(event);

		for (EffectiveXmlTransition xmlTransition : candidateXmlTransitions) {
			if (transitionMatchesExactly(xmlTransition, event, offeredEvents)) {
				MetaTransition transition = beansRegistry.getTransition(name, xmlTransition.getTo());
				
				if (transition != null) {
					return transition;
				} else {
					return new MetaTransition(name, xmlTransition.getTo(), beanClasses,
							xmlStateMachine, beansRegistry, injector);
				}
			}
		}
		
		this.candidateXmlTransitions = candidateXmlTransitions;		
		return null;
	}

	private boolean transitionMatchesExactly(EffectiveXmlTransition xmlTransition, Event event,
			List<Event> offeredEvents) {
		List<Event> allEvents = new ArrayList<>(offeredEvents);
		allEvents.add(event);
		
		List<String> allEventRefs = allEvents.stream()
		.map(e -> e.getEventName()).distinct().collect(Collectors.toList());
		
		return xmlTransition.getEventRefs().containsAll(allEventRefs) &&
				xmlTransition.getEventRefs().size() == allEventRefs.size();
	}

	@Override
	public Map<String, Map<String, Object>> getEventData() {
		Map<String, Map<String, Object>> eventData = new HashMap<>();
		
		for (Event event : offeredEvents) {
			eventData.put(event.getEventName(), event.getPayload());
		}
		
		return eventData;
	}

	@Override
	public FinishState performEntryAction(Map<String, Map<String, Object>> eventData) {
		try {
			if (entryActionMethod != null) {
				Object result = entryActionMethod.invoke(instance, eventData);
				
				if (result != null && result == FinishState.FINISHED) {
					return (FinishState) result;
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Unable to execute entry action method", e);
		}
		
		return FinishState.ONGOING;
	}

	@Override
	public void performExitAction(Map<String, Map<String, Object>> eventData) {
		offeredEvents.clear();

		try {
			if (exitActionMethod != null) {
				exitActionMethod.invoke(instance, eventData);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Unable to execute exit action method", e);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void preDestroy() {
		try {
			if (preDestroyMethod != null) {
				preDestroyMethod.invoke(instance, (Object[]) null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Unable to execute pre destroy method", e);
		}
	}
}
