/**
 * DISASTEROIDS
 * WeaponManager.java
 */
package disasteroids;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interface for a Ship's weapon.
 * @author Andy Kooiman
 */
public interface WeaponManager extends GameElement
{
    public void add( ConcurrentLinkedQueue<Weapon> weapons );

    /**
     * Returns whether we've finished reloading.
     * 
     * @return  if this <code>WeaponManager</code> can shoot
     * @since January 10, 2008
     */
    public boolean canShoot();

    public void clear();

    /**
     * Executes one timestep, but only steps the timer if it is active.
     * 
     * @param active    if <code>this</code> is the current <code>WeaponManager</code> of its parent ship
     * @since January 10, 2008
     */
    public void act( boolean active );

    public void explodeAll();

    public int getIntervalShoot();

    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound );

    public int getNumLiving();

    public ConcurrentLinkedQueue<Weapon> getWeapons();

    public void restoreBonusValues();

    public String ApplyBonus( int key );

    public int getMaxShots();

    /**
     * Returns the name of the <code>Weapon</code>.
     * Examples: "Missiles", "Bullets".
     * 
     * @since December 25, 2007
     * @return  plural name of the <code>Weapon</code>
     */
    public String getWeaponName();

    /**
     * Gets a new instance of the type of <code>Weapon</code>s held
     * by this <code>WeaponManager</code>, but does not add it to the 
     * list of currently valid <code>Weapon</code>s.
     * 
     * @return A new instance of the type of <code>Weapon</code> stored
     * @since December 30, 2007
     */
    public Weapon getWeapon( int x, int y, Color col );

    /**
     * Executes a powerful blast of this <code>Weapon</code> type
     * 
     * @since January 7, 2008
     * @param s the <code>Ship</code> which is shooting
     */
    public void berserk( Ship s );

    /**
     * Draws a timer for how long until the next Berserk
     * 
     * @param g The context in which to draw
     * @param c The color of the ship calling
     */
    public void drawTimer( Graphics g, Color c );

    /**
     * Returns the sound to play when the weapon is fired.
     * 
     * @since January 11, 2008
     */
    public byte[] getShootSound();

    /**
     * Returns the sound to play when the berserk is activated.
     * 
     * @since January 11, 2008
     */
    public byte[] getBerserkSound();
}
