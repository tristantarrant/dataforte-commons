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
package net.dataforte.commons.log4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;

/**
 * A {@link RepositorySelector} implementation which supports local Log4J configuration files (under the META-INF folder)
 * without affecting the container's configuration (e.g. JBoss).
 * 
 * @author Tristan Tarrant
 *
 */
public class ContextRepositorySelector implements RepositorySelector {
	private static boolean initialized = false;

	// This object is used for the guard because it doesn't get
	// recycled when the application is redeployed.
	private static Object guard = LogManager.getRootLogger();

	private static Map<ClassLoader, Hierarchy> repositories = new HashMap<ClassLoader, Hierarchy>();
	private static LoggerRepository defaultRepository;

	/**
	 * Register with this repository selector.
	 */
	public static synchronized void init() {
		if (!initialized) // set the global RepositorySelector
		{
			defaultRepository = LogManager.getLoggerRepository();
			RepositorySelector theSelector = new ContextRepositorySelector();
			LogManager.setRepositorySelector(theSelector, guard);
			initialized = true;
		}

		Hierarchy hierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			loadLog4JConfig(hierarchy, loader);
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize ContextRepositorySelector", e);
		}
		
		repositories.put(loader, hierarchy);		
	}

	// load log4j.xml/log4j.properties from META-INF
	private static void loadLog4JConfig(Hierarchy hierarchy, ClassLoader loader) throws Exception {
		InputStream log4JConfig = loader.getResourceAsStream("/META-INF/log4j.xml");
		if(log4JConfig!=null) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();			
			Document doc = documentBuilder.parse(log4JConfig);
			DOMConfigurator conf = new DOMConfigurator();
			conf.doConfigure(doc.getDocumentElement(), hierarchy);
		} else {
			log4JConfig = loader.getResourceAsStream("/META-INF/log4j.properties");
			if(log4JConfig!=null) {
				PropertyConfigurator conf = new PropertyConfigurator();
				Properties properties = new Properties();
				properties.load(log4JConfig);
				conf.doConfigure(properties, hierarchy);
			}
		}
	}

	private ContextRepositorySelector() {
		// constructor private - no access allowed
	}

	public LoggerRepository getLoggerRepository() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		LoggerRepository repository = (LoggerRepository) repositories.get(loader);

		if (repository == null) {
			return defaultRepository;
		} else {
			return repository;
		}
	}
}
