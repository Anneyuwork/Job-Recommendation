package db;

public class MySQLDBUtil {
	//we can putting all of those in a configuration doc
	private static final String INSTANCE = "";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "admin";
	//we can encode here every time, database always save the encoded password
	//encode is at front-end
	//database should not store the password from the user
	private static final String PASSWORD = "";
	//why no security issue? Because of jdbc:mysql://
	
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";
}
