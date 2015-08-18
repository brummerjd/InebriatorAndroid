package my.bigpackage.inebriatorapp;

import java.io.Serializable;

import android.text.Html;

public class Ingredient implements Serializable {
	
	private static final String ONE_FOURTH = Html.fromHtml("&#188;").toString();
	private static final String ONE_HALF = Html.fromHtml("&#189;").toString();
	private static final String THREE_FOURTHS = Html.fromHtml("&#190;").toString();
	
	private String Name = "";
	private double NumberOfShots = 0;
	
	public Ingredient(String name, double numberOfShots) {
		this.setName(name);
		this.setNumberOfShots(numberOfShots);
	}
	
	public void setName(String name) {
		this.Name = name;
	}
	
	public void setNumberOfShots(double num) {
		this.NumberOfShots = num;
	}
	
	public String getName() { return this.Name; }
	public double getNumberOfShots() { return this.NumberOfShots; }
	
	public static String convertForDisplay(double amt)
	{
		if (amt == 0) { return "0"; }
		
		String display = "";
		
		if (amt >= 1) { display += (int)Math.floor(amt); }
		
		if ((amt-0.25) % 1 == 0) { display += ONE_FOURTH; }
		else if ((amt-0.5) % 1 == 0) { display += ONE_HALF; }
		else if ((amt-0.75) % 1 == 0) { display += THREE_FOURTHS; }
		
		return display;
	}
}
