package org.jallaby.beans.xml.tree;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;

import org.jallaby.beans.xml.model.XmlModifier;
import org.jallaby.beans.xml.model.XmlState;
import org.jallaby.beans.xml.model.XmlStateInfo;
import org.jallaby.beans.xml.model.XmlTransition;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class XmlStateTreePoolTest {

	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoSwitchedOffToIdle(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("SwitchedOff", "Idle");
		
		assertEquals(stateInfo.getStatesToExit(), 1);
		assertEquals(stateInfo.getXmlStates().size(), 2);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "SwitchedOn");
		assertEquals(stateInfo.getXmlStates().poll().getName(), "Idle");
	}
	
	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoIdleToMakingCoffee(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("Idle", "MakingCoffee");
		
		assertEquals(stateInfo.getStatesToExit(), 1);
		assertEquals(stateInfo.getXmlStates().size(), 1);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "MakingCoffee");
	}
	
	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoMakingCoffeeToIdle(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("MakingCoffee", "Idle");
		
		assertEquals(stateInfo.getStatesToExit(), 1);
		assertEquals(stateInfo.getXmlStates().size(), 1);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "Idle");
	}
	
	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoIdleToIdle(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("Idle", "Idle");
		
		assertEquals(stateInfo.getStatesToExit(), 1);
		assertEquals(stateInfo.getXmlStates().size(), 1);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "Idle");
	}
	
	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoSwitchedOnToIdle(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("SwitchedOn", "Idle");
		
		assertEquals(stateInfo.getStatesToExit(), 0);
		assertEquals(stateInfo.getXmlStates().size(), 1);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "Idle");
	}
	
	@Test(dataProvider = "provideXmlStateTreePool")
	public void testGetXmlTargetStateInfoMakingCoffeeToSwitchedOff(final XmlStateTreePool pool) {
		XmlStateInfo stateInfo = pool.getXmlTargetStateInfo("MakingCoffee", "SwitchedOff");
		
		assertEquals(stateInfo.getStatesToExit(), 2);
		assertEquals(stateInfo.getXmlStates().size(), 1);
		assertEquals(stateInfo.getXmlStates().poll().getName(), "SwitchedOff");
	}
	
	@DataProvider
	public Object[][] provideXmlStateTreePool() {
		XmlTransition switchedOffToIdle = new XmlTransition();
		switchedOffToIdle.setTo("Idle");
		switchedOffToIdle.setEvents(Collections.singleton("switchOn"));
		
		XmlState switchedOff = new XmlState();
		switchedOff.setName("SwitchedOff");
		switchedOff.setTransitions(Collections.singleton(switchedOffToIdle));
		
		XmlTransition switchedOnToSwitchedOff = new XmlTransition();
		switchedOnToSwitchedOff.setTo("SwitchedOff");
		switchedOnToSwitchedOff.setEvents(Collections.singleton("switchOff"));
		
		XmlState switchedOn = new XmlState();
		switchedOn.setName("SwitchedOn");
		switchedOn.setTransitions(Collections.singleton(switchedOnToSwitchedOff));

		XmlTransition idleToMakingCoffee = new XmlTransition();
		idleToMakingCoffee.setTo("MakingCoffee");
		idleToMakingCoffee.setEvents(Collections.singleton("makeCoffee"));
		
		XmlState idle = new XmlState();
		idle.setName("Idle");
		idle.setXmlExtends("SwitchedOn");
		idle.setTransitions(Collections.singleton(idleToMakingCoffee));
		
		XmlTransition finishableToIdle = new XmlTransition();
		finishableToIdle.setTo("Idle");
		finishableToIdle.setEvents(Collections.singleton("finish"));
		
		XmlState finishable = new XmlState();
		finishable.setName("Finishable");
		finishable.setXmlExtends("SwitchedOn");
		finishable.setModifier(XmlModifier.xmlAbstract);
		finishable.setTransitions(Collections.singleton(finishableToIdle));
		
		XmlState makingCoffee = new XmlState();
		makingCoffee.setName("MakingCoffee");
		makingCoffee.setXmlExtends("Finishable");
		makingCoffee.setTransitions(new HashSet<>());
		
		XmlStateTreePool pool = new XmlStateTreePool();
		pool.add(switchedOff);
		pool.add(switchedOn);
		pool.add(idle);
		pool.add(finishable);
		pool.add(makingCoffee);

		return new Object[][] {{ pool }};
	}
}
