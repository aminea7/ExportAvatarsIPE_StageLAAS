package org.eclipse.om2m.Avatar;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.om2m.OM2M.Client;
import org.eclipse.om2m.OM2M.ClientInterface;
import org.eclipse.om2m.OM2M.Response;
import org.eclipse.om2m.Ontology.Service;
import org.eclipse.om2m.SocialNetworks.SocialNetwork;

public class ServicesManager {

	private ArrayList <Service> servicesList = new ArrayList <Service> ();
	private String avatarOwner;
	private static int cptTasksExecuted=0;
	private SocialNetwork socialNetwork;
	private ClientInterface client = new Client();
	private final String ORIGINATOR = "admin:admin";
	private ArrayList <MetaAvatar> socialNetworkList = new ArrayList <MetaAvatar>() ; 
 


	
	public ServicesManager(String n){
		avatarOwner=n;
		//socialNetwork=sn;
	}
	public void UpdateSN(SocialNetwork sn){
		socialNetwork=sn;
		socialNetworkList = sn.getSocialNetwork();
	}
	
	
	//Treat the execution requests
		public String ServiceExecution2 (String service){
			
			String res =  "<type>inform</type>";

			//TBD: Raccourcir IsableFirnd ac le ExtractServ			
			//System.out.println("			OPERATIONS MANAGER  ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 

			//Search it
			for (int i=0; i<servicesList.size(); i++){

				//TBDDDD VERY URGENT THE SPLIT OF SERVICS !!!!!!!
				//System.out.println("		SM COMPARAISON   "+ service+"  and  "+servicesList.get(i).getName()+"  "+servicesList.size());
				//NB: ExtractServiceFromLabel returns : ServiceX&LabelX&QosX
				if (servicesList.get(i).getName().equals(service) || servicesList.get(i).getName().split("#")[1].equals(service)){
					
					String supplier=servicesList.get(i).getSupplier();
					//Test if he is the supplier of this service or if it's a friend
					if(supplier.equals(avatarOwner)){
						
						String out=servicesList.get(i).getServiceOp().getOutputMessage();
						if (servicesList.get(i).getServiceOp().getMethode().equals("GET")){
							
							res = addXmlElement(res,"sender",avatarOwner);
							res = addXmlElement(res,"content",out);

							//System.out.println("["+avatarOwner+":Inform Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId());
							//cptTasksExecuted++;
							//System.out.println("		[EXECUTION TASK] ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 
							
						}
						
						//TBD: Treat the PUT messages !!!
						else if (servicesList.get(i).getServiceOp().getMethode().equals("PUT")){
							String in=servicesList.get(i).getServiceOp().getInputMessage();
						}
						
					}
					//Ask this friend
					else{
						//if(taskLabel.equals("Label13")){
					    //} 
						
							//Send HTTP Request to the Supplier
						  //MetaAvatar supplierMeta = socialNetwork.getFriend(supplier);
						  //System.out.println("		SM ASK FRIEND  "+supplierMeta.getName());
						
						  //String friendURL=getURLFriend(supplier);
						  String friendURL = socialNetwork.getFriend(supplier).getURL();
						  //res=res+"supplier=friend="+supplier+"  "+friendURL;

						  Response resp;
						  try {
								resp = client.request(friendURL+"?type=query&service="+service, ORIGINATOR, "NoContent");
								if (resp.getStatusCode() != 201 || resp.getRepresentation().isEmpty()) {
									System.out.println("AVATAR: HTTP RESPONSE of "+avatarOwner+":"+ resp.getRepresentation());		
									//ResponsesTreatment(resp.getRepresentation());
									String output=getXmlElement(resp.getRepresentation(),"content");
									//res=addXmlElement(res,"content",output);
									res=resp.getRepresentation();
								}
								else{
									System.out.println("AVATAR: HTTP REQUEST ERROR");
								}
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						  
							
						  //TBD URGENT
/*
						  message= new ACLMessage(ACLMessage.QUERY_IF);						
						  message.addReceiver(new AID(supplier,AID.ISLOCALNAME));
						  message.setContent(msg.getContent()) ;
						  message.setConversationId(msg.getConversationId());
						  System.out.println("["+avatarOwner+":SubRequest Message to "+supplier+"]: "+message.getContent()+", conversation: "+message.getConversationId());
*/
						  
						  
						  //TBD: RECEPTION !!!!! URGENT
						  //cptTasksExecuted++;
						  //System.out.println("		[EXECUTION TASK: ASK FRIEND] ["+name+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 
					}
					break;
				}
			}
			
			
			return res;
		}
	
	
	
	//Treat the execution requests
	public ACLMessage ServiceExecution (ACLMessage msg){
		
		ACLMessage message = null ;
		
		String sender=msg.getSender().getLocalName();
		String task=msg.getContent().split("&")[0];
		String taskLabel=msg.getContent().split("&")[1];
		//TBD: MANAGE TO EXECUTE IT
		//Test if he has the service 
		//TBD: Test from the strct data
		//TBD: Raccourcir IsableFirnd ac le ExtractServ
		//if (IsAbleTaskFriend(taskLabel)){
		
		//System.out.println("			OPERATIONS MANAGER  ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 


		//Search it
		for (int i=0; i<servicesList.size(); i++){

			// System.out.println("		COMPARAISON   "+ ExtractServiceFromLabel(taskLabel).split("&")[0]+"  and  "+servicesList.get(i).getName()+"  "+servicesList.size());
			//NB: ExtractServiceFromLabel returns : ServiceX&LabelX&QosX
			if (servicesList.get(i).getLabel().equals(taskLabel)){
				
				String supplier=servicesList.get(i).getSupplier();
				//Test if he is the supplier of this service or if it's a friend
				if(supplier.equals(avatarOwner)){
					
					String out=servicesList.get(i).getServiceOp().getOutputMessage();
					if (servicesList.get(i).getServiceOp().getMethode().equals("GET")){
						
						message= new ACLMessage(ACLMessage.INFORM);
						message.addReceiver(new AID(sender,AID.ISLOCALNAME));
						message.setContent(out) ;
						message.setConversationId(msg.getConversationId());
						System.out.println("["+avatarOwner+":Inform Message to "+sender+"]: "+message.getContent()+", conversation: "+message.getConversationId());

						cptTasksExecuted++;
						System.out.println("		[EXECUTION TASK] ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 
						
					}
					
					//TBD: Treat the PUT messages !!!
					else if (servicesList.get(i).getServiceOp().getMethode().equals("PUT")){
						String in=servicesList.get(i).getServiceOp().getInputMessage();
					}
					
					
				}
				//Ask this friend
				else{
					//if(taskLabel.equals("Label13")){
						System.out.println("		ASK FRIEND");
				    //} 
					  //TBD URGENT 
					  message= new ACLMessage(ACLMessage.QUERY_IF);						
					  message.addReceiver(new AID(supplier,AID.ISLOCALNAME));
					  message.setContent(msg.getContent()) ;
					  message.setConversationId(msg.getConversationId());
					  System.out.println("["+avatarOwner+":SubRequest Message to "+supplier+"]: "+message.getContent()+", conversation: "+message.getConversationId());

					  
					  
					  //TBD: RECEPTION !!!!! URGENT
					  //cptTasksExecuted++;
					  //System.out.println("		[EXECUTION TASK: ASK FRIEND] ["+name+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 
				}
				//break;
			}
		}
		
		return message;

		/*
		ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF) ;
		message.addReceiver(new AID(supplier,AID.ISLOCALNAME));
		message.setContent(msg.getContent()) ;
		message.setConversationId(msg.getConversationId());
		*/
	}
	
	
	public void ExecuteOwnService(String task, String taskLabel){
	
		//Search it
		for (int i=0; i<servicesList.size(); i++){

			// System.out.println("		COMPARAISON   "+ ExtractServiceFromLabel(taskLabel).split("&")[0]+"  and  "+servicesList.get(i).getName()+"  "+servicesList.size());
			//NB: ExtractServiceFromLabel returns : ServiceX&LabelX&QosX
			if (servicesList.get(i).getLabel().equals(taskLabel)){
				
				String supplier=servicesList.get(i).getSupplier();
				//Test if he is the supplier of this service or if it's a friend
				if(supplier.equals(avatarOwner)){
					
					String out=servicesList.get(i).getServiceOp().getOutputMessage();
					if (servicesList.get(i).getServiceOp().getMethode().equals("GET")){
						
						cptTasksExecuted++;
						//System.out.println("		[EXECUTION TASK] ["+avatarOwner+"] "+task+", for "+sender+"  tasksExec="+cptTasksExecuted); 
						  System.out.println("		[EXECUTION TASK ITSELF] ["+avatarOwner+"] "+task+",  tasksExec="+cptTasksExecuted); 

						
					}
					
					//TBD: Treat the PUT messages !!!
					else if (servicesList.get(i).getServiceOp().getMethode().equals("PUT")){
						String in=servicesList.get(i).getServiceOp().getInputMessage();
					}
					
					
				}

			}
		}
	}
	
	

	//Getteurs&Setteurs:
	public void addService(Service s){
		servicesList.add(s);
	}
	public  ArrayList <Service> getServices (){
		return servicesList;
	}
	
	//void Showservices
	/*
	if (taskLabel.equals("Label13")){
		  System.out.println("		SERVICES OF   "+name);
		  for (int c=0; c<servicesList.size();c++){
			  System.out.println("			"+servicesList.get(c).getName()+"   "+servicesList.get(c).getSupplier());
		  }

	}*/
	
	//Get an element from XML
	public String getXmlElement(String xml, String element){
		String res = "notFound";
		res = xml.split(element)[1].split(element)[0];
		return res;
	}
	
	//Add information to XML 
	public String addXmlElement(String xml, String element, String value){
		return xml+"<"+element+">"+value+"</"+element+">";
	}
	
	public String getURLFriend(String friend){
		String res="friendNoFound";
		for (int i=0;i<socialNetworkList.size() ;i++){
			if (socialNetworkList.get(i).getName().equals(friend)){
				res=socialNetworkList.get(i).getURL();
			}	break;
		}
		return res;
	}
}
