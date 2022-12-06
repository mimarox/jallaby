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

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.jallaby.JallabyRegistry;
import org.jallaby.spi.LifecycleHook;

/**
 * Launches the Jallaby container.
 * 
 * @author Matthias Rothe
 */
public class Launcher {
	// Base URI the Grizzly HTTP server will listen on
	public static final String HTTP_BASE_URI = "http://localhost:8080/";

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
	public static HttpServer startHttpServer() {
		final ResourceConfig rc = new ResourceConfig().packages("org.jallaby.transport.http");
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(HTTP_BASE_URI), rc);
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

		final HttpServer httpServer = startHttpServer();
		System.out.println(String.format("HTTP server started at %s", HTTP_BASE_URI));

		System.in.read();

		hook.stop();
		httpServer.stop();
	}
	// CHECKSTYLE:ON
}
