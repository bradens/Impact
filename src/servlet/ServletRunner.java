package servlet;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

public class ServletRunner implements Runnable {

	@Override
	/**
	 * This function attempts to run the jetty servlet
	 * for impact in a new thread.
	 */
	public void run() {
		try {
			Server server = new Server();
			server.addListener(":5000");
			ServletHttpContext context = (ServletHttpContext)server.getContext("/servlet");
			context.addServlet("/public/", "servlet.ServletHandler");
			ServletHttpContext context1 = (ServletHttpContext) server.getContext("/");
			String dir = System.getProperty("user.dir") + "/public/";
			context1.setResourceBase(dir);
			context1.addServlet("/", "org.mortbay.jetty.servlet.Default");
			server.start();
			server.join();
		}
		catch (Exception e) {
			System.out.println("Trouble initializing servlet. Exiting.");
			e.printStackTrace();
			return;
		}		
	}
	

}
