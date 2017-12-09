package node;
import java.security.MessageDigest;

import org.json.JSONException;
import org.json.JSONObject;


public class Block {
	private int index;
	private int timestamp;
	private int nonce;
	private Data data;
	
	private String previous_hash;
	private String hash;
	
		
	public Block(int index, int timestamp, Data data, String previous_hash) {
		this.index=index;
		this.timestamp=timestamp;
		this.previous_hash=previous_hash;
		this.data= data;
		this.nonce=0;
		
		do {
		try {
			this.nonce++;
			this.hash=FonctionHashage(this.index, this.timestamp, this.data, this.previous_hash,this.nonce);
			System.out.println("LE HASH: "+this.hash);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}while( Long.parseLong(this.hash,16)%7 != 0);
		System.out.println(this.hash + "\t"+ this.nonce);
	
	
	
	}
	
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public Data getData() {
		return data;
	}
	
	public void setData(Data data) {
		this.data = data;
	}

	public String getPrevious_hash() {
		return previous_hash;
	}

	public void setPrevious_hash(String previous_hash) {
		this.previous_hash = previous_hash;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
		this.nonce = nonce;
	}
	
	
	String FonctionHashage(int index, int timestamp, Data data, String previous_hash, int nonce) throws Exception{
		
		
		String string_a_hasher=Integer.toString(index)+Integer.toString(timestamp)+data.toString()+ previous_hash+Integer.toString(nonce);
		

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(string_a_hasher.getBytes());

        byte byteData[] = md.digest();

        //convertir le tableau de bits en une format hexad�cimal - m�thode 1
        StringBuffer sb = new StringBuffer();
        //for (int i = 0; i < byteData.length; i++) {
        for (int i = 0; i < 6; i++) {
	
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

  
        return sb.toString();
				
		}
	
	public JSONObject transformBlockToJSONObject() {
		JSONObject blockjson = new JSONObject();
		
		try {
			
			blockjson.put("index", this.index);
			blockjson.put("timestamp", this.timestamp);
			//jsonobj.put("data", this.blockchain.get(i).getData());
			
			blockjson.put("Receiver_id", this.data.Receiver_id);
			blockjson.put("Sender_id", this.data.Sender_id);
			blockjson.put("Message", this.data.Message);

			
			blockjson.put("previous_hash", this.previous_hash);
			blockjson.put("nonce", this.nonce);
			blockjson.put("hash", this.hash);
			
		}catch(JSONException e) {
			e.printStackTrace();
		}
		
		return blockjson;
	}

	
}
