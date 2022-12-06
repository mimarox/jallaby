package org.jallaby.beans.xml.model;

import java.util.Deque;
import java.util.Objects;

import org.jallaby.beans.xml.model.effective.EffectiveXmlState;

public class XmlStateInfo {
	private final int statesToExit;
	private final Deque<EffectiveXmlState> xmlStates;
	
	public XmlStateInfo(final int statesToExit, final Deque<EffectiveXmlState> xmlStates) {
		Objects.requireNonNull(xmlStates, "xmlStates must not be null");
		
		this.statesToExit = statesToExit;
		this.xmlStates = xmlStates;
	}

	/**
	 * @return the statesToExit
	 */
	public int getStatesToExit() {
		return statesToExit;
	}

	/**
	 * @return the xmlStates
	 */
	public Deque<EffectiveXmlState> getXmlStates() {
		return xmlStates;
	}
}
