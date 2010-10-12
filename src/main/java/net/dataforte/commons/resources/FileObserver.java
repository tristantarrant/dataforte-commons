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
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileObserver extends Observable implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(FileObserver.class);

	long lastModified;
	String file;
	public static final long DEFAULT_DELAY = 5000;
	Thread thread;
	long delay;
	boolean exitOnChange;

	public FileObserver(String s) {
		file = s;
		delay = DEFAULT_DELAY;
		start(null);
	}

	public FileObserver(String s, ThreadGroup group) {
		file = s;
		delay = DEFAULT_DELAY;
		start(group);
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public boolean isExitOnChange() {
		return exitOnChange;
	}

	public void setExitOnChange(boolean exitOnChange) {
		this.exitOnChange = exitOnChange;
	}

	public boolean check() {
		File f = new File(file);
		long actualLastModified = f.lastModified();
		if (actualLastModified == 0l) {
			// The file has disappeared, interrupt self
			thread.interrupt();
			return false;
		} else if (lastModified != actualLastModified) {
			lastModified = actualLastModified;
			setChanged();
			notifyObservers();
			return true;
		} else {
			return false;
		}
	}

	public void start(ThreadGroup group) {
		File f = new File(file);
		lastModified = f.lastModified();
		if (group != null)
			thread = new Thread(group, this, "Observe: " + file);
		else
			thread = new Thread(this, "Observe: " + file);
		thread.start();
	}

	public void run() {
		try {
			for (;;) {
				Thread.sleep(delay);
				boolean res = check();
				if (exitOnChange && res) {
					break;
				}
			}
		} catch (InterruptedException e) {
			// Do nothing
		} catch (Throwable t) {
			log.error("", t);
		}

	}
}
