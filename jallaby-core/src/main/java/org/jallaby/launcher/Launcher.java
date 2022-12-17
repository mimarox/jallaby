/*
 * Copyright 2022, The Jallaby Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jallaby.launcher;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jallaby.JallabyRegistry;
import org.jallaby.spi.LifecycleHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launches the Jallaby container.
 * 
 * @author Matthias Rothe
 */
public class Launcher {
	private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
	
	private static LifecycleHook hook;

	private Launcher() {
	}

	private static void loadAndStartLifecycleHook() throws Exception {
		Class<?> staticLifecycleHookResolver = Class.forName("org.jallaby.StaticLifecycleHookResolver");
		Method resolveMethod = staticLifecycleHookResolver.getMethod("resolve");
		hook = (LifecycleHook) resolveMethod.invoke(null);
		hook.start(JallabyRegistry.getInstance());
	}

	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static String startHttpServer() {
		URI httpBaseUri = getHttpBaseUri();
		final ResourceConfig rc = new ResourceConfig().packages("org.jallaby.transport.http");
		GrizzlyHttpServerFactory.createHttpServer(httpBaseUri, rc);
		
		return httpBaseUri.toString();
	}

	private static URI getHttpBaseUri() {
		String httpBase;
		
		try {
			URL jallabyPropertiesUrl = new URL(String.format("file:///%1$s%2$sconfig%2$sjallaby"
					+ ".properties", System.getProperty("user.dir"),
					System.getProperty("file.separator")));
			Properties jallabyProperties = new Properties();
			jallabyProperties.load(jallabyPropertiesUrl.openStream());
			httpBase = jallabyProperties.getProperty("http.base.url");
			
			if (httpBase == null || httpBase.trim().equals("")) {
				throw new NoSuchElementException("Properties file doesn't contain a key value pair"
						+ " for http.base.url");
			}
		} catch (Exception e) {
			LOGGER.warn("Couldn't find http base url property. "
					+ "Defaulting to http://localhost:8081/.", e);
			httpBase = "http://localhost:8081/";
		}
		
		try {
			return new URI(httpBase);
		} catch (URISyntaxException e) {
			LOGGER.error("Unable to build http base uri. System will exit!", e);
			System.exit(1);
			return null;
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args The arguments
	 * @throws IOException
	 */
	// CHECKSTYLE:OFF
	public static void main(String[] args) throws Exception {
		loadAndStartLifecycleHook();

		String httpBaseUri = startHttpServer();
		System.out.println(String.format("HTTP server started at %s", httpBaseUri));
		System.out.println("Press Ctrl+C to stop the server...");
		
		System.in.read();

		hook.stop();
	}
	// CHECKSTYLE:ON
}
