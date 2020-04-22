package rpc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class RpcHelper {
	// Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException{
		//tell front-end/response we will return JSON type
		response.setContentType("application/json");
		//write and return to front end, 
		//PrintWriter getWriter() Returns a <code>PrintWriter</code> object that can send character text to the client.
		response.getWriter().print(array);
	}

              // Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {		
		response.setContentType("application/json");
		response.getWriter().print(obj);
	}

}
