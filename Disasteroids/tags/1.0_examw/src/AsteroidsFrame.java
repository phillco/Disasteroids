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
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.awt.event.*;
import java.util.Random;
import java.util.Scanner;
import java.util.LinkedList;




public class AsteroidsFrame extends Frame implements KeyListener
{
//	private static ArrayList<Asteroid> asteroids;
	private static double highScore;
	private static String highScoreName;
	private static Ship ship, ship2;
	private static Image virtualMem;
	private static Image background;
	private static Graphics gBuff;
	private static int level;
	private boolean isFirst=true;
	private Graphics g;
	private boolean paused=false;
	public static long timeStep=0;//for synchronization
	public static long otherPlayerTimeStep=0;
	public static boolean isPlayerOne;
	public static boolean isMultiplayer;
	private AsteroidManager asteroidManager=new AsteroidManager();
	private ActionManager actionManager=new ActionManager();
	
	public AsteroidsFrame(boolean isPlayer1, boolean isMult)
	{
	//	paused=true;
		isPlayerOne=isPlayer1;
		isMultiplayer=isMult;
	}
		
	
	public void init(Graphics g)
	{
		this.g=g;
		virtualMem=createImage(800,800);
		gBuff=virtualMem.getGraphics();
		if(isFirst)
		{
			this.addKeyListener(this);
			virtualMem=createImage(getWidth(),getHeight());
			background=createImage(getWidth(),getHeight());
			drawBackground();
			gBuff=virtualMem.getGraphics();

			highScore=1000000;
			highScoreName="Phillip and Andy";
			isFirst=false;
		}
//		asteroids=new ArrayList<Asteroid>();
		level=1;
		ship=new Ship(getWidth()/2,getHeight()/2,gBuff, Color.red, 5);
		if(isMultiplayer)
			ship2=new Ship(getWidth()/2-100, getHeight()/2, gBuff, Color.blue,5);
		if(!isPlayerOne)
		{
			Ship temp=ship2;
			ship2=ship;
			ship=temp;
		}
		asteroidManager.setUpAsteroidField(level, gBuff);
	}
	
		
	public void paint(Graphics g)
	{
	/*	if(Net.connectionStatus == 2) {
			this.g=g;
			if(isFirst)
				init(g);
			if(virtualMem==null)
				init(g);
				
			gBuff.setColor(Color.gray);
			gBuff.fillRect(0,0,getWidth(),getHeight());
			
			
			gBuff.setColor(Color.black);
			gBuff.setFont(new Font("Tahoma", Font.PLAIN, 14));
			if(Net.isHost) {
				gBuff.drawString("You are hosting the game.",350,300);
				gBuff.setFont(new Font("Tahoma", Font.BOLD, 14));
				gBuff.drawString("IP: "+Net.myIP,355,320);
				
				
				gBuff.setFont(new Font("Tahoma", Font.PLAIN, 12));
				gBuff.drawString("Waiting for client...",370,340);
			}
			g.drawImage(virtualMem,0,0,this);
			repaint();
		}*/
		if(false){
		updateBackground();
		gBuff.drawImage(background, 0, 0, this);
			if(paused)
				return;
			this.g=g;
			if(isFirst)
				init(g);
		//	repaint();
			if(virtualMem==null)
				init(g);
			// Draw particles under other objects to fade correcrly
			ParticleManager.drawParticles(gBuff);
			
	//		invincibilityCount--;
	
//			asteroidManager.act();
			if(ship != null)
				ship.act();
			if(ship2 != null)
				ship2.act();
			drawScore(gBuff);
			
			g.drawImage(virtualMem,0,0,this);
			if((ship != null))
			if(ship.livesLeft()<1||(ship2!=null&&ship2.livesLeft()<1))
				endGame(g);
			if(asteroidManager.size()==0)
				nextLevel();
			repaint();
		}
		if(paused)
			return;
		if(isFirst)
			init(g);

	//	if(timeStep%9==0)
		AsteroidsServer.send("t"+String.valueOf(timeStep));
		
		if(timeStep-otherPlayerTimeStep>2 && ship2!=null)//you are too far ahead
		{
			safeSleep(20);
//			AsteroidsServer.send("PING");//encourage the creation of packets to be sent
			AsteroidsServer.send("t"+String.valueOf(timeStep));
			AsteroidsServer.flush();
			repaint();
			return;
		}
		updateBackground();
		gBuff.drawImage(background, 0, 0, this);

		timeStep++;
		try
		{actionManager.act(timeStep);}
		catch(UnsynchronizedException e)
		{
			JOptionPane.showMessageDialog(null, "A fatal error has occured:\n"+e);
			System.exit(0);
		}
		ParticleManager.drawParticles(gBuff);
		asteroidManager.act();
		ship.act();
		if(ship2!=null)
			ship2.act();
		
		drawScore(gBuff);
		
		g.drawImage(virtualMem,0,0,this);
		if(ship.livesLeft()<0 && (ship2==null || ship2.livesLeft()<0))
			endGame(g);
		if(asteroidManager.size()==0)
			nextLevel();
		repaint();
	}
	
