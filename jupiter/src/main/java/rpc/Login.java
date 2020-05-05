package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.MySQLConnection;

/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //only for test, log in should use doPost
    //here only check if log in successfully
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		JSONObject obj = new JSONObject();
		if (session != null) {
			MySQLConnection connection = new MySQLConnection();
			String userId = session.getAttribute("user_id").toString();
			obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			connection.close();
		} else {
			obj.put("status", "Invalid Session");
			response.setStatus(403);
		}
		RpcHelper.writeJsonObject(response, obj);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    //most important one !
    //find if match id, create session
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject input = RpcHelper.readJSONObject(request);
		String userId = input.getString("user_id");
		//password should be hashed in the front end, so here should be hashed password
		String password = input.getString("password");

		MySQLConnection connection = new MySQLConnection();
		JSONObject obj = new JSONObject();
		//to verify in db, if verify match return true
		if (connection.verifyLogin(userId, password)) {
			//set id to session, bind with this session
			//check if the id match
			//if .getSession()don't include false, it will create a new session--it is our logic here
			HttpSession session = request.getSession();
			session.setAttribute("user_id", userId);
			//time out
			session.setMaxInactiveInterval(600);
			//show in the result, also provide user id and full name --getFullname()
			obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
		} else {
			obj.put("status", "User Doesn't Exist");
			//401 error--unauthorized
			response.setStatus(401);
		}
		connection.close();
		RpcHelper.writeJsonObject(response, obj);
	}


}
