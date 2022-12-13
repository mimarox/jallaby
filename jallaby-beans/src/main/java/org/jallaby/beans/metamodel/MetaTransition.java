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
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jallaby.beans.BeansRegistry;
import org.jallaby.beans.annotations.ActionGroup;
import org.jallaby.beans.annotations.Concurrency;
import org.jallaby.beans.metamodel.sourcing.BeanClasses;
import org.jallaby.beans.xml.model.XmlStateInfo;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.execution.State;
import org.jallaby.execution.StateInfo;
import org.jallaby.execution.Transition;
import org.jallaby.execution.TransitionAction;
import org.jallaby.execution.TransitionActionGroup;
import org.jallaby.execution.TransitionGuard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * A transition instance that binds XML data to bean classes.
 * 
 * @author Matthias Rothe
 */
public class MetaTransition implements LifecycleBean, Transition {
	private class MetaTransitionGuard implements TransitionGuard {
		private final Object instance;
		private final Method guardMethod;
		
		MetaTransitionGuard(final Object instance, final Method guardMethod) {
			Objects.requireNonNull(instance, "instance must not be null");
			Objects.requireNonNull(guardMethod, "guardMethod must not be null");
			
			this.instance = instance;
			this.guardMethod = guardMethod;
		}

		@Override
		public boolean canProceed() {
			try {
				return (boolean) guardMethod.invoke(instance, (Object[]) null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.warn(String.format("Transition guard invocation failed."
						+ " Class: [%s], Method: [%s]. Transition will not proceed!",
						instance.getClass().getCanonicalName(),	guardMethod.getName()), e);
				return false;
			}
		}
	}
	
	private class MetaTransitionActionGroup implements TransitionActionGroup {
		private final Concurrency concurrency;
		private final List<TransitionAction> transitionActions;
		
		MetaTransitionActionGroup(final Concurrency concurrency,
				final List<TransitionAction> transitionActions) {
			Objects.requireNonNull(concurrency, "concurrency must not be null");
			Objects.requireNonNull(transitionActions, "transitionActions must not be null");
			
			this.concurrency = concurrency;
			this.transitionActions = transitionActions;
		}
		
		@Override
		public boolean isConcurrent() {
			return concurrency == Concurrency.CONCURRENT;
		}

		@Override
		public boolean isSequential() {
			return concurrency == Concurrency.SEQUENTIAL;
		}

		@Override
		public List<TransitionAction> getTransitionActions() {
			return transitionActions;
		}
	}
	
	private class MetaTransitionAction implements TransitionAction {
		private final Object instance;
		private final Method actionMethod;
		
		MetaTransitionAction(final Object instance, final Method actionMethod) {
			Objects.requireNonNull(instance, "instance must not be null");
			Objects.requireNonNull(actionMethod, "actionMethod must not be null");
			
			this.instance = instance;
			this.actionMethod = actionMethod;
		}
		
		@Override
		public void run() {
			try {
				actionMethod.invoke(instance, (Object[]) null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.warn(String.format("Transition action invocation failed."
						+ " Class: [%s], Method: [%s]",
						instance.getClass().getCanonicalName(),	actionMethod.getName()), e);
			}
		}
	}
	
	private class OrderedMetaTransitionAction implements Comparable<OrderedMetaTransitionAction> {
		private final int order;
		private final MetaTransitionAction action;
		
		OrderedMetaTransitionAction(final int order, final MetaTransitionAction action) {
			Objects.requireNonNull(action, "action must not be null");
			
			this.order = order;
			this.action = action;
		}
		
		public MetaTransitionAction getAction() {
			return action;
		}
		
		@Override
		public int compareTo(OrderedMetaTransitionAction action) {
			return order - action.order;
		}
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MetaTransition.class);
	
	private final String fromState;
	private final String toState;
	private final BeanClasses beanClasses;
	private final EffectiveXmlStateMachine xmlStateMachine;
	private final BeansRegistry beansRegistry;
	private final Injector injector;
	
	private boolean virtual;
	private Object instance;
	private Method preDestroyMethod;
	private StateInfo stateInfo;
	private final List<TransitionGuard> transitionGuards = new ArrayList<>();
	private final List<TransitionActionGroup> transitionActionGroups = new ArrayList<>();
	
	public MetaTransition(final String fromState, final String toState, final BeanClasses beanClasses,
			final EffectiveXmlStateMachine xmlStateMachine,	final BeansRegistry beansRegistry,
			final Injector injector) {
		Objects.requireNonNull(fromState, "fromState must not be null");
		Objects.requireNonNull(toState, "toState must not be null");
		Objects.requireNonNull(beanClasses, "beanClasses must not be null");
		Objects.requireNonNull(xmlStateMachine, "xmlStateMachine must not be null");
		Objects.requireNonNull(beansRegistry, "beansRegistry must not be null");
		Objects.requireNonNull(injector, "injector must not be null");
		
		this.fromState = fromState;
		this.toState = toState;
		this.beanClasses = beanClasses;
		this.xmlStateMachine = xmlStateMachine;
		this.beansRegistry = beansRegistry;
		this.injector = injector;
		
		postConstruct();
	}

