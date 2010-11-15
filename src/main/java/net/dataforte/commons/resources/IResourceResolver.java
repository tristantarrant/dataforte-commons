/**
 * Copyright 2010 Tristan Tarrant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dataforte.commons.resources;

import java.io.InputStream;
import java.util.Observer;

/**
 * The interface that all ResourceResolvers must implement.
 * 
 * @author Tristan Tarrant
 *
 */
public interface IResourceResolver {
	/**
	 * Obtains an {@link InputStream} to the specified named resource
	 * @param name the name of the resource
	 * @return an {@link InputStream} to the specified resource
	 */
	InputStream getResource(String name);

	/**
	 * Obtains an {@link InputStream} to the specified named resource and adds
	 * an {@link Observer} which will be invoked when the resource changes (using a {@link Thread})
	 * using a default check delay (currently 5 seconds)
	 * 
	 * @param name the name of the resource
	 * @param observer observer the {@link Observer} to invoke when the resource changes
	 * @return an {@link InputStream} to the specified resource
	 */
	InputStream getResource(String name, Observer observer);
	
	/**
	 * Obtains an {@link InputStream} to the specified named resource and adds
	 * an {@link Observer} which will be invoked when the resource changes (using a {@link Thread})
	 * using the specified delay
	 * 
	 * @param name the name of the resource
	 * @param observer the {@link Observer} to invoke when the resource changes
	 * @param delay the amount of delay in milliseconds between checks for changes
	 * @return an {@link InputStream} to the specified resource
	 */
	InputStream getResource(String name, Observer observer, long delay);

	/**
	 * Obtains an {@link InputStream} to the specified named resource and adds
	 * an {@link Observer} which will be invoked when the resource changes.
	 * The watch thread will be added to the specified {@link ThreadGroup}
	 * 
	 * @param name the name of the resource
	 * @param observer the {@link Observer} to invoke when the resource changes
	 * @param delay the amount of delay in milliseconds between checks for changes
	 * @param threadGroup the {@link ThreadGroup} to which the thread which checks for changes will belong
	 * @return an {@link InputStream} to the specified resource
	 */
	InputStream getResource(String name, Observer observer, long delay, ThreadGroup threadGroup);
}
