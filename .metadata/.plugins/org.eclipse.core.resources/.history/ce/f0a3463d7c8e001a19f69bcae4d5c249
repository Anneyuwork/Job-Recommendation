package rpc;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import db.MySQLConnection;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //read get my favorite
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		//should get user id from session, not from request
		//to avoid other people to change this user's data
		//for user_id's like, read user id
		String userId = request.getParameter("user_id");
		//Create connection
		MySQLConnection connection = new MySQLConnection();
		//getting all the liked items from db
		Set<Item> items = connection.getFavoriteItems(userId);
		//close db connection
		connection.close();
		//now need to return the item back to front-end
		//data structure to JSONArray
		JSONArray array = new JSONArray();
		//Every item to JSON Object
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			//every JSONObject need to add a parameter favorite
			//not needed in "my favorites", but need in the "Nearby" 
			//so keep consistency, also add in the SearchItem.java!
			obj.put("favorite", true);
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	//like an item, click the heart
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		//should get user id from session, not from request
		//to avoid other people to change this user's data
		//call constructor and build the connection 
		MySQLConnection connection = new MySQLConnection();
		//changed to JSONObject, input contains lots of things
		JSONObject input = RpcHelper.readJSONObject(request);
		//read user_id
		String userId = input.getString("user_id");
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
		//save into the databaseS
		connection.setFavoriteItems(userId, item);
		connection.close();
		//successfully saved, prepare for the Javascript call back function, only when the result is success, the heart is filled
		//if we have a exception, then directly fail
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	//dislike an item, click the checked heart to dislike
	//only difference with doPost: //only need to change setFavoriteItems to unsetFavoriteItems
	//this only delete item in the history table!! not in the item table
	//due to item might be used in the future, we only delete that for user in history table
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		MySQLConnection connection = new MySQLConnection();
		JSONObject input = RpcHelper.readJSONObject(request);
		String userId = input.getString("user_id");
		//what to pass back to front end, here is userId and favorites 
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
		//only need to change setFavoriteItems to unsetFavoriteItems
		connection.unsetFavoriteItems(userId, item.getItemId());
		connection.close();
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));

	}

}
