package main;
import static main.ProvenanceMatrix.srcTaxonomy;
import static main.ProvenanceMatrix.trgOntology;

import java.awt.Color;
import java.text.DecimalFormat;

import processing.core.PApplet;
import processing.core.PFont;

public class PopupOrder{
	public int b = -1;
	public static int s=-1;
	public PApplet parent;
	public float x = 800;
	public int y = 0;
	public int w1 = 98;
	public int w = 200;
	public int h;
	public int itemH = 19;
	public Color cGray  = new Color(240,240,240);
	public static String[] items={"Random","Reading order", "Name", "Similarity", "BFS", "DFS"}; 
	public Slider slider;
	public static int countBFS1=0; 
	public static int countBFS2=0; 
	public static int countDFS1=0; 
	public static int countDFS2=0; 
	
	public PopupOrder(PApplet parent_){
		parent = parent_;
		slider =  new Slider(parent_,5);
	}
	
	public void draw(float x_){
		x = x_;
		checkBrushing();
		if (b>=0){
			parent.fill(100);
			parent.stroke(0);
			parent.textSize(12);
			h=items.length*itemH+20;
			parent.fill(200);
			parent.stroke(0,150);
			parent.rect(x, y+23, w,h);
			// Max number of relations
			float max =-1;
			for (int j=0;j<main.ProvenanceMatrix.pairs.length;j++){
				float sqrt = PApplet.sqrt(main.ProvenanceMatrix.pairs[j].size());
				if (sqrt>max)
					max = sqrt;
			}
			for (int i=0;i<items.length;i++){
				if (i==s){
					parent.noStroke();
					parent.fill(0);
					parent.rect(x+10,y+itemH*(i)+5+25,w-25,itemH+1);
					parent.fill(255,255,0);
				}
				else if (i==b){
					parent.fill(200,0,0);
				}
				else{
					parent.fill(0);
				}
				parent.textAlign(PApplet.LEFT);
				parent.text(items[i],x+20,y+itemH*(i+1)+25);  // 
			}	
			
			if (s>=0 && items[s].equals("Similarity")) 
				slider.draw(x+110, y+itemH*4-14+25);
			
		}
		parent.fill(180);
		parent.noStroke();
		parent.rect(x,y,w1,23);
		parent.fill(0);
		parent.textAlign(PApplet.CENTER);
		parent.textSize(12);
		parent.text("Order by",x+w1/2,y+17);
		
	}
	
	 public void mouseClicked() {
		if (b<items.length){
			s = b;
			if (items[s].equals("Random")) { 
				Taxonomy.orderByRandom(parent);
			}	
			else if (items[s].equals("Reading order"))  {
				Taxonomy.orderByReading();
			}	
			else if (items[s].equals("Name"))  {
				main.ProvenanceMatrix.stateAnimation=0;
				main.ProvenanceMatrix.check2.s =false;
				Taxonomy.orderByName();
			}	
			else if (items[s].equals("Similarity"))  {
				main.ProvenanceMatrix.stateAnimation=0;
				main.ProvenanceMatrix.check2.s =false;
				Taxonomy.orderBySimilarity();
			}
			else if (items[s].equals("BFS"))  {
				main.ProvenanceMatrix.stateAnimation=0;
				main.ProvenanceMatrix.check2.s =false;
				
				srcTaxonomy.get(0).order=0;
				trgOntology.get(0).order=0;
				countBFS1=1; 
				countBFS2=1; 
				Taxonomy.BFS1(0); // the first element is assumed as ROOT
				Taxonomy.BFS2(0); // the first element is assumed as ROOT
			}
			else if (items[s].equals("DFS"))  {
				main.ProvenanceMatrix.stateAnimation=0;
				main.ProvenanceMatrix.check2.s =false;
				
				countDFS1=0; 
				countDFS2=0; 
				Taxonomy.DFS1(0); // the first element is assumed as ROOT
				Taxonomy.DFS2(0); // the first element is assumed as ROOT
			}
			
		}
	}
	 
	public void checkBrushing() {
		int mX = parent.mouseX;
		int mY = parent.mouseY;
		if (b==-1){
			if (x<mX && mX<x+w1 && y<=mY && mY<=itemH+5){
				b =100;
				return;
			}	
		}
		else{
			for (int i=0; i<items.length; i++){
				if (x<=mX && mX<=x+w && y+itemH*i+25<=mY && mY<=y+itemH*(i+1)+6+25){
					b =i;
					return;
				}	
			}
			if (x<=mX && mX<=x+w && y<=mY && mY<=y+h)
				return;
		}
		b =-1;
	}
	
	
	
}