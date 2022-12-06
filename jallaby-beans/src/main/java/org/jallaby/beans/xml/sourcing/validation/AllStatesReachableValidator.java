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

package org.jallaby.beans.xml.sourcing.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.model.effective.EffectiveXmlTransition;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError;
import org.jallaby.beans.xml.sourcing.StateMachineValidationError.ValidationSection;

/**
 * @author Matthias Rothe
 */
public class AllStatesReachableValidator implements StateMachineValidator {
	private static class Node {
		private final Set<Node> fromNodes = new HashSet<>();
		private final Set<Node> toNodes = new HashSet<>();
		private EffectiveXmlState state;
		
		/**
		 * @return the state
		 */
		public EffectiveXmlState getState() {
			return state;
		}

		/**
		 * @param state the state to set
		 */
		public void setState(EffectiveXmlState state) {
			this.state = state;
		}

		/**
		 * @return the fromNodes
		 */
		public Set<Node> getFromNodes() {
			return fromNodes;
		}

		/**
		 * @return the toNodes
		 */
		public Set<Node> getToNodes() {
			return toNodes;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(state);
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Node other = (Node) obj;
			return Objects.equals(state, other.state);
		}
	}
	
	private Set<Node> nodes = new HashSet<>();
	
	/* (non-Javadoc)
	 * @see org.jallaby.beans.xml.sourcing.validation.StateMachineValidator#validate(
	 * org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine, java.util.List)
	 */
	@Override
	public void validate(EffectiveXmlStateMachine stateMachine, List<StateMachineValidationError> validationErrors) {
		Set<EffectiveXmlState> states = stateMachine.getStates();
		
		EffectiveXmlState initialState = findState(stateMachine.getInitialState(), states);

		if (initialState == null) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.STATE_MACHINE,
					String.format("The initial state [%s] can't be found within the declared states.",
							stateMachine.getInitialState())));
			return;
		}
		
		Node initialNode = new Node();
		nodes.add(initialNode);

		initialNode.setState(initialState);
		initialNode.getToNodes().addAll(getToNodes(
				initialState.getTransitions(), states, initialNode, validationErrors));
		
		Set<EffectiveXmlState> usedStates = collectUsedStates(nodes);
		Set<EffectiveXmlState> unusedStates = new HashSet<>(states);
		unusedStates.removeAll(usedStates);
		
		for (EffectiveXmlState state : unusedStates) {
			validationErrors.add(new StateMachineValidationError(ValidationSection.STATE,
					"State [" + state.getName() + "] is unreachable and should be removed."));
		}
	}

	private Set<EffectiveXmlState> collectUsedStates(Set<Node> nodes) {
		return nodes.stream().map(node -> node.getState()).collect(Collectors.toSet());
	}

	private Set<Node> getToNodes(Set<EffectiveXmlTransition> transitions,
			Set<EffectiveXmlState> states, Node fromNode,
			List<StateMachineValidationError> validationErrors) {
		Set<Node> toNodes = new HashSet<>();
		
		for (EffectiveXmlTransition transition : transitions) {
			EffectiveXmlState state = findState(transition.getTo(), states);
			
			if (state == null) {
				validationErrors.add(new StateMachineValidationError(ValidationSection.TRANSITION,
						String.format("A transition from state [%s] leads to the non-existing"
								+ " state [%s]. This renders the state machine invalid.",
								fromNode.getState().getName(), transition.getTo())));
				continue;
			}
			
			Node node = new Node();
			node.setState(state);
			
			if (nodes.contains(node)) {
				node = findNode(node, nodes);
				node.getFromNodes().add(fromNode);
			} else {
				nodes.add(node);
				
				Node parentNode = getParentNode(node, states);
				
				if (parentNode != null && !nodes.contains(parentNode)) {
					parentNode.getToNodes().addAll(getToNodes(parentNode.getState().getTransitions(),
							states, parentNode, validationErrors));
					nodes.add(parentNode);
				}
				
				node.getFromNodes().add(fromNode);
				node.getToNodes().addAll(getToNodes(state.getTransitions(), states, node,
						validationErrors));
			}
		}
		
		return toNodes;
	}

	private EffectiveXmlState findState(String nameOfState, Set<EffectiveXmlState> states) {
		Optional<EffectiveXmlState> optionalState = states.stream().filter(
				state -> state.getName().equalsIgnoreCase(nameOfState)).findFirst();
		
		if (optionalState.isPresent()) {
			return optionalState.get();
		} else {
			return null;
		}
	}
	
	private Node findNode(Node node, Set<Node> nodes) {
		return nodes.stream().filter(storedNode -> storedNode.equals(node)).findFirst().get();
	}
	
	private Node getParentNode(Node node, Set<EffectiveXmlState> states) {
		Node parentNode = null;
		EffectiveXmlState parentState = node.getState().getParent();
		
		if (parentState != null) {
			parentNode = new Node();
			parentNode.setState(parentState);
		}
		
		return parentNode;
	}
}
