package integrationtests.sample001;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.jallaby.launcher.Launcher;

import integrationtests.common.EventResult;
import integrationtests.common.InternalServerErrorException;
import integrationtests.common.JallabyApi;
import integrationtests.common.UnexpectedServerResponseException;
import retrofit2.Response;

public abstract class Sample001TestBase {
	private static final String UUID = "fee5a05c-5f52-45dd-926a-9f2bc7f097ee";

	protected void startJallaby() {
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

	protected void buildSample() throws MavenInvocationException {
		InvocationRequest request = new DefaultInvocationRequest();
		request.setPomFile(new File(System.getProperty("user.dir") + "/src/sample-001/pom.xml"));
		request.setGoals(Arrays.asList("clean", "install"));

		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(new File("d:\\dev\\apache-maven-3.6.2"));
		invoker.execute(request);
	}
	
	protected EventResult sendEvent(final JallabyApi api, final String event) throws Exception {
		return sendEvent(api, event, new HashMap<>());
	}
	
	protected EventResult sendEvent(final JallabyApi api, final String event,
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

	protected void deleteStateMachineArchive() {
		Path archive = Paths.get(
				String.format("%1$s%2$sdeploy%2$sjallaby-beans-sample001-1.0.0-SNAPSHOT.sma",
				System.getProperty("user.dir"),	System.getProperty("file.separator")));
		archive.toFile().delete();
	}
}
