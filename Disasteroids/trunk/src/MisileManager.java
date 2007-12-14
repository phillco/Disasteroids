/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;



public class MisileManager
{
	private Graphics g;
	private double speed=10;
	private int hugeBlastProb=5;
	private int hugeBlastSize=50;
	private int probPop=2000;
	private int popQuantity=5;
	
	private LinkedList<Misile> theMisiles = new LinkedList<Misile>();
	private Queue<Misile> toBeAdded = new LinkedList<Misile>();

	
	public synchronized LinkedList<Misile> getMisiles()
	{return theMisiles;}
	
	public synchronized void act()
	{
		g=AsteroidsFrame.getGBuff();
		ListIterator<Misile> iter = theMisiles.listIterator();
		while(iter.hasNext())
		{
			Misile m = iter.next();
				if(m.needsRemoval())
					iter.remove();
				else
					m.act(g);
		}
		
		while(!toBeAdded.isEmpty())
		{
			theMisiles.add(toBeAdded.remove());
		}
		
	}

	public void explodeAll()
	{
		int probPopTemp=probPop;
		probPop=Integer.MAX_VALUE;
		for(Misile m: theMisiles)
			m.explode();
		probPop=probPopTemp;
	}

	public boolean addMisile(int x, int y, double angle, double dx, double dy, Color col)
	{
		if(theMisiles.size()>1000)
			return false;
		return toBeAdded.add(new Misile(this,x,y,angle,dx,dy,col));
	}
	
	public void setHugeBlastProb(int newProb)
	{hugeBlastProb=newProb;}
	
	public void setHugeBlastSize(int newSize)
	{hugeBlastSize=newSize;}
	
	public void setProbPop(int newProb)
	{probPop=newProb;}
	
	public void increasePopQuantity(int increase)
	{popQuantity+=increase;}
		
	public void setSpeed(int newSpeed)
	{speed=newSpeed;}
	
	public void increaseSpeed(int dSpeed)
	{speed+=dSpeed;}
	
	public void setPopQuantity(int newQuantity)
	{popQuantity=newQuantity;}
	
	public void clear()
	{theMisiles.clear();}
	
	public int getNumLivingMisiles()
	{return theMisiles.size();}

	public int probPop()
	{return probPop;}
	
	public int hugeBlastSize()
	{return hugeBlastSize;}
	
	public double speed()
	{return speed;}
	
	public int hugeBlastProb()
	{return hugeBlastProb;}
	
	public int popQuantity()
	{return popQuantity;}
}