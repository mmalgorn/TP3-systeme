package node;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;

import jade.core.*;
import jade.core.behaviours.ReceiverBehaviour;
import jade.core.behaviours.ReceiverBehaviour.NotYetReady;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ContainerController;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;

public class AgentNode extends Agent {
	
	// Generated automatically by Eclipse
	private static final long serialVersionUID = 1L;
	

	private static final long TIMEOUT = 2000;
	private static final String NAME_CONTAINER = "NodeContainer";
	
	private long tableRoutage[] = new long[6];
	private ListeGUID listeGUIDs = new ListeGUID();

	private AID guid;
	
	private ReceiverBehaviour debut;
	
	private Blockchain blockchain;
	private ArrayList<Message> listMsg = new ArrayList<Message>();
	
	AMSAgentDescription[] agents = null;
	SearchConstraints constraints = new SearchConstraints();

	//Constructeurs
		public AgentNode () {
			
		}
		
		
		public AgentNode(Blockchain bck) {
			this.setBlockchain(bck);
			this.listeGUIDs = new ListeGUID();
		}
		
		public AgentNode(Blockchain bck, ArrayList<Message> lst) {
			this.setBlockchain(bck);
			this.setListMsg(lst);
			this.listeGUIDs = new ListeGUID();
		}
		
		//Accesseur et mutateur
		public long[] getTableRoutage() {
			return tableRoutage;
		}

		public void setTableRoutage(long[] tableRoutage) {
			this.tableRoutage = tableRoutage;
		}
		
		public Blockchain getBlockchain() {
			return this.blockchain;
		}

		public void setBlockchain(Blockchain bchain) {
			this.blockchain = bchain;
		}
		
		public ArrayList<Message> getListMsg() {
			return listMsg;
		}
		
		public void deleteBlock(int index) {
			this.blockchain.removeBlockInBlockchain(index);
		}
		
		public void addBlock(Block b) {
			this.blockchain.addBlockInBlockchain(b);
		}
		
		public String getStringFromGetListMsg(ArrayList<Message> lst) {
			String str = "[";
			Message msgCurrent;
			for(int i =0; i<lst.size(); i++) {
				msgCurrent = lst.get(i);
				str+= msgCurrent.transformMsgIntoJSONObject().toString();
				str+=";";
			}
			str+="]";
			return str;
		}

		public void setListMsg(ArrayList<Message> listMsg) {
			this.listMsg = listMsg;
		}
		
		public ListeGUID getListeGUIDs() {
			return listeGUIDs;
		}

		public void setListeGUIDs(ListeGUID listeGUIDs) {
			this.listeGUIDs = listeGUIDs;
		}
		
	@Override
	protected void setup() {
		constraints = new SearchConstraints();
		constraints.setMaxResults(new Long(-1));
		
		System.out.println("New agent");
		//addBehaviour(new Behaviour_Rest());
		
		//Attribution du Peer-ID a notre noeud
		this.guid=this.getAID();	
		
		try {
			agents = AMSService.search(this,new AMSAgentDescription(),constraints);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0;i < agents.length;i++) {
			if(!agents[i].getName().equals(this.getAID())) {
			ACLMessage msg = new ACLMessage((ACLMessage.QUERY_IF));
			msg.addReceiver(agents[i].getName());
			msg.setLanguage("English");
			msg.setContent("integrerReseau");
			this.send(msg);
			}
		}		
		
		
		debut = new ReceiverBehaviour(this, TIMEOUT, MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF));
		
		addBehaviour(debut);
		
		ACLMessage msg;
		
