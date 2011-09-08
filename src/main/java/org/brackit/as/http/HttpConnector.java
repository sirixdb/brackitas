/*
 * [New BSD License]
 * Copyright (c) 2011, Brackit Project Team <info@brackit.org>  
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.brackit.as.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.brackit.as.context.BaseAppContext;
import org.brackit.as.http.app.ErrorServlet;
import org.brackit.as.http.app.FrontController;
import org.brackit.as.xquery.compiler.ASCompileChain;
import org.brackit.server.metadata.manager.MetaDataMgr;
import org.brackit.server.session.Session;
import org.brackit.server.session.SessionMgr;
import org.brackit.server.tx.IsolationLevel;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.compiler.BaseResolver;
import org.brackit.xquery.util.log.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;

/**
 * 
 * @author Henrique Valer
 * 
 */
public class HttpConnector {

	private static class SessionEndListener implements HttpSessionListener {

		private final SessionMgr sessionMgr;

		SessionEndListener(SessionMgr sessionMgr) {
			this.sessionMgr = sessionMgr;
		}

		@Override
		public void sessionDestroyed(HttpSessionEvent event) {
			HttpSession httpSession = (HttpSession) event.getSession();
			Session session = (Session) httpSession
					.getAttribute(TXServlet.SESSION);
			if (session != null) {
				sessionMgr.logout(session.getSessionID());
			}
		}

		@Override
		public void sessionCreated(HttpSessionEvent arg0) {
		}
	}

	public static final String APPS_PATH = "src/main/resources/apps";

	public static final String APP_MIME_TYPES = "mimeTypes";

	public static final String APP_ERROR_DISP_TARGET = "/app/error/";

	private static final String APP_ERROR_PREFIX = APP_ERROR_DISP_TARGET + "*";

	private static final String APP_CONTROLLER_PREFIX = "/app/*";

	private static final Logger log = Logger.getLogger(HttpConnector.class);

	private final Server server;

	public HttpConnector(final MetaDataMgr mdm, final SessionMgr sessionMgr,
			final int port) {
		Log.setLog(new JettyLogger());
		this.server = new Server(port);
		server.setSessionIdManager(new HashSessionIdManager(new Random()));
		ServletContextHandler servletContextHandler = new ServletContextHandler(
				server, "/", true, false);
		servletContextHandler.setAttribute(MetaDataMgr.class.getName(), mdm);
		servletContextHandler.setAttribute(SessionMgr.class.getName(),
				sessionMgr);
		servletContextHandler
				.setAttribute(APP_MIME_TYPES, this.loadMimeTypes());
		servletContextHandler.addEventListener(new SessionEndListener(
				sessionMgr));
		servletContextHandler.addServlet(FrontController.class,
				APP_CONTROLLER_PREFIX);
		servletContextHandler.addServlet(ErrorServlet.class, APP_ERROR_PREFIX);
		// TODO: erase it
		processDeployment(servletContextHandler, sessionMgr, mdm);
	}

	public void start() throws Exception {
		try {
			server.start();
		} catch (Exception e) {
			log.error(e);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					server.stop();
				} catch (Exception e) {
					log.error(e);
				}
			}
		});
	}

	public void stop() throws Exception {
		server.stop();
	}

	private void processDeployment(ServletContextHandler sch,
			SessionMgr sessionMgr, MetaDataMgr mdm) {
		System.out.print("Deploy applications ... ");
		File f = new File(APPS_PATH);

		try {
			Session session = sessionMgr.getSession(sessionMgr.login());
			session.setIsolationLevel(IsolationLevel.NONE);
			if (f.isDirectory()) {
				File[] apps = f.listFiles();
				for (int i = 0; i < apps.length; i++) {
					if (apps[i].isDirectory()) {
						Object o = sch.getAttribute(apps[i].getName());
						BaseAppContext bac;
						if (o == null) {
							bac = new BaseAppContext(apps[i].getName(),
									new ASCompileChain(mdm, sessionMgr
											.getSession(sessionMgr.login())
											.getTX(), new BaseResolver()));
						} else {
							bac = (BaseAppContext) o;
						}
						populateAppQueries(apps[i], bac);
						sch.setAttribute(apps[i].getName(), bac);
					}
				}
			}
		} catch (Exception e) {
			log.equals(e);
		}
	}

	private void populateAppQueries(File appFolder, BaseAppContext bac)
			throws QueryException {
		File[] f = appFolder.listFiles();
		for (int i = 0; i < f.length; i++) {
			if (f[i].isDirectory()) {
				populateAppQueries(f[i], bac);
			} else {
				if (f[i].getName().endsWith(".xq")) {
					try {
						bac.register(resolvePath(f[i].getPath()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private String resolvePath(String p) {
		return p.substring("src/main/resources/".length());
	}

	private MimetypesFileTypeMap loadMimeTypes() {
		MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getClass().getClassLoader().getResourceAsStream(
							"mime.types")));
			String strLine = null;
			while ((strLine = br.readLine()) != null) {
				mimeMap.addMimeTypes(strLine);
			}
			br.close();
		} catch (IOException e) {
			log.error("Could not load mime types", e);
		}
		return mimeMap;
	}
}