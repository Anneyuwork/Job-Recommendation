package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubClient {
	//every request is the same except the lat, lon, des 
	private static final String URL_TEMPLATE = "https://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";
	private static final String DEFAULT_KEYWORD = "developer";
	//request from GitHub, response back to server
	public JSONArray search(double lat, double lon, String keyword) {
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
				return new JSONArray();
			}
			//what is entity? what is response? what is the difference with entity with response? 
			//read response, content + metadata
			HttpEntity entity = response.getEntity();
			//only care entity
			if (entity == null) {
				//nothing back for front end
				return new JSONArray();
			}
			//entity, might have metadata 
			//entity.getContent() get the content, it is a stream, need to useInputStreamReader to read
			//InputStreamReader only can do for certain length
			//bufferReader read by line
			//bufferReader need to take a reader, so need to create a reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			//build response
			StringBuilder responseBody = new StringBuilder();
			String line = null;
			//same as while(reader.readLine()!= null) {line = reader.readLine();responseBody.append(line);}??
			//no, since readLine() pointer to next line, need to = then mover to next line.

			while ((line = reader.readLine()) != null) {
				responseBody.append(line);
			}
			//close
			//why we are not using JSONArray.put()? as last class?-- we create object, and put into the JSONArray
			//responseBody get char of JSON, and return object of JSONArray
			//no need to add toString(), just in case
			return new JSONArray(responseBody.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}

}
