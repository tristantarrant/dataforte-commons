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

package net.dataforte.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods for manipulating streams
 * 
 * @author Tristan Tarrant
 *
 */
public class StreamUtils {
	
	/**
	 * Copies all data, one byte at a time from the specified {@link InputStream} to the specified {@link OutputStream} until EOF is reached.
	 * No buffering is performed, therefore, for maximum performance, instances of {@link BufferedInputStream} and {@link BufferedOutputStream}
	 * should be used.
	 * 
	 * @param is The source InputStream
	 * @param os The target OutputStream
	 * @throws IOException
	 */
	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		for(int b=is.read(); b>=0; b=is.read()) {
			os.write(b);
		}
		os.flush();
	}
}
