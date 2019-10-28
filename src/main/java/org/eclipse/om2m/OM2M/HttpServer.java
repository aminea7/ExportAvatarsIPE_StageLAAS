package org.eclipse.om2m.OM2M;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.om2m.commons.resource.Notification;

import org.eclipse.om2m.OM2M.Mapper;

public class HttpServer {

	private static Mapper mapper = new Mapper();

	private static int PORT = 9797;
	private static String CONTEXT = "/";

	/**
	 * Get the payload as string
	 * 
	 * @param bufferedReader
	 * @return payload as string
	 */
	public static String getPayload(BufferedReader bufferedReader) {
		Scanner sc = new Scanner(bufferedReader);
		String payload = "";
		while (sc.hasNextLine()) {
			payload += sc.nextLine() + "\n";
		}
		sc.close();
		return payload;
	}

	public static class MonitorServlet extends HttpServlet {

		private static final long serialVersionUID = 2302036096330714914L;

		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			String payload = getPayload(req.getReader());
			System.out.println("Subscription received with payload:\n" + payload);

			// unmarshalling the notification
			Notification notification = null;

			// TODO

			// ...

			resp.setStatus(HttpServletResponse.SC_OK);

		}

	}

	public static void main(String[] args) throws Exception {
		
		// start the server
		Server server = new Server(PORT);
		ServletHandler servletHandler = new ServletHandler();
		
		// add servlet and context
		servletHandler.addServletWithMapping(MonitorServlet.class, CONTEXT);
		server.setHandler(servletHandler);
		server.start();
		server.join();
		
	}

}