/*
 * DISASTEROIDS
 * Bonus.javs
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 *
 * @author Phillip Cohen
 * @since March 12, 2008
 */
public class Bonus extends GameObject
{
    final int RADIUS = 12;

    private float lastHue = 0.1f;

    private float lastHB = 0.6f;

    /**
     * The type of bonus.
     * @since Classic
     */
    private int bonusType;

    public Bonus( double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy );
        bonusType = RandomGenerator.get().nextInt( 8 );
        Sound.playInternal( SoundLibrary.BONUS_SPAWN );
    }

    public Bonus( double x, double y )
    {
        this( x, y, RandomGenerator.get().nextDouble() * 2 - 1, RandomGenerator.get().nextDouble() * 2 - 1 );
    }

    public void act()
    {
        move();
        checkCollision();
        for ( int i = 0; i < 3; i++ )
            ParticleManager.addParticle( new Particle(
                                         getX() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         getY() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ),
                                         Color.getHSBColor( lastHue, lastHB, 1 - lastHB ),
                                         RandomGenerator.get().nextDouble() * 3,
                                         RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                         50, 1 ) );
    }

    /**
     * Checks, and acts, if we were picked up by a ship.
     * 
     * @since March 12, 2008
     */
    private void checkCollision()
    {
        // Go through all of the ships.        
        for ( Ship s : Game.getInstance().players )
        {
            // Were we hit by the ship's body?
            if ( s.livesLeft() >= 0 )
            {
                if ( Math.pow( getX() - s.getX(), 2 ) + ( Math.pow( getY() - s.getY(), 2 ) ) < ( RADIUS + Ship.RADIUS ) * ( RADIUS + Ship.RADIUS ) )
                {
                    Sound.playInternal( SoundLibrary.GET_BONUS );
                    Game.getInstance().removeObject( this );
                    applyBonus( s );
                }
            }
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

    public void draw( Graphics g )
    {
        lastHue = ( ( lastHue + 0.01f ) % 1 );
        lastHB = ( ( lastHB + 0.03f ) % 1 );

        AsteroidsFrame.frame().drawOutlinedCircle( g, Color.lightGray, Color.getHSBColor( lastHue, lastHB, 1 - lastHB ), (int) getX(), (int) getY(), 12 );
        g.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
        AsteroidsFrame.frame().drawString( g, (int) getCenterX() - 9, (int) getCenterY() - 1, "B", Color.darkGray );
    }

    double getCenterX()
    {
        return getX() + RADIUS / 2;
    }

    double getCenterY()
    {
        return getY() + RADIUS / 2;
    }
}
