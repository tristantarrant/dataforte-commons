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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class ResourceFinder {

	public static InputStream getResource(String name) {
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		return tccl.getResourceAsStream(name);
	}
	
	public static InputStream getLastResource(String name) throws IOException {
		ClassLoader tccl = Thread.currentThread().getContextClassLoader();
		//InputStream is = tccl.getResourceAsStream(name);
		Enumeration<URL> resources = tccl.getResources(name);
		URL url = null;
		while(resources.hasMoreElements()) {
			url = resources.nextElement();
		}
		
		return url!=null?url.openStream():null;
	}
}
