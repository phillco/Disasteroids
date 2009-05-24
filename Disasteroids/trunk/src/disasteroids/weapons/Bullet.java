/*
 * DISASTEROIDS
 * Bullet.java
 */
package disasteroids.weapons;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A fast and simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class Bullet extends Unit
{
    private BulletManager parent;

    public Bullet( BulletManager parent, Color color, double x, double y, double dx, double dy, double angle )
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
        AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), parent.getBonusValue( "radius" ).getValue() );
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
        return parent.getBonusValue( "radius" ).getValue() + 1;
    }

    public int getDamage()
    {
        return parent.getBonusValue( "damage" ).getValue();
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
    public Bullet( DataInputStream stream, BulletManager parent ) throws IOException
    {
        super( stream );
        
        this.parent = parent;
    }

    @Override
    public void remove()
    {
        parent.remove( this );
    }
}
