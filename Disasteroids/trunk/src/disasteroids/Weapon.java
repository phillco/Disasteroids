/**
 * DISASTEROIDS
 * Weapon.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.Local;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A ship's weapon, which fires <code>Unit</code>s.
 * @author Andy Kooiman
 */
public abstract class Weapon implements GameElement
{
    /**
     * All of the living units fired by this weapon.
     */
    protected ConcurrentLinkedQueue<Unit> units = new ConcurrentLinkedQueue<Unit>();

    /**
     * Remaining ammo (-1 means infinite). All bonus weapons start with zero ammo and are "picked up" by getting entryAmmo().
     */
    protected int ammo = 0;

    protected int timeTillNextBerserk = 0;

    protected int timeTillNextShot = 0;

    public Weapon()
    {
        // Set up these values.
        undoBonuses();
    }

    /**
     * Executes one timestep, but does not reload.
     */
    public void act()
    {
        for ( Unit w : units )
            w.act();
    }

    /**
     * Draws this weapon's units.
     */
    public void draw( Graphics g )
    {
        for ( Unit u : units )
            u.draw( g );
    }

    /**
     * Draws the berserk reload bar and ammo.
     */
    public void drawHUD( Graphics g, Ship parentShip )
    {
        // Only draw if we're the local player, and if this is the selected weapon.
        if ( parentShip == Local.getLocalPlayer() && parentShip.getWeaponManager() == this )
        {
            // Draw a reload bar for the next berserk.
            g.setColor( units.size() < getMaxUnits() ? parentShip.getColor() : parentShip.getColor().darker().darker() );
            g.drawRect( AsteroidsFrame.frame().getWidth() - 120, 30, 100, 10 );
            int width = ( 200 - Math.max( timeTillNextBerserk, 0 ) ) / 2;
            g.fillRect( AsteroidsFrame.frame().getWidth() - 120, 30, width, 10 );

            // Draw ammo.
            if ( !isInfiniteAmmo() )
                g.drawString( "" + ammo, AsteroidsFrame.frame().getWidth() - 40, 60 );
        }
    }

    /**
     * Draws an example unit reflecting current bonus values.
     */
    public abstract void drawOrphanUnit( Graphics g, double x, double y, Color col );

    /**
     * Returns the weapon's name, e.g. "Flamethrower".
     */
    public abstract String getName();

    //                                                                            \\
    // ------------------------------ OPERATION --------------------------------- \\
    //                                                                            \\
    /**
     * Shoots from the given origin.
     */
    public abstract void shoot( GameObject parent, Color color, double angle );

    /**
     * Fires a powerful blast from the given origin, typically in a circular pattern. The weapon must charge up first.
     */
    public abstract void berserk( GameObject parent, Color color );

    /**
     * Returns whether we can fire. Factors include ammo, max units, and the shooting timer.
     */
    public boolean canShoot()
    {
        return ( timeTillNextShot <= 0 ) && ( ammo > 0 || isInfiniteAmmo() ) && ( units.size() < getMaxUnits() );
    }

    /**
     * Returns whether we can berserk. Exactly like canShoot(), but uses the berserk timer.
     */
    public boolean canBerserk()
    {
        return ( timeTillNextBerserk <= 0 ) && ( ammo > 0 || isInfiniteAmmo() ) && ( units.size() < getMaxUnits() );
    }

    /**
     * Reloads this weapon by one notch. Should be called each step that the weapon is selected.
     */
    public void reload()
    {
        timeTillNextShot--;
        timeTillNextBerserk = Math.max( 0, timeTillNextBerserk - 1 );
    }

    //                                                                            \\
    // --------------------------------- UNITS ---------------------------------- \\
    //                                                                            \\
    /**
     * Detonates all living units.
     */
    public void explodeAllUnits()
    {
        for ( Unit w : units )
            w.explode();
    }

    /**
     * Removes all units from play.
     */
    public void clear()
    {
        units.clear();
    }

    /**
     * Removes the given unit from play.
     */
    public void remove( Unit u )
    {
        units.remove( u );
    }

    /**
     * Returns all living units.
     */
    public ConcurrentLinkedQueue<Unit> getUnits()
    {
        return units;
    }

    /**
     * Returns the max amount of units that can be in-game at once.
     */
    public abstract int getMaxUnits();

    //                                                                            \\
    // --------------------------------- AMMO ----------------------------------- \\
    //                                                                            \\
    /**
     * Returns the amount of this weapon's remaining ammo. -1 is infinite.
     */
    public int getAmmo()
    {
        return ammo;
    }

    /**
     * Returns whether this weapon has infinite ammo.
     */
    public boolean isInfiniteAmmo()
    {
        return ( ammo == -1 );
    }

    /**
     * Gives this weapon a decent cache of ammo, as if it was picked up anew.
     */
    public void giveAmmo()
    {
        ammo += getEntryAmmo();
    }

    /**
     * Returns the amount of ammo that this gun comes with.
     */
    public abstract int getEntryAmmo();

    //                                                                            \\
    // --------------------------------- BONUS ---------------------------------- \\
    //                                                                            \\
    /**
     * Applies a bonus to this weapon. 
     * 
     * @param key   the index of the bonus.
     * @return      the name of the applied bonus, e.g. "Rapid fire!", or "" if none.
     */
    public abstract String applyBonus( int key );

    /**
     * Restores all atrributes to normal levels.
     */
    public abstract void undoBonuses();

    //                                                                            \\
    // ------------------------------ NETWORKING -------------------------------- \\
    //                                                                            \\
    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt( ammo );
        stream.writeInt( timeTillNextBerserk );
        stream.writeInt( timeTillNextShot );

    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Weapon( DataInputStream stream ) throws IOException
    {
        ammo = stream.readInt();
        timeTillNextBerserk = stream.readInt();
        timeTillNextShot = stream.readInt();
    }

    /**
     * An individual bullet.
     * @author Andy Kooiman
     */
    public static abstract class Unit extends GameObject
    {
        protected Color color;

        protected int age = 0;

        public Unit( Color color, double x, double y, double dx, double dy )
        {
            super( x, y, dx, dy );
            this.color = color;
        }

        public void act()
        {
            ++age;
            move();
        }

        /**
         * Writes <code>this</code> to a stream for client/server transmission.
         */
        @Override
        public void flatten( DataOutputStream stream ) throws IOException
        {
            super.flatten( stream );
            stream.writeInt( color.getRGB() );
            stream.writeInt( age );
        }

        /**
         * Reads <code>this</code> from a stream for client/server transmission.
         */
        public Unit( DataInputStream stream ) throws IOException
        {
            super( stream );
            color = new Color( stream.readInt() );
            age = stream.readInt();
        }

        public abstract double getRadius();

        public abstract void explode();

        public abstract int getDamage();
    }
}
