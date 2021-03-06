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

import javax.servlet.ServletContext;


/**
 * A resource resolver which uses the ServletContext's facilities. An optional
 * fallback resolver may be specified (defaults to the {@link ClassLoaderResourceResolver})
 * 
 * @author Tristan Tarrant
 */
public class ServletContextResourceResolver extends AResourceResolver {
	ServletContext servletContext;
	IResourceResolver fallbackResolver;
	
	public ServletContextResourceResolver(ServletContext servletContext) {
		this.servletContext =servletContext;
		this.fallbackResolver = new ClassLoaderResourceResolver();
	}
	
	public ServletContextResourceResolver(ServletContext servletContext, IResourceResolver fallbackResolver) {
		this.servletContext = servletContext;
		this.fallbackResolver = fallbackResolver;
	}

	@Override
	public InputStream getResource(String name, Observer observer, long delay, ThreadGroup threadGroup) {
		InputStream is = null;
		if (servletContext != null) {
			is = servletContext.getResourceAsStream(name);
			// If we've been passed an observer, check the file regularly for
			// changes, in order for it be reloaded
			if (observer != null) {
				String realPath = servletContext.getRealPath(name);
				FileObserver fo = new FileObserver(realPath, threadGroup);
				fo.setExitOnChange(true);
				if(delay>0) {
					fo.setDelay(delay);
				}
				fo.addObserver(observer);
			}
		}
		if (is == null && this.fallbackResolver!=null) {
			is = fallbackResolver.getResource(name, observer, delay, threadGroup);
		}
		return is;
	}
}
