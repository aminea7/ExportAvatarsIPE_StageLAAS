package org.eclipse.om2m.AvatarsLAAS.Controller;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;



//@RestController
public class ControllerAvatar implements ErrorController {

	String avatarName;
	

	public ControllerAvatar (String an){
		avatarName=an;
		//uri=l;
		System.out.println("			CREATION OF THE CONTROLLER OF  "+an);		
	}
	
   // @RequestMapping(value="/Produits", method=RequestMethod.GET)
	/*
    public String listeProduits() {
        return "Un exemple de produit";
    }*/
    
    //"http://localhost:9797/~/mn-cse/mn-name/Repository1/op=setOn&lampid=2"
    //@RequestMapping(value="/~/mn-cse/mn-name/Repository1/{ops}", method=RequestMethod.GET)
    @RequestMapping(value=("/~/mn-cse/mn-name/Avatar1"))

	//public String listeOps (@PathVariable String ops) {	
    public ControllerMessage TreatHTTPRequests (@RequestBody String body, @RequestParam("type") String type, @RequestParam("service") String service) throws ParserConfigurationException{
    
       System.out.println("			CONTROLLER; RECIVED:  "+"   "+body);	
       ControllerMessage message= new ControllerMessage();
       
       if (true){
    	   
    	   	//if ((ops.split("=")[1]).equals("Service3")){
	    	if(type.equals("request")){
	    		message = new ControllerMessage("AvatarX", "contentC", "conversation n1200", "0115742");
	    		System.out.println("						CONTROLLER: REQUEST YES   "+message.toString());	    		
	        	
	    	}
	    	else {
	    		//System.out.println("						CONTROLLER: REQUEST NO");
	        	//return new ControllerMessage();
	    	}
       }
       else{
    	   
       }
       return message;
	    	
    }


    
	@Override
	public String getErrorPath() {
		return "ERROR GETERRORPATH";
	}

}