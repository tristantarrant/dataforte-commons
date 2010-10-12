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

import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class DOM {

	public static Document root() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// should not happen
		}
		return documentBuilder.newDocument();
	}

	public static Map<String, String> attributes(String... s) {
		Map<String, String> map = new HashMap<String, String>();

		if (s == null || (s.length % 2) != 0) {
			throw new IllegalArgumentException("Parameter count must be even");
		}

		for (int i = 0; i < s.length; i += 2) {
			map.put(s[i], s[i + 1]);
		}

		return map;
	}

	public static Node node(Document doc, String name, Map<String, String> attributes, String value) {
		return node(doc, name, attributes, doc.createTextNode(value));
	}

	public static Node node(Document doc, String name, Node... children) {
		return node(doc, name, null, children);
	}

	public static Node node(Document doc, String name, Map<String, String> attributes, Node... children) {
		Element element = doc.createElement(name);

		if (attributes != null) {
			for (Entry<String, String> attribute : attributes.entrySet()) {
				element.setAttribute(attribute.getKey(), attribute.getValue());
			}
		}

		if (children != null) {
			for (Node child : children) {
				element.appendChild(child);
			}
		}

		return element;
	}

	public static final Node mirror(Node node, Collection<?> collection, String name, int depth) {
		Document document;
		// We stop here if depth is 0
		if (depth == 0) {
			return node;
		}
		if (node instanceof Document) {
			document = (Document) node;
		} else {
			document = node.getOwnerDocument();
		}

		Element objElement = document.createElement(name);
		for (Iterator<?> it = collection.iterator(); it.hasNext();) {
			mirror(objElement, it.next(), depth - 1);
		}
		node.appendChild(objElement);
		return node;
	}

	public static final Node mirror(Node node, Map<?, ?> map, String name, int depth) {
		Document document;
		// We stop here if depth is 0
		if (depth == 0) {
			return node;
		}
		if (node instanceof Document) {
			document = (Document) node;
		} else {
			document = node.getOwnerDocument();
		}

		Element objElement = document.createElement(name);

		for (Object key : map.keySet()) {
			if (key instanceof String) {

				Object value = map.get(key);
				if (value instanceof String) {
					Element keyElement = document.createElement((String) key);
					keyElement.appendChild(document.createTextNode((String) value));
					objElement.appendChild(keyElement);
				} else if (value instanceof String[]) {
					String[] sarr = (String[]) value;
					for (String s : sarr) {
						Element keyElement = document.createElement((String) key);
						keyElement.appendChild(document.createTextNode(s));
						objElement.appendChild(keyElement);
					}
				}

			}
		}
		node.appendChild(objElement);
		return node;
	}

	public static final Node mirror(Object obj) {
		return mirror(null, obj);
	}

	public static final Node mirror(Node node, String name, Object obj) {
		return mirror(node, name, obj, -1); // Infinity
	}

	public static final Node mirror(Node node, Object obj) {
		return mirror(node, obj, -1); // Infinity
	}

	public static final Node mirror(Node node, Object obj, int depth) {
		return mirror(node, null, obj, depth);
	}

	public static final Node mirror(Node node, String objName, Object obj, int depth) {
		Document document;
		// We stop here if depth is 0
		if (depth == 0) {
			return node;
		}
		if (node == null) {
			node = root();
		}
		if (node instanceof Document) {
			document = (Document) node;
		} else {
			document = node.getOwnerDocument();
		}

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

				return mirror(node, collection, objName, depth);
			} else {
				return node;
			}
		}
		if (objName == null)
			objName = objClass.getSimpleName().toLowerCase();
		if (!objName.matches("\\p{Alnum}*")) {
			return node;
		}
		Element objElement = document.createElement(objName);
		Method methods[] = objClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method getter = methods[i];
			String name = getter.getName();
			if (name.startsWith("get") && !name.equals("getClass") && getter.getParameterTypes().length == 0) {
				try {
					Object value = getter.invoke(obj, (Object[]) null);
					if (value != null) {
						// if it's a collection, handle it specially
						if (value instanceof Collection<?>) {
							objElement = (Element) mirror(objElement, (Collection<?>) value, name.substring(3).toLowerCase(), depth - 1);
						} else if (value instanceof Map<?, ?>) {
							objElement = (Element) mirror(objElement, (Map<?, ?>) value, name.substring(3).toLowerCase(), depth - 1);
							// if it's a standard java type, handle it with
							// toString()
						} else if (value.getClass().isPrimitive() || value.getClass().getName().startsWith("java")) {
							objElement.setAttribute(name.substring(3).toLowerCase(), value.toString());
						} else {
							// Custom type: descend recursively
							objElement = (Element) mirror(objElement, value, depth - 1);
						}
					}
				} catch (Exception e) {
					// Ignore exceptions
				}
			}
		}
		node.appendChild(objElement);
		return node;
	}
	
	public static void write(Node node, Writer w) {
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (Exception e) {
			// Ignore
		}
		DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
		LSSerializer writer = impl.createLSSerializer();
		LSOutput output = impl.createLSOutput();
		output.setCharacterStream(w);
		writer.write(node, output);
	}
}
