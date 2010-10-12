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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link OutputStream} allows output going to multiple streams to be redirected
 * to a single stream
 * 
 * @author Tristan Tarrant
 *
 */
public class YOutputStream extends OutputStream {
	OutputStream os;
	List<OutputStream> sources = new ArrayList<OutputStream>();

	/**
	 * Constructs the YOutputStream.
	 * @param os the stream to which output is sent
	 * @param ss a list of OutputStreams to redirect to os
	 */
	public YOutputStream(OutputStream os, OutputStream... ss) {
		this.os = os;
		for (int i = 0; i < ss.length; i++) {
			sources.add(new YOutputStreamFilter(ss[i]));
		}
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
	}

	@Override
	public void close() throws IOException {
		os.close();
	}

	@Override
	public void write(byte[] b) throws IOException {
		os.write(b);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		os.write(b, off, len);
	}

	public class YOutputStreamFilter extends FilterOutputStream {

		public YOutputStreamFilter(OutputStream out) {
			super(out);
		}

		@Override
		public void write(int b) throws IOException {
			YOutputStream.this.write(b);
		}
	}

}
