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

import org.jallaby.JallabyRegistry;
import org.jallaby.beans.metamodel.LifecycleBean;
import org.jallaby.spi.LifecycleHook;

public class JallabyBeansLifecycleHook implements LifecycleHook {
	private final BeansRegistry beansRegistry = new BeansRegistry();
	
	private JallabyBeansWorker worker;
	
	@Override
	public void start(JallabyRegistry jallabyRegistry) {
		worker = new JallabyBeansWorker(jallabyRegistry, beansRegistry);
		worker.start();
	}

	@Override
	public void stop() {
		worker.abort();
		
		for (LifecycleBean bean : beansRegistry.getAllLifecycleBeans()) {
			bean.preDestroy();
		}
	}
}
