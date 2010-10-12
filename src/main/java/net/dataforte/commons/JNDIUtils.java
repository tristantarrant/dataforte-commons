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
package net.dataforte.commons;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a variety of utility methods for searching a JNDI tree for specific items
 * by name or by class, for injecting annotated resources fetched from JNDI, etc
 * 
 * @author Tristan Tarrant
 */
public class JNDIUtils {
	static final Logger log = LoggerFactory.getLogger(JNDIUtils.class);
	
	private static Map<Class<?>, Object> jndiClassCache = new HashMap<Class<?>, Object>();
	private static Map<String, Object> jndiNameCache = new HashMap<String, Object>();

	public static <T> T lookup(String name, Class<T> klass) throws NamingException {
		InitialContext ctx = new InitialContext();
		return klass.cast(ctx.lookup(name));
	}

	public static Object findByName(Context ctx, String name) throws NamingException {
		if(jndiNameCache.containsKey(name)) {
			return jndiNameCache.get(name);
		}
		NamingEnumeration<NameClassPair> ne = ctx.list("");
		if (ne.hasMoreElements()) {
			while (ne.hasMoreElements()) {
				try {
					NameClassPair ncp = (NameClassPair) ne.nextElement();
					Object el = ctx.lookup(ncp.getName());
					if (name.equals(ncp.getName())) {
						jndiNameCache.put(name, el);
						return el;
					} else if (el instanceof Context) {
						Object o = findByName((Context) el, name);
						if (o != null)
							return o;
					}
				} catch (Exception e) {
				}

			}
		}
		return null;
	}
	
	/**
	 * Searches the JNDI tree for the first instance of an object which implements/extends the
	 * specified class.
	 * 
	 * @param <T> The generic of the class that is being searched
	 * @param ctx The JNDI context to search
	 * @param klass The base interface/class to look for
	 * @return The first instance of the object that implements/extends klass. If no such object is found, then null is returned
	 * @throws NamingException
	 */
	public static <T> T findByClass(Context ctx, Class<T> klass) throws NamingException {
		if(jndiClassCache.containsKey(klass)) {
			return klass.cast(jndiClassCache.get(klass));
		}
		NamingEnumeration<NameClassPair> ne = ctx.list("");
		if (ne.hasMoreElements()) {
			while (ne.hasMoreElements()) {
				try {
					NameClassPair ncp = (NameClassPair) ne.nextElement();
					Object el = ctx.lookup(ncp.getName());
					if(klass.isAssignableFrom(el.getClass())) {
						jndiClassCache.put(klass, el);
						return klass.cast(el);
					} else if (el instanceof Context) {
						Object o = findByClass((Context) el, klass);
						if (o != null)
							return klass.cast(o);
					}
				} catch (Exception e) {
				}

			}
		}
		return null;
	}

	/**
	 * Binds an object to the JNDI tree. The tree must be read/write. This method also supports JBoss's extension for 
	 * binding non serializable objects to the tree.
	 * 
	 * @param jndiName
	 * @param obj
	 */
	public static void bind(String jndiName, Object obj) {
		try {
			Context ctx = new InitialContext();
			try {
				ctx.unbind(jndiName);
			} catch (Exception e) {
				// Ignore
			}
			try {
				// Use alternative method for JBoss
				Class<?> nonSerializableFactory = Class.forName("org.jboss.naming.NonSerializableFactory");
				Method rebind = nonSerializableFactory.getMethod("rebind", Context.class, String.class, Object.class);
				rebind.invoke(null, ctx, jndiName, obj);
			} catch (Exception e) {
				ctx.rebind(jndiName, obj);
			}

			log.info("Bound " + obj.getClass().getName() + " to " + jndiName);
		} catch (NamingException e) {
			log.error("Error in JNDI binding", e);
		}
	}

