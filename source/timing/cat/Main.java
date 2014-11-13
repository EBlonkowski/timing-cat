package timing.cat;

import support.APIType;
import support.OAuthTokenSecret;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

//import timing.cat.DBConnectionSingleton;
import utils.OAuthUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.util.JSON;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

public class Main
{
    OAuthConsumer mConsumer;


	/**
     * Fetches tweets matching a query
     * @param query for which tweets need to be fetched
	 * @param count maximum of tweets fetched
     * @return an array of status objects
     */
    public DBObject getSearchResults(String query, int count)
    {
        try{
            //construct the request url
            String URL_PARAM_SEPERATOR = "&";
            StringBuilder url = new StringBuilder();
            url.append("https://api.twitter.com/1.1/search/tweets.json?q=");
            //query needs to be encoded
            url.append(URLEncoder.encode(query, "UTF-8"));
            url.append(URL_PARAM_SEPERATOR);
            url.append("count=");
            url.append(count);
            URL navurl = new URL(url.toString());
            HttpURLConnection huc = (HttpURLConnection) navurl.openConnection();
            huc.setReadTimeout(5000);
            mConsumer.sign(huc);
            huc.connect();
            if(huc.getResponseCode()==400||huc.getResponseCode()==404||huc.getResponseCode()==429)
            {
                System.out.println(huc.getResponseMessage());
                try {
                    huc.disconnect();
                    Thread.sleep(this.getWaitTime("/friends/list"));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
            {
                System.out.println(huc.getResponseMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
            String temp;
            StringBuilder page = new StringBuilder();
            while( (temp = bRead.readLine())!=null)
            {
                page.append(temp);
            }
            DBObject results = (DBObject) JSON.parse(page.toString());
			return results;
        
        } catch (OAuthCommunicationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthMessageSignerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthExpectationFailedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

	/**
     * Retrieves user tokens. Asks for user input.
     * @return the access token as a support.OAuthTokenSecret
     */
    public OAuthTokenSecret GetUserAccessKeySecret()
    {
        try {
            //consumer key for Twitter Data Analytics application
            if(OAuthUtils.CONSUMER_KEY.isEmpty())
            {
                System.out.println("Register an application and copy the consumer key into the configuration file.");
                return null;
            }
            if(OAuthUtils.CONSUMER_SECRET.isEmpty())
            {
                System.out.println("Register an application and copy the consumer secret into the configuration file.");
                return null;
            }
			// On crée un objet "consumer", on passe la clef et le secret de notre application
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(OAuthUtils.CONSUMER_KEY,OAuthUtils.CONSUMER_SECRET);
			// On crée un object "provide" en donnant les différents URL du site de tweeter
            OAuthProvider provider = new DefaultOAuthProvider(OAuthUtils.REQUEST_TOKEN_URL, OAuthUtils.ACCESS_TOKEN_URL, OAuthUtils.AUTHORIZE_URL);
			// On demande un request token
            String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
			// On demande à l'utilisateur de se connecter sur le site de Tweeter
            System.out.println("Now visit:\n" + authUrl + "\n and grant this app authorization");
            System.out.println("Enter the PIN code and hit ENTER when you're done:");
			// On récupère un code composé de chiffre qui permet de finit la connection
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String pin = br.readLine();
            System.out.println("Fetching access token from Twitter");
			// On récupère le token d'accès à Twitter
            provider.retrieveAccessToken(consumer,pin);
			// On l'enregistre dans un object OAuthTokenSecret
            String accesstoken = consumer.getToken();
            String accesssecret  = consumer.getTokenSecret();
            OAuthTokenSecret tokensecret = new OAuthTokenSecret(accesstoken,accesssecret);
            return tokensecret;
        } catch (OAuthNotAuthorizedException ex) {
                ex.printStackTrace();
        } catch (OAuthMessageSignerException ex) {
                ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
                ex.printStackTrace();
        } catch (OAuthCommunicationException ex) {
                ex.printStackTrace();
        } catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

	/**
     * Loads pre-fetched user tokens
     * @return the access token as a support.OAuthTokenSecret
     */
    public static OAuthTokenSecret DEBUGUserAccessSecret()
    {
        String accesstoken = "2834367581-9Ejlz0ke7v2HZmfintgf02mVnHXz7zKjmQtQdQ0";
        String accesssecret = "A6c5FtqjGxeKSltZty41bZm0KFu71qRjxes7ZKoWnv7Pk";
        OAuthTokenSecret tokensecret = new OAuthTokenSecret(accesstoken,accesssecret);
        return tokensecret;
    }

	/** Create a OAuthConsumer object with default (TheShiningFish) credentials
	*/
	public OAuthConsumer createDefaultConsumer() {
		// On créer l'objet Consumer
        OAuthTokenSecret tokenSecret = DEBUGUserAccessSecret();
        mConsumer = new DefaultOAuthConsumer(utils.OAuthUtils.CONSUMER_KEY,utils.OAuthUtils.CONSUMER_SECRET);
        mConsumer.setTokenWithSecret(tokenSecret.getAccessToken(),tokenSecret.getAccessSecret());
		return mConsumer;
	}

	/**
     * Retrives the profile information of the user
     * @param username of the user whose profile needs to be retrieved
     * @return the profile information as a JSONObject
     */
    public DBObject getProfile(String username)
	{
        BufferedReader bRead = null;
        DBObject profile = null;
        try {
        System.out.println("Processing profile of "+username);
        boolean flag = true;
        URL url = new URL("https://api.twitter.com/1.1/users/show.json?screen_name="+username);
        HttpURLConnection huc = (HttpURLConnection) url.openConnection();
        huc.setReadTimeout(5000);
        // Step 2: Sign the request using the OAuth Secret
        mConsumer.sign(huc);
        huc.connect();
        if(huc.getResponseCode()==404||huc.getResponseCode()==401)
        {
           System.out.println(huc.getResponseMessage());
        }           
        else
        if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
        {
            try {
                huc.disconnect();
                System.out.println(huc.getResponseMessage());
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        else
            // Step 3: If the requests have been exhausted, then wait until the quota is renewed
        if(huc.getResponseCode()==429)
        {
            try {
                huc.disconnect();
                Thread.sleep(this.getWaitTime("/users/show/:id"));
                flag = false;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if(!flag)
        {
            //recreate the connection because something went wrong the first time.
            huc.connect();
        }
        StringBuilder content=new StringBuilder();
        if(flag)
        {
            bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
            String temp= "";
            while((temp = bRead.readLine())!=null)
            {
                content.append(temp);
            }
        }
        huc.disconnect();
		profile = (DBObject) JSON.parse(content.toString());
		} catch (OAuthCommunicationException ex) {
		    ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
		    ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
		    ex.printStackTrace();
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		return profile;
    }

	/**
     * Retrieves the rate limit status of the application
     * @return
     */
   public DBObject getRateLimitStatus()
   {
     try{
            URL url = new URL("https://api.twitter.com/1.1/application/rate_limit_status.json");
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setReadTimeout(5000);           
            mConsumer.sign(huc);
            huc.connect();
            BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
            StringBuffer page = new StringBuffer();
            String temp= "";
            while((temp = bRead.readLine())!=null)
            {
                page.append(temp);
            }
            bRead.close();
            return ((DBObject) JSON.parse(page.toString()));
        } catch (OAuthCommunicationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }  catch (OAuthMessageSignerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthExpectationFailedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }catch(IOException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
     return null;
   }

	/**
     * Retrieves the wait time if the API Rate Limit has been hit
     * @param api the name of the API currently being used
     * @return the number of milliseconds to wait before initiating a new request
     */
    public long getWaitTime(String api)
    {
        DBObject jobj = this.getRateLimitStatus();
        if(jobj!=null)
        {
                if(jobj.containsField("resources"))
                {
                    DBObject resourcesobj = (DBObject) jobj.get("resources");
                    DBObject apilimit = null;
                    if(api.equals(APIType.USER_TIMELINE))
                    {
                        DBObject statusobj = (DBObject) resourcesobj.get("statuses");
                        apilimit = (DBObject) statusobj.get(api);
                    }
                    else
                    if(api.equals(APIType.FOLLOWERS))
                    {
                        DBObject followersobj = (DBObject) resourcesobj.get("followers");
                        apilimit = (DBObject) followersobj.get(api);
                    }
                    else
                    if(api.equals(APIType.FRIENDS))
                    {
                        DBObject friendsobj = (DBObject) resourcesobj.get("friends");
                        apilimit = (DBObject) friendsobj.get(api);
                    }
                    else
                    if(api.equals(APIType.USER_PROFILE))
                    {
                        DBObject userobj = (DBObject) resourcesobj.get("users");
                        apilimit = (DBObject) userobj.get(api);
                    }
                    Integer numremhits = (Integer) apilimit.get("remaining");
                    if(numremhits <=1)
                    {
                        Long resettime = (Long) apilimit.get("reset");
                        resettime = resettime*1000; //convert to milliseconds
                        return resettime.longValue();
                    }
            } 
        }
        return 0;
    }

	/**
	* Point d'entrée du programme. Charge le profil de TheShiningFish et l'affiche ! 
	*/
    public static void main(String[] args) throws IOException
    {
        Main aue = new Main();
		// On créer l'objet Consumer
		aue.createDefaultConsumer();

		// On supprime la collection
		Tweet.drop();
		// On effectue une recherche (on demande les 100 derniers tweets)
        DBObject results = aue.getSearchResults("telecom sudparis OR #TelecomSudParis OR @TelecomSudParis", 100);
		// Ces variables permettent de parcourir les résultats de la recherche
		DBObject statuses, tweet;
		int i = 0;
		statuses = (DBObject) results.get("statuses");
		tweet = (DBObject) statuses.get(String.valueOf(i));
		while(tweet != null) {
			// On sauvegarde le tweet #i
			Tweet.save(Tweet.strip(tweet));
			i++;
			tweet = (DBObject) statuses.get(String.valueOf(i));
		}
		// On affiche le nombre de résultats obtenus :
		DBObject searchMetadata = (DBObject) results.get("search_metadata");
		System.out.println(searchMetadata);
		DBConnectionSingleton.getInstance().close();
    }
}
