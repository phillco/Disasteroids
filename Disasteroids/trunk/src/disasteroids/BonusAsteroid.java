/*
 * DISASTEROIDS
 * BonusAsteroid.java
 */
package disasteroids;

import java.awt.Color;

/**
 * A darker <code>Asteroid</code> that gives bonuses when shot.
 * @author Andy Kooiman
 */
public class BonusAsteroid extends Asteroid
{
    /**
     * Constructs a new Asteroid from scratch.
     * 
     * @param x				the x coordinate
     * @param y				the y coordinate
     * @param dx			the x velocity
     * @param dy			the y velocity (up is negative)
     * @param size			the diameter
     * @param lifeMax                   total amount of life
     * @since Classic
     */
    public BonusAsteroid( int x, int y, double dx, double dy, int size, int lifeMax )
    {
        super( x, y, dx, dy, size, lifeMax );
        
        fill = Color.green;
        outline = Color.white;
    }

    /**
     * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
     * This is used when a missile splits an <code>Asteroid</code>.
     * 
     * @param parent	the parent <code>Asteroid</code> to kill from
     * @since Classic
     */
    public BonusAsteroid( Asteroid parent )
    {
        super( parent );
    }

    @Override
    protected void kill()
    {
        Game.getInstance().createBonus( this );
        super.kill();
    }
}
