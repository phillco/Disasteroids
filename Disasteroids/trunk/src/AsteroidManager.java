/*
 * DISASTEROIDS
 * AsteroidManger.java
 */

import java.awt.Graphics;
import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

/**
 * Manger of the level's asteroids.
 * @author Andy Kooiman
 */
public class AsteroidManager
{
    /**
     * A list of all currently valid <code>Asteroid</code>s.
     * @since Classic
     */
    private LinkedList<Asteroid> theAsteroids;

    /**
     * A list of all pending <code>Asteroid</code>s.
     * @since Classic
     */
    private Queue<Asteroid> toBeAdded;

    /**
     * Constructs a new <code>AsteroidManager</code>.
     * @author Andy Kooiman
     * @since Classic
     */
    public AsteroidManager()
    {
        theAsteroids = new LinkedList<Asteroid>();
        toBeAdded = new LinkedList<Asteroid>();
    }

    /**
     * Creates a level's <code>Asteroid</code>s.
     * @param level The level which this asteroid field should reflect
     * @author Andy Kooiman
     * @since Classic
     */
    public void setUpAsteroidField( int level )
    {

        Random rand = RandNumGen.getAsteroidInstance();
        int numBonuses = 0;
        for ( int numAsteroids = 0; numAsteroids < ( level + 1 ) * 2; numAsteroids++ )
        {
            theAsteroids.addFirst( new Asteroid( rand.nextInt( AsteroidsFrame.GAME_WIDTH ),
                                                 rand.nextInt( AsteroidsFrame.GAME_HEIGHT ),
                                                 rand.nextDouble() * 6 - 3,
                                                 rand.nextDouble() * 6 - 3,
                                                 rand.nextInt( 150 ) + 25,
                                                 rand.nextInt(level*10+10)-9,
                                                 this ) );
            if ( rand.nextInt( 10 ) == 1 )
                numBonuses++;
        }
        for ( int numAsteroids = 0; numAsteroids < numBonuses; numAsteroids++ )
        {
            theAsteroids.addFirst( new BonusAsteroid( rand.nextInt( AsteroidsFrame.GAME_WIDTH ),
                                                      rand.nextInt( AsteroidsFrame.GAME_HEIGHT ),
                                                      rand.nextDouble() * 6 - 3,
                                                      rand.nextDouble() * 6 - 3,
                                                      rand.nextInt( 150 ) + 25,
                                                      rand.nextInt(level*10+10)-9,
                                                      this ) );

        }
    }

    /**
     * Removes all faulty <code>Asteroid</code>s and instructs all others to act.
     * @author Andy Kooiman
     * @since Classic
     */
    public void act()
    {
        ListIterator<Asteroid> itr = theAsteroids.listIterator();
        while ( itr.hasNext() )
        {
            Asteroid a = itr.next();
            if ( a.shouldRemove() )
                itr.remove();
            else
                a.act();
        }

        while ( !toBeAdded.isEmpty() )
            theAsteroids.addLast( toBeAdded.remove() );
    }

    public void draw(Graphics g)
    {
        ListIterator<Asteroid> itr = theAsteroids.listIterator();
        while ( itr.hasNext() )
            itr.next().draw(g);
    }

    /**
     * Queues an <code>Asteroid</code> to be added to <code>this</code>.
     * @param a The <code>Asteroid</code> to be added.
     * @author Andy Kooiman
     * @since Classic
     */
    public void add( Asteroid a )
    {
        toBeAdded.add( a );
    }

    /**
     * Returns the number of <code>Asteroid</code>s on the level.
     * @return The number of <code>Asteroid</code>s.
     * @author Andy Kooiman
     * @since Classic
     */
    public int size()
    {
        return theAsteroids.size();
    }

    /**
     * Removes all current and pending <code>Asteroid</code>s.
     * @author Andy Kooiman
     * @since Classic
     */
    public void clear()
    {
        theAsteroids = new LinkedList<Asteroid>();
    }
}
