package entity;

import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;


public class Item {
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;
	
	//constructor, why private? we wish to use itemBuilder to create item
	//data from GitHub and put into item through the itemBuilder
	//only set value for one time, and we don't want to change the value later
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
		this.keywords = builder.keywords;
	}
	
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getKeywords() {
		return keywords;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	
	//change item to a JSONObject
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}
	//nested class to access the Item 
	//item's constructor is private,outside cannot access
	//why static? if not, we cannot access ItemBuilder without creating the item instance.
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private String imageUrl;
		private String url;
		private Set<String> keywords;
		//ItemBuilder do not have special constructor
		
		//here all the setter is void, we can also use chaining method
		//chaining: change the void to ItemBuilder,return this, then in the GitHUbClient.java
		//builder.setItemId(getStringFieldOrEmpty(object, "id")).setName(getStringFieldOrEmpty(object, "title")).set().set();
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public void setKeywords(Set<String> keywords) {
			this.keywords = keywords;
		}
		
		//create an Item class ---a normal method to create an item
		//this means the ItemBuilder 
		public Item build() {
			return new Item(this);
		}

	}


}
