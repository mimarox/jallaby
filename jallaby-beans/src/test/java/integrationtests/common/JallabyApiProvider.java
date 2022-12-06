package integrationtests.common;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class JallabyApiProvider {
	private static JallabyApi api;
	
	public static synchronized JallabyApi provideApi() {
		if (api == null) {
			OkHttpClient client = new OkHttpClient.Builder().build();
			
			Retrofit retrofit = new Retrofit.Builder().
					baseUrl("http://localhost:8080/").
					addConverterFactory(JacksonConverterFactory.create()).
					client(client).
					build();
			
			api = retrofit.create(JallabyApi.class);
		}

		return api;
	}
}
