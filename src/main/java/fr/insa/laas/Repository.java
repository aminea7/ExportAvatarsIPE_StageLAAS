package fr.insa.laas;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.OM2M.Client;
import org.eclipse.om2m.OM2M.ClientInterface;
import org.eclipse.om2m.OM2M.Mapper;
import org.eclipse.om2m.OM2M.MapperInterface;
import org.eclipse.om2m.OM2M.Response;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;

public class Repository {
	
	/** Point of access of the http server part */
	private static final String SERVER_POA = "http://localhost:1400/monitor";
	private static final String ORIGINATOR = "admin:admin";
	private static final String POA = "Avatars_poa";

	private static ClientInterface client = new Client();
	private static MapperInterface mapper = new Mapper();

    /** Logger */
    private static Log logger = LogFactory.getLog(Repository.class);
    
	// Create the resource Repository at the addr:
	// http://localhost:8080/~/mn-cse/mn-name
	// [WARNING: To Be Executed One time]
	public static void createRepository(String AppID, String name) {
		
		//logger.info("Start CreateRepo");
		
		// OM2M
		// Create the Application resource
		AE ae = new AE();
		ae.setAppID(AppID);
		ae.setRequestReachability(true);
		//ae.getPointOfAccess().add(POA);
		ae.getPointOfAccess().add("8080");
		ae.setName(name);
		ae.getLabels().add("Type/Repository");
		ae.getLabels().add("Location/Region1");
		ae.getLabels().add("Owner/MuhammadAmine");

		//logger.info("Will try to create the ress.");

		try {
			// Creating the AE representing the device
			Response resp = client.create("http://localhost:8080/~/mn-cse/mn-name", mapper.marshal(ae), ORIGINATOR, "2");
			//logger.info(ae.toString());
			if (resp.getStatusCode() != 201) {
				System.out.println("Problem on AE creation (or conflict): " + resp.getRepresentation());
				return;
			}

			// Create the DESCRIPTOR container
			Container cnt = new Container();
			cnt.setName(name+"_DESCRIPTOR");	//RepositoryX_DESCRIPTOR
			client.create("http://localhost:8080/~/mn-cse/mn-name/"+name, mapper.marshal(cnt), ORIGINATOR, "3");

			// Create the DATA container
			Container cnt2 = new Container();
			cnt2.setName(name+"_DATA");
			client.create("http://localhost:8080/~/mn-cse/mn-name/"+name, mapper.marshal(cnt2), ORIGINATOR, "3");

			//System.out.println("		[REPO CREATED]");
			} catch (Exception e) {
			logger.info("EXCEPTION ERROR FROM REPO");
			System.err.println("Error on creating resources of the repository");
			e.printStackTrace();
			}

		}

		public static void InitializeRepositoryData() {
			/*try {
				client.delete(REPOSITORY1_DATA, ORIGINATOR);
				try {
					// Create the DATA container
					Container cnt2 = new Container();
					cnt2.setName("Repository1_DATA");
					cnt2.getLabels().add("<labelname>test<labelname>");
					client.create(
							"http://localhost:8080/~/mn-cse/mn-name/Repository1",
							mapper.marshal(cnt2), ORIGINATOR, "3");
				} catch (IOException e) {
					System.err.println("Error on creating resources Repository1_DATA");
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		}
	
		public static void DeleteRepo(String REPOSITORYX) {
			try {
				client.delete("http://localhost:8080/~/mn-cse/mn-name/"+REPOSITORYX, ORIGINATOR);
				System.out.println("		[REPO DELETED ]"+REPOSITORYX);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("		[REPO DELETED ERROR]");
			}
		}

}