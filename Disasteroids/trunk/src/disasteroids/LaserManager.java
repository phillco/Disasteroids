/**
 * DISASTEROIDS
 * LaserManager.java
 */
package disasteroids;

import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;

/**
 * A weapon manager that fires long, straight Lasers
 * @author Andy Kooiman
 */
class LaserManager extends Weapon
{
    private int speed = 20;

    private int intervalShoot = 4;

    private int damage = 5;

    private int length = 10;

    public LaserManager()
    {
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
            Laser last = new Laser( this, color, X, Y, parent.getDx(), parent.getDy(), angle );
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
        new Laser( this, col, x, y, 0, 0, 0 ).draw( g );
    }

    public void undoBonuses()
    {
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
}
