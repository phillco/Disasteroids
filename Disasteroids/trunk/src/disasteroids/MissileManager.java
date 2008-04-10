/*
 * DISASTEROIDS
 * MissileManager.java
 */
package disasteroids;

import disasteroids.sound.LayeredSound.SoundClip;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The classic weapon that fires <code>Missiles</code>.
 * @author Andy Kooiman
 * @since Classic
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
    private int life = 300;

    private int maxShots = 10;

    public MissileManager()
    {
        weapons = new ConcurrentLinkedQueue<Unit>();
    }

    public MissileManager( ConcurrentLinkedQueue<Unit> start )
    {
        weapons = start;
    }

    /**
     * Iterates through each <code>Missile</code> and either removes it or instructs it to act.
     * @author Andy Kooiman
     * @since Classic
     */
    public void act()
    {
        super.act( true );/*
    Iterator<Unit> iter = theMissiles.iterator();
    while ( iter.hasNext() )
    {
    Unit w = iter.next();
    if ( w.needsRemoval() )
    iter.remove();
    else
    w.act();
    }*/
    }

    /**
     * Instructs each <code>Missile</code> to explode, without splitting.
     * @author Andy Kooiman
     * @since Classic
     */
    @Override
    public void explodeAll()
    {
        int probPopTemp = probPop;
        probPop = Integer.MAX_VALUE;
        for ( Unit w : weapons )
            w.explode();
        probPop = probPopTemp;
    }

    /**
     * Creates and prepares to add a <code>Missile</code> with the specified properties.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param angle The angle the <code>Missile</code> will be pointing (not necessarily the angle it will be traveling).
     * @param dx The x component of velocity.
     * @param dy The y component of velocity (up is negative).
     * @param col The <code>Color</code> of the <code>Missile</code>..
     * @return True if the <code>Missile</code> was successfully added, false otherwise.
     * @author Andy Kooiman
     * @since Classic
     */
    public boolean add( int x, int y, double angle, double dx, double dy, Color col, boolean playShootSound )
    {
        return add( new Missile( this, x, y, angle, dx, dy, col ), playShootSound );
    }

    public boolean add( Unit a, boolean playShootSound )
    {
        if ( weapons.size() > maxShots )//|| timeTillNextShot > 0 )
            return false;
        timeTillNextShot = getIntervalShoot();

        if ( playShootSound )
            Sound.playInternal( getShootSound() );

        return weapons.add( a );
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

    public int getIntervalShoot()
    {
        return intervalShoot;
    }

    public void setIntervalShoot( int i )
    {
        intervalShoot = i;
    }

    public int getMaxShots()
    {
        return maxShots;
    }

    public void restoreBonusValues()
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

    public String ApplyBonus( int key )
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
                setMaxShots( 50 );
                return "Max Shots=> 50";
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

    public void draw( Graphics g )
    {
        for ( Unit w : weapons )
            w.draw( g );
    }

    public String getWeaponName()
    {
        return "Missiles";
    }

    public Unit getWeapon( int x, int y, Color col )
    {
        return new Missile( this, x, y, 0, 0, 0, col );
    }

    public void berserk( Ship s )
    {
        if ( timeTillNextBerserk > 0 || weapons.size() > maxShots )
            return;
        Sound.playInternal( getBerserkSound() );
        for ( double ang = 0; ang <= 2 * Math.PI; ang += Math.PI / 10 )
            weapons.add( new Missile( this, (int) s.getX(), (int) s.getY(), ang, s.getDx(), s.getDy(), s.getColor() ) );
        timeTillNextBerserk = 100;
    }

    @Override
    public boolean canShoot()
    {
        return super.canShoot() && weapons.size() < 100;
    }

    public SoundClip getShootSound()
    {
        return SoundLibrary.MISSILE_SHOOT;
    }

    public SoundClip getBerserkSound()
    {
        return SoundLibrary.BERSERK;
    }
}
