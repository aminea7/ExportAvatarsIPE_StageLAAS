package org.eclipse.om2m.Avatar;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.om2m.OM2M.Client;
import org.eclipse.om2m.OM2M.ClientInterface;
import org.eclipse.om2m.OM2M.Mapper;
import org.eclipse.om2m.Avatar.HTTPServer.MyHandler;
import org.eclipse.om2m.AvatarsLAAS.Controller.*;
import org.eclipse.om2m.OM2M.MapperInterface;
import org.eclipse.om2m.OM2M.ObixUtil;
import org.eclipse.om2m.OM2M.Response;
import org.eclipse.om2m.Ontology.Goal;
import org.eclipse.om2m.Ontology.Service;
import org.eclipse.om2m.Ontology.ServiceOperation;
import org.eclipse.om2m.Ontology.Task;
import org.eclipse.om2m.SocialNetworks.SocialNetwork;
//import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.resource.ContentInstance;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import FIPA.DateTimeHelper;
//import org.eclipse.om2m.commons.constants.Constants;
//import org.eclipse.om2m.commons.constants.MimeMediaType;



public class Avatar extends Agent{

	
	/*	------------------------------------------- 		D A T A 		--------------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Attributs
	private String name;
	private String msgToSend;
	private String owner;
	//Location
	private double latitude=99;
	private double longitude=99;
	//Interets
	private ArrayList <Interest> interestsList = new ArrayList <Interest> ();	//Used to get a specific data, using an index for exp
	private Map<String,Double> interestsVector = new HashMap<String, Double>();	//Used to calculate the Social Distance using its vector shape
	
	//Social Network
	private SocialNetwork socialNetwork ;
	private MetaAvatar metaAvatar = null ; 		//Contains its meta Data
	private ArrayList <String> InteretsTasksList = new ArrayList <String>();		//Contains the all the kind of interests in its tasks

	//OM2M//"http://localhost:8080/~/mn-cse/mn-name/Repository1"
	private String repoName;    //Repository1
	private ArrayList <String> repoNameList = new ArrayList <String>();
	private final String ORIGINATOR = "admin:admin";
	private ClientInterface client = new Client();
	private MapperInterface mapper = new Mapper();
	//HTTP Server
	
	//Ontology
	private String data;			//RDF Properties 
	private String rules;			//Semantic Rules
	private Model modelData;		//Model
	
	//Cache
	private ArrayList <Goal> goalsList = new ArrayList <Goal> () ;	//Contains all the goals to reach
	private ArrayList <String> delegateTasks = new ArrayList <String> (); //Contains the id Conversation about the tasks for whom he became the delegate "&&&" nb of requests
	
	//Supervision/Optimisation
	private static int cptTasks=0;
	private static int cptMessages=0;
	private static int cptMessagesHTTP=0;
	private static int cptTasksExec=0;

	//Managers
	private ServicesManager servicesManager ;
	private DelegationsManager delegationsManager;
		
    /** CSE Type */
    public final String CSE_TYPE = System.getProperty("org.eclipse.om2m.cseType","IN-CSE");
    /** CseBase id. */
    public final String CSE_ID = System.getProperty("org.eclipse.om2m.cseBaseId","in-cse");
    /** CseBase name. */
    public final String CSE_NAME = System.getProperty("org.eclipse.om2m.cseBaseName", "in-name");

    //Controller
    private ControllerAvatar controllerAvatar;	//NOT USED ?
	private int serverPort = ThreadLocalRandom.current().nextInt(1025, 9999);
	//private	HTTPServer httpServer;
	private String URL;

	
	
	/*	--------------------------------------- 		B E H A V I O R S 		----------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	//Agent Behavior
	@Override
	public void setup() {
		
		Object [] args = getArguments() ;
		//Check if it has the good nb of args and get them
		if (args.length==3) {
			
			//Save the args it received
			data = (String) args[0];	
		    rules= (String) args[1];		
		    //The model containing the data
			modelData = ModelFactory.createDefaultModel();
	        modelData.read(data);
	        //Repository
	        repoNameList = (ArrayList <String>) args[2];

	        
			//Periodic Behaviour: Sending msg
			addBehaviour(new TickerBehaviour(this,5000){
				private int cpt=0 ;
				@Override
				protected void onTick() {

				}
			}); 
			
			//One Time Behavior
			addBehaviour(new OneShotBehaviour(){
				
				@Override
				public void action(){
					ExtractName();
					System.out.println("		[CREATION OF AVATAR]: "+name+"  "+URL);
				    URL="http://localhost:"+serverPort+"/~/mn-cse/mn-name/"+name;

					servicesManager = new ServicesManager(name);//, socialNetwork);
					delegationsManager= new DelegationsManager(name);
				    controllerAvatar = new ControllerAvatar(name);

					ExtractOwner();
					ExtractLocation();
					ExtractInterests();
					ExtractServices();
					
					ExtractGoals();
					//goalsList.get(0).showGoal();
					ExportMetaData();

					try {
						HTTPServer(serverPort);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					/*
					 //Show Interets
					System.out.println("[INTEREST LIST]"+name);
					for (int i=0; i<interestsList.size(); i++){
						System.out.println("Interest n°"+i+": "+interestsList.get(i).getName()+" "+interestsList.get(i).getLevel());

					}*/
					try {TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}
					FriendsResearch();
					
					servicesManager.UpdateSN(socialNetwork);

					
					if (!goalsList.isEmpty() ){//&& name.equals("Avatar1")){
						try {
							BrowseTasks(goalsList.get(0).getTasksList());
						} catch (IOException e) {
							e.printStackTrace();
						}
						//Browse the tasks and Execute them
						//Timeout of 30 sec
						//TBD: Manage the timeout depending of the max timeout
						Timer timer2 = new Timer();
						timer2.schedule(new TimerTask() {
							  @Override
							  public void run() {
								  //System.out.println("		[TASKS OF "+name+"]"); 
								  //goalsList.get(0).showGoal();
								  
								  /*
								  //Test if the goal is executable
								  if (goalsList.get(0).ReadyForExecution()){
									  System.out.println("		[TEST READYEXEC GOAL OF "+name+"] YYOOOOOOOSSSS  "+goalsList.get(0).getName()); 
									  ExecuteGoal(goalsList.get(0).getTasksList());
								  }
								  else {
									  System.out.println("		[TEST READYEXEC GOAL OF "+name+"] NNNOOOOOO  "+goalsList.get(0).getName()); 
								  }*/
							  }
							}, 10*1000);				

					}
					else {
						System.out.println("		["+name+"] WARNING !!! NO GOALS !!!");
					}
					
					/*
					if (name.equals("Avatar1")){
						System.out.println("		IN IF  SETACTOR ON "+goalsList.get(0).getTasksList().get(0).getContent());
						goalsList.get(0).getTasksList().get(2).setActor("aaa");
					}*/
					
					
					
