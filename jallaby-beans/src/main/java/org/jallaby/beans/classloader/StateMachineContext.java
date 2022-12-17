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
//
//========================================================================
//Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//
//This program and the accompanying materials are made available under the
//terms of the Eclipse Public License v. 2.0 which is available at
//https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
//which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
//SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
//========================================================================
//

package org.jallaby.beans.classloader;

import java.io.IOException;
import java.net.URL;
import java.security.PermissionCollection;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.jallaby.beans.util.ClassMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMachineContext implements StateMachineClassLoader.Context {
	static final Logger LOG = LoggerFactory.getLogger(StateMachineContext.class);
	
	// System classes are classes that cannot be replaced by
	// the web application, and they are *always* loaded via
	// system classloader.
	private static final ClassMatcher SYSTEM_CLASSES = new ClassMatcher(
			"java.", // Java SE classes
			"javax.", // Java SE classes
			"org.xml.", // javax.xml
			"org.w3c." // javax.xml
	);
	
	// Server classes are classes that are hidden from being
	// loaded by the web application using system classloader,
	// so if web application needs to load any of such classes,
	// it has to include them in its distribution.
	private  static final ClassMatcher SERVER_CLASSES = new ClassMatcher(
			"org.jallyby." // hide jallaby classes
	);
	
	private final ClassMatcher systemClasses = new ClassMatcher(SYSTEM_CLASSES);
	private final ClassMatcher serverClasses = new ClassMatcher(SERVER_CLASSES);

	private PermissionCollection permissions;
	private List<Resource> extraClasspath;
	private boolean parentLoaderPriority;
	
	@Override
	public boolean isSystemClass(Class<?> clazz) {
		return systemClasses.match(clazz);
	}

	@Override
	public boolean isServerClass(Class<?> clazz) {
		return serverClasses.match(clazz);
	}

	@Override
	public Resource newResource(String urlOrPath) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermissionCollection getPermissions() {
		return permissions;
	}

	public void setPermissions(PermissionCollection permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public boolean isParentLoaderPriority() {
		return parentLoaderPriority;
	}

	public void setParentLoaderPriority(boolean parentLoaderPriority) {
		this.parentLoaderPriority = parentLoaderPriority;
	}
	
	@Override
	public List<Resource> getExtraClasspath() {
		return extraClasspath;
	}

	/**
	 * Set the Extra ClassPath via delimited String.
	 * <p>
	 * This is a convenience method for {@link #setExtraClasspath(List)}
	 * </p>
	 *
	 * @param extraClasspath Comma or semicolon separated path of filenames or URLs
	 *                       pointing to directories or jar files. Directories
	 *                       should end with '/'.
	 * @throws IOException if unable to resolve the resources referenced
	 * @see #setExtraClasspath(List)
	 */
	public void setExtraClasspath(String extraClasspath) throws IOException {
		setExtraClasspath(Resource.fromList(extraClasspath, false, this::newResource));
	}

	public void setExtraClasspath(List<Resource> extraClasspath) {
		this.extraClasspath = extraClasspath;
	}

	@Override
	public boolean isServerResource(String name, URL url) {
		return serverClasses.match(name, url);
	}

	@Override
	public boolean isSystemResource(String name, URL url) {
		return systemClasses.match(name, url);
	}
}
