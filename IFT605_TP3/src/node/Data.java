package node;

import jade.core.AID;

public class Data {
	AID Sender_id;
	AID Receiver_id;
	String Message;
	
	public Data(AID Sender_id, AID Receiver_id,	String Message) {
		this.Sender_id=Sender_id;
		this.Receiver_id=Receiver_id;
		this.Message=Message;
	}
	
	public String toString() {
		return (this.Sender_id.toString())+(this.Receiver_id.toString())+Message;
	}
}
