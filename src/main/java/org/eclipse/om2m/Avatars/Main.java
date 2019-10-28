package org.eclipse.om2m.Avatars;

import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.om2m.Avatar.Avatar;
import org.eclipse.om2m.Avatar.ContainerJADE;
import org.eclipse.om2m.Avatar.MainContainer;
import org.eclipse.om2m.OM2M.Client;
import org.eclipse.om2m.OM2M.ClientInterface;
import org.eclipse.om2m.OM2M.Mapper;
import org.eclipse.om2m.OM2M.MapperInterface;

public class Main {

	/** Point of access of the http server part */
	private static final String SERVER_POA = "http://localhost:1400/monitor";
	private static final String ORIGINATOR = "admin:admin";
	private static final String POA = "Avatars_poa";

	private static ClientInterface client = new Client();
	private static MapperInterface mapper = new Mapper();

	//private static final String REPOSITORY1 = "http://localhost:8080/~/mn-cse/mn-name/Repository1";
	
    //static Repository repository = new Repository();

	public static void main(String[] args) throws ControllerException {

		//TBD: Remove from here + launch from Activator
		//Repo1
		Repository.DeleteRepo("REPOSITORY1");
		for (int w=0;w<1000000;w++);
		Repository.createRepository("REPO1","REPOSITORY1");
		
		//Repo2
		Repository.DeleteRepo("REPOSITORY2");
		for (int w=0;w<1000000;w++);
		Repository.createRepository("REPO2","REPOSITORY2");
		
		//Repo3
		Repository.DeleteRepo("Repository3");
		//Repository.createRepository("Repo3","Repository3");
		
		//Repo4
		Repository.DeleteRepo("Repository4");
		//Repository.createRepository("Repo4","Repository4");
		
		MainContainer.MainContainerLunch();
		
		//TBD: Thread Pool
		
		ArrayList <String> Repo12 = new ArrayList <String> ();
		Repo12.add("REPOSITORY1");
		Repo12.add("REPOSITORY2");
		ArrayList <String> Repo23 = new ArrayList <String> ();
		Repo23.add("Repository2");
		Repo23.add("Repository3");
		ArrayList <String> Repo14 = new ArrayList <String> ();
		Repo14.add("Repository1");
		Repo14.add("Repository4");
		
		ArrayList <String> Repo1 = new ArrayList <String> ();
		Repo1.add("REPOSITORY1");
		ArrayList <String> Repo2 = new ArrayList <String> ();
		Repo2.add("REPOSITORY2");
		ArrayList <String> Repo3 = new ArrayList <String> ();
		Repo3.add("Repository3");
		ArrayList <String> Repo4 = new ArrayList <String> ();
		Repo4.add("Repository4");

		/*
		Thread t6 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar5
		    		jade.wrapper.AgentContainer cont6 = ContainerJADE.ContainerLunch("admin6");
		    		AgentController agentController6 = cont6.createNewAgent(
		    						"Avatar6",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar6.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo4 });
		    		// WARNING! Use a relative path
		    		agentController6.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t6.start(); 
		*/
		
		
		Thread t1 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar1
		    		jade.wrapper.AgentContainer cont1 = ContainerJADE.ContainerLunch("admin1");
		    		AgentController agentController1 = cont1.createNewAgent(
		    						"Avatar1",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar1.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo12 });
		    		// WARNING! Use a relative path
		    		agentController1.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t1.start();
		
		Thread t3 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar3
		    		jade.wrapper.AgentContainer cont3 = ContainerJADE.ContainerLunch("admin3");
		    		AgentController agentController3 = cont3.createNewAgent(
		    						"Avatar3",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar3.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo1 });
		    		// WARNING! Use a relative path
		    		agentController3.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t3.start();
		
		Thread t2 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar2
		    		jade.wrapper.AgentContainer cont2 = ContainerJADE.ContainerLunch("admin2");
		    		AgentController agentController2 = cont2.createNewAgent(
		    						"Avatar2",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar2.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo1 });
		    		// WARNING! Use a relative path
		    		agentController2.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t2.start();
		
		/*
		Thread t4 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar4
		    		jade.wrapper.AgentContainer cont4 = ContainerJADE.ContainerLunch("admin4");
		    		AgentController agentController4 = cont4.createNewAgent(
		    						"Avatar4",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar4.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo23 });
		    		// WARNING! Use a relative path
		    		agentController4.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t4.start();
		
		
		Thread t5 = new Thread() {
		    public void run() {
		        try {
		        	// Avatar5
		    		jade.wrapper.AgentContainer cont5 = ContainerJADE.ContainerLunch("admin5");
		    		AgentController agentController5 = cont5.createNewAgent(
		    						"Avatar5",
		    						Avatar.class.getName(),
		    						new Object[] {
		    								"/home/a7qlf/Documents/CHARBON/StageAvatars/OM2M_2019/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar5.owl",
		    								"C:\\Users\\kkhadir\\Documents\\Ontology\\rulesCam.txt",
		    								Repo3 });
		    		// WARNING! Use a relative path
		    		agentController5.start();
		        } catch(StaleProxyException v) {
		            System.out.println(v);
		        }
		    }  
		};
		t5.start(); 
		*/


		// OntologyGenerator();

		// Activator.start();

	}


	public static void OntologyGenerator() {
		/*
		 * String ontologyBase= (String) (
		 * "/home/a7qlf/Documents/CHARBON/StageAvatars/ProjetStageAvatar/om2m/org.eclipse.om2m.Avatars/OntologyFiles/OntologyBase.owl"
		 * ); Model modelData = ModelFactory.createDefaultModel();
		 * modelData.read(ontologyBase);
		 * 
		 * List<String> lines = Arrays.asList(modelData.toString()); Path file =
		 * Paths.get(
		 * "/home/a7qlf/Documents/CHARBON/StageAvatars/ProjetStageAvatar/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar77.owl"
		 * ); try { Files.write(file, lines, StandardCharsets.UTF_8); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		try (FileReader fr = new FileReader(
				"/home/a7qlf/Documents/CHARBON/StageAvatars/ProjetStageAvatar/om2m/org.eclipse.om2m.Avatars/OntologyFiles/OntologyBase.owl");
				FileWriter fw = new FileWriter(
						"/home/a7qlf/Documents/CHARBON/StageAvatars/ProjetStageAvatar/om2m/org.eclipse.om2m.Avatars/OntologyFiles/Avatar77.owl")) {
			int c = fr.read();
			while (c != -1) {
				fw.write(c);
				c = fr.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
