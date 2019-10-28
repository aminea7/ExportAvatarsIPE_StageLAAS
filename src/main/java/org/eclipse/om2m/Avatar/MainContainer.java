package org.eclipse.om2m.Avatar;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;

public class MainContainer {

	public static void MainContainerLunch() {
		try {
			//Env. d'exec.
			Runtime rt = Runtime.instance() ;
			
			//Props
			Properties p = new ExtendedProperties() ;
			p.setProperty("gui", "false");			 //Interface
			ProfileImpl pc = new ProfileImpl(p);	 //Prop
			
			//MainContainer
			AgentContainer container= rt.createMainContainer(pc) ;
			container.start();
			//System.out.println("		[MAIN CONTAINER STARTED]");

		
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	//Démarrer le cont.
	}

	public static void main(String[] args) {
		
	
	}
}
