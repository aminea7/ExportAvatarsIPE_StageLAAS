package org.eclipse.om2m.SocialNetworks;


import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest; */
import org.apache.jena.rdf.model.Model;
import org.eclipse.om2m.Ontology.Goal;






public class Repository extends Agent{

	
	/*	------------------------------------------- 		D A T A 		--------------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Attributs
	private String name;
	private String msgToSend;
	private String owner;
	//Location
	private double Lat;
	private double Long;
	
	//DataBase
	DFService dfService = new DFService();

	//Behavior
    Map<String, Double> interestsVector = new HashMap<String, Double>();		

	
	//Ontology
	private static String data;			//RDF Properties 
	private String rules;				//Semantic Rules
	private static Model modelData;		//Model

	//Cache
	private static ArrayList <Goal> goalsList = new ArrayList <Goal> () ;	//Contains all the goals to reach
	
	
	/*	--------------------------------------- 		B E H A V I O R S 		----------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	//Agent Behavior
	@Override
	public void setup() {
		
		name= this.getAID().getName() ;
		Object [] args = getArguments() ;
				
			//Periodic Behaviour: Sending msg
			addBehaviour(new TickerBehaviour(this,5000){
				private int cpt=0 ;
				@Override
				protected void onTick() {

				}
			}); 
			
			//Cyclic Behaviour: Message Reception	
			addBehaviour(new CyclicBehaviour(){
				
				@Override
				public void action() {
					ACLMessage msg = receive() ;
					if(msg != null) {
						String sender = msg.getSender().getLocalName();
						System.out.println("["+name+": Msg reception from "+sender+"]: "+msg.getContent()) ;
					}
					else {
						block() ;
					}
				}
			});
			
			//One Time Behavior
			addBehaviour(new OneShotBehaviour(){
				
				@Override
				public void action(){

				
				
				}
		  });
			
	}
		
 
	/*	--------------------------------------- 	 	M E T H O D S		----------------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/
	
	public DFService getDFService(){
		return dfService;
	}

	
	
	public void Subscription(){
		//Subscription s = new Subscription();
	}
	
	/*
	//BroadCast a msg to its Social Net.
	public void broadcastSN(ACLMessage message) {
		 
		 String friendName = null;
		 MetaAvatar MetaAvatar = null;
		 
		 //Iterator and the SocialNetwork list 
		 Iterator<MetaAvatar> itrFriend = socialNetwork.getSocialNetwork().iterator();
		 while (itrFriend.hasNext()) {
			 MetaAvatar = itrFriend.next();
			 friendName = MetaAvatar.getName();
			 //Add the receiver parameters
			 message.addReceiver(new AID(friendName, AID.ISLOCALNAME));
			 send(message);	
			 
		 }
	}*/
	
	
	public void sendMsg(String agentName, String msg) {
		
		ACLMessage message = new ACLMessage(ACLMessage.INFORM) ;
		message.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		message.setContent(msg) ;
		send(message) ;
		System.out.println("["+name+": Msg sending to "+agentName+"]: "+message.getContent()) ;

	}
	
	@Override
	protected void takeDown() {
		System.out.println("Agent destruction") ;
	}
	
	@Override
	public void doMove(Location loc) {	//Container dest.
		System.out.println("Migration to "+loc.getName()) ;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
}
