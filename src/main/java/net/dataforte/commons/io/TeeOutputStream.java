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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link OutputStream} which "multiplexes" output to multiple
 * underlying OuputStreams: all operations (write, flush, close) 
 * performed on the TeeOutputStream are passed down to the child
 * OutputStreams. 
 * 
 * @author Tristan Tarrant
 *
 */
public class TeeOutputStream extends OutputStream {
	List<OutputStream> os = new ArrayList<OutputStream>();

	/**
	 * Simple constructor. Use {@link TeeOutputStream#add(OutputStream)} to 
	 * add child OutputStreams
	 */
	public TeeOutputStream() {
	};


	/**
	 * Constructor which takes a variable number of OutputStream parameters and
	 * adds them as children to this TeeOutputStream
	 * 
	 * @param ss a var-arg list of OutputStreams
	 */
	public TeeOutputStream(OutputStream... ss) {
		for(OutputStream s : ss) {
			os.add(s);
		}
	}

	/**
	 * Adds a new OutputStream to the list of child OutputStreams
	 * 
	 * @param s
	 */
	public void add(OutputStream s) {
		if (s != null) {
			os.add(s);
		}
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream s : os) {
			s.write(b);
		}
	}

	@Override
	public void close() throws IOException {
		for (OutputStream s : os) {
			s.close();
		}
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream s : os) {
			s.flush();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream s : os) {
			s.write(b, off, len);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (OutputStream s : os) {
			s.write(b);
		}
	}

}
