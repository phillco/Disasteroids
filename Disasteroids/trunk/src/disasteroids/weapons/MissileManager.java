/*
 * DISASTEROIDS
 * MissileManager.java
 */
package disasteroids.weapons;

import disasteroids.*;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The classic weapon that fires <code>Missiles</code>.
 * @author Andy Kooiman
 */
public class MissileManager extends Weapon
{
    /**
     * The current initial speed for <code>Missile</code>s in this manager.
     * @since Classic
     */
    private double speed = 15;

    /**
     * The number of timesteps for which the <code>Missile</code>s will live before self destructing.
     */
    private int life = 125;

    protected int maxShots = 120;

    public MissileManager()
    {
        ammo = -1;
        bonusValues.put( "intervalShoot", new BonusValue( 15, 6, "Rapid fire" ) );
        bonusValues.put( "hugeBlastProb", new BonusValue( 5, 2, "Huge blast probable" ) );
        bonusValues.put( "hugeBlastSize", new BonusValue( 50, 100, "Huge blast radius" ) );
        bonusValues.put( "poppingProb", new BonusValue( 2000, 500, "Missiles more likely to split" ) );
        bonusValues.put( "poppingQuantity", new BonusValue( 5, 15, "Splitting quantity increased" ) );
        bonusValues.put( "maxShots", new BonusValue( 5, 15, "Splitting quantity increased" ) );
    }

    @Override
    public void drawOrphanUnit( Graphics g, double x, double y, Color col )
    {
        new Missile( this, col, x, y, 0, 0, 0 ).draw( g );
    }

    @Override
    public void shoot( GameObject parent, Color color, double angle )
    {
        if ( !canShoot() )
            return;

        units.add( new Missile( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );

        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = getBonusValue( "intervalShoot" ).getValue();
        Sound.playInternal( SoundLibrary.MISSILE_SHOOT );
    }

    public void shoot( Color color, double x, double y, double dx, double dy, double angle )
    {
    }

    /**
     * Launches several clones of the given missile.
     */
    public void pop( Missile origin )
    {
        if ( units.size() >= maxShots )
            return;
        
        for ( int i = 0; i < getBonusValue( "poppingQuantity" ).getValue(); i++ )
            units.add( new Missile( this, origin.color, origin.getX(), origin.getY(), 0, 0, i * 2 * Math.PI / getBonusValue( "poppingQuantity" ).getValue() + i * Math.PI ) );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( double angle = 0; angle <= 2 * Math.PI; angle += Math.PI / 10 )
        {
            if ( !canBerserk() )
                break;

            units.add( new Missile( this, color, parent.getFiringOriginX(), parent.getFiringOriginY(), parent.getDx(), parent.getDy(), angle ) );

            if ( !isInfiniteAmmo() )
                --ammo;

            ++firedShots;
        }

        if ( firedShots > 0 )
        {
            timeTillNextBerserk = firedShots * 10;
            Sound.playInternal( SoundLibrary.BERSERK );
        }
    }

    /**
     * Explodes all missiles without popping any.
     */
    @Override
    public void explodeAllUnits()
    {
        // Temporarily prevent popping.
        int poppingProbTemp = getBonusValue( "poppingProb" ).getValue();
        getBonusValue( "poppingProb" ).override( Integer.MAX_VALUE );

        for ( Unit w : units )
            w.explode();
        
        getBonusValue( "poppingProb" ).override( poppingProbTemp );
    }

    public int getMaxUnits()
    {
        return maxShots;
    }

    public int life()
    {
        return life;
    }

    public void setLife( int life )
    {
        this.life = life;
    }

    public double speed()
    {
        return speed;
    }

    public String getName()
    {
        return "Missile Launcher";
    }

    @Override
    public int getEntryAmmo()
    {
        return 0;
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
        stream.writeInt( life );
        stream.writeInt( maxShots );
        stream.writeDouble( speed );

        // Flatten all of the units.
        stream.writeInt( units.size() );
        for ( Unit u : units )
            ( (Missile) u ).flatten( stream );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public MissileManager( DataInputStream stream ) throws IOException
    {
        super( stream );
        life = stream.readInt();
        maxShots = stream.readInt();
        speed = stream.readDouble();

        // Restore all of the units.
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            units.add( new Missile( stream, this ) );
    }
}
