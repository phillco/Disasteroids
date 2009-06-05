/**
 * DISASTEROIDS
 * BulletManager.java
 */
package disasteroids.weapons;

import disasteroids.gameobjects.ShootingObject;
import disasteroids.*;
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

    public FlechetteManager( ShootingObject parent )
    {
        super( parent );
    }

    @Override
    public void shoot( Color color, double angle )
    {
        if ( !canShoot() )
            return;

        for ( int num = 0; num < 5; num++ )
        {
            if ( !canShoot() )
                break;

            addUnit( new Flechette( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(),
                    angle + ( Util.getGameplayRandomGenerator().nextDouble() - .5 ) ) );
            if ( !isInfiniteAmmo() )
                --ammo;
        }

        timeTillNextShot = intervalShoot;
        Sound.playInternal( SoundLibrary.BULLET_SHOOT );
    }

    @Override
    public void berserk( Color color )
    {
        int firedShots = 0;
        for ( int i = 0; i < 43; i++ )
        {
            for ( int j = 0; j < 5; j++ )
            {
                if ( !canBerserk() )
                    break;

                addUnit( new Flechette( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(),
                        Util.getGameplayRandomGenerator().nextAngle() + ( Util.getGameplayRandomGenerator().nextDouble() - .5 ) ) );
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

    public int getDamage()
    {
        return damage;
    }

    public double getSpeed()
    {
        return ( speed * Util.getGameplayRandomGenerator().nextDouble() );
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

        // Flatten all of the units.
        stream.writeInt( units.size() );
        for ( Unit u : units )
            ( (Flechette) u ).flatten( stream );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public FlechetteManager( DataInputStream stream, ShootingObject parent ) throws IOException
    {
        super( stream, parent );
        intervalShoot = stream.readInt();
        radius = stream.readInt();
        damage = stream.readInt();

        // Restore all of the units.
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            addUnit( new Flechette( stream, this ) );
    }
}
