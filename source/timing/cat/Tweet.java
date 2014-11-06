package timing.cat;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import timing.cat.DBConnectionSingleton;

public class Tweet {
	private static final String COLLECTION_NAME = "tweets";

	// Vide la collection
	public static void drop() {
		DB db = DBConnectionSingleton.getInstance().getDatabase();
		DBCollection tweets = db.getCollection(COLLECTION_NAME);
		tweets.drop();
	}

	public static void save(DBObject tweet) {
		DB db = DBConnectionSingleton.getInstance().getDatabase();
		DBCollection tweets = db.getCollection(COLLECTION_NAME);
		tweets.insert(tweet);
	}
}