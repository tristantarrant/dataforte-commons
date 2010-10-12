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
package net.dataforte.commons.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

/**
 * A class containing several utility methods for manipulating URLs
 * 
 * @author Tristan Tarrant
 *
 */
public class URLUtils {
	/**
	 * Returns the base url of the current request optionally including / excluding
	 * the context path
	 * 
	 * @param request
	 * @param context
	 * @return
	 */
	public static String getBaseURL(HttpServletRequest request, boolean context) {
		StringBuffer sb = request.getRequestURL();
		
		String contextPath = request.getContextPath();
		int ctxPos = sb.indexOf(contextPath, 8); // 8 works for both http and https
		if(contextPath.equals("/")) {
			contextPath = ""; // Make sure we don't append a superfluous slash in the case of ROOT webapps
		}
		return sb.substring(0, ctxPos + (context?contextPath.length():0));
	}
	
	/**
	 * Returns the base url of the current request optionally including the context
	 * path
	 * 
	 * @param request
	 * @return
	 */
	public static String getBaseURL(HttpServletRequest request) {
		return getBaseURL(request,true);
	}
	
	/**
	 * Returns the domain name of a FQDN
	 * 
	 * @param fqdn
	 * @return
	 */
	public static String getDomain(String fqdn) {
		return fqdn.substring(fqdn.indexOf('.') + 1);
	}

	/**
	 * Returns the hostname of a FQDN
	 * 
	 * @param fqdn
	 * @return
	 */
	public static String getHost(String fqdn) {
		return fqdn.substring(0, fqdn.indexOf('.'));
	}
	
	/**
	 * Performs a set of substitutions on the provided URL using
	 * information gleaned from the current request
	 * 
	 * ${baseUrl} is replaced 
	 * ${queryString} is replaced with the
	 * ${requestUri} 
	 * 
	 * @param request
	 * @param url
	 * @return
	 */
	public static String urlRewrite(HttpServletRequest request, String url) {
		// Simple substitutions:
		// ${baseUrl} http://servername/context
		// ${queryString}
		// ${requestURI}
		
		String queryString = request.getQueryString();
		String baseURL = URLUtils.getBaseURL(request);
		String reqURI;
		try {
			reqURI = URLEncoder.encode(request.getRequestURI(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			reqURI="";
		}
		return url.replaceAll("\\$\\{baseUrl\\}", baseURL).
			replaceAll("\\$\\{queryString\\}", queryString==null?"":queryString).
			replaceAll("\\$\\{requestURI\\}", reqURI);
		
	}
}
