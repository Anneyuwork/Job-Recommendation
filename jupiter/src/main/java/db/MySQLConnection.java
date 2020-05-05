package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;


public class MySQLConnection {
	private Connection conn;
	//constructor, why putting in the connection here. 
	//when outside create the MySQLConnection, we can finish create the connection
	public MySQLConnection() {
		try {
			//connect with database, connect might fail, if fail: 1)exception, 2)conn is null
			//so we can do a null check in setFavoriteItems(), when we are using the connection
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//close the connection
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//Like
	public void setFavoriteItems(String userId, Item item) {
		//connection null check
		if (conn == null) {
			//print in read, only put error in read, easier for debug
			System.err.println("DB connection failed");
			return;
		}
		//things need to think: user_id, item_id is not exist
		//user_id can solved by log in, session check , item_ID is use saveItem(item)
		//save item in the item table
		saveItem(item);
		//history also have a last favor time ,why not add here?
		//we do a default current time stamp
		
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		try {
			//why preparedStatement here, last class use statement?
			//preparedStatement can put parameter into ?
			//why we can not use %s to write this part?
			//we can, just not safe enough, due to we are having user_id here
			//also making sure that parameter is only a string, not "delete" command
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//dislike
	//why setFavoriteItems pass in item?unsetFavoriteItems pass in item_id
	//due to no need to save item here in delete, only need id
	public void unsetFavoriteItems(String userId, String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		//delete form history table only!!
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
		//statement.setString is used to prepare the sql 
		//only finish the set, we then know who to delete
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//save this item, make sure item is existed
	public void saveItem(Item item) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}
		//IGNORE if the item is existed, we will not do anything
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			//looking into DB, and insert into DB
			statement.executeUpdate();
			
			// also save the keywords when we are saving item
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
                    statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			for (String keyword : item.getKeywords()) {
				statement.setString(2, keyword);
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//for get logic, doGet in itemHistory servlet
	//this method in history table, get all the item id
	//However, we need to find the item, see Set<Item> getFavoriteItems
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}

		Set<String> favoriteItems = new HashSet<>();

		try {
			//use the foreign key
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			//we need to read from db, need to use executeQuery(), different than .executeUpdate()
			// due to sql query, we need try catch
			ResultSet rs = statement.executeQuery();
			//getting data line by line rs=rs.next logic already in the next(), like iterator
			while (rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItems.add(itemId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}
	
	//we need to find the item,form history id
	//this method go to item table, get the item!
	//item will not duplicate
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		//
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);

		String sql = "SELECT * FROM items WHERE item_id = ?";
		try {
			//template
			PreparedStatement statement = conn.prepareStatement(sql);
			
			for (String itemId : favoriteItemIds) {
				//change the id name in the template
				statement.setString(1, itemId);
				//getting all the data for this itemId
				ResultSet rs = statement.executeQuery();
				//change this back to item data structure 
				ItemBuilder builder = new ItemBuilder();
				if (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					//write a keyword method, it is a separate table
					builder.setKeywords(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	//item id = 's keyword, find out the keyword
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	
	//used in login doGet
	//database storage first name and last name, separate, this method return full name
	public String getFullname(String userId) {
		
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		//from users table
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			//if next() has content
			if (rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	//check if the login is matched 
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	//The getMessage() method of Throwable class is used to return a detailed message of the Throwable object which can also be null. One can use this method to get the detail message of exception as a string value.

	public boolean addUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		// if already exist, ignore
		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			// executeUpdate() execute 1 time!
			//why ==1? Successes return 1(since we add 1 query), failed return 0
			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
