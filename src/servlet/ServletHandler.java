package servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;

public class ServletHandler extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public ServletHandler() {
		
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws 
		ServletException, IOException {
		
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws 
		ServletException, IOException {
		doGet(req, resp);
	}
	
	public static void writeResponse(String resp, HttpServletResponse r) {
		try {
			r.setStatus(HttpServletResponse.SC_OK);
			r.getWriter().write(resp);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeErr(String message, HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			resp.getWriter().write(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
