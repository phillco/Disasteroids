/**
 * DISASTEROIDS
 * BulletManager.java
 */
package disasteroids;

import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A weapon manager that rapidly fires weak bullets.
 * @author Andy Kooiman
 */
public class FlechetteManager extends Weapon
{
    private int speed = 25;

    private int intervalShoot = 2;

    private int radius = 1;

    private int damage = 1;

    public FlechetteManager()
    {
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        for ( int num = 0; num < 5; num++ )
        {
            if ( !canShoot() )
                break;

            units.add( new Flechette( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(),
                                      angle + ( Util.getRandomGenerator().nextDouble() - .5 ) ) );
            if ( !isInfiniteAmmo() )
                --ammo;
        }

        timeTillNextShot = intervalShoot;
        Sound.playInternal( SoundLibrary.BULLET_SHOOT );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( int i = 0; i < 43; i++ )
        {
            for ( int j = 0; j < 5; j++ )
            {
                if ( !canBerserk() )
                    break;

                units.add( new Flechette( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(),
                                          Util.getRandomGenerator().nextDouble() * 2 * Math.PI + ( Util.getRandomGenerator().nextDouble() - .5 ) ) );
                ++firedShots;
                if ( !isInfiniteAmmo() )
                    --ammo;
            }
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots;
            Sound.playInternal( SoundLibrary.BERSERK );
        }
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        new Flechette( this, col, x, y, 0, 0, 0 ).draw( g );
    }

    public int getReloadTime()
    {
        return intervalShoot;
    }

    public void undoBonuses()
    {
        intervalShoot = 2;
        radius = 1;
        damage = 1;
    }

    public int getDamage()
    {
        return damage;
    }

    public String applyBonus( int key )
    {
        String ret = "";

        switch ( key )
        {
            default:
                ret = "";
        }
        return ret;
    }

    public double getSpeed()
    {
        return ( speed * Util.getRandomGenerator().nextDouble() );
    }

    public int getMaxUnits()
    {
        return 8000;
    }

    public int getRadius()
    {
        return radius;
    }

    @Override
    public String getName()
    {
        return "Flachette";
    }

    @Override
    public int getEntryAmmo()
    {
        return 1500;
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
        stream.writeInt( intervalShoot );
        stream.writeInt( radius );
        stream.writeInt( damage );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public FlechetteManager( DataInputStream stream ) throws IOException
    {
        for ( int i = 0; i < stream.readInt(); i++ )
            units.add( new Flechette( stream ) );

        intervalShoot = stream.readInt();
        radius = stream.readInt();
        damage = stream.readInt();
    }
}
