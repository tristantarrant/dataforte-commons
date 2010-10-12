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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which scans the classpath looking for Service Provider configuration files
 * following the standard Service Provider pattern specified in the JAR File Specification. 
 * 
 * @author Tristan Tarrant
 *
 */
public class ServiceFinder {
	private static Logger log = LoggerFactory.getLogger(ServiceFinder.class);
	private static final Pattern nonCommentPattern = Pattern.compile("^([^#]+)");
	
	
	/**
	 * Scans the classpath for Service Provider files for the specified class.
	 * Returns a list of SPI instances. This method use the Thread's Context
	 * ClassLoader
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public static <T> List<Class<? extends T>> findServices(Class<T> clazz) {
		return findServices(clazz, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Scans the classpath for Service Provider files for the specified class.
	 * Returns a list of SPI instances
	 * 
	 * @param <T>
	 * @param clazz
	 * @param classLoader the classloader to use
	 * @return
	 */
	public static <T> List<Class<? extends T>> findServices(Class<T> clazz, ClassLoader classLoader) {
		List<Class<? extends T>> spis = new ArrayList<Class<? extends T>>();

		Enumeration<?> resources;

		try {
			resources = classLoader.getResources("META-INF/services/" + clazz.getName());
		} catch (IOException e) {
			log.error("Could not load service files for interface " + clazz.getName(), e);
			return spis;
		}

		while (resources.hasMoreElements()) {
			URL url = (URL) resources.nextElement();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					Matcher m = nonCommentPattern.matcher(line);
					if (m.find()) {
						String spiClassName = m.group().trim();
						try {
							Class<?> spiClass = Class.forName(spiClassName, true, classLoader);
							// Try to instantiate the SPI to detect any missing classes / dependencies
							spiClass.asSubclass(clazz).newInstance();
							spis.add(spiClass.asSubclass(clazz));
						} catch (NoClassDefFoundError ncdfe) {
							log.info("Could not instantiate SPI class " + spiClassName + " because of missing dependency " + ncdfe.getMessage());
						} catch (ClassCastException cce) {
							log.warn("Skipping SPI class " + spiClassName + " as it does not implement interface " + clazz.getName());
						} catch (ClassNotFoundException cnfe) {
							log.warn("Could not find SPI class " + spiClassName);
						} catch (InstantiationException e) {
							log.warn("Could not instantiate SPI class " + spiClassName);
						} catch (IllegalAccessException e) {
							log.warn("Could not instantiate SPI class " + spiClassName);
						}
					}
				}
				reader.close();
			} catch (IOException ioe) {
				log.warn("Error while reading SPIs from " + url.toString(), ioe);
			}
		}
		return spis;
	}
}
