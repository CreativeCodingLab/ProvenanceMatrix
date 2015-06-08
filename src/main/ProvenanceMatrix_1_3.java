package main;
/*
 * DARPA project
 *
 * Copyright 2015 by Tuan Dang.
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
import java.awt.Image;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;


import edu.uic.ncdm.venn.Venn_Overview;
import processing.core.*;

public class ProvenanceMatrix_1_3 extends PApplet {
	private static final long serialVersionUID = 1L;
	public int count = 0;
	public static int currentRelation = -1;
	public static int processing = 0;
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/Lorisiformes-All.txt";
	//public String currentFile = "./NicoData/minyomerus-underspecified.txt";
	//public String currentFile = "./NicoData/large/prim-uc-entire.txt";  	//********* remember to change to length-1 instead of length -2
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/primates-large-alignment.txt";
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/weevils-merge-concepts.txt";
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/perelleschus-multiple-worlds.txt"; 	// small, nice Venn Diagram
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/2015_1982_phylo.txt"; 		
	//public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched.txt"; 
	public String currentFile = "./NicoData/relargeandlargestmonkeyalignments/2010-1968-gymno-enriched2.txt"; 		// 160 possible worlds
	//public String currentFile = "./BertramData/BD-Hubs-Diversity.txt";
	
	public static ButtonBrowse buttonBrowse;

	// Store the genes results
	public static ArrayList<String>[] pairs;
	public static ArrayList<String>[] ontologyMappings; // equivalent to pairs

	

	//public static ArrayList<Integrator> iW;
	// Contains the location and size of each gene to display
	public float size=0;
	public static float marginX = 200;
	public static float marginY = 200;
	public static String message="";

	public ThreadLoader1 loader1=new ThreadLoader1(this);
	public Thread thread1=new Thread(loader1);

	public ThreadLoader3 loader3=new ThreadLoader3();
	public Thread thread3=new Thread(loader3);

	public ThreadLoader4 loader4=new ThreadLoader4(this);
	public Thread thread4=new Thread(loader4);

	// Venn
	public Venn_Overview vennOverview; 
	public int lX=-100,lY=-100,bX=-100,bY=-100;

	// Order genes
	public static PopupOrder popupOrder;
	public static CheckBox check4;
	public static CheckBox check2;
	public static CheckBox check1;

	// Grouping animation
	public static int stateAnimation =0;
	public static int bg =0;


	// Color of miner
	public static int[] mappingColorRelations; //@amruta 
	public static Color[] sourceColors = new Color[3]; //@amruta 


	// Allow to draw 
	public static boolean isAllowedDrawing = false;
	public static int  ccc = 0; // count to draw progessing bar
	public PFont metaBold = loadFont("Arial-BoldMT-18.vlw");


	
	public static ArrayList[][] articulations;
	public static int[][] artSources;
	public HashMap<String,Integer> hashArticulations = new HashMap<String,Integer>();
	public HashMap<String,Integer> hashArtSource = new HashMap<String,Integer>();
	public String taxomX;
	public String taxomY;
	public static ArrayList<Taxonomy> srcTaxonomy = new ArrayList<Taxonomy>();
	public static ArrayList<Taxonomy> trgTaxonomy = new ArrayList<Taxonomy>();
	public static ArrayList[] a1;    // ArrayList parent -> children
	public static ArrayList[] a2;
	public static HashMap<String,Integer> hash1;   	// Hash of taxo name-index in array
	public static HashMap<String,Integer> hash2;	// Hash of taxo name-index in array
	
	public static String[] artStrings = {"Equals","Includes","is_included_in","Overlaps","Disjoint"}; 
	public static int[] goodTaxonX;  // Good taxon is the taxon cotains at least 1 Equals
	public static int[] goodTaxonY;
	public static String[] taxonTypes = {"Something wrong",
										"Congruent",
										"Non-congruent but its children are",
										"Non-congruent" };
	public static Color[] colors = {new Color(127,127,127),
									new Color(0,180,0),
									new Color(0,70,0),
									new Color(170,0,0) };
	
	public static void main(String args[]){
		PApplet.main(new String[] { ProvenanceMatrix_1_3.class.getName() });
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

		
		// Articulation colors-----------------------------
		mappingColorRelations =  new int[5];	
		mappingColorRelations[0] = new Color(0,200,0).getRGB(); 
		mappingColorRelations[1] = new Color(0,0,255).getRGB(); 
		mappingColorRelations[2] = new Color(200,200,0).getRGB(); 
		mappingColorRelations[3] = new Color(255,0,200).getRGB();		
		mappingColorRelations[4] = new Color(180,180,180).getRGB();	
		
		// Source colors-----------------------------
		sourceColors[0] = new Color(0,0,0);
		sourceColors[1] = new Color(150,255,255);
		sourceColors[2] = Color.PINK;
		
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
		
		hashArtSource.put("input",1);
		hashArtSource.put("deduced",2);
		hashArtSource.put("inferred",3);
		
		//-----------------------------
		buttonBrowse = new ButtonBrowse(this);
		popupOrder  = new PopupOrder(this);
		check4 = new CheckBox(this, "Show articulation sources");
		check2 = new CheckBox(this, "Color by congruence");
		check1 = new CheckBox(this, "Display Wikipedia images");


		//VEN DIAGRAM
		vennOverview = new Venn_Overview(this);
		thread1=new Thread(loader1);
		thread1.start();
	
		
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
					float xCheck = this.width-210;
					float yCheck = 3;
					
					check1.draw(xCheck, yCheck);
					check2.draw(xCheck, yCheck+18);
					float yCheck2 = yCheck+36;
					if (check2.s){
						for (int i=1;i<colors.length;i++){
							float yCheck3 = yCheck+20+i*15;
							this.fill(colors[i].getRGB());
							this.text(taxonTypes[i],xCheck+20,yCheck3+12);
						}
						yCheck2 = yCheck+85;
					}
					check4.draw(xCheck, yCheck2);
					if (check4.s){
						for (int i=0;i<sourceColors.length;i++){
							this.fill(sourceColors[i].getRGB());
							this.stroke(0);
							this.strokeWeight(0.2f);
							float yCheck3 = yCheck2+20+i*15;
							this.rect(xCheck+17, yCheck3, 13,13);
							this.fill(sourceColors[i].darker().getRGB());
							this.text(hashArtSource.keySet().toArray()[i].toString(),xCheck+35,yCheck3+12);
						}
					}
					
					if (srcTaxonomy==null || srcTaxonomy.size()==0)
						return;
					else{
						int numColumns = 0; 
						int numRows = trgTaxonomy.size();
						// Find number of expanded concepts
						for (int i=0; i<srcTaxonomy.size();i++){
							if (srcTaxonomy.get(i).isExpanded==1)
								numColumns++;
						}
						
						
						int num = Math.max(numRows, numColumns);
						size = (this.height-marginY)/num;
						if (size>100)
							size=100;
					}
					drawMatrix(marginX,marginY);
					//Draw images ****************************************************
					drawImages();
					
					this.textSize(11);
					popupOrder.draw(this.width-330);
				}
			}
			buttonBrowse.draw();
			
			this.textSize(13);
			this.fill(0);
			this.textAlign(PApplet.LEFT);
			this.text(message, 30, this.height-10);
		}
		catch (Exception e){
			System.out.println();
			System.out.println("*******************Catch ERROR*******************");
			message = e.getMessage();
			e.printStackTrace();
			return;
		}
	}	

	//Draw images ****************************************************
	public void drawImages() throws IOException {
		float iWidth = 180;
		float x8 = this.width-(iWidth+10)*2;
		float y8 = 150;
		this.textAlign(PApplet.LEFT);
		if (!check1.s){
			vennOverview.draw(this.width-380, 80);
		}
		else if (check1.s && ((0<=bX && bX<srcTaxonomy.size()) || (0<=bY && bY<trgTaxonomy.size()))){
			this.fill(255);
			this.noStroke();
			this.rect(x8,y8-40,380,this.height);
			if (0<=bX && bX<srcTaxonomy.size()){
				ArrayList<PImage> a =srcTaxonomy.get(bX).images;
				if (a.size()>0){
					this.fill(0);
					this.textSize(11);
					this.text("Images from Wikipedia for:", x8,y8-12);
					
					Color color = new Color(0,0,0);
					if (check2.s)
						color = colors[goodTaxonX[bX]];
					float sat = 55+(this.frameCount*20)%200;
					this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
					
					this.textSize(11);
					this.text(srcTaxonomy.get(bX).name,x8+152, y8-12);
				}
				else{
					this.fill(0);
					this.textSize(11);
					this.text("Can NOT find images for", x8,y8-12);
					
					Color color = new Color(0,0,0);
					if (check2.s)
						color = colors[goodTaxonX[bX]];
					float sat = 55+(this.frameCount*20)%200;
					this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
					
					this.textSize(11);
					this.text(srcTaxonomy.get(bX).name,x8+140, y8-12);
				}
				for (int im=0; im<a.size();im++){
					float row = im/2;
					float col = im%2;
					this.image(a.get(im), x8+col*(iWidth+10), y8+row*(iWidth+10));
				}
			}
			else if (0<=bY && bY<trgTaxonomy.size()){
				ArrayList<PImage> a =trgTaxonomy.get(bY).images;
				if (a.size()>0){
					this.fill(0);
					this.textSize(11);
					this.text("Images from Wikipedia for:", x8,y8-12);
					
					Color color = new Color(0,0,0);
					if (check2.s)
						color = colors[goodTaxonY[bY]];
					float sat = 55+(this.frameCount*20)%200;
					this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
					
					this.textSize(11);
					this.text(trgTaxonomy.get(bY).name,x8+152, y8-12);
				}
				else{
					this.fill(0);
					this.textSize(11);
					this.text("Can NOT find images for", x8,y8-12);
					
					Color color = new Color(0,0,0);
					if (check2.s)
						color = colors[goodTaxonY[bY]];
					float sat = 55+(this.frameCount*20)%200;
					this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
					
					this.textSize(11);
					this.text(trgTaxonomy.get(bY).name,x8+140, y8-12);
				}
				for (int im=0; im<a.size();im++){
					float row = im/2;
					float col = im%2;
					this.image(a.get(im), x8+row*(iWidth+10), y8+col*(iWidth+10));
				}
			}
		}
		else{
			this.fill(0);
			this.textSize(11);
			this.text("Please select a taxon in the left matrix",  x8,y8-12);
		}
	}
		
	
	@SuppressWarnings("unchecked")
	public void drawMatrix(float mX, float mY) throws IOException {
		// Compute lensing
	
		float lensingSize = PApplet.map(size, 0, 100, 20, 80);	
		int num = 5; // Number of items in one side of lensing
		
		///////////////////
		ArrayList<Integer> src = new ArrayList<Integer>();
		Map<Integer, Integer> unsortMap1 = new HashMap<Integer, Integer>();
		for (int i=0;i<srcTaxonomy.size();i++){
			int parentIndex = srcTaxonomy.get(i).parentIndex;
			if (parentIndex>=0 && srcTaxonomy.get(parentIndex).isExpanded<1){
				// set the position as of the parent
				float value = srcTaxonomy.get(parentIndex).iX.target;
				srcTaxonomy.get(i).iX.target(value);
			}
			else
				unsortMap1.put(i, srcTaxonomy.get(i).order);
		}
		Map<Integer, Integer> sorted1 = Taxonomy.sortByComparator2(unsortMap1);
		for (Map.Entry<Integer, Integer> entry : sorted1.entrySet()) {
			src.add(entry.getKey());
		}
		
		for (int order=0;order<src.size();order++){
			int index =   src.get(order);
			float value = 0;
			if (lX-num<=order && order<=lX+num) {
				srcTaxonomy.get(index).iW.target(lensingSize);
				int num2 = order-(lX-num);
				if (lX-num>=0)
					value = mX +(lX-num)*size+num2*lensingSize;
				else
					value = mX +order*lensingSize;
			}	
			else{
				srcTaxonomy.get(index).iW.target(size);
				if (order<lX-num)
					value = mX +order*size;
				else if (order>lX+num){
					if (lX-num>=0)
						value = mX +(order-(num*2+1))*size+(num*2+1)*lensingSize;
					else{
						int num3= lX+num+1;
						if (num3>0)
							value = mX +(order-num3)*size+num3*lensingSize;
						else
							value = mX +order*size;
					}	
				}	 
			}
			
			srcTaxonomy.get(index).iX.target(value);
		}
		///////////////////
		ArrayList<Integer> trg = new ArrayList<Integer>();
		Map<Integer, Integer> unsortMap2 = new HashMap<Integer, Integer>();
		for (int i=0;i<trgTaxonomy.size();i++){
			int parentIndex = trgTaxonomy.get(i).parentIndex;
			if (parentIndex>=0 && trgTaxonomy.get(parentIndex).isExpanded<1){
				// set the position as of the parent
				float value = trgTaxonomy.get(parentIndex).iY.target;
				trgTaxonomy.get(i).iY.target(value);
			}
			else
				unsortMap2.put(i, trgTaxonomy.get(i).order);
		}
		Map<Integer, Integer> sorted2 = Taxonomy.sortByComparator2(unsortMap2);
		for (Map.Entry<Integer, Integer> entry : sorted2.entrySet()) {
			trg.add(entry.getKey());
		}
		for (int order=0;order<trg.size();order++){
			int index = trg.get(order);
			float value = 0;
			if (lY-num<=order && order<=lY+num){
				trgTaxonomy.get(index).iH.target(lensingSize);
				int num2 = order-(lY-num);
				if (lY-num>=0)
					value = mY +(lY-num)*size+num2*lensingSize;
				else
					value = mY +order*lensingSize;
			}	
			else{
				trgTaxonomy.get(index).iH.target(size);
				if (order<lY-num)
					value = mY +order*size;
				else if (order>lY+num){
					if (lY-num>=0)
						value = mY +(order-(num*2+1))*size+(num*2+1)*lensingSize;
					else{
						int num3= lY+num+1;
						if (num3>0)
							value = mY +(order-num3)*size+num3*lensingSize;
						else
							value = mY +order*size;
					}	

				}	
			}
			trgTaxonomy.get(index).iY.target(value);
		}

		//--------------------------------
		for (int i=0;i<srcTaxonomy.size();i++){			
			srcTaxonomy.get(i).iW.update();
			srcTaxonomy.get(i).iX.update();		
		}

		for (int i=0;i<trgTaxonomy.size();i++){
			trgTaxonomy.get(i).iH.update();
			trgTaxonomy.get(i).iY.update();
		}


		//-----------------------------
		ArrayList<Integer> bListX = new ArrayList<Integer>(); 
		if (0<=bX && bX<srcTaxonomy.size()){
			bListX = getSubtree(bX,srcTaxonomy,a1);
		}
		ArrayList<Integer> bListY = new ArrayList<Integer>(); 
		if (0<=bY && bY<trgTaxonomy.size()){
			bListY = getSubtree(bY,trgTaxonomy,a2);
		}
		
		// Find the mappings on the other hierarchy
		if (bListX.size()>0){
			for (int i=0;i<bListX.size();i++){
				int index = bListX.get(i);
				for (int j=0;j<trgTaxonomy.size();j++){
					if (articulations[index][j].contains(0)) {// Equals
						ArrayList<Integer> b = getSubtree(j,trgTaxonomy,a2); 
						for (int k=0;k<b.size();k++){
							int index2 = b.get(k);
							bListY.add(index2);
						}
					}	
				}	                     
			}
		}
		else if (bListY.size()>0){
			for (int i=0;i<bListY.size();i++){
				int index = bListY.get(i);
				for (int j=0;j<srcTaxonomy.size();j++){
					if (articulations[j][index].contains(0)) {// Equals
						ArrayList<Integer> b = getSubtree(j,srcTaxonomy,a1); 
						for (int k=0;k<b.size();k++){
							int index2 = b.get(k);
							bListX.add(index2);
						}
					}	
				}	                     
			}
		}
		
		// Draw taxonomy (names) on X and Y axis
		float indent =10;
		float indentGap =10;
		for (int i=0;i<srcTaxonomy.size();i++){
			float ww = srcTaxonomy.get(i).iW.value;
			float xx =  srcTaxonomy.get(i).iX.value;
			
			// If not expanded, then no drawing
			if (!isTaxonXDraw(i)){
				continue;
			}	
			
			float sat = 255;
			Color color = new Color(0,0,0);
			if (bListX.size()>0 || bListY.size()>0){
				if (bListX.contains(i)){
					sat = 255;
				}
				else{
					sat = 30;
				}
			}
			if (ww>6){
				ArrayList<Integer> b = getAncestors(i,srcTaxonomy);
				float indent2 = indent+indentGap*b.size();
				
				// Check bad apples
				if (check2.s){
					color = colors[goodTaxonX[i]];
				}
				if (i==bX){
					sat = 55+(this.frameCount*20)%200;
				}
				this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
				
				this.textSize(11);
				float al = -PApplet.PI/2;
				float xx2 = xx+ww/2+5;
				this.translate(xx2,mY-indent2);
				this.rotate(al);
				this.text(srcTaxonomy.get(i).name, 0,0); // text for each column @Amruta
				this.rotate(-al);
				this.translate(-xx2, -(mY-indent2));
			}
		}
		for (int i=0;i<trgTaxonomy.size();i++){
			float hh =trgTaxonomy.get(i).iH.value;
			float yy =  trgTaxonomy.get(i).iY.value;
			// If not expanded, then no drawing
			if (!isTaxonYDraw(i)){
				continue;
			}
			
			Color color = new Color(0,0,0);
			float sat = 255;
			if (bListX.size()>0 || bListY.size()>0){
				if (bListY.contains(i)){
					sat = 255;
				}
				else{
					sat = 30;
				}
			}
			if (hh>6){
				// Check bad apples
				if (check2.s){
					color = colors[goodTaxonY[i]];
				}
				if (i==bY) {
					sat = 55+(this.frameCount*20)%200;
				}
				
				this.fill(color.getRed(),color.getGreen(), color.getBlue(), sat);
				
				ArrayList<Integer> b = getAncestors(i,trgTaxonomy);
				float indent2 = indent+indentGap*b.size();
				this.textSize(11);
				this.textAlign(PApplet.RIGHT);
				this.text(trgTaxonomy.get(i).name, mX-indent2, yy+hh/2+5); //text for each row @Amruta 
			}
		}
		
		
		// Draw Taxonomy names
		this.fill(0,0,0);
		this.textSize(13);
		this.textAlign(PApplet.CENTER);
		this.text(taxomX, 480, 20);
		this.fill(0,0,0);
		this.textSize(13);
		this.textAlign(PApplet.CENTER);
		float al = -PApplet.PI/2;
		this.translate(30,280);
		this.rotate(al);
		this.text(taxomY, 0,0); // text for each column @Amruta
		this.rotate(-al);
		this.translate(-(30), -(280));
		
		// Hierarchy links
		float arcRate = 0.5f;
		for (int i=0;i<srcTaxonomy.size();i++){
			if (a1[i]==null) 
				continue;
			if (srcTaxonomy.get(i).isExpanded<1)
				continue;
			float w1 =  srcTaxonomy.get(i).iW.value;
			float x1 =  srcTaxonomy.get(i).iX.value+w1*0.6f;
			ArrayList<Integer> b = getAncestors(i,srcTaxonomy);
			float indent2 = indent+indentGap*b.size();
			
			for (int j=0; j<a1[i].size();j++){
				int indexChild = (Integer) a1[i].get(j);
				float w2 =  srcTaxonomy.get(indexChild).iW.value;
				float x2 =  srcTaxonomy.get(indexChild).iX.value+w2*0.5f;
				this.noFill();
				this.stroke(0,0,0);
				float r = PApplet.abs(x2-x1);
				
				if (bListX.size()>0){
					if (!bListX.contains(i) || !bListX.contains(indexChild))
					this.stroke(0,20);
				}
				else if (0<=bY && bY<trgTaxonomy.size())
					this.stroke(0,20);
				
				if (w1>6 || w2>6){
					this.strokeWeight(0.3f);
					this.arc((x1+x2)/2,mY-indent2,r,r*arcRate, 0,PApplet.PI);
				}
				else{
					this.strokeWeight(0.05f);
					this.arc((x1+x2)/2,mY-indent2,r,r*arcRate,-PApplet.PI, 0);
				}	
				
				this.noStroke();
				float v = PApplet.map(w2, 0, 25, 10, 255);
				if (v>255)
					v=255;
				this.fill(0,v);
				
				if (bListX.size()>0 || bListY.size()>0){
					if (bListX.contains(i) && bListX.contains(indexChild)){
						if (srcTaxonomy.get(indexChild).isExpanded==1)
							this.triangle(x2, mY-indent2-9, x2-2, mY-indent2, x2+2, mY-indent2);
						else{ // draw a cirle and plus when it is not expanded
							this.ellipse(x2, mY-indent2-5, 10, 10);
							this.stroke(1f);
							this.stroke(255);
							this.line(x2-4, mY-indent2-5, x2+4, mY-indent2-5);
							this.line(x2, mY-indent2-9, x2, mY-indent2-1);
						}	
					}	
				}
				else{
					if (srcTaxonomy.get(indexChild).isExpanded==1)
						this.triangle(x2, mY-indent2-9, x2-2, mY-indent2, x2+2, mY-indent2);
					else{ // draw a cirle and plus when it is not expanded
						this.ellipse(x2, mY-indent2-5, 10, 10);
						this.stroke(1f);
						this.stroke(255);
						this.line(x2-4, mY-indent2-5, x2+4, mY-indent2-5);
						this.line(x2, mY-indent2-9, x2, mY-indent2-1);
					}	
				}	
			}
		}
		
		for (int i=0;i<trgTaxonomy.size();i++){
			if (a2[i]==null) 
				continue;
			if (trgTaxonomy.get(i).isExpanded<1)
				continue;
			float h1 =  trgTaxonomy.get(i).iH.value;
			float y1 =  trgTaxonomy.get(i).iY.value+h1*0.6f;
			ArrayList<Integer> b = getAncestors(i,trgTaxonomy);
			float indent2 = indent+indentGap*b.size();
			
			for (int j=0; j<a2[i].size();j++){
				int indexChild = (Integer) a2[i].get(j);
				float h2 =  trgTaxonomy.get(indexChild).iH.value;
				float y2 =  trgTaxonomy.get(indexChild).iY.value+h2*0.5f;
				
				this.noFill();
				this.stroke(0,0,0);
				float r = PApplet.abs(y2-y1);
				
				if (bListY.size()>0){
					if (!bListY.contains(i) || !bListY.contains(indexChild))
					this.stroke(0,20);
				}
				else if (0<=bX && bX<srcTaxonomy.size())
					this.stroke(0,20);
				
				if (h1>6 || h2>6){
					this.strokeWeight(0.3f);
					this.arc(mX-indent2, (y1+y2)/2,r*arcRate,r, -PApplet.PI/2, PApplet.PI/2);
				}	
				else{
					this.strokeWeight(0.05f);
					this.arc(mX-indent2, (y1+y2)/2,r*arcRate,r, PApplet.PI/2, 3*PApplet.PI/2);
				}	
				
				this.noStroke();
				float v = PApplet.map(h2, 0, 25, 10, 255);
				if (v>255)
					v=255;
				this.fill(0,v);
				
				if (bListY.size()>0 || bListX.size()>0){
					if (bListY.contains(i) && bListY.contains(indexChild)){
						if (trgTaxonomy.get(indexChild).isExpanded==1)
							this.triangle(mX-indent2-9, y2, mX-indent2, y2-2, mX-indent2, y2+2);
						else{ // draw a cirle and plus when it is not expanded
							this.ellipse(mX-indent2-5, y2, 10, 10);
							this.stroke(1f);
							this.stroke(255);
							this.line(mX-indent2-9, y2, mX-indent2-1, y2);
							this.line(mX-indent2-5, y2-4, mX-indent2-5, y2+4);
						}
					}	
				}
				else{
					if (trgTaxonomy.get(indexChild).isExpanded==1)
						this.triangle(mX-indent2-9, y2, mX-indent2, y2-2, mX-indent2, y2+2);
					else{ // draw a cirle and plus when it is not expanded
						this.ellipse(mX-indent2-5, y2, 10, 10);
						this.stroke(1f);
						this.stroke(255);
						this.line(mX-indent2-9, y2, mX-indent2-1, y2);
						this.line(mX-indent2-5, y2-4, mX-indent2-5, y2+4);
					}	
				}
			}	
		}
			
			
		// Circular sectors
		int numberOfSector = 0;
		int[] artArray = new int[mappingColorRelations.length];
		for (int i=0; i<vennOverview.numArt;i++){
			if (vennOverview.isActive[i]){
				artArray[i] = numberOfSector;
				numberOfSector++;
			}	
		}	
		float alpha = PApplet.PI*2/numberOfSector;
		
		for (int i=0;i<trgTaxonomy.size();i++){
			// Check if this is grouping
			float hh = trgTaxonomy.get(i).iH.value;
			float yy =  trgTaxonomy.get(i).iY.value+hh/2;
			if (!isTaxonYDraw(i))
				continue;
			for (int j=0;j<srcTaxonomy.size();j++){
				float ww =srcTaxonomy.get(j).iW.value;
				float xx =  srcTaxonomy.get(j).iX.value+ww/2;
				if (!isTaxonXDraw(j))
					continue;
				// Draw articulation sources
				if (check4.s){
					int source =  artSources[j][i]-1; 
					this.noStroke();
					if (source>=0){
						this.fill(sourceColors[source].getRed(),sourceColors[source].getGreen(),sourceColors[source].getBlue(),220);
						this.rect(xx-ww/2,yy-hh/2,ww,hh);
					}
				}
				
				// Draw articulation sectors
				if (articulations[j][i]==null) continue;
				for (int i2=0;i2<articulations[j][i].size();i2++){
					int indexArt = (Integer) articulations[j][i].get(i2);
					this.noStroke();
					this.fill(mappingColorRelations[indexArt]);
					if (bListX.size()>0 || bListY.size()>0){
						Color c = new Color(mappingColorRelations[indexArt]);
						if (bListX.contains(j) && bListY.contains(i))
							this.fill(c.getRed(),c.getGreen(),c.getBlue());
						else	
							this.fill(c.getRed(),c.getGreen(),c.getBlue(),15);
					}
					if (vennOverview.isActive[indexArt]){
						float radius =  PApplet.min(ww,hh)+2;
						if (check4.s)
							radius =  PApplet.min(ww,hh)*0.75f; // Smaller sectors for showing articulation sources 
						this.arc(xx,yy,radius,radius, PApplet.PI+artArray[indexArt]*alpha, PApplet.PI+(artArray[indexArt]+1)*alpha);
						
						// Draw green connections for Equals
						if (indexArt==0 && bListX.contains(j) && bListY.contains(i)){
							this.stroke(0,150,0,100);
							this.strokeWeight(0.5f);
							
							ArrayList<Integer> b = getAncestors(j,srcTaxonomy);
							float indentX = indent+indentGap*b.size();
							ArrayList<Integer> c = getAncestors(i,trgTaxonomy);
							float indentY = indent+indentGap*c.size();
							
							this.line(xx, mY-indentY, xx, yy);
							this.line(mX-indentX, yy, xx, yy);
						}
					}
				}
			}
		}
	}	

	// If not expanded, then no drawing
	public static boolean isTaxonXDraw(int index) {
		int parentIndex = srcTaxonomy.get(index).parentIndex;
		if (parentIndex>=0){
			float xx = srcTaxonomy.get(index).iX.value;
			float xxParent = srcTaxonomy.get(parentIndex).iX.value;
			if (srcTaxonomy.get(index).isExpanded<0)
				return false;
			else if (srcTaxonomy.get(index).isExpanded==0 && PApplet.abs(xx-xxParent)<1){
				srcTaxonomy.get(index).isExpanded=-1;
				return false;
			}	
		}	
		return true;
	}
	// If not expanded, then no drawing
	public static boolean isTaxonYDraw(int index) {
		int parentIndex = trgTaxonomy.get(index).parentIndex;
		if (parentIndex>=0){
			float yy = trgTaxonomy.get(index).iY.value;
			float yyParent = trgTaxonomy.get(parentIndex).iY.value;
			if (trgTaxonomy.get(index).isExpanded<0)
				return false;
			else if (trgTaxonomy.get(index).isExpanded==0 && PApplet.abs(yy-yyParent)<1){
				trgTaxonomy.get(index).isExpanded=-1;
				return false;
			}	
		}	
		return true;
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
		// Check brushing
		lX = -100;
		lY = -100;
		bX = -100;
		bY = -100;
		if (mouseX>marginX && mouseY>marginY && mouseX<marginX+size*srcTaxonomy.size() && mouseY<marginY+size*trgTaxonomy.size()){
			lX = (int) ((this.mouseX-marginX)/size);
			lY = (int) ((this.mouseY-marginX)/size);
			bX = -100;
			bY = -100;
		//	System.out.println(lX+"	"+lY+"	"+bX+"	"+bY+"	"+size);
		}	
		else if (mouseY<=marginY){
			for (int i=0;i<srcTaxonomy.size();i++){
				int parentIndex = srcTaxonomy.get(i).parentIndex;
				if (parentIndex<0 || (parentIndex>=0 && srcTaxonomy.get(parentIndex).isExpanded==1)){
					int order = srcTaxonomy.get(i).order;
					srcTaxonomy.get(i).iW.target(size);
					srcTaxonomy.get(i).iX.target(marginY +order*size);
					if (srcTaxonomy.get(i).iX.value<=this.mouseX
							&& this.mouseX<=srcTaxonomy.get(i).iX.value+size)	
						bX=i;
				}
			}	
			lX=-100;
			lY=-100;
			bY=-100;
		}
		else if (mouseX<=marginX){
			for (int i=0;i<trgTaxonomy.size();i++){
				int order = trgTaxonomy.get(i).order;
				trgTaxonomy.get(i).iH.target(size);
				trgTaxonomy.get(i).iY.target(marginY +order*size);
				if (trgTaxonomy.get(i).iY.value<=this.mouseY
					&& this.mouseY<=trgTaxonomy.get(i).iY.value+size)	
					bY = i;
			}
			lX=-100;
			lY=-100;
			bX=-100;
		}
		
	}

	public void mouseClicked() {
		if (buttonBrowse.b>=0){
			thread4=new Thread(loader4);
			thread4.start();
		}

		if (check4.b){
			check4.mouseClicked();
		}
		else if (check2.b){
			check2.mouseClicked();
		}
		else if (check1.b){
			check1.mouseClicked();
		}
		else if (popupOrder.b>=0){
			popupOrder.mouseClicked();
		}
		else if (vennOverview!=null){
			vennOverview.mouseClicked();
			//update();
		}
		
		if (bX>=0){
			if (srcTaxonomy.get(bX).isExpanded<1)
				srcTaxonomy.get(bX).isExpanded = 1;
			else
				srcTaxonomy.get(bX).isExpanded =0;
			ArrayList<Integer> children = this.getSuccessors(bX, a1);
			for (int i=0;i<children.size();i++){
				int index = children.get(i);
				srcTaxonomy.get(index).isExpanded = srcTaxonomy.get(bX).isExpanded;
			}
		}
		else if (bY>=0){
			if (trgTaxonomy.get(bY).isExpanded<1)
				trgTaxonomy.get(bY).isExpanded = 1;
			else
				trgTaxonomy.get(bY).isExpanded = 0;
			ArrayList<Integer> children = this.getSuccessors(bY, a2);
			for (int i=0;i<children.size();i++){
				int index = children.get(i);
				trgTaxonomy.get(index).isExpanded = trgTaxonomy.get(bY).isExpanded;
			}
		}
	}


	// Thread for Venn Diagram
	class ThreadLoader1 implements Runnable {
		PApplet parent;
		public ThreadLoader1(PApplet parent_) {
			parent = parent_;
		}

		@SuppressWarnings("unchecked")
		public void run() {
			try{
				srcTaxonomy = new ArrayList<Taxonomy>();
				trgTaxonomy = new ArrayList<Taxonomy>();
				/// Taxom mappings
				String[] lines = parent.loadStrings(currentFile);// hierarchy
				String[] lines2 = parent.loadStrings(currentFile.replace(".txt", "_mir.csv"));
				
				int count=0;
				int count2=0;
				int count3=0;
				for (int i=0;i<lines.length;i++){
					System.out.println(lines[i]);
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
								srcTaxonomy.add(new Taxonomy(parent, ps[j],srcTaxonomy.size()));
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
							if (!isContained(trgTaxonomy,ps[j]))
								trgTaxonomy.add(new Taxonomy(parent, ps[j],trgTaxonomy.size()));
						}
					}
				}
				hash1 = new HashMap<String,Integer>();
				for (int i=0;i<srcTaxonomy.size();i++){
					hash1.put(srcTaxonomy.get(i).name, i);
				}
				hash2 = new HashMap<String,Integer>();
				for (int i=0;i<trgTaxonomy.size();i++){
					hash2.put(trgTaxonomy.get(i).name, i);
				}
				
				// Read the structure of hierarchy
				count=0;
				count2=0;
				count3=0;
				a1 = new ArrayList[srcTaxonomy.size()];
				a2 = new ArrayList[trgTaxonomy.size()];
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
							trgTaxonomy.get(indexChild).parentIndex = indexParent;
						}
					}
				}
				articulations = new ArrayList[srcTaxonomy.size()][trgTaxonomy.size()];
				artSources = new int[srcTaxonomy.size()][trgTaxonomy.size()];
				String[] p = lines2[0].split(",");
				String year1 = p[0].split("\\.")[0];
				String year2 = p[2].split("\\.")[0];
				System.out.println(year1+"	"+year2);
				for (int i=0;i<lines2.length;i++){
					String str = lines2[i].replace("{", "").replace("}", "").replace(" ", "")
										.replace(year1+".", "").replace(year2+".", "");
					String[] ps = str.split(",");
					String s1 = ps[0];
					String s2 = ps[ps.length-2];
					int index1 = hash1.get(s1);
					int index2 = hash2.get(s2);
					for (int j=1; j<ps.length-2;j++){
						int art = hashArticulations.get(ps[j]);
						if (articulations[index1][index2]==null)
							articulations[index1][index2] = new ArrayList<Integer>();
						if (art>=5) continue; // Skip disjoint
						articulations[index1][index2].add(art);
					}
					// Get the articulation source: input, deduced, inferred
					String s3 = ps[ps.length-1];
					artSources[index1][index2] = hashArtSource.get(s3);
				}
				// Compute bad apples
				goodTaxonX = new int[srcTaxonomy.size()];
				goodTaxonY = new int[trgTaxonomy.size()];
				for (int i1=0;i1<srcTaxonomy.size();i1++){
					for (int j1=0;j1<trgTaxonomy.size();j1++){
						if (articulations[i1][j1].contains(0)) // contains Equals
							goodTaxonX[i1] = 1;
					}
					if (goodTaxonX[i1] != 1){
						ArrayList<Integer> bListX = getSubtree(i1,srcTaxonomy,a1);
						if (bListX.size()>0){
							boolean found = false;
							for (int i2=0;i2<bListX.size() && !found;i2++){
								int index = bListX.get(i2);
								for (int j2=0;j2<trgTaxonomy.size() && !found;j2++){
									if (articulations[index][j2].contains(0)) {// Equals
										found = true;
									}	
								}	                     
							}
							if (found)
								goodTaxonX[i1] = 2;
							else
								goodTaxonX[i1] = 3;
						}
					}
				}
				for (int j1=0;j1<trgTaxonomy.size();j1++){
					for (int i1=0;i1<srcTaxonomy.size();i1++){
						if (articulations[i1][j1].contains(0)) // contains Equals
							goodTaxonY[j1] = 1;
					}
					if (goodTaxonY[j1] != 1){
						ArrayList<Integer> bListY = getSubtree(j1,trgTaxonomy,a2);
						if (bListY.size()>0){
							boolean found = false;
							for (int j2=0;j2<bListY.size() && !found; j2++){
								int index = bListY.get(j2);
								for (int i2=0; i2<srcTaxonomy.size() && !found; i2++){
									if (articulations[i2][index].contains(0)) {// Equals
										found = true;
									}	
								}	                     
							}
							if (found)
								goodTaxonY[j1] = 2;
							else
								goodTaxonY[j1] = 3;
						}
					}
				}
					
				// Output Euler ->JSON
				ArrayList<String> a = new ArrayList<String>();
				
				for (int i=0;i<srcTaxonomy.size();i++){
					for (int j=0;j<trgTaxonomy.size();j++){
						if (articulations[i][j]==null)
							continue;
						String name1 = "\"source\":\""+getDotName(i,srcTaxonomy) +"\"";
						String name2 = "\"target\":\""+getDotName(j,trgTaxonomy) +"\"";
						String arti = "\"articulations\":"+articulations[i][j];
						int artSrc = artSources[i][j];
						String str = "";
						if (artSrc==1)
							str = "input";
						else if (artSrc==2)
							str = "deduced";
						else if (artSrc==3)
							str = "inferred";
						
						String artSrcStr = "\"articulation Source\":\""+str+"\"";
						
						a.add("{"+name1+","+arti+","+artSrcStr+","+name2+"},");
					//	System.out.println(articulations[i][j]);
					}
				}
				//
				//hashArtSource.put("input",1);
				//hashArtSource.put("deduced",2);
				//hashArtSource.put("inferred",3);
				
				String[] b = new String[a.size()+2];
				b[0]="[";
				for (int i=0;i<a.size();i++){
					b[i+1] = a.get(i);
				}
				b[a.size()] = b[a.size()].substring(0,b[a.size()].length()-1);
				b[a.size()+1]="]";
				
				parent.saveStrings(currentFile.replace(".txt", ".json"), b);
					
					
				
				
				
				System.out.println();
				stateAnimation=0;
				isAllowedDrawing =  true;  //******************* Start drawing **************
	
				Taxonomy.orderByReading();
				vennOverview.compute();
				
				// Find images from Wikipedia ----------------------------------
				for (int i=0;i<srcTaxonomy.size();i++){
					srcTaxonomy.get(i).setImages();
				}
				for (int i=0;i<trgTaxonomy.size();i++){
					trgTaxonomy.get(i).setImages();
				}
			}
			catch (Exception e){
				message = e.toString();
				e.printStackTrace();
			}
		}
	}
	public  String getDotName(int index, ArrayList<Taxonomy> onList) {
		String name ="";
		ArrayList<Integer> d = getAncestors(index,onList);
		for (int i=0;i<d.size();i++){
			int index2 = d.get(i);
			name+=onList.get(index2).name+".";
		}
		name+=onList.get(index).name;
		return name;
	}
		

	public  ArrayList<Integer> getSubtree(int index, ArrayList<Taxonomy> onList, ArrayList<Integer>[] aList) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		a.add(index);
		ArrayList<Integer> b = getAncestors(index,onList);
		for (int i=0;i<b.size();i++){
			a.add(b.get(i));
		}
		ArrayList<Integer> c = getSuccessors(index,aList);
		for (int i=0;i<c.size();i++){
			a.add(c.get(i));
		}
		return a;
	}
	public  ArrayList<Integer> getAncestors(int index, ArrayList<Taxonomy> onList) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		if (index>=0){
			int parentIndex = onList.get(index).parentIndex;
			if (parentIndex>=0){
				ArrayList<Integer> b = getAncestors(parentIndex, onList);
				b.add(parentIndex);
				a = b;
			}	
		}	
		return a;
	}
	
	public  ArrayList<Integer> getSuccessors(int index, ArrayList<Integer>[] aList) {
		ArrayList<Integer> a = new ArrayList<Integer>();
		if (index>=0 && aList[index]!=null){
			for (int i=0;i<aList[index].size();i++){
				int childIndex = aList[index].get(i);
				a.add(childIndex);
				ArrayList<Integer> b = getSuccessors(childIndex, aList);
				for (int j=0;j<b.size();j++){
					a.add(b.get(j));
				}
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
		
	
	// Thread for grouping
	class ThreadLoader3 implements Runnable {
		public ThreadLoader3() {}
		public void run() {
		}
	}	

	// Open new Data
	class ThreadLoader4 implements Runnable {
		PApplet parent;
		public ThreadLoader4(PApplet p) {
			parent = p;
		}
		public void run() {
			String fileName =  loadFile(new Frame(), "Open your file", ".");
			if (fileName.equals(".null"))
				return;
			else{
				currentFile = fileName;
				vennOverview = new Venn_Overview(parent);
				thread1=new Thread(loader1);
				thread1.start();
			}
		}
	}	

	public String loadFile (Frame f, String title, String defDir) {
		FileDialog fd = new FileDialog(f, title, FileDialog.LOAD);
		fd.setFilenameFilter(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
		
		fd.setDirectory(defDir);
		fd.setLocation(50, 50);
		fd.show();
		String path = fd.getDirectory()+fd.getFile();
		return path;
	}
	
	void mouseWheel(int delta) {
		//	PopupComplex.y2 -= delta/2;
		//	if (PopupComplex.y2>20)
		//		PopupComplex.y2 = 20;
		
	}



}
