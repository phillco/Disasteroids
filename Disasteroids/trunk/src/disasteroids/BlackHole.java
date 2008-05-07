/*
 * DISASTEROIDS
 * BlackHole.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.ImageLibrary;
import disasteroids.weapons.Unit;
import disasteroids.weapons.Weapon;
import java.awt.Color;
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
     * Ships are attracted from a radius of twice this.
     */
    final static int ATTRACTION_RADIUS = 400;

    /**
     * The number of objects to 'eat' before removing ourselves.
     */
    final static int NUM_TO_EAT = 30;

    /**
     * The pulling power this black hole exerts, in pixels/second^2.
     */
    private int power = Util.getRandomGenerator().nextInt( 9 ) + 8;

    /**
     * How many objects we've destroyed.
     */
    private int numEaten = 0;

    public BlackHole( double x, double y )
    {
        super( x, y, 0, 0 );
        Game.getInstance().blackHoles.add( this );
    }

    /**
     * Finds everything nearby - and SUUUCKS!
     */
    public void act()
    {
        // Find nearby objects.
        Set<GameObject> victims = getNearbyVictims();

        // Pull each one towards us.
        for ( GameObject go : victims )
        {
            double angle = Util.getAngle( this, go );
            double magnitude = Math.min( power / Util.getDistance( this, go ), 1 );
            go.setVelocity( go.getDx() + magnitude * Math.cos( angle ), go.getDy() + magnitude * Math.sin( angle ) );

            // Too close to the center! Destroy him!
            if ( isPrey( go ) && Util.getDistance( this, go ) < 35 )
            {
                go.inBlackHole();
                ++numEaten;
            }
        }

        // Remove after we've eaten enough, for gameplay reasons.
        if ( numEaten > NUM_TO_EAT )
            Game.getInstance().removeObject( this );
    }

    /**
     * Returns a set of all nearby game objects.
     * @see BlackHole#ATTRACTION_RADIUS
     */
    private Set<GameObject> getNearbyVictims()
    {
        Set<GameObject> closeObjects = new HashSet<GameObject>();

        // Add asteroids.
        for ( Asteroid ast : Game.getInstance().getAsteroidManager().getAsteroids() )
            if ( Util.getDistance( this, ast ) < ATTRACTION_RADIUS )
                closeObjects.add( ast );

        // Add players, aliens, stations, etc.
        for ( GameObject go : Game.getInstance().gameObjects )
            if ( Util.getDistance( this, go ) < ( go instanceof Ship ? ATTRACTION_RADIUS * 2 : ATTRACTION_RADIUS ) )
                closeObjects.add( go );

        // Add the units of weapons.
        for ( ShootingObject s : Game.getInstance().shootingObjects )
            for ( Weapon w : s.getManagers() )
                for ( Unit u : w.getUnits() )
                    if ( Util.getDistance( this, u ) < ATTRACTION_RADIUS )
                        closeObjects.add( u );

        // Remove anything that we shouldn't eat.
        for ( Iterator<GameObject> i = closeObjects.iterator(); i.hasNext();)
            if ( !isPrey( i.next() ) )
                i.remove();

        return closeObjects;
    }

    /**
     * Returns whether we should attract <code>victim</code>.
     * Normally true, except for things like invincible ships and other black holes.
     */
    private boolean isPrey( GameObject victim )
    {
        return !( victim instanceof Ship && ( (Ship) victim ).cannotDie() ||
                victim instanceof BlackHole );
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().drawImage( g, ImageLibrary.getBlackHole(), (int) getX(), (int) getY() );
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( numEaten );
        stream.writeInt( power );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     */
    public BlackHole( DataInputStream stream ) throws IOException
    {
        super( stream );
        numEaten = stream.readInt();
        power = stream.readInt();
        Game.getInstance().blackHoles.add( this );

    }
}
