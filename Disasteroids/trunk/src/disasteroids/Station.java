/*
 * DISASTEROIDS
 * Station.java
 */
package disasteroids;

import disasteroids.gui.Particle;
import disasteroids.gui.RelativeGraphics;
import disasteroids.sound.Sound;
import disasteroids.gui.ParticleManager;
import disasteroids.sound.SoundLibrary;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A satellite that shoots missiles at passing ships.
 * @author Phillip Cohen, Andy Kooiman
 * @since January 6, 2008
 */
public class Station extends GameObject implements ShootingObject
{
    /**
     * The angle we're facing.
     * @since January 6, 2008
     */
    private double angle;

    /**
     * Our firing manager.
     * @since January 6, 2008
     */
    private MissileManager manager;

    /**
     * Width/height of the station.
     * @since January 6, 2008
     */
    int size = 35;

    /**
     * Timer to show the clock easter egg. If 0, the egg should be hidden.
     * @since January 9, 2008 
     */
    private int easterEggCounter = 0;

    /**
     * If above 0, we've been disabled by a Ship and can't fire.
     * @since January 16, 2008
     */
    private int disableCounter = 0;

    /**
     * Whether we should be hidden in the next tick. Used for our flashing when disabled.
     * @since January 16, 2008
     */
    private boolean disableFlash = false;

    /**
     * The angle we're turning towards.
     * @since January 16, 2008
     */
    private double desiredAngle = 0.0;

    final double SWEEP_SPEED = 0.05;

    final int HITS_TO_KILL = 3;

    /**
     * How many missile hits we've taken. Reset after the disableCounter runs out.
     * @since March 9, 2008
     */
    private int hitsWhileDisabled = 0;

