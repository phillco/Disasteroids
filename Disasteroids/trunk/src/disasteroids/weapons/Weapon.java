/**
 * DISASTEROIDS
 * Weapon.java
 */
package disasteroids.weapons;

import disasteroids.*;
import disasteroids.gui.MainWindow;
import disasteroids.gui.Local;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A ship's weapon which fires <code>Unit</code>s.
 * @author Andy Kooiman, Phillip Cohen
 */
public abstract class Weapon implements GameElement
{
    /**
     * All of the living units fired by this weapon.
     */
    protected ConcurrentLinkedQueue<Unit> units = new ConcurrentLinkedQueue<Unit>();

    protected Map<Integer, BonusValue> bonusValues = new HashMap<Integer, BonusValue>();

    private int nextBonusID = 0;

    public int BONUS_FASTERBERSERK;

    /**
     * Remaining ammo (-1 means infinite). All bonus weapons start with zero ammo and are "picked up" by getting entryAmmo().
     */
    protected int ammo = 0;

    protected int timeTillNextBerserk = 0;

    protected int timeTillNextShot = 0;

    public Weapon()
    {
        genericInit();
    }

    protected void genericInit()
    {
        BONUS_FASTERBERSERK = getNewBonusID();
        int[] berserkValues =
        {
            1, 2, 4
        };
        bonusValues.put( BONUS_FASTERBERSERK, new BonusValue( berserkValues, "Faster berserk recharge" ) );
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
            g.drawRect( MainWindow.frame().getWidth() - 120, 30, 100, 10 );
            int width = ( 200 - Math.max( timeTillNextBerserk, 0 ) ) / 2;
            g.fillRect( MainWindow.frame().getWidth() - 120, 30, width, 10 );

            // Draw ammo.
            if ( !isInfiniteAmmo() )
                g.drawString( "" + ammo, MainWindow.frame().getWidth() - 40, 60 );
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
        timeTillNextBerserk = Math.max( 0, timeTillNextBerserk - getBonusValue( BONUS_FASTERBERSERK ).getValue() );
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
     * @return      the name of the applied bonus, e.g. "Rapid fire!", or "" if none.
     */
    public String applyBonus()
    {
        // Create a list of availible upgrades.
        ArrayList<BonusValue> availableUpgrades = new ArrayList( bonusValues.size() );
        for ( int key : bonusValues.keySet() )
            if ( bonusValues.get( key ).canUpgrade() )
                availableUpgrades.add( bonusValues.get( key ) );

        // If we can, upgrade.
        if ( availableUpgrades.size() > 0 )
            return availableUpgrades.get( Util.getGameplayRandomGenerator().nextInt( availableUpgrades.size() ) ).upgrade();
        else
            return "";
    }

    protected int getNewBonusID()
    {
        return nextBonusID++;
    }

    public BonusValue getBonusValue( int key )
    {
        return bonusValues.get( key );
    }

    public void undoBonuses()
    {
        for ( int key : bonusValues.keySet() )
            bonusValues.get( key ).restore();
    }

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
        stream.writeInt( bonusValues.size() );
        for ( int key : bonusValues.keySet() )
        {
            stream.writeInt( key );
            bonusValues.get( key ).flatten( stream );
        }
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public Weapon( DataInputStream stream ) throws IOException
    {
        genericInit();
        ammo = stream.readInt();
        timeTillNextBerserk = stream.readInt();
        timeTillNextShot = stream.readInt();
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            bonusValues.get( stream.readInt() ).loadFromSteam( stream );
    }
}
