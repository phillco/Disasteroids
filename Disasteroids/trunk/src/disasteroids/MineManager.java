/**
 * DISASTEROIDS
 * MineManager.java
 */
package disasteroids;

import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;

/**
 * A bonus weapon that lays dangerous <code>Mine</code>s.
 * @author Andy Kooiman
 */
public class MineManager extends Weapon
{
    private double berserkAngleOffset = 0;

    /**
     * The radius in which a mine will latch onto a target.
     */
    private int sight = 200;

    public MineManager()
    {
    }

    @Override
    public String getName()
    {
        return "Mine Layer";
    }

    @Override
    public int getEntryAmmo()
    {
        return 30;
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        units.add( new Mine( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy() ) );
        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = 20;
        Sound.playInternal( SoundLibrary.MINE_ARM );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        berserkAngleOffset += .5;
        int firedShots = 0;
        for ( double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4 )
        {
            if ( !canBerserk() )
                break;

            units.add( new Mine( this, color, parent.getX() + Math.cos( berserkAngleOffset + angle ) * 50,
                                 parent.getY() + Math.sin( berserkAngleOffset + angle ) * 50, parent.getDx(), parent.getDy() ) );

            if ( !isInfiniteAmmo() )
                --ammo;

            ++firedShots;
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots * 30;
            Sound.playInternal( SoundLibrary.MINE_ARM );
        }
    }

    public int getMaxUnits()
    {
        return 200;
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        Mine m = new Mine( this, col, x, y, 0, 0 );
        m.age = 300;
        m.draw( g );
    }

    public void undoBonuses()
    {
    }

    public String applyBonus( int key )
    {
        return "";
    }

    public int sight()
    {
        return sight;
    }
}
