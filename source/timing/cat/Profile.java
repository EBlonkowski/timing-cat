package timing.cat;

import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import timing.cat.DBConnectionSingleton;

public class Profile {
	private static final String COLLECTION_NAME = "profiles";

	// Vide la collection
	public static void drop() {
		DB db = DBConnectionSingleton.getInstance().getDatabase();
		DBCollection profiles = db.getCollection(COLLECTION_NAME);
		profiles.drop();
	}

	public static void save(DBObject profile) {
		DB db = DBConnectionSingleton.getInstance().getDatabase();
		DBCollection profiles = db.getCollection(COLLECTION_NAME);
		profiles.insert(profile);
	}
}
