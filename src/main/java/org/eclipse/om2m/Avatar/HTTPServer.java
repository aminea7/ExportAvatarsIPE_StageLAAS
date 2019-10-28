package org.eclipse.om2m.Avatar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.om2m.AvatarsLAAS.Controller.ControllerMessage;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HTTPServer {
	
	private String avatarName;
	private int serverPort;
	private ServicesManager servicesManager ;
	
	public HTTPServer (String a, int port, ServicesManager sM) throws IOException{
		
		System.out.println("			CREATION OF SERVER of "+a+", port= "+port);
		avatarName=a;
		serverPort=port;
		servicesManager=sM;
		
		HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/~/mn-cse/mn-name/"+avatarName, new MyHandler());
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
        	System.out.println("			SERVER HTTP, received: "+request+"  "+requestQuery+"   "+params.get("type"));
        	String res = "TBD";
        	
        	//Test the type
        	String test = params.get("type");
        	
        	switch (test){
        	
        	case "request":
        		res="<type>lol</type>";
        		res=res+"<sender>Mhd</sender>"; 
        		break;
        	
        	case "propagate":
        		
        		break;
        	
        	case "query":
        		String service = params.get("service");
        		res=servicesManager.ServiceExecution2(service);
        		//System.out.println("				QUERY MSG:  "+res2);
        		break;
        	}

    		
            
            t.sendResponseHeaders(200, res.length());
            OutputStream os = t.getResponseBody();
            os.write(res.getBytes("UTF-8"));
            os.close();
            
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
	
	
}