					//Buttons
					if (name.equals("Avatar1")){
						//try {TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {e.printStackTrace();}

						// Create DESCRIPTION contentInstance on the DESCRIPTOR container resource
						String content = ObixUtil.getDescriptorRep(null,"2",null);
						ContentInstance contentInstance = new ContentInstance();
						contentInstance.setContent(content);
						contentInstance.setContentInfo(MimeMediaType.OBIX);
						contentInstance.setName("Buttons");
						//RequestSender.createContentInstance("http://localhost:8080/~/mn-cse/mn-name/Repository1/Repository1_DESCRIPTOR", contentInstance);
						
						/*
						//Butttons
						try {
							client.create("http://localhost:8080/~/mn-cse/mn-name/Repository1/Repository1_DESCRIPTOR", mapper.marshal(contentInstance), ORIGINATOR, "4");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						*/
						
						//HTTP Request	
	/*
						try {
							//Response resp = client.request("http://localhost:8080/~/mn-cse/mn-name/Repository1?op=setOn&lampid=2",null, ORIGINATOR, null);
							//Response resp = client.request("http://localhost:9797/~/mn-cse/mn-name/Repository1?op=setOn&lampid=2",null, ORIGINATOR, null);
							
							String friendURL= socialNetwork.getFriend("Avatar2").getURL();

							
							//TEST REQUEST
							String xml = "<type>propagate</type>";
							xml=xml+"<sender>Avatar1</sender>";
							xml=xml+"<conversationId>Avatar1&http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#Task3&Label3</conversationId>";
							xml=xml+"<content>http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#Task3&Label3&InterestN</content>";
						    Calendar rightNow = Calendar.getInstance();
						    String date = rightNow.toInstant().toString();
							xml=xml+"<date>"+date+"</date>";
							Response resp = client.request(friendURL+"?type=propagate", ORIGINATOR, xml);
							//TBD!! What if there is no conversationId or if content is not Task&Label
							
							ResponsesHandler(resp);
							
							//TEST QUERY
							
							//Response resp = client.request(friendURL+"?type=query&service=Service13", ORIGINATOR, "NoContent");
							
							
	
						} catch (IOException e) {
							System.err.println("Error on creating resources");
							e.printStackTrace();
						}*/
						
						/*
						try {
							HTTPGET();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}
				}
		  });		
			
			
			//Cyclic Behaviour: Message Reception	
			//Performative=16  ==> Request
			//Performative=21  ==> Propagate
			//Performative=11  ==> Proposal
			//Performative=6   ==> Failure
			//Performative=0   ==> Accept
			//Performative=4   ==> Confirm

			//Performative=12  ==> Query (Its to execute a task)
			//Performative=7  ==> Inform (The response to GET Requests(or 'Querys'), or ACK of PUT inputs)

			addBehaviour(new CyclicBehaviour(){		
				@Override
				public void action() {
					ACLMessage msg = receive() ;
					if(msg != null) {		
						String sender = msg.getSender().getLocalName();
						String cont = msg.getContent();
						
						//Request Message
						if (msg.getPerformative()==16){
							System.out.println("["+name+": <-- Request Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
							String task = cont.split("&")[0];
							String taskLabel = cont.split("&")[1];
							//isAble?
							//TBD: URGENT !! USE THE SERVICES MANAGER AS IT CONTAINS SERVICES OF FRIENDS TOO
							if(IsAbleTaskFriend(taskLabel)){
								//PROPOSE
								ACLMessage message = new ACLMessage(ACLMessage.PROPOSE) ;
								message.addReceiver(new AID(sender,AID.ISLOCALNAME));
								message.setInReplyTo(msg.getConversationId());
								//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
								message.setContent(ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
								message.setConversationId(msg.getConversationId());
								message.setReplyByDate(msg.getReplyByDate());
								send(message) ;
								cptMessages++;
								System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
							}
							else{
								//Answer that he can't 
								ACLMessage message = new ACLMessage(ACLMessage.FAILURE) ;
								message.addReceiver(new AID(sender,AID.ISLOCALNAME));
								message.setInReplyTo(msg.getConversationId());
								message.setConversationId(msg.getConversationId());
								message.setReplyByDate(msg.getReplyByDate());
								send(message) ;
								cptMessages++;
								System.out.println("["+name+": Failure Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+"   "+message.getPerformative()+", nbMessages="+cptMessages) ;
							}
						}
						
						//Propagate Message
						else if (msg.getPerformative()==21){
							System.out.println("["+name+": <-- Propagate Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
							String task = cont.split("&")[0];
							String taskLabel = cont.split("&")[1];
							String taskInterest = cont.split("&")[2];

							//isAble?
							if(IsAbleTaskFriend(taskLabel)){
								
								if (name.equals("Avatar5")){
									System.out.println("							[DEBUG AVATAR5      ]  I CAN   "+cont );

								}
								
								//PROPOSE
								ACLMessage message = new ACLMessage(ACLMessage.PROPOSE) ;
								message.addReceiver(new AID(sender,AID.ISLOCALNAME));
								message.setInReplyTo(msg.getConversationId());
								//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
								message.setContent(ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & Name
								message.setConversationId(msg.getConversationId());
								message.setReplyByDate(msg.getReplyByDate());
								send(message) ;
								cptMessages++;
								System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
							}
							else{
								
								//BroadCast to its SN and memorize the nb of people he requested
								int nbRequests=broadcastSN(task+"&"+taskLabel+"&"+taskInterest,msg.getConversationId(),msg.getReplyByDate(),msg.getConversationId().split("&")[0], msg.getSender().getLocalName());
								delegationsManager.AddDelegation(new Delegation(sender, msg.getConversationId(), nbRequests));

								//TBD URGENT !!!!! IF 2nd PROPAGATION ==> GET THE GOOD TIMEOUT
								
								//Timeout of 20 sec
								Timer timer = new Timer();
								timer.schedule(new TimerTask() {
									  @Override
									  public void run() {
										//See if the task was treated 20sec after the launching of the Timeout
										ACLMessage msgToSend=delegationsManager.ManageTimeOut(msg);
										if (msgToSend!= null ){
											send(msgToSend);
											cptMessages++;
											System.out.println("["+name+": Failure Message to "+msgToSend.getAllReceiver().next().toString()+" after Timeout]: "+msgToSend.getContent()+", conversation: "+msgToSend.getConversationId()+"   , nbMessages="+cptMessages) ;	
										}
									  }
									}, 20*1000);									
							}
						}
						
						//Proposal Message
						else if (msg.getPerformative()==11){	//msg= Service77&Label77&Qos77 (Exp)
							System.out.println("["+name+": <-- Proposal Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
							//Test if it correspond to its Task
							String serviceLabel=msg.getContent().split("&")[1];
							String taskLabel=msg.getConversationId().split("&")[2];

							//String taskName = ExtractLabelTask(msg.getConversationId().split("&")[1]);	//idConv= avatar7&Task58&Date
							if (IsServiceOK(taskLabel,serviceLabel)){
								//Send an Accept Request 
								ACLMessage message = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL) ;
								message.setContent(msg.getContent()); 	//msg= Service77&Label77&Qos77 & nameOfSupplier (Exp)
								message.addReceiver(new AID(sender,AID.ISLOCALNAME));
								message.setInReplyTo(msg.getConversationId());
								message.setConversationId(msg.getConversationId());
								message.setReplyByDate(msg.getReplyByDate());
								send(message) ;
								cptMessages++;
								System.out.println("["+name+":Accept Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;

								//TBD: Wait for the ACK?
							}
						}//GRAPH STREAM TBD URGENT
						
						//Failure Message
						else if (msg.getPerformative()==6){
							System.out.println("["+name+": <-- Failure Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
							
							//Test if he's the delegate of this research
							//if (delegateTasks.contains(msg.getConversationId())){
							ACLMessage msgToSend = delegationsManager.ManageFailureRequest(msg, socialNetwork, metaAvatar);

							if (msgToSend != null){
								//It's a failure msg to the delegationSender
								if (msgToSend.getPerformative()==6){
									send(msgToSend);
								}	
								else if(msgToSend.getPerformative()==20){
									String secondDelegate=msgToSend.getContent();
									//Test if there is someone able for a 2nd delegation
									if (!secondDelegate.equals("noOne")){
										String taskData=msg.getConversationId().split("&")[1]+"&"+msg.getConversationId().split("&")[2]+"&"+msg.getConversationId().split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
										sendDelegationTask(secondDelegate, taskData , msg.getConversationId(), msg.getReplyByDate());
										System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());

									}
									else{
										//TBD URGENT !!!! SEND A FAILURE MSG TO THE DELEGATIONFROM AVATAR
									}
								}
								
							}
							
							/*
							 //It's a failure msg to the delegationSender
							if (msgToSend.getPerformative()==6){
								send(msgToSend);
							}	
							//We have to send a new Delegation
							else if(msgToSend.getPerformative()==20){
								String secondDelegate=msgToSend.getContent();
								//TBD: TEST IF ITS ITSELF + SEND THE PROPAGATE
								if (!secondDelegate.equals(name)){
									String taskData=msg.getConversationId().split("&")[1]+"&"+msg.getConversationId().split("&")[2]+"&"+msg.getConversationId().split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
									sendDelegationTask(secondDelegate, taskData , msg.getConversationId(), msg.getReplyByDate());
									System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());

								}
								else{
									System.out.println(name+"     WTTFFF I CANT BE DELEGATE A 2ND TIME");
								}
							}
							 */
						}
						//TBD : URGENT WHAT IF HE WAS DELEGATE AND HE SENT NO REQUESTS SO HE RECEIVED NO FAILURES (EXP TASK9 !!) TEST DANS BROADCAST()

						
						
