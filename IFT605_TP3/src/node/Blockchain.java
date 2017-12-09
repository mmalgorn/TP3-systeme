package node;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public class Blockchain {
	private ArrayList<Block> blockchain;
	
	public Blockchain() {
		
	}
	
	public Blockchain(ArrayList<Block> bchain) {
		this.setBlockchain(bchain);
	}

	public ArrayList<Block> getBlockchain() {
		return blockchain;
	}

	public void setBlockchain(ArrayList<Block> blockchain) {
		this.blockchain = blockchain;
	}
	
	public int sizeBlockchain() {
		return this.blockchain.size();
	}
	
	public Block getBlock(int index) {
		return this.blockchain.get(index);
	}
	
	public Block getLast() {
		return this.blockchain.get(blockchain.size()-1);
	}
	
	public int getIndex(Block blk) {
		int index=-1;
		for(int i=0; i<sizeBlockchain(); i++) {
			if(getBlock(i).equals(blk)) {
				index = i;
			}
		}
		return index;
	}
	
	public void addBlockInBlockchain(Block blockToAdd) {
		this.blockchain.add(blockToAdd);
	}
	
	public void removeBlockInBlockchain(Block blockToRemove) {
		this.blockchain.remove(blockToRemove);
	}
	
	public void removeBlockInBlockchain(int index) {
		this.blockchain.remove(index);
	}
	
	public JSONObject getJSONObjectFromGetBlockchain() {
		JSONObject jsonobj = new JSONObject();
		try {
			for(int i=0; i<this.blockchain.size(); i++) {
				jsonobj.put("index", this.blockchain.get(i).getIndex());
				jsonobj.put("timestamp", this.blockchain.get(i).getTimestamp());
				//jsonobj.put("data", this.blockchain.get(i).getData());
				
				jsonobj.put("Receiver_id", this.blockchain.get(i).getData().Receiver_id);
				jsonobj.put("Sender_id", this.blockchain.get(i).getData().Sender_id);
				jsonobj.put("Message", this.blockchain.get(i).getData().Message);

				
				jsonobj.put("previous_hash", this.blockchain.get(i).getPrevious_hash());
				jsonobj.put("nonce", this.blockchain.get(i).getNonce());
				jsonobj.put("hash", this.blockchain.get(i).getHash());
			}
		}catch(JSONException e) {
			e.printStackTrace();
		}
		
		return jsonobj;
	}

}

