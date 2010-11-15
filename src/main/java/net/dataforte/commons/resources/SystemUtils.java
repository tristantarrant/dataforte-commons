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

public class SystemUtils {
	/**
	 * Returns an appropriate system-dependent folder for storing application-specific
	 * data. The logic in this method uses the os.name to decide which is best.
	 * Currently it uses:
	 * 		~/.config/${appName} on Unix/Linux (as per Freedesktop.org)
	 * 		%APPDATA%/Sun/Java/${appName} on Windows
	 * 		~/Library/Java/${appName} on Mac OS X
	 * @param appName
	 * @return
	 */
	public static String getAppConfigFolder(String appName) {
		String configRoot = null;
		String osName = System.getProperty("os.name");
		if("Mac OS X".equals(osName)) {
			configRoot = System.getProperty("user.home")+File.separator+"Library"+File.separator+"Java";
		} else if(osName.startsWith("Windows")) {
			// If on Windows, use the APPDATA environment
			try {
				configRoot = System.getenv("APPDATA");
				if(configRoot!=null) { 
					configRoot = configRoot +File.separator+"Sun"+File.separator+"Java"; // FIXME: should be different if using other JVMs from other vendors
				}
			} catch (SecurityException e) {
				// We may be wrapped by a SecurityManager, ignore the exception
			}
		}
		if(configRoot==null) {
			// Use the user.home
			configRoot = System.getProperty("user.home")+File.separator+".config";
		}
		return configRoot+File.separator+appName;
	}

}
