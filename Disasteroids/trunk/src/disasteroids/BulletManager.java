/**
 * DISASTEROIDS
 * BulletManager.java
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
class BulletManager extends WeaponManager
{

    private int speed = 20;

    private int maxShots = 100;

    private boolean threeWayShot = false;

    private int intervalShoot = 4;

    private int radius = 2;

    private int damage = 10;

    public BulletManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
    }

    public BulletManager( ConcurrentLinkedQueue<Unit> start )
    {
        weapons = start;
    }

    public void act()
    {
        super.act(true);
        /*Iterator<Unit> iter = theBullets.iterator();
        while ( iter.hasNext() )
        {
            Unit w = iter.next();
            if ( w.needsRemoval() )
                iter.remove();
            else
                w.act();
        }*/
    }

  
      public int getIntervalShoot()
    {
        return intervalShoot;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        if ( weapons.size() > 500 || timeTillNextShot > 0 )
            return false;
        if ( threeWayShot )
        {
            weapons.add( new Bullet( this, x, y, angle + Math.PI / 8, dx, dy, col ) );
            weapons.add( new Bullet( this, x, y, angle - Math.PI / 8, dx, dy, col ) );
        }
        timeTillNextShot = intervalShoot;

        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return weapons.add( new Bullet( this, x, y, angle, dx, dy, col ) );
    }

    public void restoreBonusValues()
    {
        threeWayShot = false;
        intervalShoot = 4;
        radius = 2;
        damage = 10;
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
            case 0:
                damage += 50;
                ret += "Depleted Uranium Bullets!!";
                break;
            case 1:
                intervalShoot = 1;
                ret += "Rapid Fire";
                break;
            case 4:
                threeWayShot = true;
                ret += "Three Way Shot";
                break;
            case 7:
                radius = 6;
                ret += "Huge Bullets";
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
        return maxShots;
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
        return "Bullets";
    }

    public Unit getWeapon( int x, int y, Color col )
    {
        return new Bullet( this, x, y, 0, 0, 0, col );
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        int temp = timeTillNextShot;
        Sound.playInternal( SoundLibrary.BERSERK );
        timeTillNextShot = 0;
        for ( double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 50 )
        {
            add( s.getX(), s.getY(), angle, s.getDx(), s.getDy(), s.getColor(), false );
            timeTillNextShot = 0;
        }
        timeTillNextShot = temp;
        timeTillNextBerserk = 50;
    }

    @Override
    public boolean canShoot()
    {
        return super.canShoot()&& weapons.size() < 500 ;
    }


    public SoundClip getShootSound()
    {
        return SoundLibrary.BULLET_SHOOT;
    }

    public SoundClip getBerserkSound()
    {
        return SoundLibrary.BERSERK;
    }
}
