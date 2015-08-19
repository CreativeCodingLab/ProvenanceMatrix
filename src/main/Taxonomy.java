package main;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import processing.core.PApplet;
import processing.core.PImage;

import static main.ProvenanceMatrix_1_5.srcTaxonomy;
import static main.ProvenanceMatrix_1_5.trgTaxonomy;
import static main.ProvenanceMatrix_1_5.articulations;


public class Taxonomy {
	// For a gene, we have the count number of each type of relations
	public String name = "????";
	public Integrator iX, iY, iH,iW;
	public int order;
	public ArrayList<Integer> parentIndex = new ArrayList<Integer>();
	ArrayList<PImage> images= new ArrayList<PImage>();
	PApplet parent;
	public int isExpanded = 1;
	public boolean isLeaf = false;
	
	
	public Taxonomy(PApplet parent_, String name_, int order_){
		parent = parent_;
		name = name_;
		iX = new Integrator(main.ProvenanceMatrix_1_5.marginX,.5f,.1f);
		iY = new Integrator(main.ProvenanceMatrix_1_5.marginY,.5f,.1f);
		iW = new Integrator(0,.5f,.1f);
		iH = new Integrator(0,.5f,.1f);
		order = order_;
	}
	
	public void setImages() {
		String result = excutePost("http://en.wikipedia.org/wiki/"+name,"");
		String[] ps1 = result.split(".jpg\"");
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i<ps1.length;i++){
			String[] ps2 = ps1[i].split("src=\"");
			//for (int j = 0; j<ps2.length;j++){
			String str = ps2[ps2.length-1];
			if (str.startsWith("//upload.wikimedia.org") && str.length()<500){
				a.add("http:"+str+".jpg");
			}
			if (name.contains("Zamiaceae")){
			//	System.out.println(ps1[i]);
			}
			
		}
		
