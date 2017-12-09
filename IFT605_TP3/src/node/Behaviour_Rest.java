package node;

import java.io.StringReader;
import java.util.Date;
import org.json.JSONObject;
import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLCodec.CodecException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.StringACLCodec;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

public class Behaviour_Rest extends CyclicBehaviour {
	
	// Generated automatically by Eclipse
	private static final long serialVersionUID = -3906724068679294031L;

	private AgentNode agent = (AgentNode) myAgent;

	AMSAgentDescription[] agents = null;
	SearchConstraints constraints = new SearchConstraints();
	
	//Constructeur vide
	public Behaviour_Rest() {
	}
	
	
	@Override
	public void action() {
		// TODO Auto-generated method stub
		ACLMessage msg = myAgent.receive();
		if (msg != null) {
			if(msg.getContent().substring(0, 2).equals("201")) {
				selfReceivingMsg(msg);
			}
			else if(msg.getContent().substring(0, 13).equals("integrerReseau")) {
				traiterMsg_InReseau(msg);
			}
			else if(msg.getContent().substring(0, 8).equals("listeGUID")) {
				ListeGUID listeGUID = new ListeGUID();
				listeGUID.string_to_list(msg.toString().substring(10));
				agent.setListeGUIDs(listeGUID);
			}
			else if(msg.getContent().substring(0,4).equals("block")) {
				traiterMsg_blockToPass(msg);
			}
			else {
				traiterMsg_APIRest(msg);
			}
		}
		
	}
	
	private void traiterMsg_APIRest(ACLMessage message) {
		ACLMessage response = message.createReply();
		response.setPerformative(ACLMessage.INFORM);

		if(message.getContent().toLowerCase().equals(("get/blockchain").toLowerCase())){
			response.setContent("200 : "+agent.getBlockchain().getJSONObjectFromGetBlockchain().toString());
		}
		else if(message.getContent().toLowerCase().equals(("get/get-messages").toLowerCase())){
			response.setContent("200 : "+agent.getStringFromGetListMsg(agent.getListMsg()));
		}
		else if(message.getContent().substring(0,8).toLowerCase().equals(("post/send").toLowerCase())) {
			String msg = "201 "+message.getContent().substring(10);
			response.setContent(msg);
		}
		else {
			response.setContent("404 : request not found");
		}

		agent.send(response);

	}

	private void selfReceivingMsg(ACLMessage message) {
		int ind = this.agent.getBlockchain().sizeBlockchain();
		int timestamp = (int) new Date().getTime();
		Data data = new Data(agent.getAID(), message.getSender(), message.toString().substring(4));
		String previous_hash = this.agent.getBlockchain().getBlock(this.agent.getBlockchain().sizeBlockchain()-1).getPrevious_hash();
		
		Block blocToBuild = new Block(ind, timestamp, data,previous_hash);
		
		//on rajoute le block � la fin de la blockchain			
		Blockchain a3=agent.getBlockchain();
		a3.addBlockInBlockchain(blocToBuild);
		agent.setBlockchain(a3);
		
		
		//Envoyer le block au successeur
		ACLMessage firstMsg = new ACLMessage(ACLMessage.INFORM);
		firstMsg.setContent("block "+blocToBuild.transformBlockToJSONObject().toString());
		
		for(int i=0; i<agent.getSuccessors().length; i++) {
			firstMsg.addReceiver(agent.getSuccessors()[i]);
		}
		agent.send(firstMsg);	
		
	}

