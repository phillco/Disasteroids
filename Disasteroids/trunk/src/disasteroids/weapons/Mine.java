/*
 * DISASTEROIDS
 * Mine.java
 */
package disasteroids.weapons;

import disasteroids.*;
import disasteroids.gui.AsteroidsFrame;
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

    /**
     * Whether we've acquired a target.
     */
    private boolean shouldAccelerate = false;

    private MineManager parent;

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
            for ( int id : Game.getInstance().getObjectManager().getAllIds() )
            {
                GameObject go = Game.getInstance().getObjectManager().getObject( id );
                if ( go instanceof BlackHole || ( go instanceof Ship && (((Ship) go) == parent.getParent() )))
                    continue;

                if ( Util.getDistance( this, Game.getInstance().getObjectManager().getObject( id ) ) < parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() )
                {
                    explode();
                    break;
                }
            }
        }

        /*
        // Accelerate towards nearby targets.
        if ( isArmed() )
        {

        // First create a set of everything nearby.
        Set<GameObject> closeObjects = new HashSet<GameObject>();
        for ( int id : Game.getInstance().getObjectManager().getAllIds() )
        {
        if ( Game.getInstance().getObjectManager().getObject( id ) instanceof BlackHole )
        continue;

        if ( Util.getDistance( this, Game.getInstance().getObjectManager().getObject( id )) < parent.sight() )
        closeObjects.add( Game.getInstance().getObjectManager().getObject( id ) );
        }

        // Next, accelerate.
        shouldAccelerate = !closeObjects.isEmpty();
        if ( shouldAccelerate )
        {
        for ( GameObject go : closeObjects )
        {
        double angle = Math.atan( ( go.getY() - getY() ) / ( go.getX() - getX() ) );
        if ( go.getX() < getX() )
        angle += Math.PI;
        double magnitude = 10.0 / Math.sqrt( ( Math.pow( go.getX() - getX(), 2 ) + Math.pow( go.getY() - getY(), 2 ) ) );
        magnitude = Math.min( magnitude, 1 );//regulate the acceleration for (essentially) dividing by zero

        setVelocity( getDx() + magnitude * Math.cos( angle ), getDy() + magnitude * Math.sin( angle ) );
        }
        }
        }*/

        if ( isExploding() )
            explosionSize += 10;

        if ( age > 2500 || explosionSize >= parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() )
            parent.remove( this );
    }

    /**
     * Returns the damage caused by this <code>Mine</code>. Will be zero if still arming.
     */
    public int getDamage()
    {
        if ( isArmed() )
            return 400;
        else
            return 0;
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
            AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), explosionSize );
            AsteroidsFrame.frame().drawCircle( g, color, (int) getX(), (int) getY(), parent.getBonusValue( parent.BONUS_EXPLODERADIUS ).getValue() );
        }
        else if ( isArmed() )
        {
            AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), 10 );
            AsteroidsFrame.frame().fillCircle( g, shouldAccelerate ? Color.red : Color.black, (int) getX(), (int) getY(), 4 );
        }
        else
        {
            double multiplier = age / 100.0;
            Color outline = new Color( (int) ( color.getRed() * multiplier ), (int) ( color.getGreen() * multiplier ), (int) ( color.getBlue() * multiplier ) );
            AsteroidsFrame.frame().fillCircle( g, outline, (int) getX(), (int) getY(), 10 );
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
