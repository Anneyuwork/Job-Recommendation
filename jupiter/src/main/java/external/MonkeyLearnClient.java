package external;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.monkeylearn.ExtraParam;
import com.monkeylearn.MonkeyLearn;
import com.monkeylearn.MonkeyLearnException;
import com.monkeylearn.MonkeyLearnResponse;

public class MonkeyLearnClient {
	private static final String API_KEY = "";// make sure change it to your api key.
	//main cannot be used in the project   
	public static void main(String[] args) {
		
		String[] textList = {
				"Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit�s full-body look.", };
		List<List<String>> words = extractKeywords(textList);
		for (List<String> ws : words) {
			for (String w : ws) {
				System.out.println(w);
			}
			System.out.println();
		}
	}
	
	//for data from outside, MonkeLearn like string[]
    //why public? need to call in other class
    //static no need to create instance, utility, outside API usually this way, we can directly MonkeyLearn.
    //outside list --string[] max string.length, inside list --every string's keyword- max 3
    //List of list string is easier to process in logic
	public static List<List<String>> extractKeywords(String[] text) {
		//corner case
		if (text == null || text.length == 0) {
			return new ArrayList<>();
		}

		// Initiate the MonkeyLearn
		// Use the API key from your account
		MonkeyLearn ml = new MonkeyLearn(API_KEY);

		//default return 30 keyword, change to 3
		// Use the keyword extractor
		ExtraParam[] extraParams = { new ExtraParam("max_keywords", "3") };
		MonkeyLearnResponse response;
		try {
			response = ml.extractors.extract("ex_YCya9nrn", text, extraParams);
			//support branch processing!
			JSONArray resultArray = response.arrayResult;
			return getKeywords(resultArray);
		} catch (MonkeyLearnException e) {// it�s likely to have an exception
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	//internal method, private is OK
	private static List<List<String>> getKeywords(JSONArray mlResultArray) {
		List<List<String>> topKeywords = new ArrayList<>();
		// Iterate the result array and convert it to our format.
		//i represents 20 job descriptions
		for (int i = 0; i < mlResultArray.size(); ++i) {
			List<String> keywords = new ArrayList<>();
			JSONArray keywordsArray = (JSONArray) mlResultArray.get(i);
			for (int j = 0; j < keywordsArray.size(); ++j) {
				//keywordsArray.get(j) is a java object, we need to (JSONObject) to make it a java object
				JSONObject keywordObject = (JSONObject) keywordsArray.get(j);
				// We just need the keyword, excluding other fields.
				String keyword = (String) keywordObject.get("keyword");
				keywords.add(keyword);

			}
			topKeywords.add(keywords);
		}
		return topKeywords;
	}
}
