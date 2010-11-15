package net.dataforte.commons.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Observer;

import javax.servlet.ServletContext;

/**
 * A resource resolver which attempts to resolve resources by first checking
 * in the system-specific application configuration folder (see {@link SystemUtils})
 * and then falls back to using the @link {@link ServletContextResourceResolver}
 * 
 * @author Tristan Tarrant
 */
public class WebAppResourceResolver extends AResourceResolver {
	ServletContextResourceResolver servletCtxResolver;
	String localResourceFolder;
	
	public WebAppResourceResolver(ServletContext servletContext) {
		this(servletContext, servletContext.getServletContextName());
	}
	
	public WebAppResourceResolver(ServletContext servletContext, String appName) {
		servletCtxResolver = new ServletContextResourceResolver(servletContext);
		if(appName!=null) {
			localResourceFolder = SystemUtils.getAppConfigFolder(appName);
		}
	}

	@Override
	public InputStream getResource(String name, Observer observer, long delay, ThreadGroup threadGroup) {
		InputStream is = null;
		if(localResourceFolder!=null) {
			File file = new File(localResourceFolder, name);
			if(file.exists()) {
				try {
					is = new FileInputStream(file);
					// If we've been passed an observer, check the file regularly for
					// changes, in order for it be reloaded
					if (observer != null) {					
						FileObserver fo = new FileObserver(file.getAbsolutePath(), threadGroup);
						fo.setExitOnChange(true);
						if(delay>0) {
							fo.setDelay(delay);
						}
						fo.addObserver(observer);
					}
				} catch (FileNotFoundException e) {
					// Ignore
				}
			}
		}
		if (is == null && this.servletCtxResolver!=null) {
			is = servletCtxResolver.getResource(name, observer, delay, threadGroup);
		}
		return is;
	}
	
	
}
