package integrationtests.sample001;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jallaby.beans.sample001.business.CoffeeMachine;
import org.jallaby.beans.sample001.business.ICoffeeMachine;
import org.jallaby.launcher.Launcher;
import org.testng.annotations.Test;

import integrationtests.common.EventResult;
import integrationtests.common.InternalServerErrorException;
import integrationtests.common.JallabyApi;
import integrationtests.common.JallabyApiProvider;
import integrationtests.common.UnexpectedServerResponseException;
import retrofit2.Response;

public class CoffeeMachineTest {
	private static final String UUID = "fee5a05c-5f52-45dd-926a-9f2bc7f097ee";
	
	@Test
	public void testCoffeeMachine() throws Exception {
		startJallaby();
		buildSample();

		Thread.sleep(5000);
		
		ICoffeeMachine coffeeMachine = mock(ICoffeeMachine.class);
		when(coffeeMachine.hasPower()).thenReturn(true);
		
		CoffeeMachine.instance = coffeeMachine;

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

	private void startJallaby() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Launcher.main(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "Jallaby Container").start();
	}

	private void buildSample() throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(System.getProperty("user.dir") + "/src/sample-001/pom.xml"));
		request.setGoals(Arrays.asList("clean", "install"));

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File("d:\\dev\\apache-maven-3.6.2"));
		invoker.execute(request);
	}
	
	private EventResult sendEvent(final JallabyApi api, final String event) throws Exception {
		return sendEvent(api, event, new HashMap<>());
	}
	
	private EventResult sendEvent(final JallabyApi api, final String event,
			final Map<String, Object> body) throws Exception {
		Response<EventResult> response = api.sendEvent("CoffeeMachine", UUID,
				event, body).execute();
		
		if (response.isSuccessful()) {
			return response.body();
		} else if (response.code() == 503){
			throw new InternalServerErrorException(response.errorBody().string());
		} else {
			throw new UnexpectedServerResponseException(response.errorBody().string());
		}
	}
}
