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

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlState;
import org.jallaby.beans.xml.model.XmlStateInfo;
import org.jallaby.beans.xml.model.effective.EffectiveXmlState;

/**
 * @author Matthias Rothe
 */
public class XmlStateTreePool {
	private final Set<XmlStateTree> trees = new HashSet<>();
	
	public void add(XmlState state) {
		if (trees.isEmpty()) {
			addTreeForState(state);
		} else {
			boolean foundTree = false;
			
			for (XmlStateTree tree : trees) {
				foundTree = tree.add(state);
				
				if (foundTree) {
					break;
				}
			}
			
			if (!foundTree) {
				XmlStateTree latest = addTreeForState(state);
				Iterator<XmlStateTree> iterator = trees.iterator();

				while (iterator.hasNext()) {
					XmlStateTree tree = iterator.next();
					
					if (tree.hasUnresolvedRoot() && state.getName().equalsIgnoreCase(
							tree.getXmlExtendsOfRoot())) {
						latest.addSubTree(tree);
						iterator.remove();
					}
				}
			}
		}
	}

	private XmlStateTree addTreeForState(XmlState state) {
		XmlStateTree tree = new XmlStateTree();
		
		tree.add(state);
		trees.add(tree);
		
		return tree;
	}

	public Set<EffectiveXmlState> calculateEffectiveStates() {
		Set<EffectiveXmlState> effectiveXmlStates = new HashSet<>();
		
		for (XmlStateTree tree : trees) {
			effectiveXmlStates.addAll(tree.calculateEffectiveStates());
		}
		
		return effectiveXmlStates;
	}

	public XmlStateInfo getXmlTargetStateInfo(final String fromStateName, final String toStateName) {
		//FIXME for some reason null can end up as a value in the states list
		int statesToExit = 0;
		Deque<EffectiveXmlState> states = new LinkedList<>();
		
		XmlStateTreeNode fromStateNode = findStateNode(fromStateName);
		
		if (Objects.equals(fromStateName, toStateName)) {
			statesToExit = 1;
			states.add(makeEffectiveXmlState(fromStateNode));
		} else if (isDescendantOf(toStateName, fromStateNode)) {
			states.addAll(findEffectiveXmlStates(fromStateNode, toStateName));
		} else {
			statesToExit++;
			XmlStateTreeNode parentNode = fromStateNode;
			
			while ((parentNode = parentNode.getParent()) != null) {
				if (isDescendantOf(toStateName, parentNode)) {
					states.addAll(findEffectiveXmlStates(parentNode, toStateName));
					break;
				} else {
					if (parentNode.getState().getModifier() != XmlModifier.xmlAbstract) {
						statesToExit++;
					}
				}
			}
			
			if (parentNode == null) {
				states.addAll(findEffectiveXmlStates(toStateName));
			}
		}
		
		return new XmlStateInfo(statesToExit, states);
	}

	private XmlStateTreeNode findStateNode(final String stateName) {
		for (XmlStateTree tree : trees) {
			XmlStateTreeNode node = tree.findNode(stateName);
			
			if (node != null) {
				return node;
			}
		}
		
		return null;
	}

	private EffectiveXmlState makeEffectiveXmlState(XmlStateTreeNode node) {
		Set<EffectiveXmlState> states = node.getTree().calculateEffectiveStates();
		
		return states.stream().filter(state -> state.getName().equals(node.getState().getName()))
				.findFirst().orElse(null);
	}

	private boolean isDescendantOf(final String stateName, XmlStateTreeNode node) {
		boolean isDescendantOf = false;
		
		for (XmlStateTreeNode child : node.getChildren()) {
			if (child.getState().getName().equals(stateName)) {
				return true;
			} else {
				isDescendantOf = isDescendantOf(stateName, child);
			}
		}
		
		return isDescendantOf;
	}

	private Deque<EffectiveXmlState> findEffectiveXmlStates(final XmlStateTreeNode fromStateNode,
			String toStateName) {
		Deque<EffectiveXmlState> states = new LinkedList<>();
		XmlStateTreeNode toStateNode = findStateNode(toStateName);
		
		states.addFirst(makeEffectiveXmlState(toStateNode));
		
		XmlStateTreeNode parentNode = toStateNode;
		
		//CHECKSTYLE:OFF
		while (!isFinalNode(parentNode = parentNode.getParent(), fromStateNode)) {
		//CHECKSTYLE:ON
			if (parentNode.getState().getModifier() != XmlModifier.xmlAbstract) {
				states.addFirst(makeEffectiveXmlState(parentNode));
			}
		}
		
		return states;
	}

	private boolean isFinalNode(XmlStateTreeNode node, XmlStateTreeNode fromStateNode) {
		return node == null || node.equals(fromStateNode);
	}

	private Deque<EffectiveXmlState> findEffectiveXmlStates(String toStateName) {
		Deque<EffectiveXmlState> states = new LinkedList<>();
		XmlStateTreeNode toStateNode = findStateNode(toStateName);
		
		states.addFirst(makeEffectiveXmlState(toStateNode));
		
		XmlStateTreeNode parentNode = toStateNode;
		
		while ((parentNode = parentNode.getParent()) != null) {
			states.addFirst(makeEffectiveXmlState(parentNode));
		}
		
		return states;
	}
}
