package integrationtests.sample001;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.jallaby.beans.sample001.business.CoffeeMachine;
import org.jallaby.beans.sample001.business.CoffeeMachineInterface;
import org.testng.annotations.Test;

import integrationtests.common.EventResult;
import integrationtests.common.JallabyApi;
import integrationtests.common.JallabyApiProvider;

public class PickUpPredeployedArchivesTest extends Sample001TestBase {

	@Test
	public void shouldPickUpPredeployedArchives() throws Exception {
		deleteStateMachineArchive();
		buildSample();
		startJallaby();
		
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
	}
}
