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
package net.dataforte.commons.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JSON {
	
	public static final void mirror(Writer w, Collection<?> collection, String name, int depth) throws IOException {
		w.write(name+" : [");
		boolean first = true;
		for (Iterator<?> it = collection.iterator(); it.hasNext();) {
			if(!first)
				w.write(", ");
			else
				first = false;
			mirror(w, it.next(), depth - 1);
		}
		w.write("]");
	}

	public static final void mirror(Writer w, Map<?, ?> map, String name, int depth) throws IOException {
		w.write(name+" : {");
		boolean first = true;
		for (Object key : map.keySet()) {
			if (key instanceof String) {
				if(!first)
					w.write(", ");
				else
					first = false;
				
				Object value = map.get(key);
				if (value instanceof String) {
					w.write("\""+key+"\": \""+value+"\"");
				} else if (value instanceof String[]) {
					w.write("\""+key+"\": [");
					String[] sarr = (String[]) value;
					for (int i=0; i<sarr.length; i++) {
						if(i>0)
							w.write(", ");
						w.write("\""+sarr[i]+"\"");
					}
					w.write("]");
				}

			}
		}
		w.write("}");
		
	}

	public static final void mirror(Writer w, String name, Object obj) throws IOException {
		mirror(w, name, obj, -1); // Infinity
	}

	public static final void mirror(Writer w, Object obj) throws IOException {
		mirror(w, obj, -1); // Infinity
	}

	public static final void mirror(Writer w, Object obj, int depth) throws IOException {
		mirror(w, null, obj, depth);
	}

	public static final void mirror(Writer w, String objName, Object obj, int depth) throws IOException {
		Class<?> objClass = obj.getClass();
		// Special case: we have a collection and we have no name for it.
		// Calculate it by taking the first element of the collection's class
		// name and
		// pluralize it
		if (obj instanceof Collection<?>) {
			Collection<?> collection = Collection.class.cast(obj);
			Iterator<?> it = collection.iterator();
			if (it.hasNext()) {
				if (objName == null) {
					objClass = it.next().getClass();
					objName = objClass.getSimpleName().toLowerCase();
				}

				mirror(w, collection, objName, depth);	
			}
			return;
		} else if (obj instanceof String) {
			w.write("\""+obj.toString()+"\"");
			return;
		}
		if (objName == null)
			objName = objClass.getSimpleName().toLowerCase();
		if (!objName.matches("\\p{Alnum}*")) {
			return;
		}
		w.write("{");
		Method methods[] = objClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			
			Method getter = methods[i];
			String name = getter.getName();
			if (name.startsWith("get") && !name.equals("getClass") && getter.getParameterTypes().length == 0) {
				
				try {
					Object value = getter.invoke(obj, (Object[]) null);
					if (value != null) {
						if(i>0) 
							w.write(", ");
						// if it's a collection, handle it specially
						if (value instanceof Collection<?>) {
							mirror(w, (Collection<?>) value, name.substring(3).toLowerCase(), depth - 1);
						} else if (value instanceof Map<?, ?>) {
							mirror(w, (Map<?, ?>) value, name.substring(3).toLowerCase(), depth - 1);
							// if it's a standard java type, handle it with
							// toString()
						} else if (value.getClass().isPrimitive() || value.getClass().getName().startsWith("java")) {
							w.write(name.substring(3).toLowerCase()+" : \""+value.toString()+"\"");
						} else {
							// Custom type: descend recursively
							mirror(w, value, depth - 1);
						}
					}
				} catch (Exception e) {
					// Ignore exceptions
				}
			}
		}
		w.write("}");
	}
}
