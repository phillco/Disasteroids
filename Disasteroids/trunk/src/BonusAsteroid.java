/*
 * DISASTEROIDS
 * BonusAsteroid.java
 */

import java.awt.Color;
import java.util.Random;

/**
 * A darker <code>Asteroid</code> that gives bonuses when shot.
 * @author Andy Kooiman
 */
public class BonusAsteroid extends Asteroid
{
    /**
     * The type of bonus.
     * @since Classic
     */
    private int bonusType;

    /**
     * Creates a new <code>BonusAsteroid</code>.
     * 
     * @param x             the x coordinate
     * @param y             the y coordinate
     * @param dx            the x velocity
     * @param dy            the y velocity (up is negative)
     * @param size          the diameter
     * @param environment   the <code>AsteroidManager</code> to which it belongs
     * @since Classic
     */
    public BonusAsteroid( int x, int y, double dx, double dy, int size, int life, AsteroidManager environment )
    {
        super( x, y, dx, dy, size, life, environment );
        fill = Color.green;//radioactive?
        outline = Color.white;

        // Choose the type of bonus.
        Random rand = RandNumGen.getAsteroidInstance();
        bonusType = rand.nextInt( 8 );
    }

    /**
     * Called when the <code>BonusAsteroid</code> is killed.
     * Splits into two normal <code>Asteroid</code>s and applies bonus.
     * @param killer The <code>Ship</code> which killed <code>this</code>.
     * @author Andy Kooiman
     * @since Classic
     */
    @Override
    protected void split( Ship killer )
    {
        if ( children > 2 )
        {
            shouldRemove = true;
            return;
        }
        if ( radius < 12 )
            shouldRemove = true;
        else
        {
            environment.add( new Asteroid( (Asteroid) this ) );
            environment.add( new Asteroid( (Asteroid) this ) );
            applyBonus( killer );
            shouldRemove = true;
        }
    }

    /**
     * Applies the bonus to the killer
     * @param killer The <code>Ship</code> which killed <code>this</code>
     * @since Classic
     */
    private void applyBonus( Ship killer )
    {
        String message = "";
        switch ( bonusType )
        {
            case 0:
                message = killer.getWeaponManager().ApplyBonus( 0 );
                break;
            case 1:
                message = killer.getWeaponManager().ApplyBonus( 1 );
                break;
            case 2:
                message = killer.getWeaponManager().ApplyBonus( 2 );
                break;
            case 3:
                message = "+1 Life";
                killer.addLife();
                break;
            case 4:
                message = killer.getWeaponManager().ApplyBonus( 4 );
                break;
            case 5:
                killer.increaseScore( 10000 );
                message = "+10,000 Points";
                break;
            case 6:
                message = killer.getWeaponManager().ApplyBonus( 6 );
                break;
            case 7:
                message = killer.getWeaponManager().ApplyBonus( 7 );
                break;
        }
        if ( message.equals( "" ) )
            return;

        if( AsteroidsFrame.frame() != null)
        {
        	AsteroidsFrame.frame().writeOnBackground( message, (int) x, (int) y, killer.getColor() );        
	        if ( killer == AsteroidsFrame.frame().localPlayer() )
	            Running.log( "Bonus: " + message );
        }
    }
}
