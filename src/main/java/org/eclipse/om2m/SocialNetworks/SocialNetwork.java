package org.eclipse.om2m.SocialNetworks;


import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.om2m.Avatar.Interest;
import org.eclipse.om2m.Avatar.MetaAvatar;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import jade.lang.acl.ACLMessage;


public class SocialNetwork {

	private String avatarName;
	private MetaAvatar metaAvatar;
	//Social Network
	private ArrayList <MetaAvatar> socialNetwork = new ArrayList <MetaAvatar>() ; 
	private ArrayList <String> InteretsTasksList = new ArrayList <String>();			 //The interets of the tasks to do, we need a delegate for each one
	private ArrayList <Interest> interestsList = new ArrayList <Interest> () ;			 //Used to iterate and to get the level interest easily for each task
	private ArrayList <String> interestsDelegates =  new ArrayList <String>(); 			 //Contains triples "InterestX/AvatarX/SocialDist"

	
	
	public void addAvatar (MetaAvatar avatar){
		//TBD: URGENT TEST IF ALLREADY EXIST !!!!
		socialNetwork.add(avatar) ;
	}
	
	public  ArrayList <MetaAvatar> getSocialNetwork (){
		return socialNetwork ;
	}
	
	
	public SocialNetwork(MetaAvatar metaAvatar, ArrayList <String> iTL){
		avatarName=metaAvatar.getName();
		InteretsTasksList=iTL;
		interestsList=metaAvatar.getInterestsList();
	}
	
