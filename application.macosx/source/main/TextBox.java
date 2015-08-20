package main;
import java.awt.Color;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;

public class TextBox {
	public boolean s = false;
	public boolean b = false;
	public PApplet parent;
	public int x = 0;
	public int y = 0;
	public int h = 18;
	public int w = 100;
	public char pKey = ' ';
	public String text = "";
	public String searchText = "";
	
	// Draw srach results
	public int nResults = 40;
	public int sIndex = -1;
	public int sColumn = 1;
	public int w2 = 500; 
	public int hBox = 760;
	public int h3 = 18;  // TextBox result high
	public ArrayList<String> nameSrc = new ArrayList<String>();;
	public ArrayList<Integer> indicesSrc = new ArrayList<Integer>();;
	public ArrayList<String> nameTrg = new ArrayList<String>();;
	public ArrayList<Integer> indicesTrg = new ArrayList<Integer>();;
	public String[] listSrc;
	public String[] listTrg;
	public int mouseOnTextList = -1;
	
	public TextBox(PApplet parent_, int x_, int y_, String text_, String[] l) {
		parent = parent_;
		x = x_;
		y = y_;
		text = text_;
		listSrc = l;
	}

	public void draw() {
		checkBrushing();
		
		parent.strokeWeight(1f);
		parent.textAlign(PApplet.LEFT);
		parent.fill(20, 20, 20);
		parent.stroke(125, 125, 125);
		if (b)
			parent.stroke(Color.PINK.getRGB());
		if (s) {
			parent.stroke(Color.WHITE.getRGB());
		}
		parent.rect(x, y, w, h);

		// Main Text
		//parent.textFont(font, 12);
		parent.textAlign(PApplet.LEFT);
		parent.textSize(11);
		parent.fill(255);
		parent.text(searchText, x+5, y + h - 5);

		// Explaining Text
		parent.textSize(11);
		parent.textAlign(PApplet.LEFT);
		parent.fill(0, 0, 0);
		parent.text(text, x, y -3);
		
		if (s)
			drawClickableSearchResults();
	}

