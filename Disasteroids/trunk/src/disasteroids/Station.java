/*
 * DISASTEROIDS
 * Station.java
 */
package disasteroids;

import disasteroids.weapons.*;
import disasteroids.gui.*;
import disasteroids.sound.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A satellite that shoots missiles at passing ships.
 * @author Phillip Cohen, Andy Kooiman
 */
public class Station extends GameObject implements ShootingObject
{

    /**
     * The angle we're facing.
     */
    private double angle;

    /**
     * Our firing manager.
     */
    private MissileManager manager;

    /**
     * Width/height of the station.
     */
    int size = 35;

    /**
     * Timer to show the clock easter egg. If 0, the egg should be hidden.
     */
    private int easterEggCounter = 0;

    private int health = 500;

    /**
     * The angle we're turning towards.
     */
    private double desiredAngle = 0.0;

    final double SWEEP_SPEED = 0.05;

    /**
     * Creates the station at the given position and random floating speed.
     * 
     * @param x     x coordinate in game
     * @param y     y coordinate in game
     */
    public Station( double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy );
        angle = 0;
        manager = new MissileManager();
        manager.getBonusValue( manager.BONUS_POPPINGQUANTITY ).override( 0 );
        manager.setLife( 50 );
    }

    /**
     * Moves, acquires a target, and shoots.
     * 
     * @since January 6, 2008
     */
    public void act()
    {
        move();
        checkCollision();
        manager.act();
        manager.reload();

        angle %= 2 * Math.PI; //Make sure the angle does not grow without bound

        // Easter egg.
        if ( easterEggCounter > 0 )
        {
            easterEggCounter--;
            return;
        }

        // We're disabled.
        if ( isDisabled() )
        {
            health++;

            // Smoke and spin the turret.
            angle += 0.07 + Util.getGameplayRandomGenerator().nextDouble() / 7;
            ParticleManager.createSmoke( getX() + Util.getGameplayRandomGenerator().nextInt( size ), centerY(), Math.max( 0, ( 20 - health ) / 5 ) );

            // If about to die, set fire.
            ParticleManager.createFlames( getX() + Util.getGameplayRandomGenerator().nextInt( size ), centerY(), Math.max( 0, ( 10 - health ) / 5 ) );

            return;
        }

        // Find players within our range.        
        int range = 300;
        Ship closestShip = null;
        {
            Ship closestInvincible = null;
            for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
            {
                if ( Util.getDistance( this, s ) < range )
                {
                    if ( closestShip == null || Util.getDistance( this, s ) > Util.getDistance( this, closestShip ) )
                        closestShip = s;
                    if ( closestInvincible == null || Util.getDistance( this, s ) > Util.getDistance( this, closestInvincible ) )
                        closestInvincible = s;
                }
            }
            if ( closestShip == null && closestInvincible != null )
                closestShip = closestInvincible;
        }

        // Aim towards closest ship.
        if ( closestShip != null )
        {
            calculateAngle( closestShip );

            // Fire!
            if ( ( ( desiredAngle - angle ) + 2 * Math.PI ) % ( 2 * Math.PI ) < SWEEP_SPEED * 6 && !closestShip.cannotDie() )
            {
                if ( manager.canShoot() )
                {
                    manager.shoot( this, Color.white, 0 - angle );
                    Sound.playInternal( SoundLibrary.STATION_SHOOT );  // Play a custom sound.

                }
            }
        }
        else
            angle += 0.01;
    }

    public boolean isDisabled()
    {
        return ( health < 20 );
    }

    /**
     * Checks if players run into us and takes action.
     * 
     * @since January 9, 2007
     */
    private void checkCollision()
    {
        // Check for missile collision.
        for ( ShootingObject s : Game.getInstance().getObjectManager().getShootingObjects() )
        {
            if ( s == this )
                continue;

            // Loop through the mangers.
            for ( Weapon wm : s.getManagers() )
            {
                // Loop through the bullets.
                for ( Unit m : wm.getUnits() )
                {
                    // Were we hit by a bullet?
                    if ( ( m.getX() + m.getRadius() > getX() && m.getX() - m.getRadius() < getX() + size ) &&
                            ( m.getY() + m.getRadius() > getY() && m.getY() - m.getRadius() < getY() + size ) )
                    {
                        health -= m.getDamage();
                        m.explode();

                        if ( health < 0 )
                        {
                            destroy();
                            if ( s instanceof Ship )
                                ( ( Ship ) s ).increaseScore( 2500 );
                                return;
                            }
                    }
                }
            }
        }

        if ( isDisabled() )
            return;

        // Check for ship collision.  
        for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
        {
            // Were we hit by the ship's body?
            if ( s.livesLeft() >= 0 )
            {
                if ( ( s.getX() + s.getRadius() > getX() && s.getX() - s.getRadius() < getX() + size ) &&
                        ( s.getY() + s.getRadius() > getY() && s.getY() - s.getRadius() < getY() + size ) )
                {
                    if ( s.damage( 60, s.getName() + " learns to steer." ) )
                        return;
                }
            }
        }
    }

    /**
     * Draws this and our bullets to the given context. Uses RelativeGraphics.
     * 
     * @param g the context
     * @since January 6, 2008
     */
    public void draw( Graphics g )
    {
        // Flash when disabled.
        if ( isDisabled() && Util.getGlobalFlash() )
            return;

        int rX = RelativeGraphics.translateX( getX() );
        int rY = RelativeGraphics.translateY( getY() );

        int cX = RelativeGraphics.translateX( centerX() );
        int cY = RelativeGraphics.translateY( centerY() );

        // Draw the base.
        g.setColor( Color.darkGray );
        g.fillRect( rX, rY, size, size );
        g.setColor( new Color( 20, 20, 20 ) );
        g.drawRect( rX, rY, size, size );

        // Draw the corners.
        g.setColor( new Color( 20, 20, 20 ) );
        g.fillRect( rX - 2, rY - 2, 10, 10 );
        g.fillRect( rX + 27, rY, 10, 10 );
        g.fillRect( rX + 27, rY + 27, 10, 10 );
        g.fillRect( rX, rY + 27, 10, 10 );

//        g.setFont( new Font( "Tahoma", Font.PLAIN, 12 ) );
//        g.setColor( Color.white );
//        g.drawString( new DecimalFormat( "0.00" ).format( angle / Math.PI ), rX, rY - 9 );
//        g.drawString( new DecimalFormat( "0.00" ).format( desiredAngle / Math.PI ), rX + size, rY - 9 );

        // Draw the easter egg clock.
        if ( easterEggCounter > 0 )
        {
            g.setFont( new Font( "Tahoma", Font.PLAIN, 8 ) );

            // Numbers.
            g.setColor( Color.white );
            g.drawString( "12", cX - 3, rY + 10 );
            g.drawString( "3", rX + size - 7, cY + 5 );
            g.drawString( "6", cX - 1, rY + size - 4 );
            g.drawString( "9", rX + 4, cY + 5 );

            // Hour hand.
            double hourAngle = ( ( Calendar.getInstance().get( Calendar.HOUR ) / 12.0 ) * 2 - 0.5 ) * Math.PI;
            int eX = ( int ) ( cX + 5 * Math.cos( hourAngle ) );
            int eY = ( int ) ( cY + 5 * Math.sin( hourAngle ) );
            g.drawLine( cX, cY, eX, eY );
            g.drawLine( cX, cY + 1, eX, eY + 1 );
            g.drawLine( cX + 1, cY, eX + 1, eY );

            // Minute hand.
            double minuteAngle = ( ( Calendar.getInstance().get( Calendar.MINUTE ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
            eX = ( int ) ( cX + 10 * Math.cos( minuteAngle ) );
            eY = ( int ) ( cY + 10 * Math.sin( minuteAngle ) );
            g.drawLine( cX, cY, eX, eY );
            g.drawLine( cX, cY + 1, eX, eY + 1 );

            // Second hand.
            double secondAngle = ( ( Calendar.getInstance().get( Calendar.SECOND ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
            eX = ( int ) ( cX + 15 * Math.cos( secondAngle ) );
            eY = ( int ) ( cY + 15 * Math.sin( secondAngle ) );
            g.drawLine( cX, cY, eX, eY );
        }
        else
        {
            // Draw the turret.
            g.setColor( Color.white );
            int eX = ( int ) ( cX + 15 * Math.cos( angle ) );
            int eY = ( int ) ( cY + 15 * Math.sin( angle ) );
            g.drawLine( cX, cY, eX, eY );
            g.drawLine( cX, cY + 1, eX, eY + 1 );
            g.drawLine( cX + 1, cY, eX + 1, eY );
        }
        manager.draw( g );
    }

    /**
     * Returns the center position of the station.
     * 
     * @return      the x coordinate of the center
     * @since January 6, 2008
     */
    int centerX()
    {
        return ( int ) ( getX() + size / 2 );
    }

    /**
     * Returns the center position of the station.
     * 
     * @return      the y coordinate of the center
     * @since January 6, 2008
     */
    int centerY()
    {
        return ( int ) ( getY() + size / 2 );
    }

    @Override
    public double getFiringOriginX()
    {
        return centerX() + 25 * Math.cos( 0 - angle );
    }

    @Override
    public double getFiringOriginY()
    {
        return centerY() - 25 * Math.sin( 0 - angle );
    }

    /**
     * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
     * 
     * @return  thread-safe queue containg our <code>MissileManager</code>
     * @since January 6, 2008
     */
    public ConcurrentLinkedQueue<Weapon> getManagers()
    {
        ConcurrentLinkedQueue<Weapon> c = new ConcurrentLinkedQueue<Weapon>();
        c.add( manager );
        return c;
    }

    /**
     * Shows the easter egg for a short while.
     * @since January 9, 2008
     */
    public void setEasterEgg()
    {
        if ( easterEggCounter <= 0 )
            easterEggCounter = 290;
    }

    public void destroy()
    {
        Game.getInstance().getObjectManager().removeObject( this );

        ParticleManager.createSmoke( getX() + Util.getGameplayRandomGenerator().nextInt( size ) / 2, centerY() + Util.getGameplayRandomGenerator().nextInt( size ) / 2, 100 );
        ParticleManager.createFlames( getX() + Util.getGameplayRandomGenerator().nextInt( size ) / 2, centerY() + Util.getGameplayRandomGenerator().nextInt( size ) / 2, 250 );

        if ( Util.getGameplayRandomGenerator().nextInt( 4 ) == 0 )
            Game.getInstance().createBonus( this );

        Sound.playInternal( SoundLibrary.STATION_DIE );
    }

    /**
     * Calculates and returns the necessary angle to hit the target
     * 
     * @param target The <code>Ship</code> to shoot at
     * @since January 15, 2008
     */
    private void calculateAngle( Ship target )
    {
        double distance = Util.getDistance( this, target );
        // TODO: Sync
        double time = Math.log( distance ) * ( 5 + Util.getGameplayRandomGenerator().nextInt( 2 ) );
        double projectedX = target.getX() + time * target.getDx();
        double projectedY = target.getY() + time * target.getDy();

        desiredAngle = Math.atan( ( projectedY - centerY() ) / ( projectedX - centerX() ) );
        if ( projectedX - ( centerX() ) < 0 )
            desiredAngle += Math.PI;


        if ( ( ( desiredAngle - angle ) + 2 * Math.PI ) % ( 2 * Math.PI ) < Math.PI )
        {
            if ( Math.abs( desiredAngle - angle ) < SWEEP_SPEED )
                angle = desiredAngle;
            else
                angle += SWEEP_SPEED;
        }
        else //if it shouldn't move counterclockwise, moveclockwise
        {
            if ( Math.abs( angle - desiredAngle ) < SWEEP_SPEED )
                angle = desiredAngle;
            else
                angle -= SWEEP_SPEED;
        }
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
        stream.writeDouble( angle );
        stream.writeDouble( desiredAngle );
        stream.writeInt( health );
        stream.writeInt( easterEggCounter );
        stream.writeInt( size );
        manager.flatten( stream );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since April 11, 2008
     */
    public Station( DataInputStream stream ) throws IOException
    {
        super( stream );
        angle = stream.readDouble();
        desiredAngle = stream.readDouble();
        health = stream.readInt();
        easterEggCounter = stream.readInt();
        size = stream.readInt();
        manager = new MissileManager( stream );
    }
}
