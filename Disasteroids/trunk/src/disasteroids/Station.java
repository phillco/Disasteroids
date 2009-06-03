/*
 * DISASTEROIDS
 * Station.java
 */
package disasteroids;

import disasteroids.weapons.*;
import disasteroids.gui.*;
import disasteroids.gui.ImageLibrary;
import disasteroids.sound.*;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A satellite that shoots missiles at passing ships.
 * @author Phillip Cohen, Andy Kooiman
 */
public class Station extends ShootingObject
{
    /**
     * The angle we're facing.
     */
    private double angle;

    /**
     * Width/height of the station.
     */
    int size = 37;

    /**
     * Timer to show the clock easter egg. If 0, the egg should be hidden.
     */
    private int easterEggCounter = 0;

    private int health = 500;

    /**
     * The angle we're turning towards.
     */
    private double desiredAngle = 0.0;

    private final double SWEEP_SPEED = 0.05;

    private final double TURRET_LENGTH = 30;

    /**
     * Creates the station at the given position and random floating speed.
     * 
     * @param x     x coordinate in game
     * @param y     y coordinate in game
     */
    public Station( double x, double y, double dx, double dy )
    {
        super( x, y, dx, dy, 1 );
        angle = 0;

        // Set up missile launcher.
        MissileManager manager = new MissileManager( this );
        manager.getBonusValue( manager.BONUS_POPPINGQUANTITY ).override( 0 );
        manager.setLife( 50 );
        manager.getBonusValue( manager.BONUS_INTERVALSHOOT ).override( 55 );
        weapons[0] = manager;
    }

    /**
     * Moves, acquires a target, and shoots.
     * 
     * @since January 6, 2008
     */
    @Override
    public void act()
    {
        super.act();
        move();
        checkCollision();

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
                if ( getActiveWeapon().canShoot() )
                {
                    getActiveWeapon().shoot( this, Color.white, 0 - angle );
                    Sound.playInternal( SoundLibrary.STATION_SHOOT );  // Play a custom sound.
                }
            }
        }
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
            for ( Weapon wm : s.getWeapons() )
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
                                ( (Ship) s ).increaseScore( 2500 );
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
     */
    @Override
    public void draw( Graphics g )
    {
        // Flash when disabled.
        if ( isDisabled() && Util.getGlobalFlash() )
            return;

        MainWindow.frame().drawImage( g, ImageLibrary.getStation(), (int) getX() + size / 2 - 1, (int) getY() + size / 2 + 1, 0.0, 1.0 );
        MainWindow.frame().drawImage( g, ImageLibrary.getStationTurret(), (int) getX() + size / 2 + 1, (int) getY() + size / 2 + 1, angle, 1.0 );

        super.draw( g );
    }

    /**
     * Returns the center x coordinate of the station.
     */
    int centerX()
    {
        return (int) ( getX() + size / 2 );
    }

    /**
     * Returns the center y coordinate of the station.
     */
    int centerY()
    {
        return (int) ( getY() + size / 2 );
    }

    @Override
    public double getFiringOriginX()
    {
        return centerX() + ( TURRET_LENGTH * 1.2 ) * Math.cos( 0 - angle );
    }

    @Override
    public double getFiringOriginY()
    {
        return centerY() - ( TURRET_LENGTH * 1.2 ) * Math.sin( 0 - angle );
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
        getActiveWeapon().flatten( stream );
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
        super( stream, 1 );
        angle = stream.readDouble();
        desiredAngle = stream.readDouble();
        health = stream.readInt();
        easterEggCounter = stream.readInt();
        size = stream.readInt();
        weapons[0] = new MissileManager( stream, this );
    }
}
