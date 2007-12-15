/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.awt.Graphics;
import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;


public class AsteroidManager
{
	/**
         * A list of all currently valid <code>Asteroid</code>s
         * @since Classic
         */
	private LinkedList<Asteroid> theAsteroids;
        
        /**
         * A list of all pending <code>Asteroid</code>s
         * @since Classic
         */
	private Queue<Asteroid> toBeAdded;
	
        /**
         * Constructs a new <code>AsteroidManager</code>
         * @since Classic
         */
	public AsteroidManager()
	{
		theAsteroids=new LinkedList<Asteroid>();
		toBeAdded=new LinkedList<Asteroid>();
	}
	
        /**
         * 
         * @param level The level which this asteroid field should reflect
         * @param gBuff The <code>Graphics</code> context where the <code>Asteroid</code>s should be drawn
         * @since Classic
         */
	public void setUpAsteroidField(int level, Graphics gBuff)
	{

		Random rand=RandNumGen.getAsteroidInstance();
		int numBonuses=0;
		AsteroidsFrame env=Running.environment();
		for(int numAsteroids=0; numAsteroids<(level+1)*2; numAsteroids++)
		{
			theAsteroids.addFirst(new Asteroid(rand.nextInt(env.getWidth()),
									   rand.nextInt(env.getHeight()),
									   rand.nextDouble()*6-3,
									   rand.nextDouble()*6-3,
									   rand.nextInt(150)+25,
									   this));
			if(rand.nextInt(10)==1)
				numBonuses++;
		}
		for(int numAsteroids=0; numAsteroids<numBonuses; numAsteroids++)
		{
				theAsteroids.addFirst(new BonusAsteroid(rand.nextInt(env.getWidth()),
										   rand.nextInt(env.getHeight()),
										   rand.nextDouble()*6-3,
										   rand.nextDouble()*6-3,
										   rand.nextInt(150)+25,
										   this));

		}

	}
	
        /**
         * Removes all faulty <code>Asteroid</code>s and instructs all others to act
         * @since Classic
         */
	public void act()
	{
		ListIterator<Asteroid> itr= theAsteroids.listIterator();
		while(itr.hasNext())
		{
			Asteroid a=itr.next();
			if(a.shouldRemove())
				itr.remove();
			else
				a.act();
		}
		
		while(!toBeAdded.isEmpty())
			theAsteroids.addLast(toBeAdded.remove());

		
	}
	
        /**
         * Queues an <code>Asteroid</code> to be added to <code>this</code>
         * @param a The <code>Asteroid</code> to be added
         * @since Classic
         */
	public void add(Asteroid a)
	{
		toBeAdded.add(a);
	}
	
        /**
         * Gets the number of <code>Asteroid</code>s
         * @return the number of <code>Asteroid</code>s
         * @since Classic
         */
	public int size()
	{
		return theAsteroids.size();
	}
	
        /**
         * Removes all current and pending <code>Asteroid</code>s
         * @since Classic
         */
	public void clear()
	{
		theAsteroids=new LinkedList<Asteroid>();
                toBeAdded=new LinkedList<Asteroid>();
	}
}
