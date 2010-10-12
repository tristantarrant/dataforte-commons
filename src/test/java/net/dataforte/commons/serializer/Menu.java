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

import java.util.LinkedHashSet;
import java.util.Set;

public class Menu {
	String name;
	String command;
	String icon;
	Set<Menu> children = new LinkedHashSet<Menu>();
	
	public Menu(String name, Menu...menus) {
		this(name, null, null, menus);
	}
	
	public Menu(String name, String command, Menu...menus) {
		this(name, command, null, menus);
	}
	
	public Menu(String name, String command, String icon, Menu...menus) {
		this.name = name;
		this.command = command;
		this.icon = icon;
		if(menus!=null) {
			for(Menu child : menus) {
				children.add(child);
			}
		}
	}
	
	public static Menu menu(String name, String command, String icon, Menu...menus) {
		return new Menu(name, command, icon, menus);
	}
	
	public static Menu menu(String name, String command, Menu...menus) {
		return new Menu(name, command, menus);
	}
	
	public static Menu menu(String name, Menu...menus) {
		return new Menu(name, menus);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Set<Menu> getChildren() {
		return children;
	}

	public void setChildren(Set<Menu> children) {
		this.children = children;
	}
}
