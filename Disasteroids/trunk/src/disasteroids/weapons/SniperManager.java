/**
 * DISASTEROIDS
 * SniperManager.java
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

/**
 * A weapon manager that rapidly fires weak bullets.
 * @author Andy Kooiman
 */
public class SniperManager extends Weapon
{
    private int speed = 30;

    private int radius = 10;

    private int damage = 1000;

    // Bonus IDs.
    public int BONUS_INTERVALSHOOT;

    public SniperManager( ShootingObject parent )
    {
        super( parent );
        ammo = 20;
    }

    @Override
    protected void genericInit()
    {
        super.genericInit();
        BONUS_INTERVALSHOOT = getNewBonusID();
        bonusValues.put( BONUS_INTERVALSHOOT, new BonusValue( 30, 10, "Faster reloading" ) );
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        new SniperRound( this, col, x, y, 0, 0, 0 ).draw( g );
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        units.add( new SniperRound( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );

        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = getBonusValue( BONUS_INTERVALSHOOT ).getValue();
        Sound.playInternal( SoundLibrary.SNIPER_SHOOT );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8 )
        {
            if ( !canBerserk() )
                break;

            units.add( new SniperRound( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );
            if ( !isInfiniteAmmo() )
                --ammo;

            ++firedShots;
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots * 10;
            Sound.playInternal( SoundLibrary.BERSERK );
        }
    }

    public int getDamage()
    {
        return damage;
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getMaxUnits()
    {
        return 500;
    }

    public int getRadius()
    {
        return radius;
    }

    public String getName()
    {
        return "Sniper Rifle";
    }

    @Override
    public int getEntryAmmo()
    {
        return 20;
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
        stream.writeInt( radius );
        stream.writeInt( speed );

        // Flatten all of the units.
        stream.writeInt( units.size() );
        for ( Unit u : units )
            ( (SniperRound) u ).flatten( stream );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public SniperManager( DataInputStream stream, ShootingObject parent ) throws IOException
    {
        super( stream, parent );

        damage = stream.readInt();
        radius = stream.readInt();
        speed = stream.readInt();

        // Restore all of the units.
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            units.add( new SniperRound( stream, this ) );
    }
}
