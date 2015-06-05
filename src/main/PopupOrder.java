package main;
import static main.ProvenanceMatrix_1_3.srcTaxonomy;
import static main.ProvenanceMatrix_1_3.trgTaxonomy;

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
	public int w = 180;
	public int h;
	public int itemH = 17;
	public Color cGray  = new Color(240,240,240);
	public static String[] items={"Random","Reading order", "Name", "Similarity", "Breadth-first listing", "Depth-first listing"}; 
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
			parent.fill(0,100);
			parent.stroke(0);
			parent.textSize(11);
			h=items.length*itemH+16;
			parent.fill(200);
			parent.stroke(0,100);
			parent.rect(x, y+20, w,h);
			// Max number of relations
			for (int i=0;i<items.length;i++){
				if (i==s){
					parent.noStroke();
					parent.fill(0);
					parent.rect(x+10,y+itemH*(i)+26,w-25,itemH);
					parent.fill(255,255,0);
				}
				else if (i==b){
					parent.fill(200,0,0);
				}
				else{
					parent.fill(0);
				}
				parent.textAlign(PApplet.LEFT);
				parent.text(items[i],x+20,y+itemH*(i+1)+22);  // 
			}	
			
			//if (s>=0 && items[s].equals("Similarity")) 
			//	slider.draw(x+110, y+itemH*4-14+25);
			
		}
		parent.fill(0,50);
		parent.noStroke();
		parent.rect(x,y,w1,20);
		parent.fill(0);
		parent.textAlign(PApplet.CENTER);
		parent.textSize(11);
		parent.text("Order by",x+w1/2,y+14);
		
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
				main.ProvenanceMatrix_1_3.stateAnimation=0;
				main.ProvenanceMatrix_1_3.check2.s =false;
				Taxonomy.orderByName();
			}	
			else if (items[s].equals("Similarity"))  {
				main.ProvenanceMatrix_1_3.stateAnimation=0;
				main.ProvenanceMatrix_1_3.check2.s =false;
				Taxonomy.orderBySimilarity();
			}
			else if (items[s].equals("Breadth-first listing"))  {
				main.ProvenanceMatrix_1_3.stateAnimation=0;
				main.ProvenanceMatrix_1_3.check2.s =false;
				
				srcTaxonomy.get(0).order=0;
				trgTaxonomy.get(0).order=0;
				countBFS1=1; 
				countBFS2=1; 
				Taxonomy.BFS1(0); // the first element is assumed as ROOT
				Taxonomy.BFS2(0); // the first element is assumed as ROOT
			}
			else if (items[s].equals("Depth-first listing"))  {
				main.ProvenanceMatrix_1_3.stateAnimation=0;
				main.ProvenanceMatrix_1_3.check2.s =false;
				
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
				if (x<=mX && mX<=x+w && y+itemH*i+22<=mY && mY<=y+itemH*(i+1)+23){
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