/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */

import java.awt.*;
import javax.swing.JOptionPane;
import java.awt.event.*;

public class MenuFrame extends Frame implements KeyListener {

	private int choice; // 0 - single - 1 - multi

	private static Graphics gBuff;
	private boolean isFirst=true;
	private Graphics g;
	private static Image virtualMem;
    public MenuFrame() {
    	//this.addKeyListener(this);
    	this.choice = 0;
    	Dimension screenSize =
          Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2,
                    screenSize.height/2);
    }
    
    public void init(Graphics g) {
    	this.g=g;
		virtualMem=createImage(400,200);
		gBuff=virtualMem.getGraphics();
		if(isFirst)
		{
			this.addKeyListener(this);
			virtualMem=createImage(getWidth(),getHeight());
			gBuff=virtualMem.getGraphics();
			isFirst=false;
		}	
    }
    
    public void paint(Graphics g) {
    	this.g=g;
			if(isFirst)
				init(g);
		if(virtualMem==null)
				init(g);
    	gBuff.setColor(Color.GRAY);
    	gBuff.fillRect(0, 0, 400, 200);
    	gBuff.setColor(Color.BLACK);
    	gBuff.setFont(new Font("Tahoma", Font.BOLD, 36));
    	gBuff.drawString("DISASTEROIDS!",60,75);

		
		// Singleplayer
		gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
		if(this.choice == 0) {
			gBuff.setFont(new Font("Tahoma", Font.BOLD, 14));
			int[] xp = {150, 150, 155};
			int[] yp = {100, 110, 105};
			gBuff.fillPolygon(new Polygon(xp, yp, 3));
		}
		gBuff.drawString("Singleplayer",160,110);
		
		// Hoster
		gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
		if(choice == 1) {
			gBuff.setFont(new Font("Tahoma", Font.BOLD, 14));
			int[] xp = {130, 130, 135};
			int[] yp = {125, 135, 130};
			gBuff.fillPolygon(new Polygon(xp, yp, 3));
		}
		gBuff.drawString("Multiplayer (hoster)",140,135);
		
		// Client
		gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
		if(choice == 2) {
			gBuff.setFont(new Font("Tahoma", Font.BOLD, 14));
			int[] xp = {135, 135, 140};
			int[] yp = {150, 160, 155};
			gBuff.fillPolygon(new Polygon(xp, yp, 3));
		}
		gBuff.drawString("Multiplayer (client)",145,160);
		gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		// Exit
		gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
		if(choice == 3) {
			gBuff.setFont(new Font("Tahoma", Font.BOLD, 14));
			int[] xp = {175, 175, 180};
			int[] yp = {175, 185, 180};
			gBuff.fillPolygon(new Polygon(xp, yp, 3));
		}
		gBuff.drawString("Exit",185,185);
		
		g.drawImage(virtualMem,0,0,this);
		repaint();
		
		
    	
    }
    
    public void keyReleased(KeyEvent e){}	
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if((key == 10) || (key == 32)) {
			if(choice == 3)		
				System.exit(0);
			else
				Running.exitMenu(choice);
		}
		else {
			if(key == 38) {
				this.choice -= 1;
				if(choice < 0) choice = 3;
			}
			else
			    this.choice = (choice + 1) % 4;
		}
	}
	
	public void repaint(Graphics g) {
		paint(g);
	}
	public void update(Graphics g)
	{
		//System.out.println(timeStep);
		paint(g);
	}
    
    
}