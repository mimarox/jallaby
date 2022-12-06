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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlState;
import org.jallaby.beans.xml.model.XmlTransition;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;
import org.jallaby.beans.xml.model.effective.EffectiveXmlTransition;

/**
 * @author Matthias Rothe
 */
class XmlStateTree {
	private final Set<EffectiveXmlState> effectiveXmlStates = new HashSet<>();

	private XmlStateTreeNode root;
	private boolean recalculateEffectiveXmlStates;
	
	/**
	 * Tries to add the given state to this tree.
	 * 
	 * @param state the state to add
	 * @return <code>true</code>, if and only if the given state was successfully added
	 * to this tree, <code>false</code> otherwise.
	 */
	boolean add(XmlState state) {
		Objects.requireNonNull(state, "state must not be null");
		
		if (root == null) {
			root = new XmlStateTreeNode(this, null, state);
			resetEffectiveXmlStates();
			return true;
		} else {
			XmlStateTreeNode node = findNode(root, state.getXmlExtends());
			
			if (node != null) {
				if (XmlModifier.xmlFinal != node.getState().getModifier()) {
					node.getChildren().add(new XmlStateTreeNode(this, node, state));
					resetEffectiveXmlStates();
					return true;
				} else {
					throw new IllegalParentException(String.format("State %s can't "
							+ "be extended by %s as it is final.", node.getState().getName(),
							state.getName()));
				}
			} else {
				return false;
			}
		}
	}
	
	private void resetEffectiveXmlStates() {
		effectiveXmlStates.clear();
		recalculateEffectiveXmlStates = true;
	}
	
	XmlStateTreeNode findNode(final String name) {
		if (root == null) {
			return null;
		}
		
		return findNode(root, name);
	}
	
	private XmlStateTreeNode findNode(XmlStateTreeNode node, String name) {
		if (node.getState().getName().equals(name)) {
			return node;
		} else {
			for (XmlStateTreeNode child : node.getChildren()) {
				XmlStateTreeNode found = findNode(child, name);
				
				if (found != null) {
					return found;
				}
			}
		}
		
		return null;
	}

	boolean hasUnresolvedRoot() {
		return root.getState().getXmlExtends() != null;
	}

	String getXmlExtendsOfRoot() {
		return root.getState().getXmlExtends();
	}

	void addSubTree(XmlStateTree tree) {
		if (root == null) {
			root = tree.root;
		} else {
			root.getChildren().add(tree.root);
		}
	}

	Set<EffectiveXmlState> calculateEffectiveStates() {
		if (hasUnresolvedRoot()) {
			throw new IllegalStateException("There is a root state missing!");
		}
		
		if (recalculateEffectiveXmlStates) {
			makeEffectiveStatesRecursively(effectiveXmlStates, root, new HashSet<>(), null);
			recalculateEffectiveXmlStates = false;
		}
		
		return effectiveXmlStates;
	}

	private void makeEffectiveStatesRecursively(Set<EffectiveXmlState> effectiveXmlStates,
			XmlStateTreeNode node, Set<XmlStateTreeNode> parents, EffectiveXmlState parent) {
		if (node.getState().getModifier() != XmlModifier.xmlAbstract) {
			EffectiveXmlState child = makeEffectiveState(node.getState(), parents, parent);
			effectiveXmlStates.add(child);
			parent = child;
		}

		for (XmlStateTreeNode child : node.getChildren()) {
			Set<XmlStateTreeNode> newParents = new HashSet<>();
			
			newParents.addAll(parents);
			newParents.add(node);
			
			makeEffectiveStatesRecursively(effectiveXmlStates, child, newParents, parent);
		}
	}

	private EffectiveXmlState makeEffectiveState(XmlState state, Set<XmlStateTreeNode> parents,
			EffectiveXmlState parent) {
		Set<EffectiveXmlTransition> transitions = new HashSet<>();
		
		transitions.addAll(calculateEffectiveTransitions(state.getTransitions(), true));
		
		for (XmlStateTreeNode parentNode : parents) {
			transitions.addAll(calculateEffectiveTransitions(parentNode.getState().getTransitions(), false));
		}

		return new EffectiveXmlState(parent, state.getName(), transitions);
	}

	private Set<EffectiveXmlTransition> calculateEffectiveTransitions(Set<XmlTransition> transitions,
			boolean takePrivate) {
		Set<EffectiveXmlTransition> effectiveTransitions = new HashSet<>();
		
		for (XmlTransition transition : transitions) {
			if (takePrivate || !transition.isXmlPrivate()) {
				effectiveTransitions.add(new EffectiveXmlTransition(
						transition.getTo(), transition.getEvents()));
			}
		}
		
		return effectiveTransitions;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(root);
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
		XmlStateTree other = (XmlStateTree) obj;
		return Objects.equals(root, other.root);
	}
}
