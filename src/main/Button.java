package main;

import processing.core.PApplet;

public class Button{
	public boolean b = false;
	public boolean s = false;
	public PApplet parent;
	public float y = 100;
	public int w = 80;
	public int h = 20;
	public float x = 0;
	public int w2 = 200;
	public int itemNum = 9;
	public String text = "";
	public Button(PApplet parent_, String str){
		parent = parent_;
		text = str;
	}
	
	
	public void draw(float x_, float y_){
		x = x_;
		y = y_;
		checkBrushing();
		parent.textSize(11);
		parent.noStroke();
		parent.fill(150);
		if (b)
			parent.fill(0);
		parent.rect(x, y, w, h);
		
			
		
		parent.fill(0);
		if (b)
			parent.fill(255);
		parent.textAlign(PApplet.CENTER);
		parent.text(text,x+w/2,y+15);
			
		
	}
	
	public void mouseClicked() {
		s = !s;
		System.out.println(s);
	}
		
	 
	public void checkBrushing() {
		int mX = parent.mouseX;
		int mY = parent.mouseY;
		if (x<=mX && mX<=x+w && y<=mY && mY<=y+h){
			b =true;
			return;
		}
		b =false;
	}
	
}