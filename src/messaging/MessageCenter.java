package messaging;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import db.DatabaseConnector;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


import models.Message;
import models.Pair;

public class MessageCenter {
	
	DatabaseConnector db;
	
	public MessageCenter(DatabaseConnector db) {
		this.db = db;
	}
	
	public void sendAsEmail(Message message) {
		Properties prop = System.getProperties();
		prop.setProperty("mail.smtp.host", "localhost");
		Session session = Session.getDefaultInstance(prop);
		
		try {
			MimeMessage mm = new MimeMessage(session);
			mm.setFrom(new InternetAddress(message.getFrom()));
			mm.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(message.getTo()));
			mm.setSubject("Impact notice!");
			mm.setText(generateEmailMessage(message));
			
			// Send
			Transport.send(mm);
			confirmMessage(message);
		}
		catch (Exception e) {
			System.out.println("Exception when trying to email message.");
		}
	}
	
	public void sendAsTweet(Message message) {
		AccessToken token = loadAccessToken();
		if(token == null)
			firstTweet(message);
		else {
			try {
				TwitterFactory factory = new TwitterFactory();
				Twitter twitter = factory.getInstance();
				Pair<String, String> config = loadTwitterConfig();
				twitter.setOAuthConsumer(config.getFirst(), config.getSecond());
			    twitter.setOAuthAccessToken(token);
			    //twitter.updateStatus(generateTweetMessage(message));
			    confirmMessage(message);
			}
			catch (Exception e) {
				System.out.println("Exception when trying to tweet message.");
			}
		}
	}
	
	private void storeAccessToken(Message message, AccessToken accessToken) {
		DatabaseConnector db = new DatabaseConnector();
		db.connect("test");
		db.storeTwitterAccessToken(accessToken);
		db.close();
	}
	
	private AccessToken loadAccessToken() {
		DatabaseConnector db = new DatabaseConnector();
		db.connect("test");
		AccessToken token = db.getTwitterAccessToken();
		db.close();
		return token;
	}
	
	private Pair<String,String> loadTwitterConfig() {
		Pair<String, String> pair = new Pair<String, String>();
		
		try {
			// Open the file
			FileInputStream fstream = new FileInputStream("twitterConfig.cfg");
	
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
	
			String strLine;
		
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  if(strLine.contains("consumer key")) {
				  pair.setFirst(strLine.substring(strLine.indexOf("=")+1));
			  }
			  else if(strLine.contains("consumer secret")) {
				  pair.setSecond(strLine.substring(strLine.indexOf("=")+1));
			  }
			}
			
			in.close();
		}
		catch (IOException e) {
			System.out.println("Could not open twitter config file");
		}
		
		return pair;
	}
	
	private void firstTweet(Message message) {
		try
        {
            // The factory instance is re-useable and thread safe.
            Twitter twitter = new TwitterFactory().getInstance();
            Pair<String, String> config = loadTwitterConfig();
			twitter.setOAuthConsumer(config.getFirst(), config.getSecond());

            RequestToken requestToken = twitter.getOAuthRequestToken();
            AccessToken accessToken = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (null == accessToken)
            {
                System.out.println("Open the following URL and grant access to your Twitter account:");
                System.out.println(requestToken.getAuthorizationURL());
                System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
                String pin = null;
                try{
                    pin = br.readLine();
                } catch (IOException ex){
                	System.out.println("Exception when trying to tweet message.");
                }
                try{
                    if (pin.length() > 0)
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    else
                        accessToken = twitter.getOAuthAccessToken();
                } catch (TwitterException te) {
                    if (401 == te.getStatusCode())
                        System.out.println("Unable to get the access token."); 
                    else
                        te.printStackTrace();
                }
            }
            //persist to the accessToken for future reference.
            storeAccessToken(message, accessToken);
            twitter.updateStatus(generateTweetMessage(message));
            confirmMessage(message);
        } catch (TwitterException ex) {
        	System.out.println("Exception when trying to tweet message.");
        }
	}
	
	private String generateEmailMessage(Message message) {
		String body = "Hi,\n\n";
		body += "This is an automated message from Impact! We have detected that the function ";
		body += message.getChange() + " has changed. We are informing you because your function ";
		body += message.getImpacted() + " is known to call it. We reccomend that you talk to the ";
		body += "author of this change to avoid a bug.\n\n";
		
		body += "We hope this message has helped in creating awareness among yourself and your ";
		body += "teammates.\n\n";
		
		body += "Impact!\n";
		
		return body;
	}
	
	private String generateTweetMessage(Message message) {
		String body = "@bradensimpson ";
		body += message.getImpacted() + " is being effected by ";
		body += message.getChange();
		
		return body;
	}
	
	private void confirmMessage(Message message) {
		db.insertMessage(message);
		
		System.out.println(message.getTo() + " has been alerted because you changed the function " + 
				message.getChange() + "which effected the function " + message.getImpacted() + " .");
	}

}
