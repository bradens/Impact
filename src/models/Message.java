package models;

public class Message {
	
	public enum MessageType {
		EMAIL, TWEET
	}
	
	public enum ImpactType {
		METHOD, CLASS
	}
	
	String to;
	String from;
	String message;
	String change;
	String impacted;
	
	float weight;
	
	MessageType messageType;
	ImpactType impactType;
	public Message(String to, String from, String message, String change,
			String impacted) {
		super();
		this.to = to;
		this.from = from;
		this.message = message;
		this.change = change;
		this.impacted = impacted;
	}
	
	public Message(String to, String from, String message, String change,
			String impacted, float weight) {
		super();
		this.to = to;
		this.from = from;
		this.message = message;
		this.change = change;
		this.impacted = impacted;
		this.weight = weight;
	}

	public Message(String to, String from, String message, String change,
			String impacted, float weight, MessageType messageType,
			ImpactType impactType) {
		super();
		this.to = to;
		this.from = from;
		this.message = message;
		this.change = change;
		this.impacted = impacted;
		this.weight = weight;
		this.messageType = messageType;
		this.impactType = impactType;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getChange() {
		return change;
	}

	public void setChange(String change) {
		this.change = change;
	}

	public String getImpacted() {
		return impacted;
	}

	public void setImpacted(String impacted) {
		this.impacted = impacted;
	}

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public ImpactType getImpactType() {
		return impactType;
	}

	public void setImpactType(ImpactType impactType) {
		this.impactType = impactType;
	}

}
