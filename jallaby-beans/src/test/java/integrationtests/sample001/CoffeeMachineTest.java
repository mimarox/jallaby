package integrationtests.sample001;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.jallaby.event.EventResult;
import org.jallaby.launcher.Launcher;
import org.testng.annotations.Test;

import integrationtests.common.JallabyApi;
import integrationtests.common.JallabyApiProvider;

public class CoffeeMachineTest {

	@Test
	public void testCoffeeMachine() throws Exception {
		startJallaby();
		buildSample();

		ICoffeeMachine coffeeMachine = mock(ICoffeeMachine.class);
		
		Thread.sleep(10000);
		
		CoffeeMachine.instance = coffeeMachine;

		// send events to jallaby container
		EventResult result;
		JallabyApi api = JallabyApiProvider.provideApi();
		
		result = sendEvent(api, "switchOn");
		
		assertEquals(result.getCurrentStateName(), "SwitchedOn");
		
		// verify results
		verify(coffeeMachine).switchOn();
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
		return api.sendEvent("CoffeeMachine", "integration-test", event, body).execute().body();
	}
}
