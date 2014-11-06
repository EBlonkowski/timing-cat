package timing.cat;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;

public class DBConnectionSingleton {
	public static final String HOST = "localhost";
	public static final int PORT = 27017;
	public static final String DB_NAME = "mydb";

	private static DBConnectionSingleton mInstance = null;
	private MongoClient mMongoClient = null;
	
	private DBConnectionSingleton() {
		try {
			mMongoClient = new MongoClient( HOST , PORT );
		} catch (UnknownHostException e) {
			System.out.println("impossible de se connecter à mongodb");
			System.exit(0);
		}
	}

	public MongoClient getMongoClient() {
		return mMongoClient;
	}

	public DB getDatabase() {
		return mMongoClient.getDB(DB_NAME);
	}

	public void close() {
		if(mMongoClient == null)
			return;
		mMongoClient.close();
		mMongoClient = null;
	}

	public static DBConnectionSingleton getInstance() {
		if(mInstance == null)
			mInstance = new DBConnectionSingleton();
		return mInstance;
	}
}
