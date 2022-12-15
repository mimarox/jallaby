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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jallaby.JallabyRegistry;
import org.jallaby.execution.StateMachine;

public class JallabyBeansWorker extends Thread {
	private final JallabyRegistry registry;
	private final StateMachineBuilder stateMachineBuilder;
	private final Map<Path, String> stateMachines = new HashMap<>();
	private final Object abortMutex = new Object();
	
	private boolean abort;

	public JallabyBeansWorker(JallabyRegistry jallabyRegistry, BeansRegistry beansRegistry) {
		super("JallabyBeansWorker");

		Objects.requireNonNull(jallabyRegistry, "registry must not be null");
		this.registry = jallabyRegistry;

		this.stateMachineBuilder = new StateMachineBuilder(beansRegistry);
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

				for (WatchEvent<?> event : newKey.pollEvents()) {
					synchronized (abortMutex) {
						if (abort) {
							break outer;
						}
					}

					Kind<?> kind = event.kind();

					if (kind == OVERFLOW) {
						continue;
					}

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

	private void registerExistingStateMachines(final Path deployDirectory) {
		deployDirectory.forEach(path -> {
			if (path.getFileName().toString().endsWith(".sma")) {
				createStateMachine(path);
			}
		});
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private void createStateMachine(Path file) {
		StateMachine stateMachine = buildStateMachine(file);

		if (stateMachine != null) {
			stateMachines.put(file, stateMachine.getName());
			registry.register(stateMachine);
		}
	}

	private StateMachine buildStateMachine(Path file) {
		return stateMachineBuilder.build(file);
	}

	private void unregisterStateMachine(Path file) {
		if (stateMachines.containsKey(file)) {
			registry.unregister(stateMachines.get(file));
		}
	}
}
