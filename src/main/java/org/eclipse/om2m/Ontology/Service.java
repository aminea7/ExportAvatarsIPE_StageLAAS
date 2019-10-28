package org.eclipse.om2m.Ontology;

import java.util.ArrayList;

public class Service {

	private String name;
	private String label;
	private String supplier;
	//private ArrayList <ServiceOperation> serviceOpList= new ArrayList <ServiceOperation>();	//TBD: List of operations of the service
	//ServiceQos serviceQos;	//TBD
	private ServiceOperation serviceOp;
	
	public Service (String s, String n, String l){
		supplier=s;
		name=n;
		label=l;
	}
	
	public Service (String s, String n, String l, ServiceOperation  sol){
		supplier=s;
		name=n;
		label=l;
		serviceOp=sol;
	}
	
	public String getName(){
		return name;
	}
	public String getSupplier(){
		return supplier;
	}
	public String getLabel(){
		return label;
	}
	public ServiceOperation getServiceOp(){
		return serviceOp;
	}
}
