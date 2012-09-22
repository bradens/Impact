package messaging;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import models.Message;

public class MessageCenter {
	
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
		catch (MessagingException e) {
			
		}
	}
	
	public void sendAsTweet(Message message) {
		
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
	
	private void confirmMessage(Message message) {
		System.out.println(message.getTo() + " has been alerted because you changed the function " + 
				message.getChange() + "which effected the function " + message.getImpacted() + " .");
	}

}
