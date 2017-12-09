package node;

import jade.core.*;
import java.util.Arrays;

public class ListeGUID {

	protected static final int CAPACITY = 64;
	AID liste[] = new AID[CAPACITY];
	
	public ListeGUID() {
		for(int i=0; i<CAPACITY; i++) {
			this.liste[i]=null;
		}
	}
	
	protected void string_to_list(String string) {
		AID[] arr = (AID[]) Arrays.stream(string.substring(1, string.length()-1).split(","))
		    .map(String::trim).toArray();
		for(int i=0; i<CAPACITY; i++){
			this.liste[i]=arr[i];
		}
		
	}
	
	protected void inserer_noeud(AID guid_du_nouveau_noeud) {
		int i=0;
		while(!(this.liste[i]!=null) && i<CAPACITY){
			i++;
		}
		if(this.liste[i]==null) {
			this.liste[i]=guid_du_nouveau_noeud;
		}
	}
	
	protected int getIndex(AID guid) {
		for(int i =0; i<64; i++) {
			if(this.liste[i]==guid) {
				return i;
			}
		}
		return -1;
	}
	
	protected AID getAID(long index) {
		AID newAID = new AID();
		newAID = liste[(int) index];
		return newAID;
	}
}
