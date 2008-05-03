/*
 * DISASTEROIDS
 * Laser.java
 */
package disasteroids.weapons;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A weak beam of light. Lasers are joined together to create a continuous beam.
 * @author Andy Kooiman
 */
class Laser extends Unit
{
    private LaserManager parent;

    private int length;

    private double angle;

    private boolean isHead;

    /**
     * The next laser in this line.
     */
    private Laser next;

    public Laser( LaserManager parent, Color color, double x, double y, double dx, double dy, double angle, boolean isRoot )
    {
        super( color, x, y, dx, dy );
        this.angle = angle;
        this.parent = parent;
        this.length = parent.length();
        this.isHead = isRoot;
    }

    @Override
    public void act()
    {
        super.act();
        if ( age > 3 )
            parent.remove( this );
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().drawLine( g, color, (int) getX(), (int) getY(), length, angle );
    }

    /**
     * Sets the laser next in line. When this laser explodes, the entire line after it does also.
     */
    public void setNext( Laser next )
    {
        this.next = next;
    }

    public double getRadius()
    {
        return parent.getRadius();
    }

    /**
     * Immediately removes this laser and every laser after it in line.
     */
    public void explode()
    {
        if ( next != null )
            next.explode();
        parent.remove( this );
    }

    public int getDamage()
    {
        return parent.getDamage();
    }

    public int getNumChildren()
    {
        if ( next == null )
            return 0;
        else
            return 1 + next.getNumChildren();
    }

    public boolean isHead()
    {
        return isHead;
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
        stream.writeInt( length );
        stream.writeInt( getNumChildren() );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Laser( DataInputStream stream, LaserManager parent ) throws IOException
    {
        super( stream );
        angle = stream.readDouble();
        length = stream.readInt();

        this.parent = parent;
        int children = stream.readInt();
        double X = getX() + length * Math.cos( angle );
        double Y = getY() - length * Math.sin( angle );
        Laser l = this;
        for ( int i = 0; i < children; i++ )
        {
            // System.out.println( X +" , " + Y );
            Laser last = new Laser( parent, color, X, Y, getDx(), getDy(), angle, false );
            l.setNext( last );
            parent.units.add( last );
            X += length * Math.cos( angle );
            Y -= length * Math.sin( angle );
            l = last;
        }
    }
}