	/**
	 * Injects JNDI resources in any object. The method scans all fields and methods for standard annotations such as
	 * {@link Resource}, {@link PersistenceContext}, {@link PersistenceUnit}, {@link EJB}, {@link WebServiceRef}
	 * 
	 * @param instance the object to inject resources into
	 */
	public static void inject(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
		Context context = new InitialContext();

		Class<?> c = instance.getClass();
		
		// Initialize fields annotations
		while(c!=null) {
			Field[] fields = c.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].isAnnotationPresent(Resource.class)) {
					Resource annotation = (Resource) fields[i].getAnnotation(Resource.class);
					lookupJNDIFieldResource(context, instance, fields[i], annotation.name());
				}
				if (fields[i].isAnnotationPresent(EJB.class)) {
					EJB annotation = (EJB) fields[i].getAnnotation(EJB.class);
					lookupJNDIFieldResource(context, instance, fields[i], annotation.name());
				}
				if (fields[i].isAnnotationPresent(WebServiceRef.class)) {
					WebServiceRef annotation = (WebServiceRef) fields[i].getAnnotation(WebServiceRef.class);
					lookupJNDIFieldResource(context, instance, fields[i], annotation.name());
				}
				if (fields[i].isAnnotationPresent(PersistenceContext.class)) {
					PersistenceContext annotation = (PersistenceContext) fields[i].getAnnotation(PersistenceContext.class);
					lookupJNDIFieldResource(context, instance, fields[i], annotation.name());
				}
				if (fields[i].isAnnotationPresent(PersistenceUnit.class)) {
					PersistenceUnit annotation = (PersistenceUnit) fields[i].getAnnotation(PersistenceUnit.class);
					lookupJNDIFieldResource(context, instance, fields[i], annotation.name());
				}				
			}
	
			// Initialize methods annotations
			Method[] methods = c.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].isAnnotationPresent(Resource.class)) {
					Resource annotation = (Resource) methods[i].getAnnotation(Resource.class);
					lookupJNDIMethodResource(context, instance, methods[i], annotation.name());
				}
				if (methods[i].isAnnotationPresent(EJB.class)) {
					EJB annotation = (EJB) methods[i].getAnnotation(EJB.class);
					lookupJNDIMethodResource(context, instance, methods[i], annotation.name());
				}
				if (methods[i].isAnnotationPresent(WebServiceRef.class)) {
					WebServiceRef annotation = (WebServiceRef) methods[i].getAnnotation(WebServiceRef.class);
					lookupJNDIMethodResource(context, instance, methods[i], annotation.name());
				}
				if (methods[i].isAnnotationPresent(PersistenceContext.class)) {
					PersistenceContext annotation = (PersistenceContext) methods[i].getAnnotation(PersistenceContext.class);
					lookupJNDIMethodResource(context, instance, methods[i], annotation.name());
				}
				if (methods[i].isAnnotationPresent(PersistenceUnit.class)) {
					PersistenceUnit annotation = (PersistenceUnit) methods[i].getAnnotation(PersistenceUnit.class);
					lookupJNDIMethodResource(context, instance, methods[i], annotation.name());
				}
			}
			// Walk up the hierarchy
			c = c.getSuperclass();
		}
	}
	
	/**
	 * Inject resources in specified field. Internal method used by the invoke() method
	 */
	private static void lookupJNDIFieldResource(javax.naming.Context context, Object instance, Field field, String name) throws NamingException,
			IllegalAccessException {

		Object lookedupResource = null;
		boolean accessibility = false;

		// Attempt to lookup by name first
		if ((name != null) && (name.length() > 0)) {
			lookedupResource = context.lookup(name);
		} else {
			lookedupResource = findByClass(context, field.getType());
		}

		accessibility = field.isAccessible();
		field.setAccessible(true);
		if(log.isDebugEnabled()) {
			log.debug("Injecting "+lookedupResource+" into "+instance.getClass().getName()+"."+name);
		}
		field.set(instance, lookedupResource);
		field.setAccessible(accessibility);
	}

	/**
	 * Inject resources in specified method. Internal method used by the invoke() method
	 */
	private static void lookupJNDIMethodResource(javax.naming.Context context, Object instance, Method method, String name) throws NamingException,
			IllegalAccessException, InvocationTargetException {

		if (!method.getName().startsWith("set") || method.getParameterTypes().length != 1 || !method.getReturnType().getName().equals("void")) {
			throw new IllegalArgumentException("Invalid method resource injection annotation");
		}

		Object lookedupResource = null;
		boolean accessibility = false;

		if ((name != null) && (name.length() > 0)) {
			lookedupResource = context.lookup(name);
		} else {
			lookedupResource = findByClass(context, method.getParameterTypes()[0]);
		}

		accessibility = method.isAccessible();
		method.setAccessible(true);
		method.invoke(instance, lookedupResource);
		method.setAccessible(accessibility);
	}
}
