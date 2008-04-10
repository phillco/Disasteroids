/**
 * DISASTEROIDS
 * LaserManager.java
 */
package disasteroids;

import disasteroids.sound.LayeredSound.SoundClip;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A weapon manager that fires long, straight Lasers
 * @author Andy Kooiman
 */
class LaserManager extends Weapon
{
    private int speed = 20;

    private int maxShots = 1000;

    private int intervalShoot = 10;

    private int damage = 10;

    private int length = 10;

    public LaserManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
    }

    public LaserManager( ConcurrentLinkedQueue<Unit> start )
    {
        weapons = start;
    }

    public void act()
    {
        super.act( true );
    }

    public int getIntervalShoot()
    {
        return intervalShoot;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        if ( weapons.size() > 2500 || timeTillNextShot > 0 )
            return false;
        timeTillNextShot = intervalShoot;

        int X = x;
        int Y = y;
        Laser l = null;
        for ( int i = 0; i < 150; i++ )
        {
            Laser last = new Laser( this, X, Y, angle, dx, dy, col );
            if ( l != null )
                l.setNext( last );
            weapons.add( last );
            X += length * Math.cos( angle );
            Y -= length * Math.sin( angle );
            l = last;
        }
        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return true;
    }

    public void restoreBonusValues()
    {

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
        return maxShots;
    }

    public int getRadius()
    {
        return length / 2;
    }

    public void draw( Graphics g )
    {
        for ( Unit w : weapons )
            w.draw( g );
    }

    public String getWeaponName()
    {
        return "Laser";
    }

    public Unit getWeapon( int x, int y, Color col )
    {
        return new Laser( this, x, y, 0, 0, 0, col );
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        int temp = timeTillNextShot;
        Sound.playInternal( SoundLibrary.BERSERK );
        timeTillNextShot = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 5 )
        {
            add( (int) s.getX(), (int) s.getY(), angle, s.getDx(), s.getDy(), s.getColor(), false );
            timeTillNextShot = 0;
        }
        timeTillNextShot = temp;
        timeTillNextBerserk = 50;
    }

    public int length()
    {
        return length;
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
}
