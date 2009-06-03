/*
 * DISASTEROIDS
 * BlackHole.java
 */
package disasteroids;

import disasteroids.gui.MainWindow;
import disasteroids.gui.ImageLibrary;
import disasteroids.weapons.Unit;
import disasteroids.weapons.Weapon;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A stationary black mass that sucks in everything around it.
 * Once it has destroyed a certain number of objects, it removes itself.
 * @author Phillip Cohen
 */
public class BlackHole extends GameObject
{
    /**
     * The radius of our attraction circle, in pixels.
     */
    final static int ATTRACTION_RADIUS = 500;

    /**
     * The pulling power this black hole exerts, in pixels/second^2.
     */
    private int power;

    /**
     * How many objects we have left to destroy. Can be -1 for infinite.
     */
    private int numLeftToEat, totalToEat;

    /**
     * Angle at which to draw black hole.
     */
    private double imageAngle;

    private int creationTime = 0;

    private final int TOTAL_CREATION_TIME = 500;

    public BlackHole( double x, double y )
    {
        this( x, y, Util.getGameplayRandomGenerator().nextInt( 26 ) + 8, 50 );
    }

    public BlackHole( double x, double y, int power, int numToEat )
    {
        super( x, y, 0, 0 );
        this.power = power;
        this.numLeftToEat = totalToEat = numToEat;
    }

    /**
     * Finds everything nearby - and SUUUCKS!
     */
    public void act()
    {
        // Rotate.
        imageAngle -= .05 * ( 0.5 + 3 * getSizeMultiplier() );

        if ( creationTime < TOTAL_CREATION_TIME )
            creationTime++;

        // Find nearby objects.
        Set<GameObject> victims = getNearbyVictims();

        // Pull each one towards us.
        for ( GameObject go : victims )
        {
            double angle = Util.getAngle( this, go );

            double magnitude = getSizeMultiplier() * Math.min( Math.pow( power, 1.9 ) / Math.pow( Util.getDistance( this, go ), 1.7 ), 1 );
            go.setVelocity( go.getDx() + magnitude * Math.cos( angle ), go.getDy() + magnitude * Math.sin( angle ) );

            // Too close to the center! Destroy him!
            if ( isPrey( go ) && Util.getDistance( this, go ) < 35 )
            {
                go.inBlackHole();
                if ( numLeftToEat > 0 )
                {
                    // Remove after we've eaten enough, for gameplay reasons.
                    if ( ( go instanceof Unit == false ) && ( --numLeftToEat == 0 ) )
                    {
                        Game.getInstance().getObjectManager().removeObject( this );
                        return;
                    }
                }
            }
        }

        if ( getSizeMultiplier() < 0 )
            Game.getInstance().getObjectManager().removeObject( this );
    }

    /**
     * Returns a set of all nearby game objects.
     * @see BlackHole#ATTRACTION_RADIUS
     */
    private Set<GameObject> getNearbyVictims()
    {
        Set<GameObject> closeObjects = new HashSet<GameObject>();

        // Add game objects.
        for ( long id : Game.getInstance().getObjectManager().getAllIds() )
            if ( Util.getDistance( this, Game.getInstance().getObjectManager().getObject( id ) ) < ATTRACTION_RADIUS )
                closeObjects.add( Game.getInstance().getObjectManager().getObject( id ) );

        // Add the units of weapons.
        for ( ShootingObject s : Game.getInstance().getObjectManager().getShootingObjects() )
            for ( Weapon w : s.getWeapons() )
                for ( Unit u : w.getUnits() )
                    if ( Util.getDistance( this, u ) < ATTRACTION_RADIUS )
                        closeObjects.add( u );

        // Remove anything that we shouldn't eat.
        for ( Iterator<GameObject> i = closeObjects.iterator(); i.hasNext(); )
            if ( !isPrey( i.next() ) )
                i.remove();

        return closeObjects;
    }

    private double getSizeMultiplier()
    {
        // Size follows a function based on how much we've eaten...
        double percentEaten = 1 - (double) numLeftToEat / totalToEat;
        double rampedPercentEaten = ( 0.4 * ( percentEaten + 1 ) + 6 * Math.pow( percentEaten, 2 ) - 6.8 * Math.pow( percentEaten, 3 ) );

        // Multiplied by the introduction grow-in.
        double percentGrown = (double) creationTime / TOTAL_CREATION_TIME;
        double rampedPercentGrown = 2 * percentGrown - Math.pow( percentGrown, 2 );
        return rampedPercentEaten * rampedPercentGrown;
    }

    /**
     * Returns whether we should attract <code>victim</code>.
     * Normally true, except for things like invincible ships and other black holes.
     */
    private boolean isPrey( GameObject victim )
    {
        return !( victim == this || victim instanceof Ship && ( (Ship) victim ).cannotDie() ||
                victim instanceof BlackHole );
    }

    public void draw( Graphics g )
    {
        if ( getSizeMultiplier() > 0 )
            MainWindow.frame().drawImage( g, ImageLibrary.getBlackHole(), (int) getX(), (int) getY(), imageAngle, getSizeMultiplier() );
        // I think generating a black hole is much cooler
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( numLeftToEat );
        stream.writeInt( totalToEat );
        stream.writeInt( creationTime );
        stream.writeInt( power );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     */
    public BlackHole( DataInputStream stream ) throws IOException
    {
        super( stream );
        numLeftToEat = stream.readInt();
        totalToEat = stream.readInt();
        creationTime = stream.readInt();
        power = stream.readInt();
    }
}
