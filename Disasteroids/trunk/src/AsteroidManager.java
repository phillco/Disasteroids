/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */
import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;
import java.awt.*;
//import java.util.ConcurrentModificationException;

public class AsteroidManager
{
	
	LinkedList<Asteroid> theAsteroids;
	LinkedList<Asteroid> toBeAdded;
	
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
		
		itr=toBeAdded.listIterator();
		while(itr.hasNext())
		{
			theAsteroids.addLast(itr.next());
			itr.remove();
		}
		
	}
	
	public void add(Asteroid a)
	{
		toBeAdded.addFirst(a);
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