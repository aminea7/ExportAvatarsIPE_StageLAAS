package fr.insa.laas;

import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public class ExtractData {

	//Get its name
	public static String ExtractName(Model modelData){ 
		String name = null;
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
	    return name;
	}
	
	//Get its owner name
	public static String ExtractOwner(Model modelData){ 
		String owner=null;
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
	    return owner;
	}
	
	//Get its Location
	public static String ExtractLocation(Model modelData){ 
		Double longitude = null;
		Double latitude = null;
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
	    return latitude+"&"+longitude;
	}
	
	//Get all its interests from the semantic data
	public static ArrayList <Interest>  ExtractInterests(Model modelData){ 
		
		ArrayList <Interest> interestsList = new ArrayList();
		
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
	    	    }
				//System.out.println("[EXTRACTINTERETS] "+name+", list size: "+interestsList.size());
			    return interestsList;
	}
	
}
