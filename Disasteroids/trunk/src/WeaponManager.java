/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Owner
 */
public interface WeaponManager extends GameElement
{

    public void add( ConcurrentLinkedQueue<Weapon> weapons );

    /**
     * Returns whether a call to add at this instant will succede or fail
     * 
     * @return If this <code>WeaponManager</code> can shoot
     * @since January 10, 2008
     */
    public boolean canShoot();

    public void clear();
    
    /**
     * Executes one timestep, but only steps timers if it is active
     * 
     * @param active If <code>this</code> is the current <code>WeaponManager</code> of its parent ship
     * @since January 10, 2008
     */
    public void act(boolean active);

    public void explodeAll();

    public int getIntervalShoot();

    public boolean add( int x, int y, double angle, double dx, double dy, Color col );

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
    public Weapon getWeapon(int x, int y, Color col);
    
    /**
     * Executes a powerful blast of this <code>Weapon</code> type
     * 
     * @since January 7, 2008
     * @param Ship the <code>Ship</code> which is shooting
     */
    public void berserk(Ship s);
    
    /**
     * Draws a timer for how long until the next Berserk
     * 
     * @param g The context in which to draw
     * @param c The color of the ship calling
     */
    public void drawTimer(Graphics g, Color c);
}
