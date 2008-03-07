/**
 * DISASTEROIDS
 * WeaponManager.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.sound.LayeredSound.SoundClip;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interface for a Ship's weapon.
 * @author Andy Kooiman
 */
public abstract class WeaponManager implements GameElement
{
    protected ConcurrentLinkedQueue<Weapon> weapons;
    
    protected int timeTillNextBerserk = 0;

    protected int timeTillNextShot = 0;
    
    public void add( ConcurrentLinkedQueue<Weapon> weap )
    {
        while(!weap.isEmpty())
            weapons.add(weap.remove());
    }

    /**
     * Returns whether we've finished reloading.
     * 
     * @return  if this <code>WeaponManager</code> can shoot
     * @since January 10, 2008
     */
    public boolean canShoot()
    {
        return timeTillNextShot<=0;
    }

    public void clear()
    {
        weapons.clear();
    }

    /**
     * Executes one timestep, but only steps the timer if it is active.
     * 
     * @param active    if <code>this</code> is the current <code>WeaponManager</code> of its parent ship
     * @since January 10, 2008
     */
    public void act( boolean active )
    {
        for(Weapon w: weapons)
            w.act();
        if(active)
        {
            timeTillNextShot--;
            timeTillNextBerserk--;
        }
    }

    public void explodeAll()
    {
        for(Weapon w: weapons)
            w.explode();
    }
            

    public abstract int getIntervalShoot();

    public abstract boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound );
    
    public void remove(Weapon w)
    {
        weapons.remove(w);
    }

    public int getNumLiving()
    {
        return weapons.size();
    }

    public ConcurrentLinkedQueue<Weapon> getWeapons()
    {
        return weapons;
    }

    public abstract void restoreBonusValues();

    public abstract String ApplyBonus( int key );

    public abstract int getMaxShots();

    /**
     * Returns the name of the <code>Weapon</code>.
     * Examples: "Missiles", "Bullets".
     * 
     * @since December 25, 2007
     * @return  plural name of the <code>Weapon</code>
     */
    public abstract String getWeaponName();

    /**
     * Gets a new instance of the type of <code>Weapon</code>s held
     * by this <code>WeaponManager</code>, but does not add it to the 
     * list of currently valid <code>Weapon</code>s.
     * 
     * @return A new instance of the type of <code>Weapon</code> stored
     * @since December 30, 2007
     */
    public abstract Weapon getWeapon( int x, int y, Color col );

    /**
     * Executes a powerful blast of this <code>Weapon</code> type
     * 
     * @since January 7, 2008
     * @param s the <code>Ship</code> which is shooting
     */
    public abstract void berserk( Ship s );

    /**
     * Draws a timer for how long until the next Berserk
     * 
     * @param g The context in which to draw
     * @param c The color of the ship calling
     */
    public void drawTimer( Graphics g, Color c )
    {
        g.setColor( weapons.size() < getMaxShots() ? c : c.darker().darker() );
        g.drawRect( AsteroidsFrame.frame().getWidth() - 120, 30, 100, 10 );
        int width = ( 200 - Math.max( timeTillNextBerserk, 0 ) ) / 2;
        g.fillRect( AsteroidsFrame.frame().getWidth() - 120, 30, width, 10 );
    }

    /**
     * Returns the sound to play when the weapon is fired.
     * 
     * @since January 11, 2008
     */
    public abstract SoundClip getShootSound();

    /**
     * Returns the sound to play when the berserk is activated.
     * 
     * @since January 11, 2008
     */
    public abstract SoundClip getBerserkSound();
}