		if (debut.done()){
			try {
				msg = debut.getMessage();
				this.listeGUIDs.inserer_noeud(this.guid);				
			}
			catch (ReceiverBehaviour.TimedOut e) {
				this.listeGUIDs.inserer_noeud(this.guid);
				
				jade.core.Runtime runtime = jade.core.Runtime.instance();
				// Definir profil du nouveau container.
				Profile profile = new ProfileImpl();
				profile.setParameter(Profile.CONTAINER_NAME, NAME_CONTAINER);
				profile.setParameter(Profile.MAIN_HOST, "localhost");
				// Creer nouveau container
				ContainerController container = runtime.createAgentContainer(profile);
				System.out.println("Container cree : " + NAME_CONTAINER);
				// Deplacement de l'agente du main_container au nouveau container
				ContainerID destination = new ContainerID(NAME_CONTAINER, null);
				this.doMove(destination);

				// creation bloc genesis
				AID aid_genesis1 = new AID();
				AID aid_genesis2 = new AID();
				Data data1=new Data(aid_genesis1, aid_genesis2,	"Genesis");
				Block a= new Block( 1, (int)new Date().getTime(), data1, "0" );
				
				Blockchain a3=this.getBlockchain();
				a3.addBlockInBlockchain(a);
				this.setBlockchain(a3);
			} catch (NotYetReady e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		/*long beginTime = System.currentTimeMillis();
		// envoie requete broadcast pour decouvrir autre noeud
		
		boolean first = true;
		while ((System.currentTimeMillis() - beginTime) < TIMEOUT && first) {
			/*
			 * if(reponse_recue){ copie blockchain first = false }
			 
		}*/
		
		/*if (first) {
			
			this.listeGUIDs.inserer_noeud(this.guid);
			
			jade.core.Runtime runtime = jade.core.Runtime.instance();
			// Definir profil du nouveau container.
			Profile profile = new ProfileImpl();
			profile.setParameter(Profile.CONTAINER_NAME, NAME_CONTAINER);
			profile.setParameter(Profile.MAIN_HOST, "localhost");
			// Creer nouveau container
			ContainerController container = runtime.createAgentContainer(profile);
			System.out.println("Container cree : " + NAME_CONTAINER);
			// Deplacement de l'agente du main_container au nouveau container
			ContainerID destination = new ContainerID(NAME_CONTAINER, null);
			this.doMove(destination);

			// creation bloc genesis
			AID aid_genesis1 = new AID();
			AID aid_genesis2 = new AID();
			Data data1=new Data(aid_genesis1, aid_genesis2,	"Genesis");
			Block a= new Block( 1, (int)new Date().getTime(), data1, "0" );
			
			Blockchain a3=this.getBlockchain();
			a3.addBlockInBlockchain(a);
			agent.setBlockchain(a3);
		}*/
		
		actualiserTableRoutage();
		addBehaviour(new Behaviour_Rest());
	}


	
	@Override
	protected void takeDown() {
		System.out.println("Bye");
	}

	String hashageGUID(String adresse) throws Exception{
		String string_a_hasher=adresse;
		
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(string_a_hasher.getBytes());
        byte byteData[] = md.digest();
        //convertir le tableau de bits en une format hexad�cimal - m�thode 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++) {
         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();	
	}

	protected void actualiserTableRoutage() {
		
		for(int i=0; i<this.tableRoutage.length; i++) {
			int j = 0;
			boolean trouve = false;
			while(!trouve) {
				//SI num_Noeud = A+2^i%64 && ce noeud existe => mis dans la table de routage
				if(j==(listeGUIDs.getIndex(this.guid)+Math.pow(2, i))%64 && this.listeGUIDs.liste[j]!=null) {
					this.tableRoutage[i]=j;
					trouve=true;
				}
				//Sinon, si num_Noeud = A+2^i%64 && ce noeud n'existe pas, on prend le prochain noeud existant.
				else if(j==(listeGUIDs.getIndex(this.guid)+Math.pow(2, i))%64 && this.listeGUIDs.liste[j]==null) {
					while(this.listeGUIDs.liste[j]==null) {
						j++;
					}
					this.tableRoutage[i]=j;
				}
			}
		}	
	}	

	protected AID[] getSuccessors() {
		AID tabDest[] = new AID[ListeGUID.CAPACITY];
		
		long tableRoutage [] = this.getTableRoutage();
		
		for(int i=0; i<tableRoutage.length; i++) {
			tabDest[i] = this.getListeGUIDs().getAID(tableRoutage[i]);
		}
		return tabDest;
	}
}
