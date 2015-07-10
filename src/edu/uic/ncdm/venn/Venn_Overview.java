package edu.uic.ncdm.venn;
/*
 * EVL temperature visualization.
 *
 * Copyright 2011 by Tuan Dang.
 *
 * The contents of this file are subject to the Mozilla Public License Version 2.0 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

import processing.core.*;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import edu.uic.ncdm.venn.data.VennData;
import static main.ProvenanceMatrix_1_4.srcTaxonomy;
import static main.ProvenanceMatrix_1_4.trgTaxonomy;
import static main.ProvenanceMatrix_1_4.articulations;


public class Venn_Overview{
	int count = 0;
	public String[][] data;
    public double[] areas;
    
    public double[][] centers;
    public double[] diameters;
    public String[] labels;
    private int size;
    private double mins, maxs;
    public PApplet parent;
    public int brushing=-1;
    public float bx=0;
    public float by=0;
    public float br=0;
   public boolean[] isActive;
   
	public static float currentSize;
	public int numArt = 0;
	
	public Venn_Overview(PApplet pa) {
		parent = pa;
		centers = new double[0][0];	
	}  
    
	public void compute() {
		numArt = main.ProvenanceMatrix_1_4.artStrings.length;
		isActive = new boolean[numArt];
		for (int i =0; i< numArt; i++){
			isActive[i] = true;
		}
		isActive[4] = false;  // Disjoint
			
	
		// Obtain relation of intersections
		int[] count = new int[numArt];
		HashMap<String, Integer> hash = new HashMap<String,Integer>();
		for (int i =0; i< srcTaxonomy.size(); i++){
			for (int j =0; j< trgTaxonomy.size(); j++){
				if (articulations[i][j]==null) 	continue;
				for (int k =0; k< articulations[i][j].size(); k++){
					int art = (Integer) articulations[i][j].get(k);
					count[art]++;
				}
				if (articulations[i][j].size()>1){
					//if (articulations[i][j].size()>3) {
					//	System.out.println(articulations[i][j]);
					//	continue;
					//}	
					String str = main.ProvenanceMatrix_1_4.artStrings[(Integer) articulations[i][j].get(0)];
					for (int k =1; k< articulations[i][j].size(); k++){
						//if ((Integer) articulations[i][j].get(k)==4)
						//	continue;
						str += "&"+main.ProvenanceMatrix_1_4.artStrings[(Integer) articulations[i][j].get(k)];
					}
					if (hash.get(str)==null){
						hash.put(str, 1);
					}
					else{
						int num =hash.get(str);
						hash.put(str, num+1);
					}
				}	
			}
		}	
		data = new String[numArt+hash.size()][1];
		areas =  new double[numArt+hash.size()];		
		for (int i =0; i< numArt; i++){
			data[i][0] = main.ProvenanceMatrix_1_4.artStrings[i];
			areas[i]=count[i];
		}
		
		int k=0;
		for (Map.Entry<String, Integer> entry : hash.entrySet()) {
			data[numArt+k][0] = entry.getKey();
			areas[numArt+k] = entry.getValue();
			k++;
		}
			
		VennData dv = new VennData(data, areas);
	   VennAnalytic va = new VennAnalytic();
       VennDiagram venn = va.compute(dv);
     	
       centers = venn.centers;
       diameters = venn.diameters;
       labels = venn.circleLabels;
       mins = Double.POSITIVE_INFINITY;
       maxs = Double.NEGATIVE_INFINITY;
       for (int i = 0; i < centers.length; i++) {
    	   double margin = diameters[i] / 2;
           mins = Math.min(centers[i][0] - margin, mins);
           mins = Math.min(centers[i][1] - margin, mins);
           maxs = Math.max(centers[i][0] + margin, maxs);
           maxs = Math.max(centers[i][1] + margin, maxs);
       }
	}
	
	
	
	
	public void draw(float xPanelRight, float yy4) {
		parent.noStroke();
		parent.textAlign(PApplet.CENTER);
		parent.textSize(11);
		
		brushing=-1;
		size = 400;
        for (int i = centers.length-1; i >=0; i--) {
            double xi = (centers[i][0] - mins) / (maxs - mins);
            double yi = (centers[i][1] - mins) / (maxs - mins);
            double di = diameters[i] / (maxs - mins);

            float radius = (float) (3+ (di * size));
            float x = xPanelRight+10+(int) (xi * size);
            float y = yy4 + (int) (yi * size);
            
            if (radius>0){
            	Color color = new Color(main.ProvenanceMatrix_1_4.mappingColorRelations[i]);  
                
            	
            	if (isActive[i])
            		parent.fill(color.getRed(),color.getGreen(),color.getBlue(),180);
            	else
            		parent.fill(color.getRed(),color.getGreen(),color.getBlue(),40);
           
            	// Check brushing
              	if (PApplet.dist(x, y, parent.mouseX, parent.mouseY)<radius/2 && brushing<0){
        	   		parent.fill(color.getRed(),color.getGreen(),color.getBlue());
            	   	brushing=i;
                }
              
        	   	parent.ellipse(x , y , radius, radius);
        	   	if (isActive[i])
                	parent.fill(0,200);
        	   	else 
        	   		parent.fill(0,30);
        	    // draw the number of relationships when brushing
        	    if (brushing==i){
        	    	parent.fill(0);
        	    	parent.text((int)areas[i]+" articulations", x , y+4);
                }
        	    else{
        	    	parent.text(labels[i], x , y+4);
            	}
           }
        }
        parent.fill(Color.GRAY.getRGB());
		parent.textSize(14);
		parent.textAlign(PApplet.LEFT);
	}

	
	 public static Color rainbow(double value, float transparency) {
	        /* blue to red, approximately by wavelength */
	        float v = (float) value * 255.f;
	        float vmin = 0;
	        float vmax = 255;
	        float range = vmax - vmin;

	        if (v < vmin + 0.25f * range)
	            return new Color(0.f, 4.f * (v - vmin) / range, 1.f, transparency);
	        else if (v < vmin + 0.5 * range)
	            return new Color(0.f, 1.f, 1.f + 4.f * (vmin + 0.25f * range - v) / range, transparency);
	        else if (v < vmin + 0.75 * range)
	            return new Color(4.f * (v - vmin - 0.5f * range) / range, 1.f, 0, transparency);
	        else
	            return new Color(1.f, 1.f + 4.f * (vmin + 0.75f * range - v) / range, 0, transparency);
	    }
	 
	public boolean mouseClicked() {
		if (brushing>=0){
			 main.ProvenanceMatrix_1_4.currentRelation = brushing;
			 isActive[brushing] = !isActive[brushing];
			 return true;
		}
		else{
			main.ProvenanceMatrix_1_4.currentRelation = -2;
			return false;
		}	
	}

	
	
	// zoom in or out:
	void mouseWheel(int delta) {
		if (delta > 0) {
		} else if (delta < 0) {
		}
	}

}
