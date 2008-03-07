/**
 * DISASTEROIDS
 * MineManager.java
 */
package disasteroids;

import disasteroids.sound.LayeredSound.SoundClip;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A weapon that lays dangerous <code>Mine</code>s.
 * @author Andy Kooiman
 */
public class MineManager extends WeaponManager
{

    private int maxShots = 20;

    private double berserkAngleOffset = 0;


    public MineManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
    }



    public void act()
    {
        super.act(true);/*
        Iterator<Unit> itr = mines.iterator();
        while ( itr.hasNext() )
        {
            Unit w = itr.next();
            if ( w.needsRemoval() )
                itr.remove();
            else
                w.act();

        }
        */
    }

    public int getIntervalShoot()
    {
        return 20;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        if ( weapons.size() > maxShots || timeTillNextShot > 0 )
            return false;
        timeTillNextShot = getIntervalShoot();

        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return weapons.add( new Mine( x, y, col, this ) );
    }

    public void restoreBonusValues()
    {

    }

    public String ApplyBonus( int key )
    {
        return "";
    }

    public int getMaxShots()
    {
        return maxShots;
    }

    public void draw( Graphics g )
    {
        for ( Unit w : weapons )
            w.draw( g );
    }

    public String getWeaponName()
    {
        return "Mines";
    }

    public Unit getWeapon( int x, int y, Color col )
    {
        Mine m = new Mine( x, y, col, this);
        m.setLife( 500 );
        return m;
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        Sound.playInternal( SoundLibrary.MINE_ARM );
        berserkAngleOffset += .5;
        int temp = timeTillNextShot;
        timeTillNextShot = 0;
        for ( double angle = 0; angle < Math.PI * 2; angle += Math.PI / 4 )
        {
            add( (int) ( s.getX() + Math.cos( berserkAngleOffset + angle ) * 50 ), (int) ( s.getY() + Math.sin( berserkAngleOffset + angle ) * 50 ), angle, 0, 0, s.getColor(), false );
            timeTillNextShot = 0;
        }
        timeTillNextShot = temp;
        timeTillNextBerserk = 200;
    }

    @Override
    public boolean canShoot()
    {
        return super.canShoot()&& weapons.size() < maxShots;
    }



    public SoundClip getShootSound()
    {
        return SoundLibrary.MINE_ARM;
    }

    public SoundClip getBerserkSound()
    {
        return SoundLibrary.MINE_ARM;
    }
}
