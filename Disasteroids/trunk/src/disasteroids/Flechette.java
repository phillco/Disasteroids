/*
 * DISASTEROIDS
 * Flechette.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A weak particle fired in the dozens.
 * @author Andy Kooiman
 */
class Flechette extends Weapon.Unit
{
    private FlechetteManager parent;

    public Flechette( FlechetteManager parent, Color color, double x, double y, double dx, double dy, double angle )
    {
        super( color, x, y, dx + parent.getSpeed() * Math.cos( angle ), dy - parent.getSpeed() * Math.sin( angle ) );
        this.parent = parent;
    }

    @Override
    public void act()
    {
        super.act();
        if ( age > 50 )
            parent.remove( this );
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), parent.getRadius() );
    }

    /**
     * Removes the bullet immediately.
     */
    public void explode()
    {
        parent.remove( this );
    }

    public double getRadius()
    {
        return parent.getRadius() + 1;
    }

    public int getDamage()
    {
        return parent.getDamage();
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
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Flechette( DataInputStream stream ) throws IOException
    {
        super( stream );
    }
}
