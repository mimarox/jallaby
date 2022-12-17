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

package org.jallaby.beans;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.util.resource.PathResource;
import org.jallaby.JallabyRegistry;
import org.jallaby.beans.classloader.StateMachineClassLoader;
import org.jallaby.beans.classloader.StateMachineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JallabyBeansWorker extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(JallabyBeansWorker.class);
	
	private final JallabyRegistry jallabyRegistry;
	private final BeansRegistry beansRegistry;
	private final Map<Path, String> stateMachines = new ConcurrentHashMap<>();
	private final Object abortMutex = new Object();
	
	private boolean abort;

	public JallabyBeansWorker(final JallabyRegistry jallabyRegistry, final BeansRegistry beansRegistry) {
		super("JallabyBeansWorker");

		Objects.requireNonNull(jallabyRegistry, "jallabyRegistry must not be null");
		Objects.requireNonNull(beansRegistry, "beansRegistry must not be null");
		
		this.jallabyRegistry = jallabyRegistry;
		this.beansRegistry = beansRegistry;
	}

	public void abort() {
		synchronized (abortMutex) {
			abort = true;
		}
	}

	public void run() {
		try {
			Path deployDirectory = Paths.get(System.getProperty("user.dir") +
					System.getProperty("file.separator") + "deploy");
			
			registerExistingStateMachines(deployDirectory);

			WatchService watcher = FileSystems.getDefault().newWatchService();
			WatchKey key = deployDirectory.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);

			outer: while (true) {
				synchronized (abortMutex) {
					if (abort) {
						break;
					}
				}

				WatchKey newKey;

				try {
					newKey = watcher.take();

					if (!key.equals(newKey)) {
						continue;
					}
				} catch (InterruptedException x) {
					break;
				}

				/* inner: */ for (WatchEvent<?> event : newKey.pollEvents()) {
					synchronized (abortMutex) {
						if (abort) {
							break outer;
						}
					}
					
					Kind<?> kind = event.kind();

					if (kind == OVERFLOW) {
						continue;
					}
					
					handleEvent(deployDirectory, event);
				}

				// reset key and remove from set if directory no longer accessible
				boolean valid = newKey.reset();
				if (!valid) {
					break;
				}
			}

			watcher.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleEvent(final Path deployDirectory, final WatchEvent<?> event) {
		Kind<?> kind = event.kind();

		// Context for directory entry event is the file name of entry
		WatchEvent<Path> ev = cast(event);
		Path name = ev.context();
		Path file = deployDirectory.resolve(name);

		if (file.getFileName().toString().endsWith(".sma")) {
			if (kind == ENTRY_CREATE) {
				createStateMachine(file);
			} else if (kind == ENTRY_MODIFY) {
				unregisterStateMachine(file);
				createStateMachine(file);
			} else if (kind == ENTRY_DELETE) {
				unregisterStateMachine(file);
			}
		}
	}
	
	private void registerExistingStateMachines(final Path deployDirectory) {
		File[] files = deployDirectory.toFile().listFiles();
		
		if (files != null) {
			Arrays.asList(files).forEach(file -> {
				if (file.getName().toString().endsWith(".sma")) {
					createStateMachine(file.toPath());
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private void createStateMachine(Path file) {
		try {
			StateMachineContext context = new StateMachineContext();
			context.setExtraClasspath(Arrays.asList(new PathResource(file)));
			
			StateMachineBuilder builder = new StateMachineBuilder(
					jallabyRegistry, beansRegistry, stateMachines, file);
			builder.setContextClassLoader(new StateMachineClassLoader(context));
			builder.start();
		} catch (IOException e) {
			LOGGER.warn(String.format("Couldn't build state machine for file [%s]", file), e);
		}
	}

	private void unregisterStateMachine(Path file) {
		if (stateMachines.containsKey(file)) {
			jallabyRegistry.unregister(stateMachines.get(file));
			LOGGER.info(String.format("Unregistered state machine from file [%s] successfully!", 
					file.toString()));
		}
	}
}
