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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Phillip Cohen
 * @since March 12, 2008
 */
public class Bonus extends GameObject
{
    /**
     * Unique ID for this class. Used for C/S.
     * @since April 11, 2008
     */
    public static final int TYPE_ID = 2;

    final int RADIUS = 12;

    static final int MAX_LIFE = 1600;

    private float lastHue = 0.1f;

    private float lastHB = 0.6f;

    private int age = 0;

    /**
     * Acceleration vectors.
     * @since March 30, 2008
     */
    private double ax = 0,  ay = 0;

    /**
     * The type of bonus.
     * @since Classic
     */
    private int bonusType;

    public Bonus( double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy );
        bonusType = RandomGenerator.get().nextInt( 9 );
        Sound.playInternal( SoundLibrary.BONUS_SPAWN );
    }

    public Bonus( double x, double y )
    {
        this( x, y, RandomGenerator.get().nextDouble() * 2 - 1, RandomGenerator.get().nextDouble() * 2 - 1 );
    }

    public void act()
    {
        move();
        setSpeed( Math.min( 6, getDx() + ax ), Math.min( 6, getDy() + ay ) );

        ax *= 0.98;
        ay *= 0.98;

        if ( Math.abs( ax ) <= 0.01 || RandomGenerator.get().nextInt( 60 ) == 0 )
            ax = RandomGenerator.get().nextDouble() * 0.12 - 0.06;
        if ( Math.abs( ay ) <= 0.01 || RandomGenerator.get().nextInt( 60 ) == 0 )
            ay = RandomGenerator.get().nextDouble() * 0.12 - 0.06;
        if ( RandomGenerator.get().nextInt( 90 ) == 0 && ( Math.abs( ax ) == ax ) == ( Math.abs( getDx() ) == getDx() ) )
        {
            ax *= -1.8;
            ay *= -1.8;
        }

        ++age;
        if ( age > MAX_LIFE )
        {
            Game.getInstance().removeObject( this );
            for ( int i = 0; i < 500; i++ )
                ParticleManager.addParticle( new Particle(
                                             getX() + RandomGenerator.get().nextInt( 8 ) - 4,
                                             getY() + RandomGenerator.get().nextInt( 8 ) - 4,
                                             RandomGenerator.get().nextInt( 8 ),
                                             Color.getHSBColor( bonusType / 9.0f, 1f, .7f ),
                                             RandomGenerator.get().nextDouble() * 5,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             40, 2 ) );
        }
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
                if ( Math.pow( getX() - s.getX(), 2 ) + ( Math.pow( getY() - s.getY(), 2 ) ) <
                        ( RADIUS + s.getRadius() ) * ( RADIUS + s.getRadius() ) )
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
            case 8:
                message = killer.giveShield();
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

        AsteroidsFrame.frame().drawOutlinedCircle( g, Color.getHSBColor( bonusType / 9.0f, ( (float) age ) / MAX_LIFE, .9f ), Color.getHSBColor( lastHue, lastHB, 1 - lastHB ), (int) getX(), (int) getY(), Math.min( Math.min( RADIUS, age / 2 ), ( MAX_LIFE - age ) / 2 ) );
        g.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
        AsteroidsFrame.frame().drawString( g, (int) getCenterX() - 4, (int) getCenterY() - 1, "B", Color.darkGray );
    }

    double getCenterX()
    {
        return getX() + RADIUS / 2;
    }

    double getCenterY()
    {
        return getY() + RADIUS / 2;
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since April 11, 2008
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeDouble( ax );
        stream.writeDouble( ay );
        stream.writeInt( bonusType );
        stream.writeInt( age );
        stream.writeFloat( lastHB );
        stream.writeFloat( lastHue );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since April 11, 2008
     */
    public Bonus( DataInputStream stream ) throws IOException
    {
        super( stream );
        ax = stream.readDouble();
        ay = stream.readDouble();
        bonusType = stream.readInt();
        age = stream.readInt();
        lastHB = stream.readFloat();
        lastHue = stream.readFloat();
    }
}
