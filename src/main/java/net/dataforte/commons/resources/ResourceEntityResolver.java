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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An entity resolver for SAX parsers which searches the classpath for systemIds, including
 * WEB-INF and META-INF, using the specified resource resolver
 * 
 * @author Tristan Tarrant
 */
public class ResourceEntityResolver implements EntityResolver {
	private static final Logger log = LoggerFactory.getLogger(ResourceEntityResolver.class);
	IResourceResolver resourceResolver;
	
	public ResourceEntityResolver(IResourceResolver resourceResolver) {
		this.resourceResolver = resourceResolver;
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		if(log.isDebugEnabled()) {
			log.debug("Resolving "+systemId);
		}
		String dtdFilename = systemId.substring(systemId.lastIndexOf("/") + 1);
		
		InputStream is = resourceResolver.getResource(dtdFilename);
		if (is == null) {
			is = resourceResolver.getResource("WEB-INF/" + dtdFilename);
		}
		if (is == null) {
			is = resourceResolver.getResource("META-INF/" + dtdFilename);
		}
		if (is != null) {
			return new InputSource(is);
		} else {
			return null;
		}
	}
}