	public void SocialNetworkUpdate(String xml, MetaAvatar metaAvatar, ArrayList <String> iTL){

		//System.out.println(avatarName.toUpperCase());
		try {
		    InputSource is = new InputSource(new StringReader(xml));
		    //System.out.println(xml);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());				
			NodeList nList = doc.getElementsByTagName("m2m:cin");		

			//Iterate the avatars contained in the repository
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);		
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());		
				if ((nNode.getNodeType() == Node.ELEMENT_NODE) && !(((Element) nNode).getAttribute("rn").equals(metaAvatar.getName()))) {

					Element eElement = (Element) nNode;
					//System.out.println("ELEMENT : " + eElement.getTextContent());
					Node n = eElement.getElementsByTagName("lbl").item(0);
					Element e = (Element) nNode;
					String data = e.getTextContent();
					String name = eElement.getAttribute("rn");
					String owner = data.split("<owner>")[1].split("</owner>")[0];
					String longitude  = data.split("<longitude>")[1].split("</longitude>")[0];
					String latitude = data.split("<latitude>")[1].split("</latitude>")[0];
					String nbInterest = data.split("<nb_interest>")[1].split("</nb_interest>")[0];
					String url = data.split("<url>")[1].split("</url>")[0];

					//System.out.println("DATA of: "+metaAvatar.getName()+": "+owner+", "+longitude+", "+latitude+", "+nbInterest);
					//Iterates the interests of the friend Avatar to complete its interets list and vector 
					Map<String,Double> interestsV = new HashMap<String, Double>();	
					ArrayList <Interest> interestsL = new ArrayList <Interest> () ;			//Used to iterate

					for (int i=0; i<Integer.parseInt(nbInterest); i++){
						String interest=data.split("<interest>")[i+1].split("</interest>")[0];
						interestsV.put(interest.split("/")[0], Double.parseDouble(interest.split("/")[1]));
						interestsL.add(new Interest(interest.split("/")[0], Double.parseDouble(interest.split("/")[1])));
						//System.out.println("Interest n°"+(i+1)+": name= "+interest.split("/")[0]+", value= "+Double.parseDouble(interest.split("/")[1]));
					}

					//Social Distance calcul
					double socialDistance = SocialDistance(metaAvatar, interestsV,Double.parseDouble(latitude), Double.parseDouble(longitude), owner);
					//System.out.println("[SD from "+avatarName+"]: "+name+"= "+socialDistance);
					
					//Creation of a friend Meta Avatar
					MetaAvatar friend = new MetaAvatar(name, owner, Double.parseDouble(latitude), Double.parseDouble(longitude), interestsV, interestsL, socialDistance, url);
					//TBD: TEST IF WE KEEP IT
					//TBD: TEST IF HE'S ALREADY ON THE LIST !!
					addAvatar(friend);
					System.out.println("		[ADDING A FRIEND]"+avatarName+": add the avatar "+name);

					}
			}
			DetermineDelegate(metaAvatar);
			
		    } catch (Exception e) {
			e.printStackTrace();
		    }
	}
	
	//For each interest, determine the delegate friend which will take care of each task having this interest 
	public void DetermineDelegate(MetaAvatar metaAvatar){
				
		//Iterate its interest
		for (int i=0; i<InteretsTasksList.size(); i++){
			String interest=InteretsTasksList.get(i);
			Double minSD = 0.2 + 1.5*0.4 + GetInterestLevel(interest)*0.4;		//Its Own SD
			//WARNING!!! Can we suppose that as it has the interest itself, then it's its delegate ???

			//System.out.println("[DETERMINE DELEGATE]"+avatarName+":Own SD= "+minSD+" for "+interest);
			String delegate=avatarName;
			
			//Iterate all its friends
			for (int f=0; f<socialNetwork.size(); f++){

				if (socialNetwork.get(f).ContainsInterest(interest)!=-1.0){
					Double interestLevel= socialNetwork.get(f).ContainsInterest(interest);
					Double socialDistInt = SocialDistanceInterest(metaAvatar, socialNetwork.get(f),interestLevel);
					//System.out.println("[DETERMINE DELEGATE]"+avatarName+": Yes the friend "+socialNetwork.get(f).getName()+" has the interest "+interest+" with a lvl: "+interestLevel+" and a SD: "+socialDistInt);
					
					//Check if its friend has a better SD than its
					if (socialDistInt>minSD){
						minSD=socialDistInt;
						delegate=socialNetwork.get(f).getName();
					}		
				}
			}
			interestsDelegates.add(interest+"/"+delegate+"/"+minSD);
			//System.out.println("[ADD INTERESTS DELEGATES]"+avatarName+" add: "+delegate+" as delegate for "+interest);

		}
		//System.out.println("[SHOW INTERESTS DELEGATES]"+avatarName+": "+interestsDelegates.toString());
		
	}
	
	
	public String DetermineAnotherDelegate(MetaAvatar metaAvatar, String interest, String originalSender, String delegationFrom){
		
		Double minSD = -999999.0;		//Its Own SD
		//WARNING!!! Can we suppose that as it has the interest itself, then it's its delegate ???

		//System.out.println("[DETERMINE DELEGATE]"+avatarName+":Own SD= "+minSD+" for "+interest);
		String delegate="noOne";
		
		//Iterate all its friends
		for (int f=0; f<socialNetwork.size(); f++){
			//System.out.println("[DETERMINE ANOTHER DELEGATE] TEST WITH "+socialNetwork.get(f).getName());

			if (socialNetwork.get(f).ContainsInterest(interest)!=-1.0 && !socialNetwork.get(f).getName().equals(originalSender) && !socialNetwork.get(f).getName().equals(avatarName) && !socialNetwork.get(f).getName().equals(delegationFrom)){
				Double interestLevel= socialNetwork.get(f).ContainsInterest(interest);
				Double socialDistInt = SocialDistanceInterest(metaAvatar, socialNetwork.get(f),interestLevel);
				//System.out.println("[DETERMINE DELEGATE]"+avatarName+": Yes the friend "+socialNetwork.get(f).getName()+" has the interest "+interest+" with a lvl: "+interestLevel+" and a SD: "+socialDistInt);
				
				//Check if its friend has a better SD than its
				if (socialDistInt>minSD){
					minSD=socialDistInt;
					delegate=socialNetwork.get(f).getName();
				}		
			}
		}
		System.out.println("[DETERMINE DELEGATE]"+avatarName+": delegate="+delegate   +"  for "+interest);

		return delegate;
		
		
		
	}
	
	//Check if the avatar has a certain interest, if YES: Return the level, if FALSE: Return 99.0
	public Double GetInterestLevel(String interest){
		Double res=-99.0;
		for (int i=0; i<interestsList.size(); i++){
			if(interestsList.get(i).getName().equals(interest)){
				res=interestsList.get(i).getLevel();
			}
		}
		return res;
	}
	
	//Return the delegatesList
	public ArrayList <String> getDelegatesList(){
		return interestsDelegates;
	}
	
	//Get the delegate of a certain Interest
	public String getDelegate(String interest){
		String delegate=null;
		for (int i=0; i<interestsDelegates.size(); i++){
			if (interestsDelegates.get(i).contains(interest)){
				delegate = interestsDelegates.get(i).split("/")[1];
				//System.out.println("[GET DELEGATE]"+avatarName+": "+"the delegate of "+interest+" is "+delegate);
			}
		}
		return delegate;
	}
	
	//Show its friends names
	public void showSocialNetwork(){
		System.out.println("[SHOW SOC. NET.]" +avatarName+": ");
		for (int i=0; i<socialNetwork.size(); i++){
			System.out.println("Friend n°"+i+": "+socialNetwork.get(i).getName());
		}
	}
	
	
	public boolean ContainsFriend(String name){
		boolean res=false;
		for (int i=0; i<socialNetwork.size(); i++){
			if (socialNetwork.get(i).getName().equals(name)){
				res=true;
				break;
			}
		}
		return res;
	}
	
	public MetaAvatar getFriend(String name){
		MetaAvatar res=null;
		for (int i=0; i<socialNetwork.size(); i++){
			if (socialNetwork.get(i).getName().equals(name)){
				res=socialNetwork.get(i);
				break;
			}
		}
		return res;
	}
	
	
	/*	------------------------------------ 		Co-work Object Relationship (C-WOR) 		---------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/

	
    /**		
     * Calculates the cosine similarity for two given vectors.
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine similarity between the two vectors
     */
	
    public static Double CoWorkSimilarity(final Map<String, Double> leftVector, final Map<String, Double> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        final Set<String> intersection = getIntersection(leftVector, rightVector);
        //System.out.println("[intersection]"+intersection);

        final double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (final Double value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        //System.out.println("[dotProduct]"+dotProduct);

        double d2 = 0.0d;
        for (final Double value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;

        } else {
            cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
        }
        return cosineSimilarity;
    }

    /**		//Aux. method to calculate the cosine similarity
     * Returns a set with strings common to the two given maps.
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(final Map<String, Double> leftVector,
            final Map<String, Double> rightVector) {
        final Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    
    /**		//Aux. method to calculate the cosine similarity
     * Computes the dot product of two vectors. It ignores remaining elements. It means
     * that if a vector is longer than other, then a smaller part of it will be used to compute
     * the dot product.
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dot(final Map<String, Double> leftVector, final Map<String, Double> rightVector,
            final Set<String> intersection) {
        double dotProduct = 0;
        for (final String key : intersection) {
            
            dotProduct += leftVector.get(key) * rightVector.get(key);
            //System.out.println("[dot keys] "+leftVector.get(key)+" *  "+rightVector.get(key)+" = "+dotProduct);
        }
        
        return dotProduct;
    }
    

	/*	----------------------------------- 		Co-Location Objects Relationship (C-LOR)  		---------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	
    //distanceVolOiseau
    public static double CoLocSimilarity (double LatA,double LongA,double LatB,double LongB) {
    
    LatA = Math.toRadians(LatA);
    LongA = Math.toRadians(LongA);
    LatB = Math.toRadians(LatB);
    LongB = Math.toRadians(LongB);
    double d = 6371 * Math.acos(Math.cos(LatA) * Math.cos(LatB) * Math.cos(LongB - LongA)+Math.sin(LatA) * Math.sin(LatB));
     return d;
    }
    

	/*	---------------------------------- 		Ownership Object Relationship (OOR) 		------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
    
    public static int OwnershipSimilarity (String user1,String user2) {
        
         if (user1.equals(user2)) return 1;
         else return 0;
   
        }  

	/*	------------------------------------------ 		SOCIAL DISTANCE 		-----------------------------------------------------	*/
	/*																																	*/
	/*	-----------------------------------------------------------------------------------------------------------------------------	*/
	   
  public static double SocialDistance(MetaAvatar metaAvatar, Map<String, Double> InterestsVectorB,double LatB, double LongB, String userB) {
  
	  	//System.out.println("Vect of A1: "+metaAvatar.getInterestsVector().toString()+", and its friend's: "+InterestsVectorB.toString());
        double coWorkDistance=SocialNetwork.CoWorkSimilarity(InterestsVectorB,metaAvatar.getInterestsVector());
        int ownerDistance=SocialNetwork.OwnershipSimilarity(userB, metaAvatar.getOwner());
        double coLocDistance=SocialNetwork.CoLocSimilarity(metaAvatar.getLatitude(),metaAvatar.getLongitude(),LatB,LongB);
        double socialDistance = 0.4*coWorkDistance+ 0.4*(1/coLocDistance)+ 0.2*ownerDistance;
        //System.out.println("[SocialDist]From "+metaAvatar.getName()+" CWD= "+coWorkDistance+", OD= "+ownerDistance+", CLD= "+(1/coLocDistance)+" ==> SD= "+socialDistance);
        return socialDistance;
  }
  
  //Social Distance relative to a interest, the same as the normal one, but concerning the Cowork Distance, we just use the level of interest (which is also between 0 and 1) 
  public static double SocialDistanceInterest(MetaAvatar metaAvatar, MetaAvatar metaFriend, double interestLevel) {
	  
	  //System.out.println("Vect of A1: "+metaAvatar.getInterestsVector().toString()+", and its friend's: "+InterestsVectorB.toString());
      int ownerDistance=SocialNetwork.OwnershipSimilarity(metaFriend.getOwner(), metaAvatar.getOwner());
      double coLocDistance=SocialNetwork.CoLocSimilarity(metaAvatar.getLatitude(),metaAvatar.getLongitude(),metaFriend.getLatitude(),metaFriend.getLongitude());
      double socialDistance = 0.4*interestLevel+ 0.4*(1/coLocDistance)+ 0.2*ownerDistance;
      //System.out.println("[SocialDistINTEREST]"+metaAvatar.getName()+" CWD= "+interestLevel+", OD= "+ownerDistance+", CLD= "+(1/coLocDistance)+" ==> SD= "+socialDistance);
      return socialDistance;
}
  
    
    
    
}