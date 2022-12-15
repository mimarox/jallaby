package integrationtests.sample001;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.jallaby.beans.sample001.business.CoffeeMachine;
import org.jallaby.beans.sample001.business.CoffeeMachineInterface;
import org.testng.annotations.Test;

import integrationtests.common.EventResult;
import integrationtests.common.InternalServerErrorException;
import integrationtests.common.JallabyApi;
import integrationtests.common.JallabyApiProvider;

public class CoffeeMachineTest extends Sample001TestBase {
	
	@Test
	public void testCoffeeMachine() throws Exception {
		deleteStateMachineArchive();
		startJallaby();
		buildSample();

		Thread.sleep(5000);
		
		CoffeeMachineInterface coffeeMachine = mock(CoffeeMachineInterface.class);
		when(coffeeMachine.hasPower()).thenReturn(true);
		
		CoffeeMachine.setInstance(coffeeMachine);

		// send events to jallaby container
		EventResult result;
		JallabyApi api = JallabyApiProvider.provideApi();
		
		result = sendEvent(api, "switchOn");
		
		assertEquals(result.getCurrentStateName(), "Idle");
		verify(coffeeMachine).switchOn();
		verify(coffeeMachine).sleep();
		
		String coffeeType = "Latte Macchiato";
		
		Map<String, Object> type = new HashMap<>();
		type.put("type", coffeeType);
		result = sendEvent(api, "makeCoffee", type);
		
		assertEquals(result.getCurrentStateName(), "MakingCoffee");
		verify(coffeeMachine).wakeUp();
		verify(coffeeMachine).makeCoffee(coffeeType);
		
		result = sendEvent(api, "finished");
		
		assertEquals(result.getCurrentStateName(), "Idle");
		verify(coffeeMachine, times(2)).sleep();
		
		result = sendEvent(api, "switchOff");
		
		assertEquals(result.getCurrentStateName(), "SwitchedOff");
		verify(coffeeMachine, times(2)).switchOff();
	}
	
	@Test(dependsOnMethods = "testCoffeeMachine",
			expectedExceptions = InternalServerErrorException.class,
			expectedExceptionsMessageRegExp = "{\"errorCode\":200,\"errorDescription\":"
					+ "\"The given state machine is unknown.\",\"eventName\":\"switchOn\","
					+ "\"instanceId\":\"fee5a05c-5f52-45dd-926a-9f2bc7f097ee\","
					+ "\"stateMachineName\":\"CoffeeMachine\"}")
	public void testUnregisterStateMachine() throws Exception {
		deleteStateMachineArchive();
		
		JallabyApi api = JallabyApiProvider.provideApi();
		sendEvent(api, "switchOn");
	}
}
