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

import static net.dataforte.commons.serializer.JSON.*;
import static net.dataforte.commons.serializer.Menu.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class JSONTest {

	@Test
	public void testMenu() throws IOException {
		Menu menu = menu("Root",
				menu("Child 1"),
				menu("Child 2")
		);
		StringWriter sw = new StringWriter();
		mirror(sw, menu);
		assertEquals("{name : \"Root\", children : [{name : \"Child 1\", children : []}, {name : \"Child 2\", children : []}]}", sw.toString());
	}
	
	@Test
	public void testList() throws IOException {
		List<String> list = Arrays.asList("a","b","c");
		StringWriter sw = new StringWriter();
		mirror(sw, "list", list);
		assertEquals("list : [\"a\", \"b\", \"c\"]", sw.toString());
	}
	
}
