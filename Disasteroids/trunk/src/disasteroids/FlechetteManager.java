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
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A weapon manager that rapidly fires weak bullets.
 * @author Andy Kooiman
 */
public class FlechetteManager extends Weapon
{

    private int speed = 25;

    private int maxShots = 1000;

    private int intervalShoot = 2;

    private int radius = 1;

    private int damage = 1;

    public FlechetteManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
    }

    public FlechetteManager( ConcurrentLinkedQueue<Unit> start )
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
        if ( weapons.size() > maxShots || timeTillNextShot > 0 )
            return false;
        timeTillNextShot = intervalShoot;
        Random rand=RandomGenerator.get();
        boolean successful=true;
        for(int num=0; num<5; num++)
            successful=successful&&weapons.add(new Flechette(this, x,y, angle+(rand.nextDouble()-.5), dx, dy, col));
        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return successful;
    }

    public void restoreBonusValues()
    {
        intervalShoot = 2;
        radius = 1;
        damage = 1;
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
        return (int)(speed*RandomGenerator.get().nextDouble());
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
        return "Flechette Launcher";
    }

    public Unit getWeapon( int x, int y, Color col )
    {
        return new Flechette( this, x, y, 0, 0, 0, col );
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        int temp = timeTillNextShot;
        Sound.playInternal( SoundLibrary.BERSERK );
        timeTillNextShot = 0;
        Random rand=RandomGenerator.get();
        for ( int i=0; i<43; i++ )
        {
            add( (int)s.getX(), (int)s.getY(),rand.nextDouble()*2*Math.PI , s.getDx(), s.getDy(), s.getColor(), false );
            timeTillNextShot = 0;
        }
        timeTillNextShot = temp;
        timeTillNextBerserk = 50;
    }

    @Override
    public boolean canShoot()
    {
        return super.canShoot()&& weapons.size() < maxShots ;
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
