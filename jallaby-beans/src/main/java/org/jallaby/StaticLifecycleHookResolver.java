package org.jallaby;

import org.jallaby.beans.JallabyBeansLifecycleHook;
import org.jallaby.spi.LifecycleHook;

public class StaticLifecycleHookResolver {
	
	private StaticLifecycleHookResolver() {	
	}
	
	public static LifecycleHook resolve() {
		return new JallabyBeansLifecycleHook();
	}
}