	public void updateBackground()
	{
		Graphics g=background.getGraphics();
		g.drawImage(background,0,-2,this);
		Random rand=RandNumGen.getStarInstance();
		for(int y=getHeight()-3; y<getHeight(); y++)
			for(int x=0; x<getWidth(); x++)
			{
				if(rand.nextInt(1000)<1)
					g.setColor(Color.white);
				else	
					g.setColor(Color.black);
				g.fillRect(x,y,1,1);
			}
	}
	
	public void nextLevel()
	{
		paused=true;
//		AsteroidsServer.send("t0");
//		otherPlayerTimeStep=-20;
		level++;
		ship.addLife();		
		ship.setInvincibilityCount(100);		
		ship.increaseScore(2500);				
		ship.getMisileManager().clear();		
		if(ship2!=null)
		{
			ship2.addLife();
			ship2.setInvincibilityCount(100);
			ship2.increaseScore(2500);
			ship2.getMisileManager().clear();
		}

		asteroidManager.clear();
		actionManager.clear();
		
		drawBackground();
		restoreBonusValues();
		System.out.println("Seed: " + RandNumGen.seed
			+ "\nAsteroid generated numbers:\n" + 
			RandNumGen.getAsteroidInstance().nextInt(9) + "\n"  + 
			RandNumGen.getAsteroidInstance().nextInt(9) + "\n"  + 
			RandNumGen.getAsteroidInstance().nextInt(9) + "\n"  + 
			RandNumGen.getAsteroidInstance().nextInt(9) + "\n"  + 
			RandNumGen.getAsteroidInstance().nextInt(9) + "\n"			
			);
		asteroidManager.setUpAsteroidField(level, gBuff);
		paused=false;
	}
	
	public static void staticNextLevel()
	{
		Running.environment().nextLevel();
	}
	
	private void drawScore(Graphics g)
	{
		g.setColor(ship.getColor());
		g.setFont(new Font("Tahoma", Font.PLAIN, 14));
		if(ship != null)
			g.drawString("Lives: "+ship.livesLeft(),20,50);
		g.drawString("Level: "+level,120,50);
		g.drawString("Score: "+ship.score(),200,50);
		
		if(ship2!=null)
		{//player 2
			g.setColor(ship2.getColor());
			g.drawString("Lives:"+ship2.livesLeft(), 20, 65);
			g.drawString("Score:"+ship2.score(), 200, 65);
		}			
		g.setColor(Color.green);
		
		// [PC] Do not draw high score if we don't have one.
		if((highScoreName != null) && (highScoreName != ""))
			g.drawString("High Score is "+highScore+" (by "+highScoreName+")",350,50);
		g.drawString("Asteroids: "+asteroidManager.size(),700,50);
	}
	
