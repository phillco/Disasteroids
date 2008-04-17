/**
 * DISASTEROIDS
 * Weapon.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.sound.LayeredSound.SoundClip;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A ship's weapon.
 * @author Andy Kooiman
 */
public abstract class Weapon implements GameElement
{
    protected ConcurrentLinkedQueue<Unit> weapons;

    protected int timeTillNextBerserk = 0;

    protected int timeTillNextShot = 0;

    protected int ammo = 0;

    public void add( ConcurrentLinkedQueue<Unit> weap )
    {
        while ( !weap.isEmpty() )
            weapons.add( weap.remove() );
    }

    /**
     * Returns whether we've finished reloading.
     * 
     * @return  if this <code>WeaponManager</code> can shoot
     * @since January 10, 2008
     */
    public boolean canShoot()
    {
        return timeTillNextShot <= 0 && ( ammo == -1 || ammo > 0 );
    }

    /**
     * Removes all units from play.
     */
    public void clear()
    {
        weapons.clear();
    }

    /**
     * Executes one timestep. Does not reload.
     * 
     * @since January 10, 2008
     */
    public void act()
    {
        for ( Unit w : weapons )
            w.act();
    }

    /**
     * Reloads this weapon by one notch. Should be called each step that the weapon is selected.
     */
    public void reload()
    {
        timeTillNextShot--;
        timeTillNextBerserk--;
    }

    public void explodeAll()
    {
        for ( Unit w : weapons )
            w.explode();
    }

    public abstract String getName();

    public abstract int getIntervalShoot();

    public abstract boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound );

    public void remove( Unit w )
    {
        weapons.remove( w );
    }

    public int getNumLiving()
    {
        return weapons.size();
    }

    public ConcurrentLinkedQueue<Unit> getWeapons()
    {
        return weapons;
    }

    public abstract void restoreBonusValues();

    public abstract String ApplyBonus( int key );

    public abstract int getMaxShots();

    public int getAmmo()
    {
        return ammo;
    }

    /**
     * Returns the starting amount of ammo that this gun should come with.
     * @return  starting level of ammo
     */
    public abstract int getEntryAmmo();

    /**
     * Gives this weapon a decent cache of ammo, as if it was picked up anew.
     */
    public void giveAmmo()
    {
        ammo += getEntryAmmo();
    }

    /**
     * Returns a new unit that isn't part of the game. Useful for the GUI.
     * 
     * @return  a new instance of the type of <code>Unit</code> stored
     * @since December 30, 2007
     */
    public abstract Unit getOrphanUnit( int x, int y, Color col );

    /**
     * Executes a powerful blast that must charge up. Typically operates in a circle.
     * 
     * @param s the <code>Ship</code> which is shooting
     * @since January 7, 2008
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

        if ( ammo != -1 )
            g.drawString( "" + ammo, AsteroidsFrame.frame().getWidth() - 40, 60 );

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

    /**
     * An individual bullet.
     * @author Andy Kooiman
     */
    public static abstract class Unit extends GameObject
    {
        public abstract int getRadius();

        public abstract void explode();

        public abstract boolean needsRemoval();

        public abstract int getDamage();

        public abstract String getName();
    }
}
