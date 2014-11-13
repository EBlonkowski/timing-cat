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

	/**
	* @param tweet un tweet au format tweeter
	* @result l'objet tweet débarassé de ses champs inutils on garde : la date de création, l'id, le texte
	*/
	public static DBObject strip(DBObject tweet) {
		DBObject strippedTweet = new BasicDBObject();
		strippedTweet.put("id", tweet.get("id").toString());
		strippedTweet.put("created_at", tweet.get("created_at"));
		strippedTweet.put("text", tweet.get("text"));
		return strippedTweet;
	}
}
