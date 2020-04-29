package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;


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
	
	//read JSONArray/Object, due to we need to know the body in POST request
	//we need to know what need to be deleted, so post request send the body with JSON Array/Object
	//now we can read POST request and get the request as JSON object
	//however, inside logic needs list of item
	// Parses a JSONObject from http request.
	public static JSONObject readJSONObject(HttpServletRequest request) throws IOException {
		BufferedReader reader = new BufferedReader(request.getReader());
		StringBuilder requestBody = new StringBuilder();
		//now is the requestBody
		String line = null;
		while ((line = reader.readLine()) != null) {
			requestBody.append(line);
		}
		//JSON String -->to JSON Object, just put into()
		return new JSONObject(requestBody.toString());
	}

    // here Convert a JSON object to Item object
	//why we put into 2 methods? for future extension 
	public static Item parseFavoriteItem(JSONObject favoriteItem) {
		ItemBuilder builder = new ItemBuilder();
		builder.setItemId(favoriteItem.getString("item_id"));
		builder.setName(favoriteItem.getString("name"));
		builder.setAddress(favoriteItem.getString("address"));
		builder.setUrl(favoriteItem.getString("url"));
		builder.setImageUrl(favoriteItem.getString("image_url"));
		
		//save the keywords
		Set<String> keywords = new HashSet<>();
		JSONArray array = favoriteItem.getJSONArray("keywords");
		for (int i = 0; i < array.length(); ++i) {
			keywords.add(array.getString(i));
		}
		builder.setKeywords(keywords);
		return builder.build();
	}

}
