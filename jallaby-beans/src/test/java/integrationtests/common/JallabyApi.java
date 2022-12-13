package integrationtests.common;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface JallabyApi {

	@PUT("{stateMachine}/{instanceId}/{event}")
	Call<EventResult> sendEvent(@Path("stateMachine") String stateMachine,
			@Path("instanceId") String instanceId, @Path("event") String event,
			@Body Map<String, Object> body);
}