						//Accept Message
						else if (msg.getPerformative()==0){
							System.out.println("["+name+": <-- Accept Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
							//TBD!!!!! Warning: CHECK IF HE CAN STILL DO THIS
							//If yes
							//Ack Message: 
							ACLMessage message = new ACLMessage(ACLMessage.CONFIRM) ;
							message.addReceiver(new AID(sender,AID.ISLOCALNAME));
							message.setInReplyTo(msg.getConversationId());
							message.setContent(msg.getContent()); 	//msg= Service77&Label77&Qos77 & NameOfSupplier (Exp)
							message.setConversationId(msg.getConversationId());
							message.setReplyByDate(msg.getReplyByDate());
							send(message) ;
							cptMessages++;
							System.out.println("["+name+":Confirm Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
						}
						
						//Confirm Message
						else if (msg.getPerformative()==4){
								System.out.println("["+name+": <-- Confirm Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
								ACLMessage msgToSend = delegationsManager.ManageConfirmRequest(msg, socialNetwork, metaAvatar, servicesManager, goalsList);
								if (msgToSend!= null ){
									send(msgToSend);
									cptMessages++;
									System.out.println("["+name+":Confirm Message to "+msgToSend.getAllReceiver().next().toString()+"]: "+msgToSend.getContent()+", conversation: "+msgToSend.getConversationId()+", nbMessages="+cptMessages+"    "+msgToSend.getPerformative()) ;
								}	
						}
												
						
						//Query (Execution Request)
						else if (msg.getPerformative()==12 && msg!=null){
							System.out.println("["+name+": <-- Execution Query Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()); 
							ACLMessage msgToSend = servicesManager.ServiceExecution(msg);
							send(msgToSend);
							
						}
						
						//Inform (Result of GET/PUT Requets
						else if (msg.getPerformative()==7){
							System.out.println("["+name+": <-- Inform Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()); 
						}
					}
					else {
						block() ;
					}
				}
			});
			
			
		}
		else {
			System.out.println("ERROR: No enough Args for : "+name) ;		
			doDelete() ;
		}
		
	}
	
	/*	--------------------------------------- 		A V A T A R   M E T H O D S 		-------------------------------------------	*/
	/*											Extract all the Avatar Data (its name, location, etc.)									*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Get its name
	public void ExtractName(){ 
		String queryString = 
	    		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+ 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+

	    	        "SELECT ?avatar "+
	    	        "WHERE {?avatar rdf:type avataront:Avatar ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name=binding.get("avatar").toString().split("#")[1];
	    	    }
	}
	
	//Get its owner name
	public void ExtractOwner(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?owner "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasOwner ?owner ."+ 
	    	        "}";
		 
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	owner=binding.get("owner").toString();
	    			//System.out.println("[EXTRACTOWNER] "+name+": "+owner) ;		
	    	    }
	}
	
	//Get its Location
	public void ExtractLocation(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?location "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasLocation ?location ."+ 
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	latitude=Double.parseDouble(binding.get("location").toString().split("/")[0]);
	    	    	longitude=Double.parseDouble(binding.get("location").toString().split("/")[1]);
	    	    	//System.out.println("[EXTRACTLOC] "+name+": "+latitude+"/"+longitude) ;		
	    	    }
	}
	
	//Get all its interests from the semantic data
	public void ExtractInterests(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?interest "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasInterest ?interest ."+
	    	         "?avatar rdf:type avataront:Avatar ."+
	    	        "}";

			    Query query = QueryFactory.create(queryString);
			    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
			    ResultSet results =  qe.execSelect();
			    //ResultSetFormatter.out(System.out, results);
			    String name2 = null;
			    
			    //For each Interest
			    while(results.hasNext()){ 
			    	QuerySolution binding = results.nextSolution(); 
			    	name2=binding.get("interest").toString();
	    	    	//Name and level Interest split
	    	    	String [] parts = name2.split("/");
	    	    	interestsList.add(new Interest(parts[0],Double.parseDouble(parts[1])));
	    	    	interestsVector.put(parts[0],Double.parseDouble(parts[1]));
	    	    }
				//System.out.println("[EXTRACTINTERETS] "+name+", list size: "+interestsList.size());
	}

	//Get all its services from the semantic data
	public void ExtractServices(){ 
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT   ?service ?operation ?omsg ?met ?lab "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasService ?service ."+
		    	     "?service avataront:hasLabel ?lab ."+
	    	         "?service <http://iserve.kmi.open.ac.uk/ns/msm#hasOperation>  ?operation ."+
	    	         "?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasOutputMessage>  ?omsg ."+
	    	         "?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasMethod>  ?met ."+
	    	         //TBD: Search the input msg if it's a PUT Method
	    	         //"?operation <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasInputMessage>  ?imsg ."+
	    	         "}";

			    Query query = QueryFactory.create(queryString);
			    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
			    ResultSet results =  qe.execSelect();
			    //ResultSetFormatter.out(System.out, results);
			    
			    //For each Service
			    //TBD: Each Service may have many operations
			    while(results.hasNext()){ 
			    	QuerySolution binding = results.nextSolution(); 
			    	//TBD: Check if a service has many ops
			    	String service=binding.get("service").toString();
			    	String serviceOp=binding.get("operation").toString();
			    	String outputMsg=binding.get("omsg").toString();
			    	//String inputMsg=binding.get("imsg").toString();
			    	String method=binding.get("met").toString();
			    	String label=binding.get("lab").toString();

			    	//Create a service instance and add it to the list
			    	ServiceOperation sOP = new ServiceOperation(serviceOp, method, "inputMSG:TBD", outputMsg);
			    	Service serv = new Service(name, service, label, sOP);
			    	servicesManager.addService(serv);
			    	//System.out.println("			"+name+"   SIZE:"+servicesList.size() +"   adding serv  "+service);
	    	    }
	}

	/*	--------------------------------------- 		G O A L S 	M E T H O D S		--------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	
	//Get all the goals to achieve from the semantic data
	public String ExtractGoals(){ 
		String queryString = 
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?goal "+
	    	        "WHERE { "+   
	    	         "?avatar avataront:hasGoal ?goal ."+ 
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    String name = "test";
	    	    
	    	    //For each goal
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name=binding.get("goal").toString();
	    	    	
	    	    	//We create an instance of goal
	    	    	Goal newGoal = new Goal(name);
	    	    	ExtractTasks(newGoal);
	    	    	goalsList.add(newGoal);
	    	    }
		return name;	
	}
		
	//Extract all the tasks contained in a goal
	public void ExtractTasks(Goal goal){ 
		//System.out.println("We extract the tasks of "+goal.getName());
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+

	    	        "SELECT ?task "+
	    	        "WHERE {<"+   
	    	         goal.getName()+">" + " avataront:hasChildTask ?task ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    String name2 = null;
	    	    
	    	    //For each Task
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("task").toString();	
	    	    			   	
	    	    	String interest=ExtractInterestTask(name2);
	    	    	String label=ExtractLabelTask(name2);
	    	    	
	    	    	//Check if it's a composed task
	    	    	if(IsGroupedTask(name2)){
		   				 //System.out.println(name2+" :Composed");		
			    	     Task newTask = new Task(name2,true,IsAbleTask(name2),interest,label);
		   				 ExtractGroupedTask(newTask);
				    	 goal.addTask(newTask);  
		   			 }
		    	    else{
		   				 //System.out.println(name2+" :Not Composed");	    	    	
			    	     Task newTask = new Task(name2,false,IsAbleTask(name2),interest,label);
				    	 goal.addTask(newTask);  
		    	    }    	
	    	    }
	}
	
	
	/*	--------------------------------------- 		T A S K S 	M E T H O D S		---------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
	//Check if it is an atomic task or a composite task
	public boolean IsGroupedTask(String task){
			//System.out.println("Task to test: "+task);
			String queryString = "PREFIX DEMISA: <http://www.semanticweb.org/kkhadir/ontologies/2019/1/DEMISA#>\n" +
		    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
		    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+				
					" ASK {<"+task+"> rdf:type DEMISA:GroupedTask .}";			
		    Query query = QueryFactory.create(queryString) ;
		    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
		    boolean b = qexec.execAsk();
		    //ResultSetFormatter.out(System.out, b);
		    qexec.close() ;   
			return b;	
		}
	
	//Extract the atomic tasks from a group task
	public void ExtractGroupedTask(Task groupedTask){ 
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?task "+
	    	        "WHERE {<"+   
	    	        groupedTask.getContent()+">" + " avataront:hasChildTask ?task ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
 
	    	    String name2 = null;
	    		ArrayList <Task> tasksList = new ArrayList <Task>() ;	//Will contain all the sub tasks
    	    	//System.out.println("[EXTRACTGROUPEDTASK]");
	    	    
	    	    //For each Task
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("task").toString();	
	    	        	    	
	    	    	//We create a new Task
	    	    	String interest=ExtractInterestTask(name2);
	    	    	String label=ExtractLabelTask(name2);
		    	    Task newTask = new Task(name2,false,IsAbleTask(name2),interest, label);
	    	    	tasksList.add(newTask);
	    	    }
	    	    groupedTask.majTasksList(tasksList);	
	}
	
	//Extract the interest of a task
	public String ExtractInterestTask(String task){ 
		String name2=null;
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?interest "+
	    	        "WHERE {<"+ task + "> avataront:hasInterest ?interest ."+
	    	        "}";
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("interest").toString();	
	    	    	//System.out.println("[EXTRACTINTERESTTASK: ]"+name+": "+task+" has the interest: "+name2);
	    	    	//We add this interest to the InterestsTasks List if it's not already in 
	    	    	if(!InteretsTasksList.contains(name2)){
	    	    		InteretsTasksList.add(name2);
	    	    	}
	    	    }
	    return name2;
	}
	
	//Extract the label of a task
	public String ExtractLabelTask(String task){ 
		String name2=null;
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    	        "SELECT ?label "+
	    	        "WHERE {<"+ task + "> avataront:hasLabel ?label ."+
	    	        "}";
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("label").toString();	
	    	    }
    	//System.out.println("[EXTRACTLABELTASK: ]"+name+": "+task+" has the label: "+name2);
	    return name2;
	}
	
	//Check if he can realize this task himself <=> If he has a task and service with the same label
	public boolean IsAbleTask(String task){
		//System.out.println("TASK FROM ISABLE: "+task);
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
				" ASK {<"+
					task + "> avataront:hasLabel ?label ."+
					"?service avataront:hasLabel ?label ."+
					"?service rdf:type iserve:Service ."+
					"}";

		Query query = QueryFactory.create(queryString) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
	    boolean b = qexec.execAsk();
	    //ResultSetFormatter.out(System.out, b);
	    qexec.close() ;
	    return b;	
	}

	//Check if he can realize a task for a friend <=> If he has a service with a similar label than the label asked for
	public boolean IsAbleTaskFriend(String taskLabel){
		String queryString = 
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
				" ASK {"+
					"?service avataront:hasLabel \""+taskLabel+"\" ."+
					"?service rdf:type iserve:Service ."+
					"}";

		Query query = QueryFactory.create(queryString) ;
	    QueryExecution qexec = QueryExecutionFactory.create(query, modelData) ;
	    boolean b = qexec.execAsk();
	    //ResultSetFormatter.out(System.out, b);
	    qexec.close() ;
	    return b;	
	}
	
	//Get the service (its name, Label and QoS) with a certain label
	public String ExtractServiceFromLabel(String labelService){ 
		String name2="ExtractServiceERROR"; String name3="ExtractServiceERROR"; String name4="ExtractServiceERROR";
		String queryString =
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+
	    	        "SELECT  ?service ?label ?qos "+
	    	        "WHERE {?service avataront:hasLabel \""+labelService+"\" ."+
	    	        "?service avataront:hasLabel ?label ."+
	    	        "?service <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasQoS> ?qos ."+
					"?service rdf:type iserve:Service ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    //ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("service").toString();
	    	    	name3=binding.get("label").toString();	
	    	    	name4=binding.get("qos").toString();	
	    	    	//System.out.println("[EXTRACT SERVICE : ]"+name+": "+labelService+"  "+binding.get("qos").toString());	
	    	    }
	    return name2+"&"+name3+"&"+name4;
	}
	
	//Check if he can realize a task for a friend <=> If he has a service with a similiar label than the label asked for
	public boolean IsServiceOK(String taskLabel, String serviceLabel){
		if (taskLabel.equals(serviceLabel))
			return true;	
		else
			return false;
	}
	
	
	/*	----------------------------------- 	 	S O C I A L  M E T H O D S		-----------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	//Export its meta Data at the repository as a content instance, in the Data container
	public void ExportMetaData(){
		
		System.out.println("		[EXPORT META DATA OF AVATAR]: "+name);

		
		// Push a description into a content instance
		ContentInstance descriptor = new ContentInstance();
		String agentName=name.split("@")[0];
		descriptor.setName(agentName);
		descriptor.setContent("Content");
		descriptor.setContentInfo("application/obix:0");
		//Labels
		//descriptor.getLabels().add("<TEST>"+"TEST"+"</TEST>");
		descriptor.getLabels().add("<name>"+agentName+"</name>");
		descriptor.getLabels().add("<owner>"+owner+"</owner>");
		descriptor.getLabels().add("<latitude>"+latitude+"</latitude>");
		descriptor.getLabels().add("<longitude>"+longitude+"</longitude>");
		descriptor.getLabels().add("<url>"+URL+"</url>");
		//System.out.println("TEST 			<url>"+URL+"</url>");
		//Interests
		descriptor.getLabels().add("<nb_interest>"+interestsList.size()+"</nb_interest>");
		for (int i=0; i<interestsList.size();i++){
			if (interestsList.get(i).getLevel()!=0.0){
				descriptor.getLabels().add("<interest>"+interestsList.get(i).getName()+"/"+interestsList.get(i).getLevel()+"</interest>");
			}
		}
		try {
			//Browse all the Repos that he's linked to
			for (int b=0; b<repoNameList.size(); b++){
				String adress="http://localhost:8080/~/mn-cse/mn-name/"+repoNameList.get(b)+"/"+repoNameList.get(b)+"_DATA";
				String repo1="http://localhost:8080/~/mn-cse/mn-name/Repository1/Repository1_DATA/";
				client.create(adress, mapper.marshal(descriptor), ORIGINATOR, "4");
				System.out.println("		[EXPORT META DATA OF AVATAR]: "+name+" in repo: "+repoNameList.get(b)+"  "+adress);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("		ERROOOOOOOOOOOOOOOR!!![EXPORT MT OF AVATAR]: "+name);

		}
		
	}

	//Check all the avatars availables in the repository
	public void FriendsResearch(){
		
		//Create its metaAvatar to use it to calculate the Social Distance
		metaAvatar = new MetaAvatar(name, owner, latitude, longitude, interestsVector, interestsList, -99.0, URL);	//-99: It is a symolic value, as the Avatar don't have to calculate the SD with itself
		socialNetwork = new SocialNetwork(metaAvatar, InteretsTasksList);

		for (int s=0; s<repoNameList.size();s++){
			try {
				Response resp = client.retrieve("http://localhost:8080/~/mn-cse/mn-name/"+repoNameList.get(s)+"/"+repoNameList.get(s)+"_DATA?rcn=4", ORIGINATOR);
				//System.out.println("RESP: "+resp.getRepresentation());
				socialNetwork.SocialNetworkUpdate(resp.getRepresentation(), metaAvatar, InteretsTasksList);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("FriendsRes ERROR!");
			}
		}
		
		
	}	//TBD: RS Update !!!
	
	//Browse the tasks to deal with the tasks he can't execute
	public void BrowseTasks(ArrayList <Task> tasksList) throws IOException{
		//System.out.println("[BROWSE TASKS]"+name+": "+goalsList.get(0).getName());
		for (int s=0; s<tasksList.size();s++){
			//Able
			if(tasksList.get(s).getIsAble()){
				//System.out.println("["+name+"] "+tasksList.get(s).getContent()+": Able");
				cptTasks++;
				//System.out.println("	[CAN DO TASK ITSELF]"+name+": "+tasksList.get(s).getContent()+", total="+cptTasks);
				tasksList.get(s).setActor(name);

			}
			//Non Able ==> Check if grouped
			else {
				//Grouped ==> Recursion on this tasks list composing this grouped Task
				if(tasksList.get(s).getGrouped()){
					//System.out.println(tasksList.get(s).getContent()+": Not Able and Grouped task");
					//WARNING!!! TBD: Can't he ask someone about the entire Grouped Task, before looking at its atomic tasks components
					BrowseTasks(tasksList.get(s).getTasksList());
				}
				//Non Grouped
				else {
					//ASK
					String interest = tasksList.get(s).getInterest();
					String delegate = socialNetwork.getDelegate(interest);
					//System.out.println("["+name+"] "+tasksList.get(s).getContent()+": Not Able and NOT Grouped task and will ask "+friend+", it's an "+interest);
					//Check if he's its own delegate, if yes, he don't have to send this message
					if (delegate.equals(name)){
						//BroadCast to its SN
						broadcastSN2(tasksList.get(s).getContent()+"&"+tasksList.get(s).getLabel()+"&"+tasksList.get(s).getInterest(),"newConversation",null, this.name,name);					//Content: Task7&Label7&InterestP
					}
					//Send a message to the delegate to ask him to propagate the research of a friend of him who can do this task
					else{
						sendDelegationTask2(delegate,tasksList.get(s).getContent()+"&"+tasksList.get(s).getLabel()+"&"+tasksList.get(s).getInterest(), "newConversation", null);				//Content: Exp: Task7&Label7&InterestY
					}
				
				}

			}
		}
	}
	
	public void ExecuteGoal (ArrayList <Task> tasksList){
		
		 for (int s=0; s<tasksList.size(); s++){
			 
			  String task=tasksList.get(s).getContent();
			  String taskLabel=tasksList.get(s).getLabel();
			  String taskInterest=tasksList.get(s).getInterest();
			  String taskData=task+"&"+taskLabel+"&"+taskInterest;
			  String supplier=tasksList.get(s).getActor();
			  
			  //Check if it has the service
			  if (supplier.equals(name)){
				  servicesManager.ExecuteOwnService(task, taskLabel);
				  //cptTasksExec++;
			  }

			  //He can't but someone can, ask the supplier
			  else if (!supplier.equals("noSupplier")){

					  ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF) ;
					  message.addReceiver(new AID(supplier,AID.ISLOCALNAME));
					  message.setContent(taskData) ;
				      Calendar rightNow = Calendar.getInstance();
				      int min = rightNow.get(Calendar.MINUTE);
					  int hour = rightNow.get(Calendar.HOUR_OF_DAY);
					  int day = rightNow.get(Calendar.DAY_OF_MONTH);
					  int month = rightNow.get(Calendar.MONTH);
					  int year = rightNow.get(Calendar.YEAR);
					  //message.setConversationId(name+"&"+task+"&"+hour+"-"+min+"-"+day+"-"+month+"-"+year);
					  message.setConversationId(name+"&"+taskData+"&"+hour+min+day+month+year);
					  
					  Date startTime = rightNow.getTime();
					  message.setReplyByDate(startTime);
					  send(message) ;
					  System.out.println("["+name+":Query Message to "+supplier+"]: "+message.getContent()+", conversation: "+message.getConversationId()) ;

			  }
			  
			  //No one can do this task, but if it's a Grouped Task ==> Recursion on this tasks list composing this grouped Task
			  else if(tasksList.get(s).getGrouped()){
					 ExecuteGoal(tasksList.get(s).getTasksList());
			  }
		  }
	}

	
	/*	---------------------------------- 	 	C O M M U N I C A T I O N   M E T H O D S		-----------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	//Ask a delegate to ask its SN about a task
		public void sendDelegationTask2(String agentName, String taskData, String conversation, String date ) throws IOException {
			
			String message = "<type>propagate</type>";
			message = addXmlElement(message, "content", taskData) ;
			message = addXmlElement(message,"sender",name);

			//cptMessages++;

			if (conversation.equals("newConversation")){

				//message.setConversationId(name+"&"+task+"&"+hour+"-"+min+"-"+day+"-"+month+"-"+year);
				message = addXmlElement(message, "conversationId",name+"&"+taskData);
			    
				Calendar rightNow = Calendar.getInstance();
			    Date startTime = rightNow.getTime();
			    Instant now = rightNow.toInstant();
				message = addXmlElement(message, "date", now.toString());
				
				//System.out.println("["+name+":Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;

			}
			
			//Delegation of Delegation
			else {
				
				message = addXmlElement(message, "conversationId",conversation);
				message = addXmlElement(message, "date",date);

				//System.out.println("["+name+":Second Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
			}
			String friendURL= socialNetwork.getFriend(agentName).getURL();
			
			Response response2 = client.request(friendURL+"?type=propagate", ORIGINATOR, message);
			ResponsesHandler(response2);

			
		}
	
	//Ask a delegate to ask its SN about a task
	public void sendDelegationTask(String agentName, String taskData, String conversation, Date date ) {
		
		ACLMessage message = new ACLMessage(ACLMessage.PROPAGATE) ;
		message.setContent(taskData) ;
		message.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		cptMessages++;

		if (conversation.equals("newConversation")){
		    Calendar rightNow = Calendar.getInstance();
		    int min = rightNow.get(Calendar.MINUTE);
			int hour = rightNow.get(Calendar.HOUR_OF_DAY);
			int day = rightNow.get(Calendar.DAY_OF_MONTH);
			int month = rightNow.get(Calendar.MONTH);
			int year = rightNow.get(Calendar.YEAR);
			//message.setConversationId(name+"&"+task+"&"+hour+"-"+min+"-"+day+"-"+month+"-"+year);
			message.setConversationId(name+"&"+taskData+"&"+hour+min+day+month+year);
			
		    Date startTime = rightNow.getTime();
			message.setReplyByDate(startTime);
			System.out.println("["+name+":Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;

		}
		
		//Delegation of Delegation
		else {
			message.setConversationId(conversation);
			message.setReplyByDate(date);
			System.out.println("["+name+":Second Propagate Message to "+agentName+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;

		}
		send(message) ;
		
	}
	
	//BroadCast a msg to its Social Net.
		public int broadcastSN2(String taskData, String conversation, String date, String originalSender, String delegationFrom) throws IOException {
			
			int msgs=0; 	//Nb of Avatars he sent to this msg
			
			String message = "<type>request</type>";
			message = addXmlElement(message, "content",taskData) ;
			message = addXmlElement(message, "sender", name);
			
			//Test if it's a new conversation or not 
			if (conversation.equals("newConversation")){
				Calendar rightNow = Calendar.getInstance();
				
				message = addXmlElement(message,"conversationId", name+"&"+taskData);
				String instant = rightNow.toInstant().toString();
				message = addXmlElement(message, "date", instant);
			}
			else {
				message = addXmlElement(message,"conversationId", conversation);
    			message = addXmlElement(message, "date", date);
			}  
			 String friendName = null;
			 MetaAvatar metaAvatar = null;
			 //Iterator and the SocialNetwork list 
			 Iterator<MetaAvatar> itrFriend = socialNetwork.getSocialNetwork().iterator();
			 while (itrFriend.hasNext()) {
				 metaAvatar = itrFriend.next();
				 friendName = metaAvatar.getName();

				 //To avoid sending to the originalSender
				 if (!friendName.equals(originalSender) && !friendName.equals(delegationFrom)){
					 
					 //System.out.println("			[TEST INTEREST BC]"+name+": Has the avatar friend "+friendName+" the interest "+taskData.split("&")[2]+" for the task: "+taskData.split("&")[0]);
					 //Test if this friend has the the interest of the task
					 String taskInterest = taskData.split("&")[2];
					 if (metaAvatar.ContainsInterest(taskInterest) != -1){
						 //Add the receiver parameters
						 String friendURL = socialNetwork.getFriend(friendName).getURL();
						 
						 Response response2 = client.request(friendURL+"?type=request", ORIGINATOR, message);
						 ResponsesHandler(response2);
						 
						 //System.out.println("["+name+":Request Message to "+friendName+"]: "+message.getContent()+", conversation: "+message.getConversationId()) ;
						 //cptMessagesHTTP++;
						 msgs++;

					 }
					}
			 }
			 
			 //We add this conversation to the delegatesConversations								
			 if(conversation.equals("newConversation")){
				 delegationsManager.AddDelegation(new Delegation(name,getXmlElement(message,"conversationId") , msgs));
			 }

			 return msgs;
		}
	
	//BroadCast a msg to its Social Net.
	public int broadcastSN(String taskData, String conversation, Date date, String originalSender, String delegationFrom) {
		
		int msgs=0; 	//Nb of Avatars he sent to this msg
		
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST) ;
		message.setContent(taskData) ;
		//Test if it's a new conversation or not 
		if (conversation.equals("newConversation")){
			Calendar rightNow = Calendar.getInstance();
			 int min = rightNow.get(Calendar.MINUTE);
			 int hour = rightNow.get(Calendar.HOUR_OF_DAY);
			 int day = rightNow.get(Calendar.DAY_OF_MONTH);
			 int month = rightNow.get(Calendar.MONTH);
			 int year = rightNow.get(Calendar.YEAR);
			 message.setConversationId(name+"&"+taskData+"&"+hour+min+day+month+year);
			  
			 Date startTime = rightNow.getTime();
			 message.setReplyByDate(startTime);

		}
		else {
			message.setConversationId(conversation);
			message.setReplyByDate(date);

		}  
		 String friendName = null;
		 MetaAvatar metaAvatar = null;
		 //Iterator and the SocialNetwork list 
		 Iterator<MetaAvatar> itrFriend = socialNetwork.getSocialNetwork().iterator();
		 while (itrFriend.hasNext()) {
			 metaAvatar = itrFriend.next();
			 friendName = metaAvatar.getName();

			 //To avoid sending to the originalSender
			 if (!friendName.equals(originalSender) && !friendName.equals(delegationFrom)){
				 
				 //System.out.println("			[TEST INTEREST BC]"+name+": Has the avatar friend "+friendName+" the interest "+taskData.split("&")[2]+" for the task: "+taskData.split("&")[0]);
				 //Test if this friend has the the interest of the task
				 String taskInterest = taskData.split("&")[2];
				 if (metaAvatar.ContainsInterest(taskInterest) != -1){
					 //Add the receiver parameters
					 message.addReceiver(new AID(friendName, AID.ISLOCALNAME));
					 System.out.println("["+name+":Request Message to "+friendName+"]: "+message.getContent()+", conversation: "+message.getConversationId()) ;
					 cptMessages++;
					 msgs++;

				 }
				}
		 }
		 send(message);	
		 
		 //We add this conversation to the delegatesConversations								
		 if(conversation.equals("newConversation")){
			 delegationsManager.AddDelegation(new Delegation(name,message.getConversationId() , msgs));
		 }

		 return msgs;
	}
	
	public void DetermineNewDelegate(String cont){
		
	}
	
	
	//Construct the response to inform the original sender
	public void DelegateResponse(){
		
	}
		
	
	public void sendMsg(String agentName, String msg) {
		
		ACLMessage message = new ACLMessage(ACLMessage.INFORM) ;
		message.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		message.setContent(msg) ;
		send(message) ;
		System.out.println("["+name+": Msg sending to "+agentName+"]: "+message.getContent()) ;
	}

	
	/*	----------------------------------- 	 	R E S S O U R C E S  M E T H O D S		--------------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	

	/*	----------------------------------- 	 	S H O W  M E T H O D S		--------------------------------------------------	*/
	/*																																*/
	/*	---------------------------------------------------------------------------------------------------------------------------	*/

	
	//Show the complete ontology (its model)
	public void showModel(){
		//System.out.println(modelData);		
		String[] rdfTab = modelData.toString().split(" "); 
		for (int s=0; s<rdfTab.length;s++){
			System.out.println(rdfTab[s]);
		}
	}
	
	//Show Interests List
	public void showInterestsList(){
		System.out.println("Interests List: ");	
		for (int s=0; s<interestsList.size();s++){
			System.out.println("Name: "+interestsList.get(s).getName()+", Level: "+String.valueOf(interestsList.get(s).getLevel()));
		}
	}
	
	//Show the services
	public String ShowServices(){ 
		String name2=null;
		String queryString =  
	    		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
	    	    "PREFIX avataront: <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#>\n"+
	    		"PREFIX iserve: <http://iserve.kmi.open.ac.uk/ns/msm#>"+

	    	        "SELECT  ?qos "+
	    	        "WHERE {"+
	    	        "?service avataront:hasLabel ?label ."+
					//"?service <http://www.laas-cnrs.fr/recherches/SARA/ontologies/AvatarOnt#hasQoS>  ?qos ."+
					"?service rdf:type iserve:Service ."+
	    	        "}";
		   
	    	    Query query = QueryFactory.create(queryString);
	    	    QueryExecution qe = QueryExecutionFactory.create(query, modelData);
	    	    ResultSet results =  qe.execSelect();
	    	    ResultSetFormatter.out(System.out, results);
	    	    
	    	    while(results.hasNext()){ 
	    	    	QuerySolution binding = results.nextSolution(); 
	    	    	name2=binding.get("qos").toString();	
	    	    }
    	//System.out.println("[EXTRACTLABELTASK: ]"+name+": "+task+" has the label: "+name2);
	    return name2;
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
	
	public void HTTPGET() throws Exception {
		  StringBuilder result = new StringBuilder();
		  URL url = new URL("http://localhost:9797/~/mn-cse/mn-name/Repository/op=Service3");
		  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		  conn.setRequestMethod("GET");
		  BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		  String line;
		  while ((line = rd.readLine()) != null) {
		     result.append(line);
		  }
		  rd.close();
		  System.out.println("AVATAR: HTTP REQUEST SENT, resp= "+result.toString());

	} 
	
	// HTTP POST request
		private void sendGET() throws Exception {

			String url = "https://selfsolve.apple.com/wcResults.do";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			//print result
			System.out.println("				SEND GET: "+response.toString());

		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		
		public void HTTPServer (int port) throws IOException{
			
			System.out.println("			CREATION OF SERVER of "+name+", port= "+port);
			serverPort=port;
			
			HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
	        server.createContext("/~/mn-cse/mn-name/"+name, new MyHandler());
	        server.setExecutor(null); // creates a default executor
	        server.start();
	        
		}
		

		
		class MyHandler implements HttpHandler {
	        @Override
	        public void handle(HttpExchange t) throws IOException {
	        	
	        	//The request received
	        	Map<String, String> params = queryToMap(t.getRequestURI().getQuery()); 

	        	String request = IOUtils.toString(t.getRequestBody(), "UTF-8");
	        	String requestQuery = t.getRequestURI().getQuery();
	        	//System.out.println("			SERVER HTTP of "+name+", received: "+request+"  "+requestQuery+"   "+params.get("type"));
	        	String res = "";
	        	
	        	//Test the type
	        	String test = params.get("type");
	        	
	        	String sender = getXmlElement(request,"sender");
        		String content = getXmlElement(request,"content");
        		String conversationId = getXmlElement(request,"conversationId");
        		
				cptMessagesHTTP++;
				System.out.println("	[HTTP SERVER of "+name+": <-- "+test+" Reception from "+sender+"]: "+content+", conversation: "+conversationId+", nbHTTPMessages="+cptMessagesHTTP) ;
	        	
	        	switch (test){
	        	
        		//Request Message
	        	case "request":
	        		
	        		String task = content.split("&")[0];
					String taskLabel = content.split("&")[1];
					String date = getXmlElement(request,"date");
	        		//try{
	        		//} catch (Exception e) {e.printStackTrace();}
	        		
					//isAble?
					//TBD: URGENT !! USE THE SERVICES MANAGER AS IT CONTAINS SERVICES OF FRIENDS TOO
					if(IsAbleTaskFriend(taskLabel)){
						//PROPOSE
						res = addXmlElement(res,"type","propose");
						res = addXmlElement(res,"sender",name);
						res = addXmlElement(res,"conversationId",conversationId);
						//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
						res = addXmlElement(res,"content",ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
						res = addXmlElement(res,"date",date);
						//cptMessagesHTTP++;
						//System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
					}
					else{
						//Answer that he can't 
						res = addXmlElement(res,"type","failure");
						res = addXmlElement(res,"sender",name);
						res = addXmlElement(res,"content","failure") ;

						res = addXmlElement(res,"conversationId",conversationId);
						//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
						//addXmlElement(res,"content",ExtractServiceFromLabel(taskLabel)+"&"+name) ;	//ServiceX & LabelX & QosX & name
						res = addXmlElement(res,"date",date);
						//cptMessagesHTTP++;
						//System.out.println("["+name+": Failure Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+"   "+message.getPerformative()+", nbMessages="+cptMessages) ;
					}
	        		break;
	        	
	        	case "accept":
	        		res="<type>confirm</type><sender>"+name+"</sender><content>ok</content>";
					//cptMessagesHTTP++;
	        		break;
	        		
	        	case "failure":
					String date2 = getXmlElement(request,"date");

	        		//String date = getXmlElement(request,"date");
	        		//System.out.println("["+name+": <-- Failure Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
					
					//Test if he's the delegate of this research
					//if (delegateTasks.contains(msg.getConversationId())){
					String msgToSend = delegationsManager.ManageFailureRequest2(request, socialNetwork, metaAvatar);

					//He was the delegate of this conversation
					if (msgToSend != null){
						//It's a failure msg to the delegationSender because there is no time
						if (msgToSend.contains("<type>failure</type>")){
							String friend = getXmlElement(msgToSend,"receiver");
							String friendURL = socialNetwork.getFriend(friend).getURL();
							Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, msgToSend);
							
							//ResponsesHandler(response2);
						}	
						//There is still more time 
						else if(msgToSend.contains("second")){
							String secondDelegate= getXmlElement(msgToSend,"secondDelegate");
							String delegationFrom= getXmlElement(msgToSend,"delegationFrom");

							//Test if there is someone able for a 2nd delegation
							if (!secondDelegate.equals("noOne")){
																
								String taskData=conversationId.split("&")[1]+"&"+conversationId.split("&")[2]+"&"+conversationId.split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
								sendDelegationTask2(secondDelegate, taskData , conversationId, date2);
								//System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());
							}
							else{
								//TBD URGENT !!!! SEND A FAILURE MSG TO THE DELEGATIONFROM AVATAR
								
								String res2 = "<type>failure</type>";
								res2 = addXmlElement(res2,"conversationId",conversationId);
								//res = addXmlElement(res,"date",date);
								res2 = addXmlElement(res2,"sender",name);
								res2 = addXmlElement(res2,"content","No more time");
								res2 = addXmlElement(res2,"receiver",delegationFrom);
								
								String friendURL = socialNetwork.getFriend(delegationFrom).getURL();
								Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, res2);
							}
						}
					}
	        		 
	        		break;
	        	
	        	case "failuretimeout":
					res="<type>okfailureTO</type><content>okFTO</content><sender>"+name+"</sender><conversationId>"+conversationId+"</conversationId><date>date</date>";
	        		break;
	        	
	        	case "propagate":
					System.out.println("	[HTTP TEST SERVER of "+name+": <-- Propagate Reception from "+sender+"  "+request);

	        		//System.out.println("["+name+": <-- Propagate Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
					String taskP = content.split("&")[0];
					String taskLabelP = content.split("&")[1];
					String taskInterestP = content.split("&")[2];
					String dateP = getXmlElement(request,"date");

					
					//isAble?
					if(IsAbleTaskFriend(taskLabelP)){
						
						//PROPOSE
						res = addXmlElement(res,"type","propose");
						res = addXmlElement(res,"sender",name);
						res = addXmlElement(res,"conversationId",conversationId);
						//TBD: WARNING!!! We have to see data about the Qos and all the info. about the service
						res = addXmlElement(res,"content",ExtractServiceFromLabel(taskLabelP)+"&"+name) ;	//ServiceX & LabelX & QosX & name
						res = addXmlElement(res,"date",dateP);
						
						//cptMessagesHTTP++;
						//System.out.println("["+name+":Proposal Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
					
					}
					else{
						
						//BroadCast to its SN and memorize the nb of people he requested
						int nbRequests=broadcastSN2(taskP+"&"+taskLabelP+"&"+taskInterestP,conversationId,dateP,conversationId.split("&")[0], sender);
						delegationsManager.AddDelegation(new Delegation(sender, conversationId, nbRequests));
						res="<type>okPropagation</type><content>okPropagation</content><sender>"+name+"</sender><conversationId>"+conversationId+"</conversationId><date>"+dateP+"</date>";
						//TBD URGENT !!!!! IF 2nd PROPAGATION ==> GET THE GOOD TIMEOUT
						
						//Timeout of 20 sec
						Timer timer = new Timer();
						timer.schedule(new TimerTask() {
							  @Override
							  public void run() {
								//See if the task was treated 20sec after the launching of the Timeout
								String msgToSend=delegationsManager.ManageTimeOut2(request);
								if (msgToSend!= null ){
									
									String friend = getXmlElement(msgToSend,"receiver");
									String friendURL = socialNetwork.getFriend(friend).getURL();
									
									try {
										//System.out.println("		TEST URL TIMEOUT of "+name+"		"+friendURL+"?type=failuretimeout   "+msgToSend);
										Response response2 = client.request(friendURL+"?type=failuretimeout", ORIGINATOR, msgToSend);
										ResponsesHandler(response2);

									} catch (IOException e) {
										e.printStackTrace();
									}
									
									//cptMessages++;
									//System.out.println("["+name+": Failure Message to "+msgToSend.getAllReceiver().next().toString()+" after Timeout]: "+msgToSend.getContent()+", conversation: "+msgToSend.getConversationId()+"   , nbMessages="+cptMessages) ;	
								}
							  }
							}, 20*1000);									
					}
					
					//res="<type>okPropagation</type><sender>"+name+"</sender><conversationId>"+conversationId+"</conversationId><date>date9852</date><content>Yoo</content>";
/*
					res = addXmlElement(res,"type","okpropagation");
					res = addXmlElement(res,"sender",name);
					res = addXmlElement(res,"conversationId",conversationId);
					res = addXmlElement(res,"date","date"); 
					res = addXmlElement(res,"content","content"); 
*/				
					System.out.println("	[HTTP TEST RETURN SERVER of "+name+"  "+res);

	        		break;
	        	
	        	case "query":
	        		String service = params.get("service");
	        		res=servicesManager.ServiceExecution2(service);
	        		//System.out.println("				QUERY MSG:  "+res2);
	        		break;
	        		
	        	default:
	        		date= getXmlElement(request, "date");
	        		
	        		res = addXmlElement(res,"type","accept");
					res = addXmlElement(res,"conversationId",conversationId);
					res = addXmlElement(res,"date",date);
					res = addXmlElement(res,"sender",name);
	        		break;
	        	}
	        	
	        	
	    		
	            t.sendResponseHeaders(200, res.length());
	            OutputStream os = t.getResponseBody();
	            os.write(res.getBytes("UTF-8"));
	            os.close();
	            
	        }
	    }
		
		//HTTP Responses Handler
		public void ResponsesHandler(Response resp){
			if (resp.getStatusCode() != 201 || resp.getRepresentation().isEmpty()) {
				String res="";
				//System.out.println("AVATAR: HTTP RESPONSE to "+name+":"+ resp.getRepresentation());		
				String response=resp.getRepresentation();
				try{
					String type=response.split("<type>")[1].split("</type>")[0];
					//cptMessagesHTTP++;
					System.out.println("AVATAR: HTTP SERVER RESPONSE to "+name+":"+ resp.getRepresentation()+"  "+type+", nbHTTPMsg: "+cptMessagesHTTP);		
					
					String content = getXmlElement(response,"content");
					String sender = getXmlElement(response,"sender");
					
					switch (type){
					
					//Propose Message
					case "propose":
						String conversationId = getXmlElement(response,"conversationId");
						String date = getXmlElement(response,"date");
						String serviceLabel=content.split("&")[1];
						String taskLabel=conversationId.split("&")[2];
						
						//System.out.println("["+name+": <-- Proposal Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;

						if (IsServiceOK(taskLabel,serviceLabel)){
							//Send an Accept Request 
							res = addXmlElement(res,"type","accept");
							res = addXmlElement(res,"conversationId",conversationId);
							res = addXmlElement(res,"date",date);
							res = addXmlElement(res,"sender",name);
							res = addXmlElement(res,"content","ok");
							
							//Send the request
							String friendURL =  socialNetwork.getFriend(sender).getURL();
							Response resp2 = client.request(friendURL+"?type=accept", ORIGINATOR, res);
							ResponsesHandler(resp2);
							//cptMessagesHTTP++;
							//System.out.println("["+name+":Accept Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId()+", nbMessages="+cptMessages) ;
							
						}
						break;
					
					//Failure Message
					case "failure":
						
						String conversationIdF = getXmlElement(response,"conversationId");
						String dateF = getXmlElement(response,"date");
						
						//System.out.println("["+name+": <-- Failure Reception from "+sender+"]: "+msg.getContent()+", conversation: "+msg.getConversationId()) ;
						
						//Test if he's the delegate of this research
						//if (delegateTasks.contains(msg.getConversationId())){
						String msgToSend = delegationsManager.ManageFailureRequest2(response, socialNetwork, metaAvatar);

						//He was the delegate of this conversation
						if (msgToSend != null){
							//It's a failure msg to the delegationSender because there is no time
							if (msgToSend.contains("<type>failure</type>")){
								String friend = getXmlElement(msgToSend,"receiver");
								String friendURL = socialNetwork.getFriend(friend).getURL();
								Response response2 = client.request(friendURL+"?type=failure", ORIGINATOR, msgToSend);
								
								//ResponsesHandler(response2);
							}	
							//There is still more time 
							else if(msgToSend.contains("second")){
								String secondDelegate=msgToSend.split("=")[1];
								//Test if there is someone able for a 2nd delegation
								if (!secondDelegate.equals("noOne")){
																	
									String taskData=conversationIdF.split("&")[1]+"&"+conversationIdF.split("&")[2]+"&"+conversationIdF.split("&")[3]; //Extract the Task Data From the conversationID, Conv.ID = avatarOriginal & taskData
									sendDelegationTask2(secondDelegate, taskData , conversationIdF, dateF);
									//System.out.println("							NEW DELEG  "+name+":    2nd delegate = "+secondDelegate+" task  "+taskData+"  conv  "+msg.getConversationId());

								}
								else{
									//TBD URGENT !!!! SEND A FAILURE MSG TO THE DELEGATIONFROM AVATAR
								}
							}
						}
						
						break;
					
					case "failuretimeout":
						String conversationIdTO = getXmlElement(response,"conversationId");
						res="<type>okfailureTO</type><content>okFTO</content><sender>"+name+"</sender><conversationId>"+conversationIdTO+"</conversationId><date>date</date>";
						break;
						
					//Inform Message
					case "inform":
						System.out.println("		INFORM "+response);
						break;
					
					default:
						break;
					}
					
					
					
				
				}catch (Exception e) {e.printStackTrace();}
				
			}
			else{
				System.out.println("AVATAR: HTTP REQUEST ERROR");
			}
		}
		
		//Mapping the params in the URIQuery
		public Map<String, String> queryToMap(String query) {
		    Map<String, String> result = new HashMap<>();
		    for (String param : query.split("&")) {
		        String[] entry = param.split("=");
		        if (entry.length > 1) {
		            result.put(entry[0], entry[1]);
		        }else{
		            result.put(entry[0], "");
		        }
		    }
		    return result;
		}
		//Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery()); 
		
		//Get an element from XML
		public String getXmlElement(String xml, String element){
			String res = "notFound";
			res = xml.split("<"+element+">")[1].split("</"+element+">")[0];
			return res;
		}
		
		//Add information to XML 
		public String addXmlElement(String xml, String element, String value){
			xml = xml+"<"+element+">"+value+"</"+element+">";
			return xml;
		}
}
