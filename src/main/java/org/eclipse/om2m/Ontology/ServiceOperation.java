package org.eclipse.om2m.Ontology;

public class ServiceOperation {

	private String name;
	private String methode;	//GET or SET
	private String inputMessage;
	private String outputMessage;
	
	
	public ServiceOperation(String n, String m, String im, String om){
		name=n;
		methode=m;
		inputMessage=im;
		outputMessage=om;
	}
	
	
	public String getMethode(){
		return methode;
	}
	public String getOutputMessage(){
		return outputMessage;
	}
	public String getInputMessage(){
		return inputMessage;
	}
}
