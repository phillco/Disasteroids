/**
 * DISASTEROIDS
 * BulletManager.java
 */
package disasteroids;

import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A default weapon that rapidly fires weak bullets.
 * @author Andy Kooiman
 */
class BulletManager extends Weapon
{
    private int speed = 20;

    private boolean threeWayShot;

    private int intervalShoot;

    private int radius;

    private int damage;

    public BulletManager()
    {
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        units.add( new Bullet( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );

        // This bonus fires two extra bullets at an angle.
        if ( threeWayShot )
        {
            units.add( new Bullet( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle + Math.PI / 8 ) );
            units.add( new Bullet( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle - Math.PI / 8 ) );
        }

        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = intervalShoot;
        Sound.playInternal( SoundLibrary.BULLET_SHOOT );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 50 )
        {
            if ( !canBerserk() )
                break;

            units.add( new Bullet( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );
            
            if ( !isInfiniteAmmo() )
                --ammo;
            
            firedShots += 1;
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots * 2;
            Sound.playInternal( SoundLibrary.BERSERK );
        }
    }

    public int getMaxUnits()
    {
        return 500;
    }

    @Override
    public String getName()
    {
        return "Machine Gun";
    }

    @Override
    public int getEntryAmmo()
    {
        return -1;
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color color )
    {
        new Bullet( this, color, x, y, 0, 0, 0 ).draw( g );
    }

    //                                                                            \\
    // --------------------------------- BONUS ---------------------------------- \\
    //                                                                            \\
    public void undoBonuses()
    {
        threeWayShot = false;
        intervalShoot = 4;
        radius = 2;
        damage = 10;
        ammo = -1;
    }

    public int getDamage()
    {
        return damage;
    }

    public String applyBonus( int key )
    {
        switch ( key )
        {
            case 0:
                damage += 50;
                return "Depleted Uranium Bullets!";
            case 1:
                intervalShoot = 1;
                return "Rapid Fire";
            case 4:
                threeWayShot = true;
                return "Three Way Shoot";
            case 7:
                radius = 6;
                return "Huge Bullets";
            default:
                return "";
        }
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getRadius()
    {
        return radius;
    }
}
