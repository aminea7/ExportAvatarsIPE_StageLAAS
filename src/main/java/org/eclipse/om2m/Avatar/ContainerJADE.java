package org.eclipse.om2m.Avatar;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class ContainerJADE {
	
	static AgentContainer agentContainer = null ;
	static AgentController agentController = null ;
	

	public static AgentContainer ContainerLunch(String nameAgent) {
		
		try {
			//Env. d'exec.
			Runtime rt = Runtime.instance() ;
			//Props
			ProfileImpl pc = new ProfileImpl(false);	 //Prop ==> Non Main container
			pc.setParameter(ProfileImpl.MAIN_HOST, "localhost");	//Addr
			
			//Container
			AgentContainer ac= rt.createAgentContainer(pc) ;	//Container
			ac.start() ;
			agentContainer = ac ;	

			//AgentController
			//AgentController agentControl = ac.createNewAgent(nameAgent, "Avatars.Avatar", new Object [] {"DavinciCode"}); //Controller agent of the cont.
			//agentControl.start();
			//agentController = agentControl ;	
		
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return agentContainer;
	}
	
	public AgentController get_agentController() {
		return agentController ;
	}
	
	public static void main(String[] args) {
		
	}

}
