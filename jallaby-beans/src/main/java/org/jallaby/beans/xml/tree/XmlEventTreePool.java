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
import java.util.Iterator;
import java.util.Set;

import org.jallaby.beans.xml.model.XmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;

/**
 * @author Matthias Rothe
 */
public class XmlEventTreePool {
	private final Set<XmlEventTree> trees = new HashSet<>();
	
	public void add(XmlEvent event) {
		if (trees.isEmpty()) {
			addTreeForEvent(event);
		} else {
			boolean foundTree = false;
			
			for (XmlEventTree tree : trees) {
				foundTree = tree.add(event);
				
				if (foundTree) {
					break;
				}
			}
			
			if (!foundTree) {
				XmlEventTree latest = addTreeForEvent(event);
				Iterator<XmlEventTree> iterator = trees.iterator();

				while (iterator.hasNext()) {
					XmlEventTree tree = iterator.next();
					
					if (tree.hasUnresolvedRoot() && event.getName().equalsIgnoreCase(
							tree.getXmlExtendsOfRoot())) {
						latest.addSubTree(tree);
						iterator.remove();
					}
				}
			}
		}
	}

	private XmlEventTree addTreeForEvent(XmlEvent event) {
		XmlEventTree tree = new XmlEventTree();
		
		tree.add(event);
		trees.add(tree);
		
		return tree;
	}

	public Set<EffectiveXmlEvent> calculateEffectiveEvents() {
		Set<EffectiveXmlEvent> effectiveXmlEvents = new HashSet<>();
		
		for (XmlEventTree tree : trees) {
			effectiveXmlEvents.addAll(tree.calculateEffectiveEvents());
		}
		
		return effectiveXmlEvents;
	}
}
