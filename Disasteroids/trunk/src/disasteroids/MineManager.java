/**
 * DISASTEROIDS
 * MineManager.java
 */
package disasteroids;

import disasteroids.sound.Sound;
import disasteroids.sound.Tone;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A weapon that lays dangerous <code>Mine</code>s.
 * @author Andy Kooiman
 */
public class MineManager implements WeaponManager
{
    private ConcurrentLinkedQueue<Weapon> mines;

    private int maxShots = 20;

    private double berserkAngleOffset = 0;

    private int timeTillNextBerserk = 0;

    private int timeTillNextShot = 0;

    public MineManager()
    {
        mines = new ConcurrentLinkedQueue<Weapon>();
    }

    public void act( boolean active )
    {
        act();
        if ( active )
        {
            timeTillNextShot--;
            timeTillNextBerserk--;
        }
    }

    public void act()
    {
        Iterator<Weapon> itr = mines.iterator();
        while ( itr.hasNext() )
        {
            Weapon w = itr.next();
            if ( w.needsRemoval() )
                itr.remove();
            else
                w.act();

        }
    }

    public void add( ConcurrentLinkedQueue<Weapon> weapons )
    {
        while ( !weapons.isEmpty() )
            mines.add( weapons.remove() );
    }

    public void clear()
    {
        mines = new ConcurrentLinkedQueue<Weapon>();
    }

    public void explodeAll()
    {
        for ( Weapon w : mines )
            w.explode();
    }

    public int getIntervalShoot()
    {
        return 20;
    }

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        if ( mines.size() > maxShots || timeTillNextShot > 0 )
            return false;
        timeTillNextShot = getIntervalShoot();

        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return mines.add( new Mine( x, y, col ) );
    }

    public int getNumLiving()
    {
        return mines.size();
    }

    public ConcurrentLinkedQueue<Weapon> getWeapons()
    {
        return mines;
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
        for ( Weapon w : mines )
            w.draw( g );
    }

    public String getWeaponName()
    {
        return "Mines";
    }

    public Weapon getWeapon( int x, int y, Color col )
    {
        Mine m = new Mine( x, y, col );
        m.setLife( 500 );
        return m;
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 )
            return;
        Sound.playInternal( Sound.BERSERK_SOUND );
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

    public boolean canShoot()
    {
        return !( mines.size() > maxShots || timeTillNextShot > 0 );
    }

    public void drawTimer( Graphics g, Color c )
    {
        g.setColor( mines.size() < maxShots ? c : c.darker().darker() );
        g.drawRect( AsteroidsFrame.frame().getWidth() - 120, 30, 100, 10 );
        int width = ( 200 - Math.max( timeTillNextBerserk, 0 ) ) / 2;
        g.fillRect( AsteroidsFrame.frame().getWidth() - 120, 30, width, 10 );
    }

    public Tone[] getShootSound()
    {
        return Sound.MINE_ARM_SOUND;
    }

    public Tone[] getBerserkSound()
    {
        return Sound.BERSERK_SOUND;
    }
}
