/**
 * DISASTEROIDS
 * SniperManager.java
 */
package disasteroids;

import disasteroids.sound.LayeredSound.SoundClip;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A weapon manager that rapidly fires weak bullets.
 * @author Andy Kooiman
 */
class SniperManager extends Weapon
{
    private int speed = 30;

    private int intervalShoot = 50;

    private int radius = 10;

    private int damage = 1000;

    public SniperManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
        ammo = 20;
    }

    public SniperManager( ConcurrentLinkedQueue<Unit> start )
    {
        weapons = start;
        ammo = 20;
    }

    public int getIntervalShoot()
    {
        return intervalShoot;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        if ( timeTillNextShot > 0 || !( ammo > 0 && ammo != -1 ) )
            return false;
        timeTillNextShot = intervalShoot;

        if ( ammo != -1 )
            ammo--;
        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return weapons.add( new SniperRound( this, x, y, angle, dx, dy, col ) );
    }

    public void restoreBonusValues()
    {
        intervalShoot = 50;
    /* radius = 15;
    damage = 1000;*/
    }

    public int getDamage()
    {
        return damage;
    }

    public String ApplyBonus( int key )
    {
        String ret = "";

        switch ( key )
        {
            case 1:
                intervalShoot = Math.max( intervalShoot - 10, 30 );
                ret = "Faster Reload";
                break;
            default:
                ret = "";
        }
        return ret;
    }

    public int getSpeed()
    {
        return speed;
    }

    public int getMaxShots()
    {
        return 20;
    }

    public int getRadius()
    {
        return radius;
    }

    public void draw( Graphics g )
    {
        for ( Unit w : weapons )
            w.draw( g );

    }

    public String getWeaponName()
    {
        return "Sniper";
    }

    public Unit getOrphanUnit( int x, int y, Color col )
    {
        return new SniperRound( this, x, y, 0, 0, 0, col );
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        int temp = timeTillNextShot;
        Sound.playInternal( SoundLibrary.BERSERK );
        timeTillNextShot = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8 )
        {
            add( (int) s.getX(), (int) s.getY(), angle, s.getDx(), s.getDy(), s.getColor(), false );
            timeTillNextShot = 0;
        }
        timeTillNextShot = temp;
        timeTillNextBerserk = 300;
    }

    @Override
    public boolean canShoot()
    {
        return super.canShoot() && weapons.size() < 500;
    }

    public SoundClip getShootSound()
    {
        return SoundLibrary.SNIPER_SHOOT;
    }

    public SoundClip getBerserkSound()
    {
        return SoundLibrary.BERSERK;
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
}
