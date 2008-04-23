/*
 * DISASTEROIDS
 * Bonus.javs
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.Local;
import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;
import disasteroids.gui.RelativeGraphics;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
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
    static enum Class
    {
        WEAPON( 3 ), POWERUP( 9 );

        final int types;

        private Class( int types )
        {
            this.types = types;
        }
    }
    /**
     * The class of bonus (category).
     * @since April 12, 2008
     */
    private Class myClass = Class.values()[Util.getRandomGenerator().nextInt( Class.values().length )];

    /**
     * The type of bonus (within its category).
     * @since Classic
     */
    private int bonusType = Util.getRandomGenerator().nextInt( myClass.types );

    final int RADIUS = 12;

    static final int MAX_LIFE = 1600;

    private float lastHue = 0.1f;

    private float lastHB = 0.6f;

    private int age = 0;

    private double angle = Math.PI / 2;

    /**
     * Acceleration vectors.
     * @since March 30, 2008
     */
    private double ax = 0,  ay = 0;

    public Bonus( double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy );
        Sound.playInternal( SoundLibrary.BONUS_SPAWN );
    }

    public Bonus( double x, double y )
    {
        this( x, y, Util.getRandomGenerator().nextDouble() * 2 - 1, Util.getRandomGenerator().nextDouble() * 2 - 1 );
    }

    public void act()
    {
        move();
        setSpeed( Math.min( 6, getDx() + ax ), Math.min( 6, getDy() + ay ) );

        ax *= 0.98;
        ay *= 0.98;

        if ( Math.abs( ax ) <= 0.01 || Util.getRandomGenerator().nextInt( 60 ) == 0 )
            ax = Util.getRandomGenerator().nextDouble() * 0.08 - 0.04;
        if ( Math.abs( ay ) <= 0.01 || Util.getRandomGenerator().nextInt( 60 ) == 0 )
            ay = Util.getRandomGenerator().nextDouble() * 0.08 - 0.04;
        if ( Util.getRandomGenerator().nextInt( 90 ) == 0 && ( Math.abs( ax ) == ax ) == ( Math.abs( getDx() ) == getDx() ) )
        {
            ax *= -1.2;
            ay *= -1.2;
        }

        ++age;
        if ( age > MAX_LIFE )
        {
            Game.getInstance().removeObject( this );
            for ( int i = 0; i < 500; i++ )
                ParticleManager.addParticle( new Particle(
                                             getX() + Util.getRandomGenerator().nextInt( 8 ) - 4,
                                             getY() + Util.getRandomGenerator().nextInt( 8 ) - 4,
                                             Util.getRandomGenerator().nextInt( 8 ),
                                             Color.getHSBColor( bonusType / 9.0f, 1f, .7f ),
                                             Util.getRandomGenerator().nextDouble() * 5,
                                             Util.getRandomGenerator().nextDouble() * 2 * Math.PI,
                                             40, 2 ) );
            Sound.playInternal( SoundLibrary.BONUS_FIZZLE );
        }
        checkCollision();
        for ( int i = 0; i < 3; i++ )
            ParticleManager.addParticle( new Particle(
                                         getX() + Util.getRandomGenerator().nextInt( 8 ) - 4,
                                         getY() + Util.getRandomGenerator().nextInt( 8 ) - 4,
                                         Util.getRandomGenerator().nextInt( 4 ),
                                         Color.getHSBColor( lastHue, lastHB, 1 - lastHB ),
                                         Util.getRandomGenerator().nextDouble() * 3,
                                         Util.getRandomGenerator().nextDouble() * 2 * Math.PI,
                                         50, 1 ) );
        angle = ( angle + 0.03 ) % ( 2 * Math.PI );
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
                    if ( applyBonus( s ) )
                    {
                        Sound.playInternal( SoundLibrary.GET_BONUS );
                        Game.getInstance().removeObject( this );
                    }
                }
            }
        }
    }

    /**
     * Applies the bonus to the ship who won the bonus..
     * 
     * @param player    the <code>Ship</code> which picked up the bonus
     * @return          whether it was awarded
     * @since Classic
     */
    private boolean applyBonus( Ship player )
    {
        if ( player == null )
            return false;

        String message = "";
        switch ( myClass )
        {
            case WEAPON:
                // "Give" the weapon.
                if ( player.getWeapons()[bonusType + 2].getAmmo() == 0 )
                    message = "Picked up a " + player.getWeapons()[bonusType + 2].getName() + "!";
                else
                    message = "Got ammo for " + player.getWeapons()[bonusType + 2].getName() + ".";

                player.getWeapons()[bonusType + 2].giveAmmo();
                player.setWeapon( bonusType + 2 );
                break;
            case POWERUP:
                switch ( bonusType )
                {
                    case 0:
                        message = player.getWeaponManager().applyBonus( 0 );
                        break;
                    case 1:
                        message = player.getWeaponManager().applyBonus( 1 );
                        break;
                    case 2:
                        message = player.getWeaponManager().applyBonus( 2 );
                        break;
                    case 3:
                        message = "+1 Life";
                        player.addLife();
                        break;
                    case 4:
                        message = player.getWeaponManager().applyBonus( 4 );
                        break;
                    case 5:
                        player.increaseScore( 10000 );
                        message = "+10,000 Points";
                        break;
                    case 6:
                        message = player.getWeaponManager().applyBonus( 6 );
                        break;
                    case 7:
                        message = player.getWeaponManager().applyBonus( 7 );
                        break;
                    case 8:
                        message = player.giveShield();
                        break;
                }
                break;
        }

        if ( message.length() == 0 )
            return false;

        if ( !Local.isStuffNull() )
        {
            Local.getStarBackground().writeOnBackground( message, (int) getX(), (int) getY(), player.getColor() );
            if ( player == AsteroidsFrame.frame().localPlayer() )
                Running.log( "Bonus: " + message );
        }

        return true;
    }

    public void draw( Graphics g )
    {
        lastHue = ( ( lastHue + 0.01f ) % 1 );
        lastHB = ( ( lastHB + 0.03f ) % 1 );

        switch ( myClass )
        {
            case POWERUP:
                AsteroidsFrame.frame().drawOutlinedCircle( g, Color.getHSBColor( ( (float) bonusType ) / myClass.types, ( (float) age ) / MAX_LIFE, .9f ), Color.getHSBColor( lastHue, lastHB, 1 - lastHB ), (int) getX(), (int) getY(), Math.min( Math.min( RADIUS, age / 2 ), ( MAX_LIFE - age ) / 2 ) );
                break;
            case WEAPON:

                int cX = RelativeGraphics.translateX( getX() );
                int cY = RelativeGraphics.translateY( getY() );
                int deviance = 5 + Math.min( Math.min( RADIUS, age / 2 ), ( MAX_LIFE - age ) / 2 );
                int x1=(int)(cX+deviance*Math.cos(Math.toRadians(0)+angle));
                int x2=(int)(cX+deviance*Math.cos(Math.toRadians(-120)+angle));
                int x3=(int)(cX+deviance*Math.cos(Math.toRadians(120)+angle));
                int y1=(int)(cY-deviance*Math.sin(Math.toRadians(0)+angle));
                int y2=(int)(cY-deviance*Math.sin(Math.toRadians(-120)+angle));
                int y3=(int)(cY-deviance*Math.sin(Math.toRadians(120)+angle));
                Polygon p=new Polygon();
                p.addPoint(x1,y1);
                p.addPoint(x2,y2);
                p.addPoint(x3,y3);
                g.setColor( Color.getHSBColor( bonusType / 9.0f, ( (float) age ) / MAX_LIFE, .9f ) );
                g.fillPolygon(p);
                g.setColor( Color.getHSBColor( lastHue, lastHB, 1 - lastHB ) );
                g.drawPolygon(p);
                break;
        }
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