		for (int i = 0; i<a.size();i++){
			try{
				PImage image = parent.loadImage(a.get(i));
				image.resize(180, 180);
				images.add(image);
			}
			catch (Exception e) {
			    e.printStackTrace();
			    continue;
			} 
		}
	}
		
	public static String excutePost(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;  
		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection)url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type", 
		        "application/x-www-form-urlencoded");

		    connection.setRequestProperty("Content-Length", 
		        Integer.toString(urlParameters.getBytes().length));
		    connection.setRequestProperty("Content-Language", "en-US");  

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
		    String line;
		    while((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    return "";
		  } finally {
		    if(connection != null) {
		      connection.disconnect(); 
		    }
		  }
		}
	
	
	// Order genes by random
	public static void orderByRandom(PApplet p){	
		ArrayList<Integer> a = new ArrayList<Integer>();
		for (int i=0;i<srcTaxonomy.size();i++){
			a.add(i);
		}
		while (a.size()>0){
			int num = (int) p.random(a.size());
			srcTaxonomy.get(a.size()-1).order = a.get(num);
			a.remove(num);
		}
		a = new ArrayList<Integer>();
		for (int i=0;i<trgTaxonomy.size();i++){
			a.add(i);
		}
		while (a.size()>0){
			int num = (int) p.random(a.size());
			trgTaxonomy.get(a.size()-1).order = a.get(num);
			a.remove(num);
		}
	}
	
	
	
	public static void orderByReading(){	
		for (int i=0;i<srcTaxonomy.size();i++){
			srcTaxonomy.get(i).order =  i;
		}
		for (int i=0;i<trgTaxonomy.size();i++){
			trgTaxonomy.get(i).order =  i;
		}
	}
		
	// Order genes by name
	public static void orderByName(){	
		Map<String, Integer> unsortMap1 = new HashMap<String, Integer>();
		for (int i=0;i<srcTaxonomy.size();i++){
			unsortMap1.put(srcTaxonomy.get(i).name, i);
		}
		Map<String, Integer> treeMap1 = new TreeMap<String, Integer>(unsortMap1);
		int count1=0;
		for (Map.Entry<String, Integer> entry : treeMap1.entrySet()) {
			int inputOrder = entry.getValue();
			srcTaxonomy.get(inputOrder).order = count1;
			count1++;
		}
		
		Map<String, Integer> unsortMap2 = new HashMap<String, Integer>();
		for (int i=0;i<trgTaxonomy.size();i++){
			unsortMap2.put(trgTaxonomy.get(i).name, i);
		}
		Map<String, Integer> treeMap2 = new TreeMap<String, Integer>(unsortMap2);
		int count2=0;
		for (Map.Entry<String, Integer> entry : treeMap2.entrySet()) {
			int inputOrder = entry.getValue();
			trgTaxonomy.get(inputOrder).order = count2;
			count2++;
		}
	}
	
	
	
	
	
	public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
		// Convert Map to List
		List<Map.Entry<String, Integer>> list = 
			new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	public static Map<Integer, Integer> sortByComparator2(Map<Integer, Integer> unsortMap) {
		// Convert Map to List
		List<Map.Entry<Integer, Integer>> list = 
			new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1,
                                           Map.Entry<Integer, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	
	
	// Order by similarity *********** 
	public static void orderBySimilarity(){	
		ArrayList<Integer> processedProteins1 =  new ArrayList<Integer>();
		int curIndex1 = 0;
		srcTaxonomy.get(curIndex1).order=curIndex1;
		processedProteins1.add(curIndex1);
		int order1 = 1;
		for (int i=0;i<srcTaxonomy.size();i++){
			int similarIndex =  getSimilarGene(processedProteins1);
			if (similarIndex<0) break;  // can not find more small molecules
			srcTaxonomy.get(similarIndex).order=order1;
			order1++;
			processedProteins1.add(similarIndex);
		}
		
		ArrayList<Integer> processedProteins2 =  new ArrayList<Integer>();
		int curIndex2 = 0;
		trgTaxonomy.get(curIndex2).order=curIndex2;
		processedProteins2.add(curIndex2);
		int order2 = 1;
		for (int i=0;i<trgTaxonomy.size();i++){
			int similarIndex =  getSimilarGene2(processedProteins2);
			if (similarIndex<0) break;  // can not find more small molecules
			trgTaxonomy.get(similarIndex).order=order2;
			order2++;
			processedProteins2.add(similarIndex);
		}
	}
	
	public static void orderBySimilarity2(){	
		ArrayList<Integer> processedProteins =  new ArrayList<Integer>();
		int curIndex = 0;
		trgTaxonomy.get(curIndex).order=curIndex;
		processedProteins.add(curIndex);
		int order = 1;
		for (int i=0;i<trgTaxonomy.size();i++){
			int similarIndex =  getSimilarGene2(processedProteins);
			if (similarIndex<0) break;  // can not find more small molecules
			trgTaxonomy.get(similarIndex).order=order;
			order++;
			processedProteins.add(similarIndex);
		}
	}
	
	public static int getSimilarGene(ArrayList<Integer> a){
		float minDis = Float.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int i=0;i<srcTaxonomy.size();i++){
			int index1 = i;
			if (a.contains(index1)) continue;
			int index2 = a.get(0);
			float dis = computeDis(index1,index2, main.ProvenanceMatrix_1_5.popupOrder.slider.val);
			/*if (a.size()>1){
				index2 = a.get(a.size()-2);
				dis += computeDis(index1,index2, main.PathwayMatrixTaxo.popupOrder.slider.val);
			}*/
			if (dis<minDis){
				minDis = dis;
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	public static int getSimilarGene2(ArrayList<Integer> a){
		float minDis = Float.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int i=0;i<trgTaxonomy.size();i++){
			int index1 = i;
			if (a.contains(index1)) continue;
			int index2 = a.get(0);
			float dis = computeDis2(index1,index2, main.ProvenanceMatrix_1_5.popupOrder.slider.val);
			/*if (a.size()>1){
				index2 = a.get(a.size()-1);
				dis += computeDis2(index1,index2, main.PathwayMatrixTaxo.popupOrder.slider.val);
			}*/
			if (dis<minDis){
				minDis = dis;
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	
	@SuppressWarnings("unchecked")
	public static float computeDis(int orderReading1, int orderReading2, float val){
		float dis = 0;
		for (int i=0;i<trgTaxonomy.size();i++){
			dis += computeDisOfArrayList(articulations[orderReading1][i],articulations[orderReading2][i],val);
		}
		return dis;
	}
	
	@SuppressWarnings("unchecked")
	public static float computeDis2(int orderReading1, int orderReading2, float val){
		float dis = 0;
		for (int i=0;i<srcTaxonomy.size();i++){
			dis += computeDisOfArrayList(articulations[i][orderReading1],articulations[i][orderReading2],val);
		}
		return dis;
	}
	

	public static float computeDisOfArrayList(ArrayList<Integer> a1, ArrayList<Integer> a2, float val){
		if (a1==null && a2==null) return 0;
		else if (a1==null) return a2.size();
		else if (a2==null) return a1.size();
		
		int numCommonElements = 0;
		for (int i=0;i<a1.size();i++){
			int num1 = a1.get(i);
			if (a2.contains(num1))
				numCommonElements++;
		}
		return (a1.size()+a2.size()-val*numCommonElements);
		// main.MainViewer.popupOrder.slider.val=0    We consider total number of element;
		// main.MainViewer.popupOrder.slider.val=2    We consider only the different;
	}
	
	public static void BFS1(int index){
		if (main.ProvenanceMatrix_1_5.a1[index]==null) return;
		for (int i=0;i<main.ProvenanceMatrix_1_5.a1[index].size();i++){
			int index2 = (Integer) main.ProvenanceMatrix_1_5.a1[index].get(i);
			srcTaxonomy.get(index2).order=main.PopupOrder.countBFS1;
			main.PopupOrder.countBFS1++;
		}
		for (int i=0;i<main.ProvenanceMatrix_1_5.a1[index].size();i++){
			int index2 = (Integer) main.ProvenanceMatrix_1_5.a1[index].get(i);
			BFS1(index2);
		}
	}	
	public static void BFS2(int index){
		if (main.ProvenanceMatrix_1_5.a2[index]==null) return;
		ArrayList<Integer> childrenList2 = new ArrayList<Integer>();
		for (int i=0;i<main.ProvenanceMatrix_1_5.a2[index].size();i++){
			childrenList2.add((Integer) main.ProvenanceMatrix_1_5.a2[index].get(i));
		}
		
		
		for (int i=0;i<childrenList2.size();i++){
			int index2 = childrenList2.get(i);
			trgTaxonomy.get(index2).order=main.PopupOrder.countBFS2;
			main.PopupOrder.countBFS2++;
		}
		for (int i=0;i<childrenList2.size();i++){
			int index2 = childrenList2.get(i);
			BFS2(index2);
		}
		
		// Sort by EQUALS
		/*
		for (int i=0;i<main.PathwayMatrixTaxo.a2[index].size();i++){
			int index2 = (Integer) main.PathwayMatrixTaxo.a2[index].get(i);
			trgOntology.get(index2).order=main.PopupOrder.countBFS2;
			main.PopupOrder.countBFS2++;
		}*/
		
	}
	
	
		
	public static int computeScore(int index2, int i){
		int score = srcTaxonomy.size()+i;
		for (int j=0;j<srcTaxonomy.size();j++){
			if (articulations[j][index2]==null)
				continue;
			boolean found=false;
			for (int k=0;k<articulations[j][index2].size();k++){
				int value = (Integer) articulations[j][index2].get(k);
				if (value==0){
					score = j;
					found =  true;
					break;
				}	
			}
			if (found)
				break;
			
		}
		//System.out.println("	"+index2+"	"+pos +"	");
		//if (pos<srcTaxo.size() && pos>=0)
		//	System.out.println("		"+srcTaxo.get(pos).name);
		if (main.ProvenanceMatrix_1_5.a2[index2]!=null){
			ArrayList<Integer> childrenList4 = new ArrayList<Integer>();
			for (int i4=0;i4<main.ProvenanceMatrix_1_5.a2[index2].size();i4++){
				childrenList4.add((Integer) main.ProvenanceMatrix_1_5.a2[index2].get(i4));
			}
			for (int i4=0;i4<childrenList4.size();i4++){
				int index4 = childrenList4.get(i4);
				score += computeScore(index4,i4);
			}
		}
		
		return score;
	}
		
	
	public static int computeNum(int index2){
		if (main.ProvenanceMatrix_1_5.a2[index2]!=null){
			ArrayList<Integer> childrenList4 = new ArrayList<Integer>();
			for (int i4=0;i4<main.ProvenanceMatrix_1_5.a2[index2].size();i4++){
				childrenList4.add((Integer) main.ProvenanceMatrix_1_5.a2[index2].get(i4));
			}
			int sum=0;
			for (int i4=0;i4<childrenList4.size();i4++){
				int index4 = childrenList4.get(i4);
				sum += computeNum(index4);
			}
			return sum+1;
		}
		else
			return 1;
	}
		
	
	public static void DFS1(int index){
		srcTaxonomy.get(index).order=main.PopupOrder.countDFS1;
		main.PopupOrder.countDFS1++;
		if (main.ProvenanceMatrix_1_5.a1[index]==null) return;
		for (int i=0;i<main.ProvenanceMatrix_1_5.a1[index].size();i++){
			int index2 = (Integer) main.ProvenanceMatrix_1_5.a1[index].get(i);
			DFS1(index2);
		}
	}
	
	public static void DFS2(int index){
		trgTaxonomy.get(index).order=main.PopupOrder.countDFS2;
		main.PopupOrder.countDFS2++;
		if (main.ProvenanceMatrix_1_5.a2[index]==null) return;
		for (int i=0;i<main.ProvenanceMatrix_1_5.a2[index].size();i++){
			int index2 = (Integer) main.ProvenanceMatrix_1_5.a2[index].get(i);
			DFS2(index2);
		}
	}
	
	
	// Sort Reactions by score (average positions of proteins)
	public static Map<Integer, Integer> sortByComparator2(Map<Integer, Integer> unsortMap, boolean decreasing) {
		// Convert Map to List
		List<Map.Entry<Integer, Integer>> list = 
			new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		if (decreasing){
			Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
				public int compare(Map.Entry<Integer, Integer> o1,
	                                           Map.Entry<Integer, Integer> o2) {
						return -(o1.getValue()).compareTo(o2.getValue());
				}
			});
		}
		else{
			Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
				public int compare(Map.Entry<Integer, Integer> o1,
	                                           Map.Entry<Integer, Integer> o2) {
						return (o1.getValue()).compareTo(o2.getValue());
				}
			});
		}
 
		// Convert sorted map back to a Map
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<Integer, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}
