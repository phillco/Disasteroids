/*
 * DISASTEROIDS
 * SniperRound.java
 */
package disasteroids.weapons;

import disasteroids.gui.MainWindow;
import java.awt.Graphics;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A very fast bullet fired by the sniper rifle.
 * @author Andy Kooiman
 */
class SniperRound extends Unit
{
    private SniperManager parent;

    private double angle;

    private int damage;

    public SniperRound( SniperManager parent, Color color, double x, double y, double dx, double dy, double angle )
    {
        super( color, x, y, dx + parent.getSpeed() * Math.cos( angle ), dy - parent.getSpeed() * Math.sin( angle ) );
        this.angle = angle;
        this.parent = parent;
        damage = parent.getDamage();
    }

    public double getRadius()
    {
        return parent.getRadius();
    }

    public void explode()
    {
        damage *= .8;
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
        MainWindow.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, Math.PI + angle );
    }

    public int getDamage()
    {
        return damage;
    }

    @Override
    public void remove()
    {
        parent.remove( this );
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
        stream.writeDouble( angle );
        stream.writeInt( damage );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public SniperRound( DataInputStream stream, SniperManager parent ) throws IOException
    {
        super( stream );
        angle = stream.readDouble();
        damage = stream.readInt();

        this.parent = parent;
    }
}
