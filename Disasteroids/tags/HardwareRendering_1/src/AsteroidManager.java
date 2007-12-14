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
	
	private LinkedList<Asteroid> theAsteroids;
	private Queue<Asteroid> toBeAdded;
	
	public AsteroidManager()
	{
		theAsteroids=new LinkedList<Asteroid>();
		toBeAdded=new LinkedList<Asteroid>();
	}
	
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
									   gBuff,
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
										   gBuff,
										   this));

		}

	}
	
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
	
	public void add(Asteroid a)
	{
		toBeAdded.add(a);
	}
	
	public int size()
	{
		return theAsteroids.size();
	}
	
	public void clear()
	{
		theAsteroids=new LinkedList<Asteroid>();
	}
}