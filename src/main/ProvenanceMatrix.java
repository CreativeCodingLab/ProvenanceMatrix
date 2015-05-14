package main;
/*
 * DARPA project
 *
 * Copyright 2014 by Tuan Dang.
 *
 * The contents of this file are subject to the Mozilla Public License Version 2.0 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import edu.uic.ncdm.venn.Venn_Overview;
import processing.core.*;

public class ProvenanceMatrix extends PApplet {
	private static final long serialVersionUID = 1L;
	public int count = 0;
	public static int currentRelation = -1;
	public static int processing = 0;
	public String currentFile = "./level3RAS/1_RAF-Cascade.owl"; //@Amruta


	public static ButtonBrowse buttonBrowse;

	// Store the genes results
	public static ArrayList<String>[] pairs;
	public static ArrayList<String>[] ontologyMappings; // equivalent to pairs

	// Global data
	public static String[] minerNames;
	public static ArrayList<Taxonomy> srcTaxonomy = new ArrayList<Taxonomy>();
	public static ArrayList<Taxonomy> trgOntology = new ArrayList<Taxonomy>();


	//public static ArrayList<Integrator> iW;
	// Contains the location and size of each gene to display
	public float size=0;
	public static float marginX = 200;
	public static float marginY = 120;
	public static String message="";

	public ThreadLoader1 loader1=new ThreadLoader1(this);
	public Thread thread1=new Thread(loader1);

	public ThreadLoader3 loader3=new ThreadLoader3();
	public Thread thread3=new Thread(loader3);

	public ThreadLoader4 loader4=new ThreadLoader4(this);
	public Thread thread4=new Thread(loader4);

	// Venn
	public Venn_Overview vennOverview; 
	public int bX,bY;

	// Order genes
	public static PopupOrder popupOrder;
	public static CheckBox check1;
	public static CheckBox check2;
	public static CheckBox check3;

	// Grouping animation
	public static int stateAnimation =0;
	public static int bg =0;


	// Color of miner
	public static int[] mappingColorRelations; //@amruta 


	// Allow to draw 
	public static boolean isAllowedDrawing = false;
	public static int  ccc = 0; // count to draw progessing bar

	public PFont metaBold = loadFont("Arial-BoldMT-18.vlw");


	
	public static ArrayList[][] articulations;
	public HashMap<String,Integer> hashArticulations = new HashMap<String,Integer>();
	public String[] lineX;
	public String[] lineY;
	public String taxomX;
	public String taxomY;
	public static ArrayList[] a1;    // ArrayList parent -> children
	public static ArrayList[] a2;
	public static HashMap<String,Integer> hash1;   	// Hash of taxo name-index in array
	public static HashMap<String,Integer> hash2;	// Hash of taxo name-index in array
	
	String[] artStrings = {"Equals","Includes","is_included_in","Overlaps","Disjoint"}; 

	public static void main(String args[]){
		PApplet.main(new String[] { ProvenanceMatrix.class.getName() });
	}

	public void setup() {
		textFont(metaBold,14);
		size(1440, 900);
		//size(2000, 1200);
		if (frame != null) {
			frame.setResizable(true);
		}
		background(0);
		frameRate(12);
		curveTightness(0.7f); 
		smooth();

		

		

		//-----------------------------

		mappingColorRelations =  new int[5];	
		mappingColorRelations[0] = new Color(0,200,0).getRGB(); 
		mappingColorRelations[1] = new Color(0,0,255).getRGB(); 
		mappingColorRelations[2] = new Color(200,200,0).getRGB(); 
		mappingColorRelations[3] = new Color(200,0,0).getRGB();		
		mappingColorRelations[4] = new Color(0,0,0,60).getRGB();	

		//-----------------------------
		buttonBrowse = new ButtonBrowse(this);
		popupOrder  = new PopupOrder(this);
		check1 = new CheckBox(this, "Lensing");
		check2 = new CheckBox(this, "Show Disjoint");
		check3 = new CheckBox(this, "Highlighting groups");


		//VEN DIAGRAM
		vennOverview = new Venn_Overview(this);
		if (!currentFile.equals("")){
			thread1=new Thread(loader1);
			thread1.start();
		}

		
		
		// enable the mouse wheel, for zooming
		addMouseWheelListener(new java.awt.event.MouseWheelListener() {
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
				mouseWheel(evt.getWheelRotation());
			}
		});
	}	


	public void draw() {
		background(255);
		// Draw 
		try{
			// Print message
			if (processing<mappingColorRelations.length){
				ccc+=1;
				if (ccc>10000) ccc=0;
				this.fill(mappingColorRelations[processing],100+(ccc*10)%155);
				this.noStroke();
				this.arc(marginX,this.height-20, 30, 30, 0, PApplet.PI*2*(processing+1)/5);


				this.fill(mappingColorRelations[processing]);
				this.textSize(14);
				this.textAlign(PApplet.LEFT);
				this.text(message, marginX+20,this.height-14);
			}

			if (isAllowedDrawing){
				if (currentFile.equals("")){
					int ccc = this.frameCount*6%255;
					this.fill(ccc, 255-ccc,(ccc*3)%255);
					this.textAlign(PApplet.LEFT);
					this.textSize(20);
					this.text("Please select a BioPax input file", 300,250);
					float x6 =74;
					float y6 =25;
					this.stroke(ccc, 255-ccc,(ccc*3)%255);
					this.line(74,25,300,233);
					this.noStroke();
					this.triangle(x6, y6, x6+4, y6+13, x6+13, y6+4);
				}
				else{
					check1.draw(this.width-100, 30);
					check2.draw(this.width-100, 50);
					//	check3.draw(this.width-500, 48);
					
					drawMatrix();
					this.textSize(12);

					popupOrder.draw(this.width-198);

				}

			}
			buttonBrowse.draw();
			//popupRelation.draw(this.width-304);
		}
		catch (Exception e){
			System.out.println();
			System.out.println("*******************Catch ERROR*******************");
			e.printStackTrace();
			return;
		}
	}	

	public void drawMatrix() throws IOException {
		if (srcTaxonomy==null || srcTaxonomy.size()==0)
			return;
		else{
			int numColumns = srcTaxonomy.size(); 
			int numRows = trgOntology.size();
			int num = Math.max(numRows, numColumns);
			size = (this.height-marginY)/num;
			if (size>100)
				size=100;
		}
		drawGenes(200,150);
		
		// Draw color legend
		float y3 = 100;
		float x3 = this.width-150;
		
		this.textAlign(PApplet.LEFT);
		this.textSize(13);
		for (int i=0;i<artStrings.length;i++){
			y3 +=20;
			this.fill(mappingColorRelations[i]);
			this.noStroke();
			this.ellipse(x3,y3,17,17);
			if (i==4)
				this.fill(0,120);  // For disjoins
			this.text(artStrings[i],x3+13,y3+4);
		}
	}

	public void drawGenes(float mX, float mY) throws IOException {
		// Compute lensing
		//if (check1.s){
			bX = (int) ((mouseX-mX)/size);
			bY = (int) ((mouseY-mY)/size);
		//}
		//else{
	//		bX = -100;//srcOntology.size()+10;
	//		bY = -100;//srcOntology.size()+10;

//		}
		float lensingSize = PApplet.map(size, 0, 100, 20, 80);	

		int num = 5; // Number of items in one side of lensing
		
		if (check1.s){
			for (int i=0;i<srcTaxonomy.size();i++){
				int order = srcTaxonomy.get(i).order;
				if (bX-num<=order && order<=bX+num) {
					srcTaxonomy.get(i).iW.target(lensingSize);
					int num2 = order-(bX-num);
					if (bX-num>=0)
						setValue(srcTaxonomy.get(i).iX, mX +(bX-num)*size+num2*lensingSize);
					else
						setValue(srcTaxonomy.get(i).iX, mX +order*lensingSize);
				}	
				else{
					srcTaxonomy.get(i).iW.target(size);
					if (order<bX-num)
						setValue(srcTaxonomy.get(i).iX, mX +order*size);
					else if (order>bX+num){
						if (bX-num>=0)
							setValue(srcTaxonomy.get(i).iX, mX +(order-(num*2+1))*size+(num*2+1)*lensingSize);
						else{
							int num3= bX+num+1;
							if (num3>0)
								setValue(srcTaxonomy.get(i).iX, mX +(order-num3)*size+num3*lensingSize);
							else
								setValue(srcTaxonomy.get(i).iX, mX +order*size);
						}	
					}	 
				}	
			}
		}	
		else{
			float beginX = mX;
			for (int i=0;i<srcTaxonomy.size();i++){
				float ss = size;
				int order = srcTaxonomy.get(i).order;
				srcTaxonomy.get(i).iW.target(size);
				setValue(srcTaxonomy.get(i).iX, mX +order*size);
					
			}	
		}
		
		

		for (int j=0;j<trgOntology.size();j++){
			int order = trgOntology.get(j).order;
			if (bY-num<=order && order<=bY+num){
				trgOntology.get(j).iH.target(lensingSize);
				int num2 = order-(bY-num);
				if (bY-num>=0)
					setValue(trgOntology.get(j).iY, mY +(bY-num)*size+num2*lensingSize);
				else
					setValue(trgOntology.get(j).iY, mY +order*lensingSize);
			}	
			else{
				trgOntology.get(j).iH.target(size);
				if (order<bY-num)
					setValue(trgOntology.get(j).iY, mY +order*size);
				else if (order>bY+num){
					if (bY-num>=0)
						setValue(trgOntology.get(j).iY, mY +(order-(num*2+1))*size+(num*2+1)*lensingSize);
					else{
						int num3= bY+num+1;
						if (num3>0)
							setValue(trgOntology.get(j).iY, mY +(order-num3)*size+num3*lensingSize);
						else
							setValue(trgOntology.get(j).iY, mY +order*size);
					}	

				}	
			}	
		}

		//--------------------------------

		for (int i=0;i<srcTaxonomy.size();i++){			
			srcTaxonomy.get(i).iW.update();
			srcTaxonomy.get(i).iX.update();		
		}

		for (int i=0;i<trgOntology.size();i++){
			trgOntology.get(i).iH.update();
			trgOntology.get(i).iY.update();
		}


		//-----------------------------
		ArrayList<Integer> bList = new ArrayList<Integer>(); 
		if (!check1.s && (0<=bX && bX<srcTaxonomy.size() && this.mouseY<mY)){
			bList.add(bX);
			ArrayList<Integer> b = getParentSource(bX);
			for (int i=0;i<b.size();i++){
				bList.add(b.get(i));
			}
			for (int i=0;i<srcTaxonomy.size();i++){
				if (srcTaxonomy.get(i).parentIndex==bX)
					bList.add(i);				
			}
		}
		for (int i=0;i<srcTaxonomy.size();i++){
			float ww = srcTaxonomy.get(i).iW.value;
			float xx =  srcTaxonomy.get(i).iX.value;
			this.fill(0,255);
			if (ww>6){
				this.textSize(11);
				if (bList.size()>0){
					if (bList.contains(i)){
						this.fill(0,0,0);
					}
					else{
						this.fill(0,0,0,30);
					}
				}
				float al = -PApplet.PI/3;
				this.translate(xx+ww/2+5,mY-15);
				this.rotate(al);
				this.text(srcTaxonomy.get(i).name, 0,0); // text for each column @Amruta
				this.rotate(-al);
				this.translate(-(xx+ww/2+5), -(mY-15));
			}
		}
		for (int i=0;i<trgOntology.size();i++){
			float hh =trgOntology.get(i).iH.value;
			float yy =  trgOntology.get(i).iY.value;
			this.fill(0,255);
			if (hh>6){
				this.textSize(11);
				this.textAlign(PApplet.RIGHT);
				this.text(trgOntology.get(i).name, mX-15, yy+hh/2+5); //text for each row @Amruta 
			}
		}
		if (bX<0 || srcTaxonomy.size()>bX){
			this.textSize(13);
			this.textAlign(PApplet.CENTER);
			this.text(taxomX, 500, 40);
		}
			this.textSize(13);
			this.textAlign(PApplet.CENTER);
			float al = -PApplet.PI/2;
			this.translate(60,300);
			this.rotate(al);
			this.text(taxomY, 0,0); // text for each column @Amruta
			this.rotate(-al);
			this.translate(-(60), -(300));
	
		
		// Hierarchy
		float arcRate = 0.7f;
		for (int i=0;i<srcTaxonomy.size();i++){
			if (a1[i]==null) 
				continue;
			float w1 =  srcTaxonomy.get(i).iW.value;
			float x1 =  srcTaxonomy.get(i).iX.value+w1*0.6f;
			
			for (int j=0; j<a1[i].size();j++){
				int indexChild = (Integer) a1[i].get(j);
				float w2 =  srcTaxonomy.get(indexChild).iW.value;
				float x2 =  srcTaxonomy.get(indexChild).iX.value+w2*0.5f;
				this.noFill();
				this.stroke(0,0,0);
				float r = PApplet.abs(x2-x1);
				
				if (bList.size()>0){
					if (!bList.contains(i) || !bList.contains(indexChild))
					this.stroke(0,20);
				}
				
				if (0<=bX && bX<srcTaxonomy.size()){
					if (w1>6 || w2>6){
						this.strokeWeight(0.6f);
						this.arc((x1+x2)/2,mY-10,r,r*arcRate, 0,PApplet.PI);
					}
					else{
						this.strokeWeight(0.05f);
						this.arc((x1+x2)/2,mY-10,r,r*arcRate,-PApplet.PI, 0);
					}	
				}
				else{
					this.strokeWeight(0.4f);
					if (w1>6 || w2>6)
						this.arc((x1+x2)/2,mY-10,r,r*arcRate, 0,PApplet.PI);
					else
						this.arc((x1+x2)/2,mY-10,r,r*arcRate,-PApplet.PI, 0);
				}
				
				this.noStroke();
				float v = PApplet.map(w2, 0, 25, 10, 255);
				if (v>255)
					v=255;
				this.fill(0,v);
				this.triangle(x2, mY-15, x2-2.5f, mY-10, x2+2.5f, mY-10);
			}
		}
		
		for (int i=0;i<trgOntology.size();i++){
			if (a2[i]==null) 
				continue;
			float h1 =  trgOntology.get(i).iH.value;
			float y1 =  trgOntology.get(i).iY.value+h1*0.6f;
			for (int j=0; j<a2[i].size();j++){
				int indexChild = (Integer) a2[i].get(j);
				float h2 =  trgOntology.get(indexChild).iH.value;
				float y2 =  trgOntology.get(indexChild).iY.value+h2*0.5f;
				
				this.noFill();
				this.stroke(0,0,0);
				float r = PApplet.abs(y2-y1);
				
				if (0<=bY && bY<trgOntology.size()){
					if (h1>6 || h2>6){
						this.strokeWeight(0.6f);
						this.arc(mX-10, (y1+y2)/2,r*arcRate,r, -PApplet.PI/2, PApplet.PI/2);
					}	
					else{
						this.strokeWeight(0.05f);
						this.arc(mX-10, (y1+y2)/2,r*arcRate,r, PApplet.PI/2, 3*PApplet.PI/2);
					}	
				}
				else{
					this.strokeWeight(0.4f);
					if (h1>6 || h2>6)
						this.arc(mX-10, (y1+y2)/2,r*arcRate,r, -PApplet.PI/2, PApplet.PI/2);
					else
						this.arc(mX-10, (y1+y2)/2,r*arcRate,r, PApplet.PI/2, 3*PApplet.PI/2);
					
				}
				
				this.noStroke();
				float v = PApplet.map(h2, 0, 25, 10, 255);
				if (v>255)
					v=255;
				this.fill(0,v);
				this.triangle(mX-15, y2, mX-10, y2-2.5f, mX-10, y2+2.5f);
			}
		}
			
			
		//Circular sectors
		for (int i=0;i<trgOntology.size();i++){
			// Check if this is grouping
			float yy =  trgOntology.get(i).iY.value;
			float hh = trgOntology.get(i).iH.value;
			int count = 0;
			int numberOfSector = 4;
			if (check2.s)
				numberOfSector =5;
			for (int j=0;j<srcTaxonomy.size();j++){
				float xx =  srcTaxonomy.get(j).iX.value;
				float ww =srcTaxonomy.get(j).iW.value;

				if (articulations[j][i]==null) continue;
				
				for (int i2=0;i2<articulations[j][i].size();i2++){
					int localRelationIndex = (Integer) articulations[j][i].get(i2);
				//	System.out.println(i2+"	matcherValues[i2]="+matcherValues[i2]);
					this.noStroke();
					this.fill(mappingColorRelations[localRelationIndex]);
					//float alpha = PApplet.PI*2/matcherValues.length;
					float alpha = PApplet.PI*2/numberOfSector;
					if (localRelationIndex==4){
						 if (check2.s)
							this.arc(xx+ww/2,yy+hh/2,PApplet.min(ww,hh), PApplet.min(ww,hh), localRelationIndex*alpha, (localRelationIndex+1)*alpha);
					}else	
						this.arc(xx+ww/2,yy+hh/2,PApplet.min(ww,hh)+2, PApplet.min(ww,hh)+2, localRelationIndex*alpha, (localRelationIndex+1)*alpha);
					count++;
				}
			}
		}
	}	


	public void setValue(Integrator inter, float value) {
		inter.target(value);
	}

	public void mousePressed() {
		if (popupOrder.b>=0){
			popupOrder.slider.checkSelectedSlider1();
		}
	}
	public void mouseReleased() {
		if (popupOrder.b>=0){
			popupOrder.slider.checkSelectedSlider2();
		}
	}
	public void mouseDragged() {
		if (popupOrder.b>=0){
			popupOrder.slider.checkSelectedSlider3();
		}

	}

	public void mouseMoved() {

	}

	public void mouseClicked() {
		if (buttonBrowse.b>=0){
			thread4=new Thread(loader4);
			thread4.start();
		}

		if (check1.b){
			check1.mouseClicked();
		}
		else if (check2.b){
			check2.mouseClicked();
		}
		else if (check3.b){
			check3.mouseClicked();
		}
		else if (popupOrder.b>=0){
			popupOrder.mouseClicked();
		}
		else if (vennOverview!=null){
			vennOverview.mouseClicked();
			//update();
		}

	}


	public String loadFile (Frame f, String title, String defDir, String fileType) {
		FileDialog fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFile(fileType);
		fd.setDirectory(defDir);
		fd.setLocation(50, 50);
		fd.show();
		String path = fd.getDirectory()+fd.getFile();
		return path;
	}


	public void keyPressed() {
		
	}


	// Thread for Venn Diagram
	class ThreadLoader1 implements Runnable {
		PApplet parent;
		public ThreadLoader1(PApplet parent_) {
			parent = parent_;
		}

		@SuppressWarnings("unchecked")
		public void run() {
			// Initialize articulation has
			hashArticulations.put("=",0);
			hashArticulations.put(">",1);
			hashArticulations.put("<",2);
			hashArticulations.put("><",3);
			hashArticulations.put("!",4);
			hashArticulations.put("|",4);
			hashArticulations.put("equals",0);
			hashArticulations.put("includes",1);
			hashArticulations.put("is_included_in",2);
			hashArticulations.put("overlaps",3);
			hashArticulations.put("disjoint",4);
			
			
			lineX = parent.loadStrings("./NicoData/converted/srcChild_min2015.txt");
			lineY = parent.loadStrings("./NicoData/converted/trgChild_min1982.txt");
			
			/// Taxom mappings
			/*
			String[] lines = parent.loadStrings("./NicoData/minyomerus-underspecified.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/minyomerus-underspecified_mir.csv");  // Articulation
			int year1 = 2015;
			int year2 = 1982;
			int unUsedCollumnAtTheEnd = 1;*/
			
			
			/*
			String[] lines = parent.loadStrings("./NicoData/large/prim-uc-entire.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/large/primates-all-mir.csv");
			int year1 = 2005;
			int year2 = 1993;
			int unUsedCollumnAtTheEnd = 0;*/
			
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/primates-large-alignment.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/primates-large-alignment_mir.csv");
			int year1 = 2005;
			int year2 = 1993;
			int unUsedCollumnAtTheEnd = 1;*/
			
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/weevils-merge-concepts.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/weevils-merge-concepts_mir.csv");
			int year1 = 2000;
			int year2 = 1995;
			int unUsedCollumnAtTheEnd = 1;*/
			
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/perelleschus-multiple-worlds.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/perelleschus-multiple-worlds_mir.csv");
			int year1 = 1954;
			int year2 = 1936;
			int unUsedCollumnAtTheEnd = 1;*/
			
			
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched_mir.csv");
			int year1 = 2010;
			int year2 = 1968;
			int unUsedCollumnAtTheEnd = 1;*/
			
			// 160 possible worlds
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched2.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched2_mir.csv");
			int year1 = 2010;
			int year2 = 1968;
			int unUsedCollumnAtTheEnd = 1;
			
			/* 
			// 20x10 maxtrix
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2015_1982_phylo.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/2015_1982_phylo_mir.csv");
			int year1 = 2015;
			int year2 = 1982;
			int unUsedCollumnAtTheEnd = 1;
			*/
			
			// 30x20 
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/Lorisiformes-All.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/Lorisiformes-All_mir.csv");
			int year1 = 2005;
			int year2 = 1993;
			int unUsedCollumnAtTheEnd = 1;
			*/
			/*
			String[] lines = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/Lorisiformes-All.txt");// hierarchy
			String[] lines2 = parent.loadStrings("./NicoData/relargeandlargestmonkeyalignments/Lorisiformes-All_mir.csv");
			int year1 = 2005;
			int year2 = 1993;
			int unUsedCollumnAtTheEnd = 1;*/
			
			int count=0;
			int count2=0;
			int count3=0;
			
			for (int i=0;i<lines.length;i++){
				if (lines[i].contains("#"))
					continue;
				if (lines[i].trim().equals("")){
					count++;
					continue;
				}	
				if (count==0 && count2==0){
					taxomX = lines[i]; 
					count2++;
				}
				else if (count==0 && count2>0){
					String str = lines[i].replace("(", "").replace(")", "");
					String[] ps = str.split(" ");
					for (int j=0; j<ps.length;j++){
						if (!isContained(srcTaxonomy,ps[j]))
							srcTaxonomy.add(new Taxonomy(ps[j],srcTaxonomy.size()));
					}
				}
				else if (count==1 && count3==0){
					taxomY = lines[i]; 
					count3++;
				}
				else if (count==1 && count3>0){
					String str = lines[i].replace("(", "").replace(")", "");
					String[] ps = str.split(" ");
					for (int j=0; j<ps.length;j++){
						if (!isContained(trgOntology,ps[j]))
							trgOntology.add(new Taxonomy(ps[j],trgOntology.size()));
					}
				}
			}
			hash1 = new HashMap<String,Integer>();
			for (int i=0;i<srcTaxonomy.size();i++){
				hash1.put(srcTaxonomy.get(i).name, i);
			}
			hash2 = new HashMap<String,Integer>();
			for (int i=0;i<trgOntology.size();i++){
				hash2.put(trgOntology.get(i).name, i);
			}
			
			// Read the structure of the 1st
			count=0;
			count2=0;
			count3=0;
			a1 = new ArrayList[srcTaxonomy.size()];
			a2 = new ArrayList[trgOntology.size()];
			for (int i=0;i<lines.length;i++){
				if (lines[i].contains("#"))
					continue;
				if (lines[i].trim().equals("")){
					count++;
					continue;
				}	
				if (count==0 && count2==0){
					count2++;
				}
				else if (count==0 && count2>0){
					String str = lines[i].replace("(", "").replace(")", "");
					String[] ps = str.split(" ");
					int indexParent = hash1.get(ps[0]);
					
					for (int j=1; j<ps.length;j++){
						int indexChild = hash1.get(ps[j]);
						//System.out.println(indexChild+ "     "+srcOntology.get(indexChild).name);
						if (a1[indexParent]==null)
							a1[indexParent] =  new ArrayList<Integer>();
						a1[indexParent].add(indexChild);
						srcTaxonomy.get(indexChild).parentIndex = indexParent;
					}
				}
				else if (count==1 && count3==0){
					count3++;
				}
				else if (count==1 && count3>0){
					String str = lines[i].replace("(", "").replace(")", "");
					String[] ps = str.split(" ");
					int indexParent = hash2.get(ps[0]);
					for (int j=1; j<ps.length;j++){
						int indexChild = hash2.get(ps[j]);
						if (a2[indexParent]==null)
							a2[indexParent] =  new ArrayList<Integer>();
						a2[indexParent].add(indexChild);
					}
				}
			}
			
			articulations = new ArrayList[srcTaxonomy.size()][trgOntology.size()];
			for (int i=0;i<lines2.length;i++){
				String str = lines2[i].replace("{", "").replace("}", "").replace(" ", "")
									.replace(year1+".", "").replace(year2+".", "");
				String[] ps = str.split(",");
				String s1 = ps[0];
				String s2 = ps[ps.length-1-unUsedCollumnAtTheEnd];
				//System.out.println(str);
				for (int j=1; j<ps.length-1-unUsedCollumnAtTheEnd;j++){
					int art = hashArticulations.get(ps[j]);
					int index1 = hash1.get(s1);
					int index2 = hash2.get(s2);
					if (articulations[index1][index2]==null)
						articulations[index1][index2] = new ArrayList<Integer>();
					if (art>=5) continue; // Skip disjoint
					articulations[index1][index2].add(art);
				}
			}
			
			isAllowedDrawing =  false;

				
			
			
			System.out.println();
		//	vennOverview.initialize();

			stateAnimation=0;
			isAllowedDrawing =  true;  //******************* Start drawing **************

			// Compute the summary for each Gene
			Taxonomy.orderByReading();
			//write();

			//vennOverview.compute();
			check2.s  = false;
		}
	}



	
	public  ArrayList<Integer> getParentSource(int index) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		if (index>=0){
			int parentIndex = srcTaxonomy.get(index).parentIndex;
			if (parentIndex>=0){
				ArrayList<Integer> b = getParentSource(parentIndex);
				b.add(parentIndex);
				a = b;
			}	
		}	
		return a;
	}
		
	public  boolean isContained(ArrayList<Taxonomy> a, String str) {
		for (int i=0;i<a.size();i++){
			if (a.get(i).name.equals(str))
				return true;
		}
		return false;
	}
		

	// This function returns all the files in a directory as an array of Strings
	public  ArrayList<String> listFileNames(String dir, String imgType) {
		File file = new File(dir);
		ArrayList<String> a = new ArrayList<String>();
		if (file.isDirectory()) { // Do
			String names[] = file.list();
			for (int i = 0; i < names.length; i++) {
				ArrayList<String> b = listFileNames(dir + "/" + names[i], imgType);
				for (int j = 0; j < b.size(); j++) {
					a.add(b.get(j));	
				}

			}
		} else if (dir.endsWith(imgType)) {
			a.add(dir);
		}
		return a;
	}




	

	// Thread for grouping
	class ThreadLoader3 implements Runnable {
		public ThreadLoader3() {}
		public void run() {
		}
	}	

	// Thread for grouping
	class ThreadLoader4 implements Runnable {
		PApplet parent;
		public ThreadLoader4(PApplet p) {
			parent = p;
		}
		public void run() {
			String fileName =  loadFile(new Frame(), "Open your file", "..", ".txt");
			if (fileName.equals("..null"))
				return;
			else{
				currentFile = fileName;
				//VEN DIAGRAM
				vennOverview = new Venn_Overview(parent);
				thread1=new Thread(loader1);
				thread1.start();
			}
		}
	}	

	void mouseWheel(int delta) {
		//	PopupComplex.y2 -= delta/2;
		//	if (PopupComplex.y2>20)
		//		PopupComplex.y2 = 20;
		
	}



}
