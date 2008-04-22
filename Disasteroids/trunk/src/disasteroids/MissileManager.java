/*
 * DISASTEROIDS
 * MissileManager.java
 */
package disasteroids;

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
     * The time between each shot
     * @since December 15, 2007
     */
    private int intervalShoot = 15;

    /**
     * The current initial speed for <code>Missile</code>s in this manager.
     * @since Classic
     */
    private double speed = 15;

    /**
     * The current probability of a huge blast in <code>Missile</code>s of this manager.
     * @since Classic
     */
    private int hugeBlastProb = 5;

    /**
     * The current size of huge blasts.
     * @since Classic
     */
    private int hugeBlastSize = 50;

    /**
     * The current probability that <code>Missile</code>s of this manager will split.
     * @since Classic
     */
    private int probPop = 2000;

    /**
     * The number of <code>Missile</code>s generated when splitting occurs.
     * @since Classic
     */
    private int popQuantity = 5;

    /**
     * The number of timesteps for which the <code>Missile</code>s will live before
     * self destructing.
     */
    private int life = 125;

    protected int maxShots = 200;

    public MissileManager()
    {
        ammo = -1;
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

        units.add( new Missile( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );

        if ( !isInfiniteAmmo() )
            --ammo;

        timeTillNextShot = intervalShoot;
        Sound.playInternal( SoundLibrary.MISSILE_SHOOT );
    }

    /**
     * Launches several clones of the given missile.
     */
    public void pop( Missile origin )
    {
        for ( int i = 0; i < popQuantity(); i++ )
            units.add( new Missile( this, origin.color, origin.getX(), origin.getY(), 0, 0, i * 2 * Math.PI / popQuantity() + i * Math.PI ) );
    }

    @Override
    public void berserk( GameObject parent, Color color )
    {
        int firedShots = 0;
        for ( double angle = 0; angle <= 2 * Math.PI; angle += Math.PI / 10 )
        {
            if ( !canBerserk() )
                break;

            units.add( new Missile( this, color, parent.getX(), parent.getY(), parent.getDx(), parent.getDy(), angle ) );

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
        int probPopTemp = probPop;
        probPop = Integer.MAX_VALUE;
        for ( Unit w : units )
            w.explode();
        probPop = probPopTemp;
    }

    /**
     * Sets the probability of a huge blast.
     * @param newProb The new probability.
     * @author Andy Kooiman
     * @since Classic
     */
    public void setHugeBlastProb( int newProb )
    {
        hugeBlastProb = newProb;
    }

    /**
     * Sets the size of huge blasts.
     * @param newSize The new size.
     * @author Andy Kooiman
     * @since Classic
     */
    public void setHugeBlastSize( int newSize )
    {
        hugeBlastSize = newSize;
    }

    /**
     * Sets the probability of <code>Missile</code>s splitting.
     * @param newProb The new probability.
     * @author Andy Kooiman
     * @since Classic
     */
    public void setProbPop( int newProb )
    {
        probPop = newProb;
    }

    /**
     * Increases the number of new <code>Missile</code>s added when a split occurs.
     * @param increase The number to increase the current pop quantity by.
     * @author Andy Kooiman
     * @since Classic
     */
    public void increasePopQuantity( int increase )
    {
        popQuantity += increase;
        popQuantity = Math.min( popQuantity, 50 );
    }

    /**
     * Sets the new default speed.
     * @param newSpeed The new speed.
     * @author Andy Kooiman
     * @since Classic
     */
    public void setSpeed( int newSpeed )
    {
        speed = newSpeed;
    }

    /**
     * Increases the default speed.
     * @param dSpeed The change in speed.
     * @author Andy Kooiman
     * @since Classic
     */
    public void increaseSpeed( int dSpeed )
    {
        speed += dSpeed;
    }

    /**
     * Sets the pop quantity to a value.
     * @param newQuantity The new pop quantity.
     * @author Andy Kooiman
     * @since Classic
     */
    public void setPopQuantity( int newQuantity )
    {
        popQuantity = newQuantity;
    }

    /**
     * Gets the probability of a split occurring.
     * @return The probability of a split.
     * @author Andy Kooiman
     * @since Classic
     */
    public int probPop()
    {
        return probPop;
    }

    /**
     * Gets the current size of a huge blast.
     * @return The current size of a huge blast.
     * @author Andy Kooiman
     * @since Classic
     */
    public int hugeBlastSize()
    {
        return hugeBlastSize;
    }

    /**
     * Gets the current default speed for new <code>Missile</code>s of this manager.
     * @return The current default speed.
     * @author Andy Kooiman
     * @since Classic
     */
    public double speed()
    {
        return speed;
    }

    /**
     * Gets the current probabiltiy of a huge blast.
     * @return The current probabiltiy of a huge blast.
     * @author Andy Kooiman
     * @since Classic
     */
    public int hugeBlastProb()
    {
        return hugeBlastProb;
    }

    /**
     * Gets the current quantity of <code>Missile</code>s created when a split occurs.
     * @return The number of <code>Missile</code>s created per split.
     * @author Andy Kooiman
     * @since Classic
     */
    public int popQuantity()
    {
        return popQuantity;
    }

    public void setIntervalShoot( int i )
    {
        intervalShoot = i;
    }

    public int getMaxUnits()
    {
        return maxShots;
    }

    public void undoBonuses()
    {
        setHugeBlastProb( 5 );
        setHugeBlastSize( 50 );
        setProbPop( 2000 );
        setPopQuantity( 5 );
        setSpeed( 15 );
        setIntervalShoot( 15 );
        setMaxShots( 10 );
    }

    private void setMaxShots( int numShots )
    {
        maxShots = numShots;
    }

    public String applyBonus( int key )
    {
        switch ( key )
        {
            case 0:
                setHugeBlastProb( 2 );
                return "Huge Blast Probable";
            case 1:
                setHugeBlastSize( 100 );
                return "Huge Blast Radius";
            case 2:
                setProbPop( 500 );
                return "Probability of Splitting Increased";
            case 4:
                increasePopQuantity( 15 );
                return "Split Quantity /\\ 15";
            case 6:
                setMaxShots( 150 );
                return "Max Shots = 150";
            case 7:
                setIntervalShoot( 3 );
                return "Rapid Fire";
            default:
                return "";
        }
    }

    public int life()
    {
        return life;
    }

    public void setLife( int life )
    {
        this.life = life;
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

        stream.writeInt( hugeBlastProb );
        stream.writeInt( hugeBlastSize );
        stream.writeInt( intervalShoot );
        stream.writeInt( life );
        stream.writeInt( maxShots );
        stream.writeInt( popQuantity );
        stream.writeInt( probPop );
        stream.writeDouble( speed );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public MissileManager( DataInputStream stream ) throws IOException
    {
        for ( int i = 0; i < stream.readInt(); i++ )
            units.add( new Missile( stream ) );

        hugeBlastProb = stream.readInt();
        hugeBlastSize = stream.readInt();
        intervalShoot = stream.readInt();
        life = stream.readInt();
        maxShots = stream.readInt();
        popQuantity = stream.readInt();
        probPop = stream.readInt();
        speed = stream.readDouble();
    }
}