	public void keyPressed() {
		if (s) {
			char c = (char) parent.key;
			nameSrc = new ArrayList<String>();
			indicesSrc = new ArrayList<Integer>();
			nameTrg = new ArrayList<String>();
			indicesTrg = new ArrayList<Integer>();
			if (c == 8 && searchText.length() > 0) {
				searchText = searchText.substring(0, searchText.length() - 1);
				if (searchText.length() == 1 && searchText.equals(""))
					searchText = "";
			}
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
					|| (c >= '0' && c <= '9') || (c == ' ') || (c == '-')
					|| (c == '.')) {
				searchText = searchText + parent.key;
			}

			else if (c == 10)
				searchText = "";
			// Making sure duplicates are not included
			
			for (int i = 0; i < listSrc.length; i++) {
				if (listSrc[i]!=null){
					String tolower = listSrc[i].toLowerCase();
					if (tolower.contains(searchText.toLowerCase())) {
						nameSrc.add(tolower);
						indicesSrc.add(i);
					} 
				}
			}
			for (int i = 0; i < listTrg.length; i++) {
				if (listTrg[i]!=null){
					String tolower = listTrg[i].toLowerCase();
					if (tolower.contains(searchText.toLowerCase())) {
						nameTrg.add(tolower);
						indicesTrg.add(i);
					} 
				}
			}
		}
	}

	public void checkBrushing() {
		int mX = parent.mouseX;
		int mY = parent.mouseY;
		if (x < mX && mX < x + w && y < mY && mY < y + h + 5) {
			b = true;
			return;
		}
		b = false;
	}

	public int mouseClicked() {
		if (b) {
			s = true;
		}
		else{
			s = false;
		}
		// Making Default list 
		if (searchText.equals("")){
			nameSrc = new ArrayList<String>();
			indicesSrc = new ArrayList<Integer>();
			nameTrg = new ArrayList<String>();
			indicesTrg = new ArrayList<Integer>();
			for (int i = 0; i < listSrc.length; i++) {
				if (i<nResults){
					String tolower = listSrc[i].toLowerCase();
					nameSrc.add(tolower);
					indicesSrc.add(i);
				}
			}
			for (int i = 0; i < listTrg.length; i++) {
				if (i<nResults){
					String tolower = listTrg[i].toLowerCase();
					nameTrg.add(tolower);
					indicesTrg.add(i);
				}
			}
		}
		return sIndex;
	}

	// Used for search box
	void drawClickableSearchResults() {
		float y1 = y+25;
		parent.strokeWeight(1);
		parent.stroke(200);
		parent.fill(100,100,100,250);
		parent.rect(x+w, y, w2, hBox);
		
		parent.fill(0);
		parent.textSize(14);
		parent.textAlign(PApplet.RIGHT);
		parent.text(ProvenanceMatrix_1_7.taxomY, x+w+237, y+20);
		
		parent.textAlign(PApplet.LEFT);
		parent.text(ProvenanceMatrix_1_7.taxomX, x+w+276, y+20);
		
		
		parent.textSize(11);
		int selected =-1;
		mouseOnTextList = -1;
		
		if (parent.mouseX >= x+w+14  && parent.mouseX <= x + w+w2*0.45f)
			sColumn = 1;
		else if (parent.mouseX >= x+w+w2*0.55  && parent.mouseX <= x + w+w2-14)
			sColumn = 2;
		else
			sColumn = 0;
		
		for (int i = 0; i < nResults && i < nameTrg.size(); i++) {
			if (parent.mouseX >= x+w  && parent.mouseX <= x + w+w2/2
				&& parent.mouseY > y1 && parent.mouseY <= y1+h3)
				mouseOnTextList = i;
			if (mouseOnTextList==i && sColumn==1) {
				parent.stroke(Color.PINK.getRGB());
				parent.fill(0, 0, 0);
				parent.rect(x + w+20, y1, w2*0.45f, h3-0.5f);
				
				parent.textAlign(PApplet.RIGHT);
				parent.fill(Color.PINK.getRGB());
				parent.text(nameTrg.get(i), x + w + w2*0.455f, y1 + 12);
				parent.textAlign(PApplet.CENTER);
				y1 += h3;
				selected = indicesTrg.get(i);
				
			} else {
				parent.noStroke();
				parent.fill(0, 0, 0);
				parent.rect(x + w + 20, y1, w2*0.45f, h3-0.5f);
				parent.textAlign(PApplet.RIGHT);
				String tt1 = nameTrg.get(i);
				parent.fill(220);
				parent.text(tt1, x + w + w2*0.455f, y1 + 12);
				
				parent.textAlign(PApplet.CENTER);
				y1 += h3;
			}
		}
		y1 = y+25;
		for (int i = 0; i < nResults && i < nameSrc.size(); i++) {
			if (parent.mouseX >= x+w+w2/2  && parent.mouseX <= x + w+w2
				&& parent.mouseY > y1 && parent.mouseY <= y1+h3)
				mouseOnTextList = i;
			if (mouseOnTextList==i && sColumn==2) {
				parent.stroke(Color.PINK.getRGB());
				parent.fill(0, 0, 0);
				parent.rect(x + w + w2*0.55f, y1, w2*0.41f, h3-0.5f);
				
				parent.textAlign(PApplet.LEFT);
				parent.fill(Color.PINK.getRGB());
				parent.text(nameSrc.get(i), x + w + w2*0.57f, y1 + 12);
				parent.textAlign(PApplet.CENTER);
				y1 += h3;
				selected = indicesSrc.get(i);
			} else {
				parent.noStroke();
				parent.fill(0, 0, 0);
				parent.rect(x + w + w2*0.55f, y1, w2*0.41f, h3-0.5f);
				parent.textAlign(PApplet.LEFT);
				String tt1 = nameSrc.get(i);
				parent.fill(220);
				parent.text(tt1, x + w + w2*0.57f, y1 + 12);
				
				parent.textAlign(PApplet.CENTER);
				y1 += h3;
			}
		}
		sIndex = selected;
		
	}
}