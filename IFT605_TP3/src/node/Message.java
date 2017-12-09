package node;

import org.json.JSONException;
import org.json.JSONObject;

import jade.core.AID;

public class Message {
	private AID receiver;
	private AID sender;
	private String msg;
	
	public Message(AID rec, AID send, String mssge) {
		this.setReceiver(rec);
		this.setSender(send);
		this.setMsg(mssge);
	}

	public AID getSender() {
		return sender;
	}

	public void setSender(AID sender) {
		this.sender = sender;
	}

	public AID getReceiver() {
		return receiver;
	}

	public void setReceiver(AID receiver) {
		this.receiver = receiver;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public JSONObject transformMsgIntoJSONObject() {
		JSONObject msgJson = new JSONObject();
		
		try {
			msgJson.put("receiver", this.receiver.getName());
			msgJson.put("sender", this.sender.getName());
			msgJson.put("content", this.msg);
			
		}catch(JSONException e) {
			e.printStackTrace();
		}
		return msgJson;
	}
	
	
}
