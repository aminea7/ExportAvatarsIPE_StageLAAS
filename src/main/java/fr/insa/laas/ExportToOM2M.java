package fr.insa.laas;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.om2m.OM2M.Client;
import org.eclipse.om2m.OM2M.ClientInterface;
import org.eclipse.om2m.OM2M.Mapper;
import org.eclipse.om2m.OM2M.MapperInterface;
import org.eclipse.om2m.commons.resource.ContentInstance;

public class ExportToOM2M {
	
	private static ClientInterface client = new Client();
	private static MapperInterface mapper = new Mapper();
	private final static String ORIGINATOR = "admin:admin";

	
	public static void ExportMetaData(String avatarOntology, String urlAvatar, ArrayList<String> repoNameList){
		
		//The model containing the data/ontology
		Model modelData = ModelFactory.createDefaultModel();
        modelData.read(avatarOntology);
		String url=urlAvatar;
		//Extract the necesseries Meta Data of the Avatar from the URL
		String name = ExtractData.ExtractName(modelData);
		String owner = ExtractData.ExtractOwner(modelData);
		String location = ExtractData.ExtractLocation(modelData);
		Double latitude = Double.parseDouble(location.split("&")[0]);
		Double longitude = Double.parseDouble(location.split("&")[1]);
		ArrayList <Interest> interestsList = ExtractData.ExtractInterests(modelData);
		//System.out.println("		[EXPORT META DATA OF AVATAR]: "+name+"		"+latitude+"  "+longitude);

		
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
		descriptor.getLabels().add("<url>"+url+"</url>");
		
		
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
	
	public static void main(String[] args) {
		//Export the Repositories
		//Repo1
		Repository.DeleteRepo("REPOSITORY1");
		Repository.createRepository("REPO1","REPOSITORY1");
		//Repo2
		Repository.DeleteRepo("REPOSITORY2");
		Repository.createRepository("REPO2","REPOSITORY2");
		//Repo3
		Repository.DeleteRepo("Repository3");
		//Repository.createRepository("Repo3","Repository3");
		//Repo4
		Repository.DeleteRepo("Repository4");
		//Repository.createRepository("Repo4","Repository4");
		
		/***				List Repositories						***/
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
		
		//Export Meta Data of the Avatars
		ExportMetaData("OntologyFiles/Avatar1.owl","http://localhost:"+9701+"/Avatar1/",Repo1);
		ExportMetaData("OntologyFiles/Avatar2.owl","http://localhost:"+9702+"/Avatar2/",Repo1);
		ExportMetaData("OntologyFiles/Avatar3.owl","http://localhost:"+9703+"/Avatar3/",Repo1);


	}

}
