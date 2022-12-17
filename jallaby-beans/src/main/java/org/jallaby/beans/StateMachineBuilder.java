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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jallaby.JallabyRegistry;
import org.jallaby.beans.metamodel.MetaState;
import org.jallaby.beans.metamodel.sourcing.BeanClasses;
import org.jallaby.beans.metamodel.sourcing.BeanClassesProvider;
import org.jallaby.beans.xml.model.effective.EffectiveXmlEvent;
import org.jallaby.beans.xml.model.effective.EffectiveXmlStateMachine;
import org.jallaby.beans.xml.sourcing.XmlDeclarationProvider;
import org.jallaby.event.EventValidator;
import org.jallaby.execution.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Builds state machines from deployed SMA (State Machine Archive) files.
 * 
 * @author Matthias Rothe
 */
public class StateMachineBuilder extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineBuilder.class);
	
	private final JallabyRegistry jallabyRegistry;
	private final BeansRegistry beansRegistry;
	private final Map<Path, String> stateMachines;
	private final Path path;
	
	/**
	 * Ctor.
	 * 
	 * @param jallabyRegistry the jallaby registry
	 * @param beansRegistry the beans registry
	 * @param stateMachines the state machines
	 * @param path The file to build the state machine from
	 */
	public StateMachineBuilder(final JallabyRegistry jallabyRegistry, final BeansRegistry beansRegistry,
			final Map<Path, String> stateMachines, final Path path) {
		super("StateMachineBuilder");

		Objects.requireNonNull(jallabyRegistry, "jallabyRegistry must not be null");
		Objects.requireNonNull(beansRegistry, "beansRegistry must not be null");
		Objects.requireNonNull(stateMachines, "stateMachines must not be null");
		Objects.requireNonNull(path, "path must not be null");
		
		this.jallabyRegistry = jallabyRegistry;
		this.beansRegistry = beansRegistry;
		this.stateMachines = stateMachines;
		this.path = path;
	}

	/**
	 * Builds a state machine from the deployed SMA (State Machine Archive) file.
	 */
	@Override
	public void run() {
		File smaFile = path.toFile();

		try (JarFile jarFile = new JarFile(smaFile)) {
			Attributes attributes = jarFile.getManifest().getMainAttributes();
			String[] basePackages = attributes.getValue("base-packages").split(", ");
			
			URL smaUrl = new URL("jar:file:" + smaFile.getPath() + "!/");
			
			BeanClasses beanClasses = new BeanClassesProvider()
					.provideFrom(smaUrl, basePackages);
			
			JarEntry stateMachineXmlEntry =
					jarFile.getJarEntry("META-INF/state-machine.xml");
			
			try (InputStream is = jarFile.getInputStream(stateMachineXmlEntry)) {
				EffectiveXmlStateMachine exsm = new XmlDeclarationProvider().provide(is);
				StateMachine stateMachine = buildStateMachineUsing(beanClasses, exsm);
				registerStateMachine(stateMachine);
			}
		} catch (Exception e) {
			LOGGER.warn("Failed building the state machine", e);
		}
	}

	private StateMachine buildStateMachineUsing(final BeanClasses beanClasses,
			final EffectiveXmlStateMachine xmlStateMachine) {
		Injector injector = Guice.createInjector(instantiateModules(beanClasses.getModules()));
		
		MetaState initialState = new MetaState(xmlStateMachine.getInitialState(), beanClasses,
				xmlStateMachine, beansRegistry, injector);
		
		return new StateMachine(xmlStateMachine.getName(),
				initialState, buildEventValidator(beanClasses, xmlStateMachine.getEvents()));
	}

	private Set<Module> instantiateModules(final Set<Class<? extends Module>> moduleClasses) {
		Set<Module> modules = new HashSet<>();
		
		for (Class<? extends Module> moduleClass : moduleClasses) {
			try {
				modules.add(moduleClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.warn(String.format("Couldn't instantiate module class [%s]",
						moduleClass.getCanonicalName()), e);
			}
		}
		
		return modules;
	}

	private EventValidator buildEventValidator(final BeanClasses beanClasses,
			final Set<EffectiveXmlEvent> events) {
		Set<EventValidator> eventValidators = new LinkedHashSet<>();
		
		eventValidators.add(new DefaultEventValidator(events));
		
		for (Class<? extends EventValidator> clazz : beanClasses.getEventValidators()) {
			try {
				eventValidators.add(clazz.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.warn(String.format("Event validator from class %s "
						+ "could not be instantiated.", clazz.getCanonicalName()), e);
			}
		}
		
		return event -> {
			for (EventValidator eventValidator : eventValidators) {
				if (!eventValidator.isValidEvent(event)) {
					return false;
				}
			}
			
			return true;
		};
	}
	
	private void registerStateMachine(final StateMachine stateMachine) {
		if (stateMachine != null) {
			stateMachines.put(path, stateMachine.getName());
			jallabyRegistry.register(stateMachine);
			
			LOGGER.info(String.format("Successfully created state machine [%s]!", 
					stateMachine.getName()));
		}
	}
}
