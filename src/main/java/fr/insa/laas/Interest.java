package fr.insa.laas;

public class Interest {

	//Attributs
	private String interest;
	private double interestLevel;
	
	public Interest(String i, double iL){
		interest=i;
		interestLevel=iL;
	}
	
	public String getName(){
		return interest;
	}
	
	public double getLevel(){
		return interestLevel;
	}
}