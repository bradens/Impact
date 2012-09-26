package models;

import impact.Resources;

public class Message {
	
	public enum MessageType {
		EMAIL, TWEET
	}
	
	public enum ImpactType {
		METHOD, CLASS
	}
	
	public enum ImpactScale {
		HIGH, MED, LOW
	}
	
	String to;
	String from;
	String change;
	String impacted;
	
	float weight;
	
	MessageType messageType;
	ImpactType impactType;
	ImpactScale impactScale;
	
	public Message(String to, String from, String change,
			String impacted) {
		super();
		this.to = to;
		this.from = from;
		this.change = change;
		this.impacted = impacted;
	}
	
	public Message(String to, String from, String change,
			String impacted, float weight) {
		super();
		this.to = to;
		this.from = from;
		this.change = change;
		this.impacted = impacted;
		this.weight = weight;
		
		initializeScale();
	}

	public Message(String to, String from, String change,
			String impacted, float weight, MessageType messageType,
			ImpactType impactType) {
		super();
		this.to = to;
		this.from = from;
		this.change = change;
		this.impacted = impacted;
		this.weight = weight;
		this.messageType = messageType;
		this.impactType = impactType;
		
		initializeScale();
	}
	
	private void initializeScale() {
		if(weight <= Resources.O_LOW)
			impactScale = ImpactScale.LOW;
		else if(weight > Resources.O_LOW && weight <= Resources.O_MED)
			impactScale = ImpactScale.MED;
		else
			impactScale = ImpactScale.HIGH;
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
		
		initializeScale();
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

	public ImpactScale getImpactScale() {
		return impactScale;
	}

	public void setImpactScale(ImpactScale impactScale) {
		this.impactScale = impactScale;
	}

}
