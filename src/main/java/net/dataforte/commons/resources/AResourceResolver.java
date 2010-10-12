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
 * This class makes writing implementations of {@link IResourceResolver} easier
 * by providing default implementations for some of its methods
 * 
 * @author Tristan Tarrant
 * 
 */
public abstract class AResourceResolver implements IResourceResolver {

	@Override
	public InputStream getResource(String name) {
		return getResource(name, null, -1, null);
	}

	@Override
	public InputStream getResource(String name, Observer observer, long delay) {
		return getResource(name, observer, delay, null);
	}

	@Override
	public InputStream getResource(String name, Observer observer) {
		return getResource(name, observer, -1, null);
	}

}
