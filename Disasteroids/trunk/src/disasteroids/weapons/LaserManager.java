/**
 * DISASTEROIDS
 * LaserManager.java
 */
package disasteroids.weapons;

import disasteroids.*;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A weapon manager that fires long, straight Lasers
 * @author Andy Kooiman
 */
public class LaserManager extends Weapon
{
    private int speed = 20;

    private int intervalShoot = 4;

    private int damage = 5;

    private int length = 10;

    public LaserManager()
    {
        super();
    }

    @Override
    public String getName()
    {
        return "Lasergun";
    }

    @Override
    public int getEntryAmmo()
    {
        return 260;
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        // Create the laser beam.
        createLinkedLaser( parent, color, angle );

        timeTillNextShot = intervalShoot;
        if ( !isInfiniteAmmo() )
            ammo--;
        Sound.playInternal( SoundLibrary.SNIPER_SHOOT );
    }

    /**
     * Creates a laser beam by linking many individual <code>Laser</code>s.
     */
    private void createLinkedLaser( GameObject parent, Color color, double angle )
    {
        double X = parent.getX();
        double Y = parent.getY();
        Laser l = null;
        for ( int i = 0; i < 150; i++ )
        {
            Laser last = new Laser( this, color, X, Y, parent.getDx(), parent.getDy(), angle, ( i == 0 ) );
            if ( l != null )
                l.setNext( last );
            units.add( last );
            X += length * Math.cos( angle );
            Y -= length * Math.sin( angle );
            l = last;
        }
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 4 )
        {
            if ( !canBerserk() )
                break;

            createLinkedLaser( parent, color, angle );

            if ( !isInfiniteAmmo() )
                --ammo;

            firedShots++;
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots * 10;
            Sound.playInternal( SoundLibrary.BERSERK );
        }
    }

    public int getMaxUnits()
    {
        return 8000;
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        new Laser( this, col, x, y, 0, 0, 0, false ).draw( g );
    }

    public int getDamage()
    {
        return damage;
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getRadius()
    {
        return length / 2;
    }

    public int length()
    {
        return length;
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
        stream.writeInt( damage );
        stream.writeInt( intervalShoot );
        stream.writeInt( length );
        stream.writeInt( speed );

        // Flatten all of the units.
        // To save space, we flatten only the head of each beam.
        Set<Laser> heads = new HashSet<Laser>();                
        for ( Unit u : units )
        {
            Laser l = (Laser) u;
            if ( l.isHead() )
                heads.add( l );
        }
        
        stream.writeInt( heads.size() );
        for ( Laser l : heads )
                l.flatten( stream );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public LaserManager( DataInputStream stream ) throws IOException
    {
        super( stream );
        damage = stream.readInt();
        intervalShoot = stream.readInt();
        length = stream.readInt();
        speed = stream.readInt();

        // Restore all of the units.
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            units.add( new Laser( stream, this ) );
    }
}
