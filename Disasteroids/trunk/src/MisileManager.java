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
        
        /**
         * The current initial speed for <code>Misile</code>s in this manager
         * @since Classic
         */
	private double speed=10;
        
        /**
         * The current probability of a huge blast in <code>Misile</code>s of this manager
         * @since Classic
         */
	private int hugeBlastProb=5;
        
        /**
         * The current size of huge blasts
         * @since Classic
         */
	private int hugeBlastSize=50;
        
        /**
         * The current probability that <code>Misile</code>s of this manager will split
         * @since Classic
         */
	private int probPop=2000;
        
        /**
         * The number of <code>Misile</code>s generated when splitting occurs
         * @since Classic
         */
	private int popQuantity=5;
	
        /**
         * The list of currently valid <code>Misile</code>s
         * @since Classic
         */
	private LinkedList<Misile> theMisiles = new LinkedList<Misile>();
        
        /**
         * The list of <code>Misile</code>s waiting to be added
         * @since Classic
         */
	private Queue<Misile> toBeAdded = new LinkedList<Misile>();

	/**
         * Gets all currently valid <code>Misile</code>s
         * @return All currently valid <code>Misile</code>s
         * @since Classic
         */
	public synchronized LinkedList<Misile> getMisiles()
	{return theMisiles;}
	
        /**
         * Iterates through each <code>Misile</code> and either removes it or instructs it to act
         * @since Classic
         */
	public synchronized void act()
	{
		Graphics g=AsteroidsFrame.getGBuff();
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

        /**
         * Instructs each <code>Misile</code> to explode, without splitting
         * @since Classic
         */
	public void explodeAll()
	{
		int probPopTemp=probPop;
		probPop=Integer.MAX_VALUE;
		for(Misile m: theMisiles)
			m.explode();
		probPop=probPopTemp;
	}

        /**
         * Creates and prepares to add a <code>Misile</code> with the specified properties
         * @param x The x coordinate
         * @param y The y coordinate
         * @param angle The angle the <code>Misile</code> will be pointing, not necessarily the angle it will be traveling
         * @param dx The x component of velocity
         * @param dy The y component of velocity (up is negative)
         * @param col The <code>Color</code> of the <code>Misile</code>
         * @return <code>true</code> if and only if the <code>Misile</code> was successfully added
         * @since Classic
         */
	public boolean addMisile(int x, int y, double angle, double dx, double dy, Color col)
	{
		if(theMisiles.size()>1000)
			return false;
		return toBeAdded.add(new Misile(this,x,y,angle,dx,dy,col));
	}
	
        /**
         * Sets the probability of a huge blast
         * @param newProb The new probability
         * @since Classic
         */
	public void setHugeBlastProb(int newProb)
	{hugeBlastProb=newProb;}
	
        /**
         * Sets the size of huge blasts
         * @param newSize The new size
         * @since Classic
         */
	public void setHugeBlastSize(int newSize)
	{hugeBlastSize=newSize;}
	
        /**
         * Sets the probability of <code>Misile</code>s splitting
         * @param newProb The new probability
         * @since Classic
         */
	public void setProbPop(int newProb)
	{probPop=newProb;}
	
        /**
         * Increases the number of new <code>Misile</code>s added when a split occurs
         * @param increase The number to increase the current pop quantity by
         * @since Classic
         */
	public void increasePopQuantity(int increase)
	{popQuantity+=increase;}
		
        /**
         * Sets the new default speed
         * @param newSpeed The new speed
         * @since Classic
         */
	public void setSpeed(int newSpeed)
	{speed=newSpeed;}
	
        /**
         * Increases the default speed
         * @param dSpeed The change in speed
         * @since Classic
         */
	public void increaseSpeed(int dSpeed)
	{speed+=dSpeed;}
	
        /**
         * Sets the pop quantity to a value
         * @param newQuantity The new pop quantity
         * @since Classic
         */
	public void setPopQuantity(int newQuantity)
	{popQuantity=newQuantity;}
	
        /**
         * Removes all misiles from this manager
         * @since Classic
         */
	public void clear()
	{theMisiles.clear();}
	
        /**
         * Gets the number of currently valid <code>Misile</code>s in this manager
         * @return The number of <code>Misile</code>s
         * @since Classic
         */
	public int getNumLivingMisiles()
	{return theMisiles.size();}

        /**
         * Gets the probability of a split occurring
         * @return The probability of a split
         * @since Classic
         */
	public int probPop()
	{return probPop;}
	
        /**
         * Gets the current size of a huge blast
         * @return The current size of a huge blast
         * @since Classic
         */
	public int hugeBlastSize()
	{return hugeBlastSize;}
	
        /**
         * Gets the current default speed for new <code>Misile</code>s of this manager
         * @return The current default speed
         * @since Classic
         */
	public double speed()
	{return speed;}
	
        /**
         * Gets the current probabiltiy of a huge blast
         * @return The current probabiltiy of a huge blast
         * @since Classic
         */
	public int hugeBlastProb()
	{return hugeBlastProb;}
	
        /**
         * Gets the current quantity of <code>Misile</code>s created when a split occurs
         * @return The number of <code>Misile</code>s created per split
         * @since Classic
         */
	public int popQuantity()
	{return popQuantity;}
}
