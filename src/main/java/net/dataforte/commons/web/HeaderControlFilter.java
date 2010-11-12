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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderControlFilter implements Filter {
	final Logger log = LoggerFactory.getLogger(HeaderControlFilter.class);

	/**
	 * The default character encoding to set for requests that pass through this
	 * filter.
	 */
	protected String encoding = null;

	/**
	 * The encoding to which requests need to be reencoded
	 */
	protected String reencoding = null;

	/**
	 * Whether to force secure (SSL) URLs
	 */
	boolean forceSecure = false;

	/**
	 * Host for which to force secure URLs
	 */
	String forceSecureHost = null;

	/**
	 * The filter configuration object we are associated with. If this value is
	 * null, this filter instance is not currently configured.
	 */
	protected FilterConfig filterConfig = null;

	/**
	 * A list of default headers to add to responses
	 */
	protected List<KeyValuePair> responseHeaders = null;

	/**
	 * Select and set (if specified) the character encoding to be used to
	 * interpret request parameters for this request.
	 * 
	 * @param request
	 *            The servlet request we are processing
	 * @param result
	 *            The servlet response we are creating
	 * @param chain
	 *            The filter chain we are processing
	 * 
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet error occurs
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		// Verify if we need to change to secure mode
		if (forceSecure && !request.isSecure()) {

			String host = httpRequest.getHeader("X-FORWARDED-HOST");
			if (host == null) {
				host = httpRequest.getHeader("HOST");
			}
			if (host.equals(forceSecureHost)) {
				String queryString = httpRequest.getQueryString();
				String url = "https://" + host + httpRequest.getRequestURI() + (queryString != null ? "?" + queryString : "");
				if (log.isDebugEnabled()) {
					String originalurl = httpRequest.getScheme() + "://" + host + httpRequest.getRequestURI() + (queryString != null ? "?" + queryString : "");
					log.debug("Forcing redirect from " + originalurl + " to " + url);
				}
				httpResponse.sendRedirect(url);
				return;
			}
		}

		if (reencoding != null && request.getCharacterEncoding() != null && !reencoding.equals(request.getCharacterEncoding())) {
			request = new HeaderControlRequest(httpRequest, reencoding);
		}

		// Verify if we need to set an encoding
		if (encoding != null) {
			if (request.getCharacterEncoding() == null) {
				request.setCharacterEncoding(encoding);
			}
			httpResponse.setCharacterEncoding(encoding);
		}

		// Add response headers
		for (KeyValuePair header : responseHeaders) {
			httpResponse.addHeader(header.key, header.value);
		}

		try {
			// Pass control on to the next filter
			chain.doFilter(request, response);
		} catch (Throwable t) {
			log.error("", t);
			throw new ServletException(t);
		}
	}

	/**
	 * Place this filter into service.
	 * 
	 * @param filterConfig
	 *            The filter configuration object
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		this.encoding = filterConfig.getInitParameter("request.encoding");
		this.reencoding = filterConfig.getInitParameter("request.reencoding");
		this.forceSecure = "true".equals(filterConfig.getInitParameter("force.secure"));
		this.forceSecureHost = filterConfig.getInitParameter("force.secure.host");
		this.responseHeaders = new ArrayList<KeyValuePair>();
		for (Enumeration<String> en = filterConfig.getInitParameterNames(); en.hasMoreElements();) {
			String name = en.nextElement();
			if (name.startsWith("response.")) {
				String headerName = name.substring(9);
				responseHeaders.add(new KeyValuePair(headerName, filterConfig.getInitParameter(headerName)));
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Encoding: " + encoding + ", Force Secure: " + Boolean.toString(forceSecure));
		}
	}

	/**
	 * Take this filter out of service.
	 */
	public void destroy() {

		this.encoding = null;
		this.filterConfig = null;

	}

	public class KeyValuePair {
		String key;
		String value;

		public KeyValuePair(String key, String value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param key
		 *            the key to set
		 */
		public void setKey(String key) {
			this.key = key;
		}

		/**
		 * @param value
		 *            the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}

	}

	public class HeaderControlRequest extends HttpServletRequestWrapper {
		String encoding;
		ServletInputStream is;

		public HeaderControlRequest(HttpServletRequest request, String encoding) {
			super(request);
			this.encoding = encoding;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			if (is == null) {
				is = new InputStreamReencoder(super.getInputStream(), super.getCharacterEncoding(), encoding);
			}
			return is;
		}

	}
	
	public class InputStreamReencoder extends ServletInputStream {
        ByteArrayInputStream is;
        
        public InputStreamReencoder(ServletInputStream in, String inEncoding, String outEncoding) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            try {
                do {
                    bytesRead = in.read(buffer);
                    if (bytesRead > 0) {
                        byteOut.write(buffer, 0, bytesRead);
                    }
                } while (bytesRead > 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        // Ignore
                    }
                }
            }
            byte[] data = byteOut.toByteArray();
            try {
                        data = new String(data, inEncoding).trim().getBytes(outEncoding);
                        is = new ByteArrayInputStream(data);
                } catch (UnsupportedEncodingException e) {
                        // Ignore
                }

        }

        @Override
        public int read() throws IOException {
                return is.read();
        }
}

}