	private void drawBackground()
	{
		Graphics g=background.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.white);
		Random rand=RandNumGen.getStarInstance();
		for(int star=0; star<getWidth()*getHeight()/1000; star++)
		{
			g.fillRect(rand.nextInt(getWidth()), rand.nextInt(getHeight()), 1, 1);
		}
	}
	
	public void newGame()
	{
		init(g);
		if(isMultiplayer&& isPlayerOne)
			AsteroidsServer.send("ng");
	//	repaint();
	}
	
	public void endGame(Graphics g)
	{
		paused=true;
		g.setFont(new Font("Tahoma", Font.BOLD, 32));
		Driver_Sound.wheeeargh();
		for(float sat=0; sat<=1; sat+=.00005)
		{
			g.setColor(Color.getHSBColor(sat,sat,sat));
			g.drawString("Game Over",250,400);
		}
		this.setIgnoreRepaint(true);
//		JOptionPane.showMessageDialog(null,"You died with a score of "+score);
		if(ship2!=null)
		{
			if(ship.score()>ship2.score())
				JOptionPane.showMessageDialog(null, "Player One Wins");
			else
				JOptionPane.showMessageDialog(null, "Player Two Wins");
		}
		if(ship.score()>highScore||(ship2!=null && ship2.score()>highScore))
		{
			JOptionPane.showMessageDialog(null,"NEW HIGH SCORE!!!");
			if(ship2==null || ship.score()>ship2.score())
			{
				highScoreName=JOptionPane.showInputDialog(null,"Input name here:");
				AsteroidsServer.send("HS"+highScoreName);
			}
			if(ship2!=null)	
				highScore=Math.max(ship.score(), ship2.score());
			else
				highScore=ship.score();
			//if(oldHighScore>10000000)
			//{
			//	JOptionPane.showMessageDialog(null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT");
			//	try{Runtime.getRuntime().exec("C:/Windows/System32/calc");}catch(Exception e){}
			//}
		}
		newGame();
		this.setIgnoreRepaint(false);
		paused=false;
		repaint();
	}
	
		
	public static Ship getShip()
	{return ship;}
	
	public static Graphics getGBuff()
	{return gBuff;}
	
	public synchronized void keyReleased(KeyEvent e)
	{
		if(!paused || e.getKeyCode()==80)
		{
			if(e.getKeyCode() >= 37 && e.getKeyCode() <= 40)
			// Get the raw code from the keyboard
			//performAction(e.getKeyCode(), ship);
			actionManager.add(new Action(ship, 0-e.getKeyCode(), timeStep+15));
			AsteroidsServer.send("k"+String.valueOf(0-e.getKeyCode())+","+String.valueOf(timeStep+15));
			// [AK] moved to a new method to also be called by another class, receiving data from other computer
			//repaint();
		}
		repaint();
	}	
	public void keyTyped(KeyEvent e){}
	public synchronized void keyPressed(KeyEvent e)
	{
		if(!paused || e.getKeyCode()==80)
		{
			// Get the raw code from the keyboard
			//performAction(e.getKeyCode(), ship);
			actionManager.add(new Action(ship, e.getKeyCode(), timeStep+10));
			AsteroidsServer.send("k"+String.valueOf(e.getKeyCode())+","+String.valueOf(timeStep+10));
			// [AK] moved to a new method to also be called by another class, receiving data from other computer
			//repaint();
		}
		repaint();
	}
	
	public synchronized void performAction(int action, Ship actor)
	{
		// Network start ket
//		if(action == 123) {
//			Net.startNetworkPrompt();
//		}

		/*============================
		 * Decide what key was pressed
		 *==========================*/	
		switch(action)
		{
			case 32:	// Space
				if(actor.canShoot())
					actor.shoot(true);
				break; 
			case 37:	// Left Arrow
				actor.left();
				break;
			case 39:	// Right Arrow
				actor.right();
				break;
			case 38:	// Up Arrow
				actor.forward();
				break;
			case 40:	// Down Arrow
				actor.backwards();
				break;
			
			case -37:
				actor.unleft();
				break;
			case -39:
				actor.unright();
				break;
			case -38:
				actor.unforward();
				break;
			case -40:
				actor.unbackwards();
				break;
			
			case 33:	// Page Up
				actor.fullUp();
				break;
			case 34:	// Page Down
				actor.fullDown();
				break;
			case 155:	// Insert
				actor.fullLeft();
				break;
			case 127:	// Delete
				actor.fullRight();
				break;
			case 35:	// End
				actor.allStop();
				break;
			case 192:	// ~
				// Berserk!
				actor.berserk();
				break;
			case 36:	// Home
				actor.getMisileManager().explodeAll();
				break;
			case 80: //'p'
				paused=!paused;
				break;
			default:
				break;
		}
		repaint();

	}
	
	public void setOtherPlayerTimeStep(int step)
	{otherPlayerTimeStep=step;}
	
	public void update(Graphics g)
	{
		//System.out.println(timeStep);
		paint(g);
	}
	
	private void restoreBonusValues()
	{
		ship.getMisileManager().setHugeBlastProb(5);
		ship.getMisileManager().setHugeBlastSize(50);
		ship.getMisileManager().setProbPop(2000);
		ship.getMisileManager().setPopQuantity(5);
		ship.getMisileManager().setSpeed(10);
		ship.setMaxShots(10);
		
		if(ship2!=null)
		{
			ship2.getMisileManager().setHugeBlastProb(5);
			ship2.getMisileManager().setHugeBlastSize(50);
			ship2.getMisileManager().setProbPop(2000);
			ship2.getMisileManager().setPopQuantity(5);
			ship2.getMisileManager().setSpeed(10);
			ship2.setMaxShots(10);
		}
	}	
	
	public static int getLevel()
	{return level;}
	
	public static Ship getShip2()
	{return ship2;}
	
	public static void safeSleep(int milis)
	{
		try{
			Thread.sleep(milis);
		}catch(InterruptedException e){}
	}
	
	public void writeOnBackground(String message, int x, int y, Color col)
	{
		Graphics g=background.getGraphics();
		g.setColor(col);
		g.setFont(new Font("Tahoma", Font.BOLD, 9));
		g.drawString(message,x,y);
	}
	
	public ActionManager actionManager()
	{
		return actionManager;
	}
	
	public static boolean isPlayerOne()
	{
		return isPlayerOne;
	}
	
	public void setHighScore(String name)
	{
		highScoreName=name;
	}
}

