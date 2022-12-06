package org.jallaby.beans.xml.tree;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlState;

class XmlStateTreeNode {
	private final XmlStateTree tree;
	private final XmlStateTreeNode parent;
	private final XmlState state;
	private final Set<XmlStateTreeNode> children = new HashSet<>();
	
	XmlStateTreeNode(final XmlStateTree tree, final XmlStateTreeNode parent, final XmlState state) {
		Objects.requireNonNull(tree, "tree must not be null");
		Objects.requireNonNull(state, "state must not be null");
		
		this.tree = tree;
		this.parent = parent;
		this.state = state;
	}

	/**
	 * @return the tree
	 */
	XmlStateTree getTree() {
		return tree;
	}
	
	/**
	 * @return the parent
	 */
	XmlStateTreeNode getParent() {
		return parent;
	}
	
	/**
	 * @return the state
	 */
	XmlState getState() {
		return state;
	}

	/**
	 * @return the children
	 */
	Set<XmlStateTreeNode> getChildren() {
		return children;
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
		XmlStateTreeNode other = (XmlStateTreeNode) obj;
		return Objects.equals(state, other.state);
	}
}