	private void traiterMsg_InReseau(ACLMessage message) {
		constraints = new SearchConstraints();
		constraints.setMaxResults(new Long(-1));
		
		StringACLCodec codec = new StringACLCodec(new StringReader((message.toString().substring(15))), null);
		AID guid_msg = new AID();
		try {
			guid_msg = codec.decodeAID();
		} catch (CodecException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		agent.getListeGUIDs().inserer_noeud(guid_msg);
		
		try {
			agents = AMSService.search(myAgent,new AMSAgentDescription(),constraints);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i < agents.length;i++) {
			if(!agents[i].getName().equals(myAgent.getAID())) {
			ACLMessage msg = new ACLMessage((ACLMessage.INFORM));
			msg.addReceiver(agents[i].getName());
			msg.setLanguage("English");
			msg.setContent("listeGUID " + agent.getListeGUIDs().toString());
			myAgent.send(msg);
			}
		}
	}
	
	private void traiterMsg_blockToPass(ACLMessage message) {
		//Recuperer le block et l'envoyer 
		String bloc_string = message.getContent().substring(6);
		JSONObject bloc= new JSONObject(bloc_string);
		
		
		/*Ajout simple du bloc a la fin de la Blockchain*/
		if(bloc.getString("previous_hash") == agent.getBlockchain().getLast().getHash() ) { 
			
			//on recupere AID du sender
			String sender1=bloc.getString("Sender_id");
			StringACLCodec codec1 = new StringACLCodec(new StringReader(sender1), null);
			AID guid_msg1 = new AID();
			try {
				guid_msg1 = codec1.decodeAID();
			} catch (CodecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//on recupere AID du receiver
			String receiver1= bloc.getString("Receiver_id");
			StringACLCodec codec2 = new StringACLCodec(new StringReader(sender1), null);
			AID guid_msg2 = new AID();
			try {
				guid_msg2 = codec2.decodeAID();
			} catch (CodecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//on rajoute le block � la fin de la blockchain			
			Data data1=new Data(guid_msg1, guid_msg2,	bloc.getString("Message"));
			Block a= new Block( bloc.getInt("index"), bloc.getInt("timestamp"), data1, bloc.getString("previous_hash") );
			
			Blockchain a3=agent.getBlockchain();
			a3.addBlockInBlockchain(a);
			agent.setBlockchain(a3);
			
			//On envoi le bloc � la table de routage
			ACLMessage firstMsg = message;
			for(int i=0; i<agent.getSuccessors().length; i++) {
				firstMsg.addReceiver(agent.getSuccessors()[i]);
			}
			agent.send(firstMsg);	

		}
		
		
		/*Si le bloc va remplacer le dernier de la blockchain*/
		else if( bloc.getString("previous_hash").equals(agent.getBlockchain().getLast().getPrevious_hash()) &&  0> bloc.getString("hash").compareTo(agent.getBlockchain().getLast().getHash())  ) {
			
			Data data2=null;
			
			//on recupere AID du sender
			String sender1=bloc.getString("Sender_id");
			StringACLCodec codec3 = new StringACLCodec(new StringReader(sender1), null);
			AID guid_msg3 = new AID();
			try {
				guid_msg3 = codec3.decodeAID();
			} catch (CodecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			//on recupere AID du receiver
			String receiver2= bloc.getString("Receiver_id");
			StringACLCodec codec2 = new StringACLCodec(new StringReader(sender1), null);
			AID guid_msg4 = new AID();
			try {
				guid_msg4 = codec2.decodeAID();
			} catch (CodecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			/*On recupere data du dernier bloc avant de le remplacer si on �tait l'�metteur du message */
			if(guid_msg3==agent.getAID()) {
				data2=agent.getBlockchain().getLast().getData();		
			}
			
			/*On remplace le dernier bloc de la blockchain par celui recu*/
			Blockchain BC_modif=agent.getBlockchain();	//on recupere la blockchain
			BC_modif.removeBlockInBlockchain(agent.getBlockchain().sizeBlockchain() -1 );//on enleve le dernier bloc
			
			Data nouv_data=new Data(guid_msg3,guid_msg4,bloc.getString("Message"));//on recr�er les data
			Block nouv_modif= new Block(bloc.getInt("index"),bloc.getInt("timestamp"), nouv_data, bloc.getString("previous_hash"));//on recree un block avec les parametres
			BC_modif.addBlockInBlockchain(nouv_modif);//on affecte le bloc a la blockchain
		
			agent.setBlockchain(BC_modif);//on reaffecte la blockchain 
			
			
			//On envoi le bloc � la table de routage
			ACLMessage firstMsg = message;
			for(int i=0; i<agent.getSuccessors().length; i++) {
				firstMsg.addReceiver(agent.getSuccessors()[i]);
			}
			agent.send(firstMsg);	

			
			
			
			
			if(data2!=null) {
				Block nouv_block=new Block(agent.getBlockchain().getLast().getIndex()+1, (int)new Date().getTime(), data2, agent.getBlockchain().getLast().getHash());
				
				//Envoyer le block au successeur
				ACLMessage firstMsg2 = new ACLMessage(ACLMessage.INFORM);
				firstMsg2.setContent("block "+nouv_block.transformBlockToJSONObject().toString());
				
				for(int i=0; i<agent.getSuccessors().length; i++) {
					firstMsg2.addReceiver(agent.getSuccessors()[i]);
				}
				agent.send(firstMsg2);	

			}
		}
		
		
	}
	
}
