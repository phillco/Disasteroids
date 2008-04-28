/*
 * DISASTEROIDS
 * Unit.java
 */

package disasteroids.weapons;

import disasteroids.GameObject;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An individual unit - a bullet, a mine, a missile, a grenade, etc - launched by a <code>Weapon</code>.
 * @author Andy Kooiman, Phillip Cohen
 */
public abstract class Unit extends GameObject
{
    protected Color color;

    /**
     * How many timesteps that we've lived.
     */
    protected int age = 0;

    public Unit( Color color, double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy );
        this.color = color;
    }

    /**
     * Ages and moves.
     */
    public void act()
    {
        ++age;
        move();
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( color.getRGB() );
        stream.writeInt( age );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Unit( DataInputStream stream ) throws IOException
    {
        super( stream );
        color = new Color( stream.readInt() );
        age = stream.readInt();
    }

    public abstract double getRadius();

    public abstract void explode();

    public abstract int getDamage();
}