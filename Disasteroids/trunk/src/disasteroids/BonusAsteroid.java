/*
 * DISASTEROIDS
 * BonusAsteroid.java
 */
package disasteroids;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
     * @since Classic
     */
    public BonusAsteroid( int x, int y, double dx, double dy, int size, int life )
    {
        super( x, y, dx, dy, size, life );
        fill = Color.green; // radioactive?
        outline = Color.white;

        // Choose the type of bonus.
        Random rand = RandNumGen.getAsteroidInstance();
        bonusType = rand.nextInt( 8 );
    }

    /**
     * Called when the <code>BonusAsteroid</code> is killed.
     * Splits into two normal <code>Asteroid</code>s and applies bonus.    
     * @author Andy Kooiman
     * @since Classic
     */
    @Override
    protected void kill()
    {
        if ( children > 2 )
            return;

        if ( radius >= 12 )
        {
            Game.getInstance().asteroidManager.add( new Asteroid( (Asteroid) this ), true );
            Game.getInstance().asteroidManager.add( new Asteroid( (Asteroid) this ), true );
            if ( killer != null )
                applyBonus( killer );
        }
    }

    /**
     * Applies the bonus to the killer
     * @param killer The <code>Ship</code> which killed <code>this</code>
     * @since Classic
     */
    private void applyBonus( Ship killer )
    {
        if ( killer == null )
            return;
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

        if ( AsteroidsFrame.frame() != null )
        {
            AsteroidsFrame.frame().writeOnBackground( message, (int) getX(), (int) getY(), killer.getColor() );
            if ( killer == AsteroidsFrame.frame().localPlayer() )
                Running.log( "Bonus: " + message );
        }
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since January 10, 2008
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( bonusType );


    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 29, 2007
     */
    public BonusAsteroid( DataInputStream stream ) throws IOException
    {
        super( stream );
        bonusType = stream.readInt();

        fill = Color.green; // radioactive?
        outline = Color.white;
    }
}
