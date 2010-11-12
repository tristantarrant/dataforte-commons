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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {
	
	/**
	 * Returns all resources beneath a folder. Supports filesystem, JARs and JBoss VFS
	 * 
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public static URL[] getResources(String folder) throws IOException {
		List<URL> urls = new ArrayList<URL>();
		ArrayList<File> directories = new ArrayList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new IOException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(folder);
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				String resProtocol = res.getProtocol();
				if (resProtocol.equalsIgnoreCase("jar")) {
					JarURLConnection conn = (JarURLConnection) res.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries())) {
						if (e.getName().startsWith(folder) && !e.getName().endsWith("/")) {
							urls.add(new URL(res+"/"+e.getName().substring(folder.length()+1)));  // FIXME: fully qualified name
						}
					}
				} else if(resProtocol.equalsIgnoreCase("vfszip")||resProtocol.equalsIgnoreCase("vfs")) { // JBoss 5+
					try {
						Object content = res.getContent();
						Method getChildren = content.getClass().getMethod("getChildren");
						List<?> files = (List<?>)getChildren.invoke(res.getContent());
						Method toUrl = null;
						for(Object o : files) {
							if(toUrl==null) {
								toUrl = o.getClass().getMethod("toURL");
							}
							urls.add((URL)toUrl.invoke(o));
						}
					} catch (Exception e) {
						throw new IOException("Error while scanning "+res.toString(),e);
					}
				} else if(resProtocol.equalsIgnoreCase("file")) {
					directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
				} else {
					throw new IOException("Unknown protocol for resource: "+res.toString());
				}
			}
		} catch (NullPointerException x) {
			throw new IOException(folder + " does not appear to be a valid folder (Null pointer exception)");
		} catch (UnsupportedEncodingException encex) {
			throw new IOException(folder + " does not appear to be a valid folder (Unsupported encoding)");
		}

		// For every directory identified capture all the .class files
		for (File directory : directories) {
			if (directory.exists()) {
				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (String file : files) {
					urls.add(new URL("file://"+directory.getAbsolutePath()+File.separator+file));
				}
			} else {
				throw new IOException(folder + " (" + directory.getPath() + ") does not appear to be a valid folder");
			}
		}
		URL[] urlsA = new URL[urls.size()];
		urls.toArray(urlsA);
		return urlsA;
	}

	/**
	 * Returns all classes within the specified package. Supports filesystem, JARs and JBoss VFS
	 * 
	 * @param folder
	 * @return
	 * @throws IOException
	 */
	public static Class<?>[] getClassesForPackage(String pckgname) throws ClassNotFoundException {
		// This will hold a list of directories matching the pckgname.
		// There may be more than one if a package is split over multiple
		// jars/paths
		List<Class<?>> classes = new ArrayList<Class<?>>();
		ArrayList<File> directories = new ArrayList<File>();
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			// Ask for all resources for the path
			Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
			while (resources.hasMoreElements()) {
				URL res = resources.nextElement();
				if (res.getProtocol().equalsIgnoreCase("jar")) {
					JarURLConnection conn = (JarURLConnection) res.openConnection();
					JarFile jar = conn.getJarFile();
					for (JarEntry e : Collections.list(jar.entries())) {

						if (e.getName().startsWith(pckgname.replace('.', '/')) && e.getName().endsWith(".class") && !e.getName().contains("$")) {
							String className = e.getName().replace("/", ".").substring(0, e.getName().length() - 6);							
							classes.add(Class.forName(className, true, cld));
						}
					}
				} else if(res.getProtocol().equalsIgnoreCase("vfszip")) { // JBoss 5+
					try {
						Object content = res.getContent();
						Method getChildren = content.getClass().getMethod("getChildren");
						List<?> files = (List<?>)getChildren.invoke(res.getContent());
						Method getPathName = null;
						for(Object o : files) {
							if(getPathName==null) {
								getPathName = o.getClass().getMethod("getPathName");
							}
							String pathName = (String) getPathName.invoke(o);
							if(pathName.endsWith(".class")) {
								String className = pathName.replace("/", ".").substring(0, pathName.length() - 6);
								classes.add(Class.forName(className, true, cld));
							}
						}
					} catch (Exception e) {
						throw new IOException("Error while scanning "+res.toString(),e);
					}
				} else {
					directories.add(new File(URLDecoder.decode(res.getPath(), "UTF-8")));
				}
			}
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)", x);
		} catch (UnsupportedEncodingException encex) {
			throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)", encex);
		} catch (IOException ioex) {
			throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname, ioex);
		}

		// For every directory identified capture all the .class files
		for (File directory : directories) {
			if (directory.exists()) {
				// Get the list of the files contained in the package
				String[] files = directory.list();
				for (String file : files) {
					// we are only interested in .class files
					if (file.endsWith(".class")) {
						// removes the .class extension
						classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
					}
				}
			} else {
				throw new ClassNotFoundException(pckgname + " (" + directory.getPath() + ") does not appear to be a valid package");
			}
		}
		Class<?>[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
	}
}
