/*
 * DISASTEROIDS
 * AsteroidManger.java
 */

import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manger of the level's asteroids.
 * @author Andy Kooiman
 */
public class AsteroidManager implements Serializable
{

    /**
     * A list of all currently valid <code>Asteroid</code>s.
     * @since Classic
     */
    private ConcurrentLinkedQueue<Asteroid> theAsteroids;

    /**
     * Constructs a new <code>AsteroidManager</code>.
     * @author Andy Kooiman
     * @since Classic
     */
    public AsteroidManager()
    {
        theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
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
            theAsteroids.add( new Asteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                                 rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                                 rand.nextDouble() * 6 - 3,
                                                 rand.nextDouble() * 6 - 3,
                                                 rand.nextInt( 150 ) + 25,
                                                 rand.nextInt( level * 10 + 10 ) - 9,
                                                 this ) );
            if ( rand.nextInt( 10 ) == 1 )
                numBonuses++;
        }
        for ( int numAsteroids = 0; numAsteroids < numBonuses; numAsteroids++ )
        {
            theAsteroids.add( new BonusAsteroid( rand.nextInt( Game.getInstance().GAME_WIDTH ),
                                                      rand.nextInt( Game.getInstance().GAME_HEIGHT ),
                                                      rand.nextDouble() * 6 - 3,
                                                      rand.nextDouble() * 6 - 3,
                                                      rand.nextInt( 150 ) + 25,
                                                      rand.nextInt( level * 10 + 10 ) - 9,
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
        Iterator<Asteroid> itr = theAsteroids.iterator();
        while ( itr.hasNext() )
        {
            Asteroid a = itr.next();
            if ( a.shouldRemove() )
                itr.remove();
            else
                a.act();
        }
    }

    public void draw( Graphics g )
    {
        for( Asteroid a : theAsteroids)
            a.draw(g);
    }

    /**
     * Queues an <code>Asteroid</code> to be added to <code>this</code>.
     * @param a The <code>Asteroid</code> to be added.
     * @author Andy Kooiman
     * @since Classic
     */
    public void add( Asteroid a )
    {
        theAsteroids.add(a);
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
        theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
    }
    
    @Override
    public String toString()
    {
        String returnString = getClass().getName() + "@" + Integer.toHexString(hashCode()) + "\n[";
        for(Asteroid a : theAsteroids)
            returnString += a.toString()+"\n";
        returnString += "]";
        return returnString;
        
    }
    
    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param d     the stream to write to (sent to the client)
     * @since December 29, 2007
     */
    public void flatten(DataOutputStream stream) throws IOException
    {        
        // Write asteroid count.
        stream.writeInt(theAsteroids.size());
        
        // Write asteroids.
        for( Asteroid a : theAsteroids )
            a.flatten(stream);
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @since December 29, 2007
     */
    public AsteroidManager( DataInputStream stream ) throws IOException
    {
        this.theAsteroids = new ConcurrentLinkedQueue<Asteroid>();
        int size = stream.readInt();
        for(int i = 0; i < size; i++)
            theAsteroids.add(new Asteroid(stream));
    }
    
    
}
