package integrationtests.common;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class JallabyApiProvider {
	private static JallabyApi api;
	
	public static synchronized JallabyApi provideApi() {
		if (api == null) {
			OkHttpClient client = new OkHttpClient.Builder()
					.callTimeout(24, TimeUnit.HOURS)
					.connectTimeout(24, TimeUnit.HOURS)
					.readTimeout(24, TimeUnit.HOURS)
					.build();
			
			Retrofit retrofit = new Retrofit.Builder().
					baseUrl("http://localhost:8081/").
					addConverterFactory(JacksonConverterFactory.create()).
					client(client).
					build();
			
			api = retrofit.create(JallabyApi.class);
		}

		return api;
	}
}
