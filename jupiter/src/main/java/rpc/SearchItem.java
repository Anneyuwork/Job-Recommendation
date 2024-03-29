package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;
import external.GitHubClient;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    //use throws, since it was overriding from the parent: HttpServlet.class
    //client.search() return List<Item>
    //then write back to JSON Array
    //RpcHelper() writeJsonArray--change JSON Array to JSON String and give back to response 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");
		//what parameter does request have, we see lat, lon, do we have a doc? GitHub API doc!
		//description-need to according to the GitHub API doc!!not keyword
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		//here change the List of item back to JSONArray
		GitHubClient client = new GitHubClient();
		List<Item> items = client.search(lat, lon, null);
		
		MySQLConnection connection = new MySQLConnection();
		//why getFavoriteItemId() is public
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		connection.close();
		
		JSONArray array = new JSONArray();
		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			//same consistency in ItemHistory.java
			//not every item needs favorite true
			//we need to use user id, get connection, 
			//only when we have item_id, due to 
			obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
			array.put(obj);
			//.toJSONObject() in the item
			//array.put(item.toJSONObject());
		}
		RpcHelper.writeJsonArray(response, array);
		//???we didn't pass in the keyword, how to pass the keyword? 
		//???String keyword = String.parseString(request.getParameter("keyword"));
		//no need to be description ! based on API doc
		//RpcHelper.writeJsonArray(response, client.search(lat, lon, null));

		/*interesting here, should return--but use void, and have return come as response
		since response is already a instance created by Java and passed in, return is a string in JSON format
		////tell front-end/response we will return JSON type
		//response.setContentType("application/json");
		////write and return to front end
		//PrintWriter writer = response.getWriter();
		
		JSONArray array = new JSONArray();
		array.put(new JSONObject().put("username", "abcd"));
		array.put(new JSONObject().put("username", "1234"));
		//why we can directly use RpcHelper.writeJsonArray? RpcHelper is public class
		//.writeJsonArray is a static method, so helper or util usually use static
		RpcHelper.writeJsonArray(response, array);
		//writer.print(array);
		*/
		/*
		//here is request, .getParameter is getting parameter from request passed in
		   if (request.getParameter("username") != null) {
			JSONObject obj = new JSONObject();
			String username = request.getParameter("username");
			obj.put("username", username);
			writer.print(obj);
		}

		 */
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
