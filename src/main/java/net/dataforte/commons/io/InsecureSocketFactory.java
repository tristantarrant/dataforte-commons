/**
 * Copyright 2010 Tristan Tarrant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dataforte.commons.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class provides a SSLSocketFactory which accepts all certificates
 * silently. It should only be used in development and testing environments.
 * 
 * Example uses:
 * 
 * <ul>
 * <li>Add the following entries to your java.security file: 
 * <code>
 * ssl.SocketFactory.provider=net.dataforte.commons.io.InsecureSocketFactory
 * ssl.ServerSocketFactory.provider=net.dataforte.commons.io.InsecureSocketFactory
 * </code></li>
 * <li>LDAPS connections: 
 * <code>
 * env.put("java.naming.ldap.factory.socket", InsecureSocketFactory.class.getName());
 * Context ctx = new InitialLdapContext(env, null);
 * </code></li>
 * </ul>
 * 
 * @author Tristan Tarrant
 */
public class InsecureSocketFactory extends SSLSocketFactory {
	protected SSLSocketFactory _factory;

	public InsecureSocketFactory() {
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");

			ctx.init(null, new TrustManager[] { new InsecureTrustManager() }, null);

			_factory = ctx.getSocketFactory();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class InsecureTrustManager implements X509TrustManager {

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	}

	public static SocketFactory getDefault() {
		return new InsecureSocketFactory();
	}

	@Override
	public Socket createSocket() throws IOException {
		return _factory.createSocket();
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {
		return _factory.createSocket(host, port);
	}

	public Socket createSocket(String host, int port) throws IOException {
		return _factory.createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		return _factory.createSocket(host, port, localHost, localPort);
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return _factory.createSocket(address, port, localAddress, localPort);
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
		return _factory.createSocket(socket, host, port, autoClose);
	}

	public String[] getDefaultCipherSuites() {
		return _factory.getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites() {
		return _factory.getSupportedCipherSuites();
	}

}
