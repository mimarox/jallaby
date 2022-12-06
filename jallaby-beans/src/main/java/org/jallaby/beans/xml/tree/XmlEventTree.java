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

import org.jallaby.beans.xml.model.XmlEvent;
import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlProperty;
import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlProperty;

class XmlEventTree {
	private static class Node {
		private final XmlEvent event;
		private final Set<Node> children = new HashSet<>();
		
		Node(XmlEvent event) {
			this.event = event;
		}

		/**
		 * @return the event
		 */
		public XmlEvent getEvent() {
			return event;
		}

		/**
		 * @return the children
		 */
		public Set<Node> getChildren() {
			return children;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(event);
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
			return Objects.equals(event, other.event);
		}
	}
	
	private Node root;
	
	/**
	 * Tries to add the given event to this tree.
	 * 
	 * @param event the event to add
	 * @return <code>true</code>, if and only if the given event was successfully added
	 * to this tree, <code>false</code> otherwise.
	 */
	boolean add(XmlEvent event) {
		Objects.requireNonNull(event, "event must not be null");
		
		if (root == null) {
			root = new Node(event);
			return true;
		} else {
			Node node = findNode(root, event.getXmlExtends());
			
			if (node != null) {
				if (XmlModifier.xmlFinal != node.getEvent().getModifier()) {
					node.getChildren().add(new Node(event));
					return true;
				} else {
					throw new IllegalParentException(String.format("Event %s can't "
							+ "be extended by %s as it is final.", node.event.getName(),
							event.getName()));
				}
			} else {
				return false;
			}
		}
	}

	private Node findNode(Node node, String name) {
		if (node.getEvent().getName().equals(name)) {
			return node;
		} else {
			for (Node child : node.getChildren()) {
				Node found = findNode(child, name);
				
				if (found != null) {
					return found;
				}
			}
		}
		
		return null;
	}

	boolean hasUnresolvedRoot() {
		return root.getEvent().getXmlExtends() != null;
	}

	String getXmlExtendsOfRoot() {
		return root.getEvent().getXmlExtends();
	}

	void addSubTree(XmlEventTree tree) {
		if (root == null) {
			root = tree.root;
		} else {
			root.getChildren().add(tree.root);
		}
	}

	Set<EffectiveXmlEvent> calculateEffectiveEvents() {
		if (hasUnresolvedRoot()) {
			throw new IllegalStateException("There is a root event missing!");
		}
		
		Set<EffectiveXmlEvent> effectiveXmlEvents = new HashSet<>();
		makeEffectiveEventsRecursively(effectiveXmlEvents, root, new HashSet<>());
		
		return effectiveXmlEvents;
	}

	private void makeEffectiveEventsRecursively(Set<EffectiveXmlEvent> effectiveXmlEvents,
			Node node, Set<Node> parents) {
		if (node.getEvent().getModifier() != XmlModifier.xmlAbstract) {
			effectiveXmlEvents.add(makeEffectiveEvent(node.getEvent(), parents));
		}

		for (Node child : node.getChildren()) {
			Set<Node> newParents = new HashSet<>();
			
			newParents.addAll(parents);
			newParents.add(node);
			
			makeEffectiveEventsRecursively(effectiveXmlEvents, child, newParents);
		}
	}

	private EffectiveXmlEvent makeEffectiveEvent(XmlEvent event, Set<Node> parents) {
		Set<EffectiveXmlProperty> properties = new HashSet<>();
		
		properties.addAll(calculateEffectiveProperties(event.getProperties(), true));
		
		for (Node parent : parents) {
			properties.addAll(calculateEffectiveProperties(parent.getEvent().getProperties(), false));
		}

		return new EffectiveXmlEvent(event.getName(), properties);
	}

	private Set<EffectiveXmlProperty> calculateEffectiveProperties(Set<XmlProperty> properties,
			boolean takePrivate) {
		Set<EffectiveXmlProperty> effectiveProperties = new HashSet<>();
		
		for (XmlProperty property : properties) {
			if (takePrivate || !property.isXmlPrivate()) {
				effectiveProperties.add(new EffectiveXmlProperty(
						property.getName(), property.getType()));
			}
		}
		
		return effectiveProperties;
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
		XmlEventTree other = (XmlEventTree) obj;
		return Objects.equals(root, other.root);
	}
}
