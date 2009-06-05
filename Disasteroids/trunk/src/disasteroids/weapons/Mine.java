/*
 * DISASTEROIDS
 * Mine.java
 */
package disasteroids.weapons;

import disasteroids.game.Game;
import disasteroids.gameobjects.GameObject;
import disasteroids.gameobjects.BlackHole;
import disasteroids.gameobjects.Bonus;
import disasteroids.gameobjects.Ship;
import disasteroids.*;
import disasteroids.gui.MainWindow;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A mine that moves towards nearby targets and explodes violently on impact.
 * @author Andy Kooiman
 */
public class Mine extends Unit
{
    /**
     * The size of the explosion. If 0, we're not exploding.
     */
    private int explosionSize = 0;

    private MineManager parent;

    private GameObject myTarget;

    /**
     * Creates a new <code>Minde</code>
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param col The <code>Color</code> of the outside ring.
     */
    public Mine( MineManager parent, Color color, double x, double y, double dx, double dy )
    {
        super( color, x, y, dx, dy );
        this.parent = parent;
    }

    @Override
    public void act()
    {
        super.act();
        decelerate( .95 );

        // Explode if an object comes too close.
        if ( isArmed() )
        {
            for ( long id : Game.getInstance().getObjectManager().getAllIds() )
            {
                GameObject go = Game.getInstance().getObjectManager().getObject( id );
                if ( go instanceof BlackHole || go instanceof Bonus || ( go instanceof Ship && ( ( (Ship) go ).cannotDie() || ( (Ship) go ) == parent.getParent() ) ) )
                    continue;

                if ( Util.getDistance( this, Game.getInstance().getObjectManager().getObject( id ) ) < parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() )
                {
                    explode();
                    break;
                }
            }
        }

        // Accelerate towards nearby targets.
        if ( ( parent.getBonusValue( parent.BONUS_TRACKING ).getValue() == 1 ) && isArmed() )
        {
            // Is our current target out of sight?
            //if ( myTarget != null && Util.getDistance( this, myTarget ) > parent.sight() )
            {
                parent.releaseTarget( myTarget );
                myTarget = null;
            }

            // Get a new target by finding the closest object.
            if ( myTarget == null )
            {
                GameObject closestObject = null;
                for ( long id : Game.getInstance().getObjectManager().getAllIds() )
                {
                    GameObject go = Game.getInstance().getObjectManager().getObject( id );

                    if ( go instanceof BlackHole || go instanceof Bonus || ( go instanceof Ship && ( ( (Ship) go ) == parent.getParent() ) ) )
                        continue;

                    if ( Util.getDistance( this, go ) < parent.sight() && parent.isTargetAvailible( go ) )
                    {
                        if ( closestObject == null || ( Util.getDistance( this, go ) < Util.getDistance( this, closestObject ) ) )
                            closestObject = go;
                    }

                }
                myTarget = closestObject;
                parent.reserveTarget( myTarget );
            }

            // Next, accelerate.
            if ( myTarget != null )
            {
                double angle = Math.atan( ( myTarget.getY() - getY() ) / ( myTarget.getX() - getX() ) );
                if ( myTarget.getX() < getX() )
                    angle += Math.PI;
                double magnitude = Math.min( 0.05, Util.getDistance( this, myTarget ) / 8 );
                setVelocity( getDx() + magnitude * Math.cos( angle ), getDy() + magnitude * Math.sin( angle ) );
            }
        }

        if ( isExploding() )
            explosionSize += 10;

        if ( age > 2500 )
            explode();
        if ( explosionSize >= parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() )
            parent.remove( this );
    }

    /**
     * Returns the damage caused by this <code>Mine</code>. Will be zero if still arming.
     */
    public int getDamage()
    {
        if ( isExploding() )
            return 40;
        else if ( isArmed() )
            return 100;
        else
            return 0;
    }

    public GameObject getTarget()
    {
        return myTarget;
    }

    @Override
    public void remove()
    {
        parent.remove( this );
    }

    /**
     * Draws this <code>Mine</code> in the given context.
     * 
     * @param g The <code>Graphics</code> context in which to draw.
     */
    public void draw( Graphics g )
    {
        if ( isExploding() )
        {
            MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), explosionSize );
            MainWindow.frame().drawCircle( g, color, (int) getX(), (int) getY(), parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() );
        }
        else if ( isArmed() )
        {
            MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), 10 );
            MainWindow.frame().fillCircle( g, ( myTarget != null ) ? Color.red : Color.black, (int) getX(), (int) getY(), 4 );
        }
        else
        {
            double multiplier = age / 100.0;
            Color outline = new Color( (int) ( color.getRed() * multiplier ), (int) ( color.getGreen() * multiplier ), (int) ( color.getBlue() * multiplier ) );
            MainWindow.frame().fillCircle( g, outline, (int) getX(), (int) getY(), 10 );
        }
    }

    /**
     * Returns the distance (from the center) that we're dangerous from.
     * 
     * @return The current radius.
     */
    public double getRadius()
    {
        return isExploding() ? ( explosionSize ) : 10;
    }

    /**
     * Detonates the mine, creating a large explosion.
     */
    public void explode()
    {
        if ( !isExploding() && isArmed() )
            explosionSize = 10;
    }

    public boolean isExploding()
    {
        return explosionSize > 0;
    }

    public boolean isArmed()
    {
        return age > 100;
    }

    //                                                                            \\
    // ------------------------------ NETWORKING -------------------------------- \\
    //                                                                            \\
    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( explosionSize );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Mine( DataInputStream stream, MineManager parent ) throws IOException
    {
        super( stream );
        explosionSize = stream.readInt();

        this.parent = parent;
    }
}
