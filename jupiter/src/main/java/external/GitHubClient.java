package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

//get response -- JSON String 
//changed to JSONArray array
//then use getItemList(array) to return List<item>(used in the backend logic and database)
public class GitHubClient {
	//every request is the same except the lat, lon, des 
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	//request from GitHub, response back to server
	
	public List<Item> search(double lat, double lon, String keyword) {
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		try {
			//why encode?URL cannot have%&? need to use UTF-8 encode 
			keyword = URLEncoder.encode(keyword, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = String.format(URL_TEMPLATE, keyword, lat, lon);
		//request to GitHub
		//create the httpclient, from dependency
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
			//???building connection? what did execute do?yes !built connection
			//httpclient send request(http get),and get response 
			CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
			//check if we successfully get response, we also can implement 400 500
			if (response.getStatusLine().getStatusCode() != 200) {
				return new ArrayList<>();
			}
			//what is entity? what is response? what is the difference with entity with response? 
			//read response, content + metadata
			HttpEntity entity = response.getEntity();
			//only care entity
			if (entity == null) {
				//nothing back for front end
				return new ArrayList<>();
			}
			//entity, might have metadata 
			//entity.getContent() get the content, it is a stream, need to useInputStreamReader to read
			//InputStreamReader only can do for certain length, what if 11?
			//bufferReader read by line, bufferReader need to take a reader, so need to create a reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			//build response
			StringBuilder responseBody = new StringBuilder();
			String line = null;
			//same as while(reader.readLine()!= null) {line = reader.readLine();responseBody.append(line);}??
			//no, since readLine() pointer to next line, need to = then mover to next line.
			//.readLine() --changed stream to character
			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			//JSONArry can identify the format of the string, if format is wrong, it will throw JSONexception
			JSONArray array = new JSONArray(responseBody.toString());
			//change array to list of item
			return getItemList(array);
			//close
			//why we are not using JSONArray.put()? as last class?-- we create object, and put into the JSONArray
			//responseBody get char of JSON, and return object of JSONArray
			//no need to add toString(), just in case
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	//helper method, only need in GitHubClient.java
	//change JSON Array to list of item
	private List<Item> getItemList(JSONArray array) {
		List<Item> itemList = new ArrayList<>();
		List<String> descriptionList = new ArrayList<>();
		
		for (int i = 0; i < array.length(); i++) {
			// We need to extract keywords from description since GitHub API
			// doesn't return keywords.
			String description = getStringFieldOrEmpty(array.getJSONObject(i), "description");
			if (description.equals("") || description.equals("\n")) {
				descriptionList.add(getStringFieldOrEmpty(array.getJSONObject(i), "title"));
			} else {
				descriptionList.add(description);
			}	
		}

		// We need to get keywords from multiple text in one request since
		// MonkeyLearnAPI has limitations on request per minute.
		//list of string -->array of string (MonkeyLearn like array of string, but java like list of string)
		List<List<String>> keywords = MonkeyLearnClient
				.extractKeywords(descriptionList.toArray(new String[descriptionList.size()]));
		
		for (int i = 0; i < array.length(); ++i) {
			//get the jsonObject from the JSON Array
			JSONObject object = array.getJSONObject(i);
			//create an ItemBuilder
			ItemBuilder builder = new ItemBuilder();
			//builder.setAddress(object.getString{"location"}); 
			//what if get location is null? later will have NPE
			//so we want to put "" instead of  null, method below
			//"id""title"..here needs to match the key in the response!
			//here is data clean, filter, only set needed variable
			//advantage : user no need to press null, and also no need to worry about the order
			builder.setItemId(getStringFieldOrEmpty(object, "id"));
			builder.setName(getStringFieldOrEmpty(object, "title"));
			builder.setAddress(getStringFieldOrEmpty(object, "location"));
			builder.setUrl(getStringFieldOrEmpty(object, "url"));
			builder.setImageUrl(getStringFieldOrEmpty(object, "company_logo"));
			//keywords of hashset..
			//List<String > list = keyword.get(i);
			//builder.setKeywords(new HashSet <String> (List));
			builder.setKeywords(new HashSet<String>(keywords.get(i)));
			
			Item item = builder.build();
			itemList.add(item);
		}
		
		return itemList;
	}
	//so we want to put "" instead of null
	private String getStringFieldOrEmpty(JSONObject obj, String field) {
		return obj.isNull(field) ? "" : obj.getString(field);
	}


}
