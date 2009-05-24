/*
 * DISASTEROIDS
 * BigNukeLauncher.java
 */
package disasteroids.weapons;

import disasteroids.*;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A bonus weapon that fires <code>BigNuke</code>s which then burst into explosive particles (<code>BigNukeCharge</code>).
 * @author Phillip Cohen
 */
public class BigNukeLauncher extends Weapon
{
    private int chainReactionChance = 30;


    public BigNukeLauncher()
    {
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        units.add( new BigNuke( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );

        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = 80;
    }

    /**
     * Shoots up to 8 bullets in a perfect circle to form one big explosion.
     */
    @Override
    public void berserk( GameObject parent, Color color )
    {
        if ( !canBerserk() )
            return;

        int shotsToFire = ammo == -1 ? 8 : Math.min( 8, ammo );
        for ( int i = 0; i < shotsToFire; i++ )
            units.add( new BigNuke( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), Math.PI * 2 * i / shotsToFire ) );

        if ( !isInfiniteAmmo() )
            ammo -= shotsToFire;
        timeTillNextBerserk = shotsToFire * 30;
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        new BigNuke( this, col, x, y, 0, 0, 0 ).draw( g );
    }

    @Override
    public String getName()
    {
        return "Big Nuke";
    }

    @Override
    public int getMaxUnits()
    {
        // This includes nukes and charges.
        return 200;
    }

    @Override
    public int getEntryAmmo()
    {
        return 4;
    }

    @Override
    public String applyBonus( )
    {
        return "";
    }

    @Override
    public void undoBonuses()
    {
    }

    public int getChainReactionChance()
    {
        return chainReactionChance;
    }


    
     //                                                                            \\
    // ------------------------------ NETWORKING -------------------------------- \\
    //                                                                            \\
    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        
        // Flatten all of the units.
        stream.writeInt( units.size() );
        for ( Unit u : units )
        {
            stream.writeBoolean( u instanceof BigNuke );
            u.flatten( stream );
        }
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public BigNukeLauncher( DataInputStream stream ) throws IOException
    {
        super( stream );
        
        // Restore all of the units.
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
        {
            if ( stream.readBoolean())
                units.add( new BigNuke( stream, this ));
            else
                units.add( new BigNukeCharge( stream, this ));
        }
    }
}
