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
package net.dataforte.commons.slf4j;

import org.slf4j.Logger;

/**
 * Creates a slf4j using the stack to determine the context so that 
 * the class does not need to be specified.
 *  
 * @author Tristan Tarrant
 */
public class LoggerFactory {
	public static Logger make() {
		Throwable t = new Throwable();
		StackTraceElement parent = t.getStackTrace()[1];
		return org.slf4j.LoggerFactory.getLogger( parent.getClass());
	}
}