    /**
     * Creates the station at the given position and random floating speed.
     * 
     * @param x     x coordinate in game
     * @param y     y coordinate in game
     * @since January 6, 2008
     */
    public Station( double x, double y, double dx, double dy )
    {
        setLocation( x, y );
        setDx( dx );
        setDy( dy );
        angle = 0;
        manager = new MissileManager();
        manager.setPopQuantity( 0 );
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
        manager.act( true );

        angle %= 2 * Math.PI; //Make sure the angle does not grow without bound

        // Easter egg.
        if ( easterEggCounter > 0 )
        {
            easterEggCounter--;
            return;
        }

        // Disabled.
        if ( disableCounter > 0 )
        {
            disableCounter--;

            // Smoke and spin the turret.
            Random r = RandomGenerator.get();
            angle += 0.07 + r.nextDouble() / 7;
            ParticleManager.addParticle( new Particle( getX() + r.nextInt( size ), centerY(),
                                                       r.nextInt( 5 ) + 2, r.nextBoolean() ? Color.gray : Color.darkGray,
                                                       r.nextInt( 3 ) + 1, r.nextDouble() * 1.4 + 0.5,
                                                       50, 30 ) );

            for ( int i = 0; i < hitsWhileDisabled; i++ )
                ParticleManager.addParticle( new Particle( getX() + r.nextInt( size ), centerY(),
                                                           r.nextInt( 5 ) + 2, r.nextBoolean() ? Color.red : Color.orange,
                                                           r.nextInt( 3 ) + 1, r.nextDouble() * 1.4 + 0.5,
                                                           50, 30 ) );


            if ( disableCounter == 0 )
                hitsWhileDisabled = 0;
            return;
        }

        // Find players within our range.        
        int range = 300;
        Ship closestShip = null;
        {
            Ship closestInvincible = null;
            for ( Ship s : Game.getInstance().players )
            {
                if ( getProximity( s ) < range )
                {
                    if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                        closestShip = s;
                    if ( closestInvincible == null || getProximity( s ) > getProximity( closestInvincible ) )
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
                if ( manager.canShoot() && manager.add( (int) ( centerX() + 25 * Math.cos( 0 - angle ) ), (int) ( centerY() - 25 * Math.sin( 0 - angle ) ), 0 - angle, 0, 0, Color.white, false ) )
                    Sound.playInternal( SoundLibrary.STATION_SHOOT );  // Play a custom sound.
            }
        }
        else
            angle += 0.01;
    }

    /**
     * Checks if players run into us and takes action.
     * 
     * @since January 9, 2007
     */
    private void checkCollision()
    {
        // Check for missile collision.
        for ( ShootingObject s : Game.getInstance().shootingObjects )
        {
            if ( s == this )
                continue;

            // Loop through the mangers.
            for ( WeaponManager wm : s.getManagers() )
            {
                // Loop through the bullets.
                for ( WeaponManager.Unit m : wm.getWeapons() )
                {
                    // Were we hit by a bullet?
                    if ( ( m.getX() + m.getRadius() > getX() && m.getX() - m.getRadius() < getX() + size ) &&
                            ( m.getY() + m.getRadius() > getY() && m.getY() - m.getRadius() < getY() + size ) )
                    {
                        if ( m instanceof Missile && disableCounter > 0 && !( (Missile) m ).isExploding() )
                        {
                            hitsWhileDisabled++;
                            if ( hitsWhileDisabled > 3 )
                            {
                                destroy();
                                return;
                            }       
                        }
                        
                        m.explode();
                        disable();
                    }
                }
            }
        }

        if ( disableCounter > 0 )
            return;

        // Check for ship collision.  
        for ( Ship s : Game.getInstance().players )
        {
            // Were we hit by the ship's body?
            if ( s.livesLeft() >= 0 )
            {
                if ( ( s.getX() + Ship.RADIUS > getX() && s.getX() - Ship.RADIUS < getX() + size ) &&
                        ( s.getY() + Ship.RADIUS > getY() && s.getY() - Ship.RADIUS < getY() + size ) )
                {
                    if ( s.looseLife( s.getName() + " learns to steer." ) )
                        return;
                }
            }
        }
    }

    /**
     * Returns the distance to a given ship using pythagoras.
     * 
     * @param s     the ship
     * @return      the distance to it
     * @since January 6, 2008
     */
    private double getProximity( Ship s )
    {
        return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
    }

    /**
     * Draws this and our bullets to the given context. Uses RelativeGraphics.
     * 
     * @param g the context
     * @since January 6, 2008
     */
    public void draw( Graphics g )
    {
        if ( disableCounter > 0 )
        {
            disableFlash = !disableFlash;
            if ( disableFlash )
                return;
        }

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
            int eX = (int) ( cX + 5 * Math.cos( hourAngle ) );
            int eY = (int) ( cY + 5 * Math.sin( hourAngle ) );
            g.drawLine( cX, cY, eX, eY );
            g.drawLine( cX, cY + 1, eX, eY + 1 );
            g.drawLine( cX + 1, cY, eX + 1, eY );

            // Minute hand.
            double minuteAngle = ( ( Calendar.getInstance().get( Calendar.MINUTE ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
            eX = (int) ( cX + 10 * Math.cos( minuteAngle ) );
            eY = (int) ( cY + 10 * Math.sin( minuteAngle ) );
            g.drawLine( cX, cY, eX, eY );
            g.drawLine( cX, cY + 1, eX, eY + 1 );

            // Second hand.
            double secondAngle = ( ( Calendar.getInstance().get( Calendar.SECOND ) / 60.0 ) * 2 - 0.5 ) * Math.PI;
            eX = (int) ( cX + 15 * Math.cos( secondAngle ) );
            eY = (int) ( cY + 15 * Math.sin( secondAngle ) );
            g.drawLine( cX, cY, eX, eY );
        }
        else
        {
            // Draw the turret.
            g.setColor( Color.white );
            int eX = (int) ( cX + 15 * Math.cos( angle ) );
            int eY = (int) ( cY + 15 * Math.sin( angle ) );
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
        return (int) ( getX() + size / 2 );
    }

    /**
     * Returns the center position of the station.
     * 
     * @return      the y coordinate of the center
     * @since January 6, 2008
     */
    int centerY()
    {
        return (int) ( getY() + size / 2 );
    }

    /**
     * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
     * 
     * @return  thread-safe queue containg our <code>MissileManager</code>
     * @since January 6, 2008
     */
    public ConcurrentLinkedQueue<WeaponManager> getManagers()
    {
        ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
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

    public void disable()
    {
        if ( disableCounter <= 0 )
            disableCounter = 290;

        for ( WeaponManager.Unit w : manager.getWeapons() )
            w.explode();

        Sound.playInternal( SoundLibrary.STATION_DISABLED );
    }

    public void destroy()
    {
        Game.getInstance().removeObject( this );
        Random r = RandomGenerator.get();
        for ( int i = 0; i < 120; i++ )
        {
            ParticleManager.addParticle( new Particle( getX() + r.nextInt( size ), centerY(),
                                                       r.nextInt( 5 ) + 2, r.nextBoolean() ? Color.red : Color.orange,
                                                       RandomGenerator.get().nextDouble() * 6,
                                                       RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                                       30, 10 ) );
        }

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
        double distance = getProximity( target );
        double time = Math.log( distance ) * ( 5 + RandomGenerator.get().nextInt( 2 ) );
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
}