	@Override
	public void postConstruct() {
		beansRegistry.registerTransition(this);
		
		Class<?> transitionClass = findTransitionClass();
		
		if (transitionClass != null) {
			instance = injector.getInstance(transitionClass);
			transitionGuards.addAll(buildMetaTransitionGuards(transitionClass));
			transitionActionGroups.addAll(buildTransitionActionGroups(transitionClass));
			Method postConstructMethod = findPostConstructMethod(transitionClass);
			preDestroyMethod = findPreDestroyMethod(transitionClass);
			
			try {
				if (postConstructMethod != null) {
					postConstructMethod.invoke(instance, (Object[]) null);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOGGER.warn("Unable to execute post construct method", e);
			}
		} else {
			virtual = true;
		}
		
		stateInfo = buildStateInfo();
	}

	private Class<?> findTransitionClass() {
		return beanClasses.getTransitions().stream()
		.filter(clazz -> {
			org.jallaby.beans.annotations.Transition transitionAnnotation =
					clazz.getAnnotation(org.jallaby.beans.annotations.Transition.class);
			
			return transitionAnnotation != null &&
					transitionAnnotation.fromState().equals(fromState) &&
					transitionAnnotation.toState().equals(toState);
		}).findFirst().orElse(null);
	}

	private List<MetaTransitionGuard> buildMetaTransitionGuards(final Class<?> transitionClass) {
		List<MetaTransitionGuard> metaTransitionGuards = new ArrayList<>();
		Method[] methods = transitionClass.getMethods();
		
		for (Method method : methods) {
			org.jallaby.beans.annotations.TransitionGuard transitionGuard =
					method.getAnnotation(org.jallaby.beans.annotations.TransitionGuard.class);
			
			if (transitionGuard != null && method.getParameterCount() == 0) {
				metaTransitionGuards.add(new MetaTransitionGuard(instance, method));
			}
		}
		
		return metaTransitionGuards;
	}

	private List<MetaTransitionActionGroup> buildTransitionActionGroups(final Class<?> transitionClass) {
		org.jallaby.beans.annotations.Transition transitionAnnotation =
				transitionClass.getAnnotation(org.jallaby.beans.annotations.Transition.class);
		
		ActionGroup[] actionGroups = transitionAnnotation.actionGroups();
		
		List<MetaTransitionActionGroup> metaActionGroups = new ArrayList<>();
		
		for (ActionGroup actionGroup : actionGroups) {
			metaActionGroups.add(new MetaTransitionActionGroup(actionGroup.concurrency(),
					buildTransitionActions(actionGroup, transitionClass)));
		}
		
		return metaActionGroups;
	}

	private List<TransitionAction> buildTransitionActions(final ActionGroup actionGroup,
			final Class<?> transitionClass) {
		switch (actionGroup.concurrency()) {
			case CONCURRENT:
				return buildConcurrentTransitionActions(actionGroup.name(), transitionClass);
			case SEQUENTIAL:
				return buildSequentialTransitionActions(actionGroup.name(), transitionClass);
			default:
				return new ArrayList<>();
		}
	}

	private List<TransitionAction> buildConcurrentTransitionActions(final String group,
			final Class<?> transitionClass) {
		List<TransitionAction> actions = new ArrayList<>();
		
		for (Method method : transitionClass.getMethods()) {
			org.jallaby.beans.annotations.TransitionAction actionAnnotation =
					method.getAnnotation(org.jallaby.beans.annotations.TransitionAction.class);
			
			if (actionAnnotation != null && actionAnnotation.group().equals(group)) {
				actions.add(new MetaTransitionAction(instance, method));
			}
		}
		
		return actions;
	}

	private List<TransitionAction> buildSequentialTransitionActions(final String group,
			final Class<?> transitionClass) {
		PriorityQueue<OrderedMetaTransitionAction> orderedActions = new PriorityQueue<>();
		
		for (Method method : transitionClass.getMethods()) {
			org.jallaby.beans.annotations.TransitionAction actionAnnotation =
					method.getAnnotation(org.jallaby.beans.annotations.TransitionAction.class);
			
			if (actionAnnotation != null && actionAnnotation.group().equals(group)) {
				orderedActions.add(new OrderedMetaTransitionAction(actionAnnotation.order(),
						new MetaTransitionAction(instance, method)));
			}
		}
		
		List<TransitionAction> actions = new ArrayList<>();
		
		for (OrderedMetaTransitionAction orderedAction : orderedActions) {
			actions.add(orderedAction.getAction());
		}
		
		return actions;
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

	private StateInfo buildStateInfo() {
		XmlStateInfo xmlStateInfo = xmlStateMachine.getXmlTargetStateInfo(fromState, toState);
		
		Deque<State> states = new LinkedList<>();
		
		for (EffectiveXmlState xmlState : xmlStateInfo.getXmlStates()) {
			states.addLast(buildState(xmlState));
		}
		
		return new StateInfo() {
			
			@Override
			public int getStatesToExit() {
				return xmlStateInfo.getStatesToExit();
			}
			
			@Override
			public Deque<State> getStates() {
				return states;
			}
		};
	}

	private MetaState buildState(EffectiveXmlState xmlState) {
		MetaState state = beansRegistry.getState(xmlState.getName());
		
		if (state != null) {
			return state;
		} else {
			return new MetaState(xmlState.getName(), beanClasses,
					xmlStateMachine, beansRegistry, injector);
		}
	}

	@Override
	public List<TransitionGuard> getTransitionGuards() {
		return transitionGuards;
	}

	@Override
	public List<TransitionActionGroup> getTransitionActionGroups() {
		return transitionActionGroups;
	}

	@Override
	public StateInfo getTargetStateInfo() {
		return stateInfo;
	}

	@Override
	public void preDestroy() {
		try {
			if (!virtual && preDestroyMethod != null) {
				preDestroyMethod.invoke(instance, (Object[]) null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.warn("Unable to execute pre destroy method", e);
		}
	}

	public String fromState() {
		return fromState;
	}

	public String toState() {
		return toState;
	}
}
