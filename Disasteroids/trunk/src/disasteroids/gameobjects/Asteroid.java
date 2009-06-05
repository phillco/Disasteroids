/*
 * DISASTEROIDS
 * Asteroid.java
 */
package disasteroids.gameobjects;

import disasteroids.game.Game;
import disasteroids.game.GameElement;
import disasteroids.*;
import disasteroids.weapons.Weapon;
import disasteroids.gui.MainWindow;
import disasteroids.gui.ImageLibrary;
import disasteroids.gui.Local;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import disasteroids.weapons.Unit;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A game object which the players remove to score.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Asteroid extends GameObject implements GameElement
{
    /**
     * Our radius.
     */
    protected int radius;

    /**
     * Our life and its scale. Life is deducted when we're shot.
     */
    protected int lifeMax,  life;

    /**
     * The angle that the image is drawn at. Does not affect gameplay.
     */
    protected double angle = 0;

    public Asteroid( double x, double y, double dx, double dy, int diameter, int lifeMax )
    {
        super( x, y, dx, dy );
        // TODO: Sync, objectmanager
        this.radius = diameter / 2;
        this.life = this.lifeMax = Math.max( 1, lifeMax );

        // Enforce a minimum size.
        if ( diameter < 25 )
            diameter = 25 + Util.getGameplayRandomGenerator().nextInt( 25 );

        // Enforce a mininum speed.
        checkMovement();
    }

    /**
     * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
     * This is used when a missile splits an <code>Asteroid</code>.
     * 
     * @param parent	the parent <code>Asteroid</code> to kill from
     */
    public Asteroid( Asteroid parent )
    {
        super( parent.getX(), parent.getY(), Util.getGameplayRandomGenerator().nextDouble() * 2 - 1, Util.getGameplayRandomGenerator().nextDouble() * 2 - 1 );
        this.radius = parent.radius * 3 / 5;

        // Live half as long as parents.
        this.life = this.lifeMax = parent.lifeMax / 2 + 1;

        // Enforce a mininum speed.
        checkMovement();
    }

    public void draw( Graphics g )
    {
        MainWindow.frame().drawImage( g, ImageLibrary.getAsteroid(), (int) getX(), (int) getY(), angle, radius * 2.0 / ImageLibrary.getAsteroid().getWidth( null ) );
    }

    public void act()
    {
        move();
        checkCollision();
        if ( !Game.getInstance().isPaused() )
            angle += radius % 2 == 0 ? .05 : -.05;
    }

    /**
     * Splits and removes the asteroid.
     */
    public void split( Ship killer )
    {
        if ( killer != null )
        {
            killer.increaseScore( radius * 2 );
            killer.setNumAsteroidsKilled( killer.getNumAsteroidsKilled() + 1 );

            // Write the score on the background.
            if ( !Local.isStuffNull() )
                Local.getStarBackground().writeOnBackground( "+" + String.valueOf( radius * 2 ), (int) getX(), (int) getY(), killer.getColor().darker() );
        }

        if ( radius >= 45 )
        {
            Game.getInstance().getObjectManager().addObject( new Asteroid( this ) , false);
            Game.getInstance().getObjectManager().addObject( new Asteroid( this ) , false);
        }
        
        Game.getInstance().getObjectManager().removeObject( this );
    }

    /**
     * Checks, and acts, if we were hit by a missile or ship.
     * 
     * @since Classic
     */
    private void checkCollision()
    {
        // Go through all of the ships.        
        for ( Ship s : Game.getInstance().getObjectManager().getPlayers() )
        {
            // Were we hit by the ship's body?
            if ( s.livesLeft() >= 0 )
            {
                if ( Util.getDistance( this, s ) < radius + s.getRadius() )
                {
                    if ( s.damage( radius / 2.0 + 8, s.getName() + ( Math.abs( getSpeed() ) > Math.abs( s.getSpeed() ) ? " was hit by" : " slammed into" ) + " an asteroid." ) )
                    {
                        split( s );
                        return;
                    }
                }
            }
        }

        // Go through ships, stations, etc.
        for ( ShootingObject s : Game.getInstance().getObjectManager().getShootingObjects() )
        {
            for ( Weapon wm : s.getWeapons() )
            {
                // Loop through all this ship's Missiles.
                for ( Unit m : wm.getUnits() )
                {
                    // Were we hit by a missile?
                    if ( m.getDamage() > 0 && Util.getDistance( this, m ) < radius + m.getRadius() )
                    {
                        Sound.playInternal( SoundLibrary.ASTEROID_DIE );

                        m.explode();
                        life = Math.max( 0, life - m.getDamage() );
                        if ( life <= 0 )
                        {
                            split( s instanceof Ship ? (Ship) s : null );
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes sure that we're moving fast enough.
     */
    private void checkMovement()
    {
        if ( Math.abs( getDx() ) < .5 )
        {
            if ( getDx() < 0 )
            {
                setDx( getDx() - 1 );
            }
            else
            {
                setDx( getDx() - 1 );
            }
        }

        if ( Math.abs( getDy() ) < .5 )
        {
            if ( getDy() < 0 )
            {
                setDx( getDy() - 1 );
            }
            else
            {
                setDx( getDy() - 1 );
            }
        }

    }

    public int getRadius()
    {
        return radius;
    }
    

    /**
     * Generates and returns a <code>String</code> representation of <code>this</code>
     * It will have the form "[Asteroid@(#,#), radius #]".
     * @return a <code>String</code> representation of <code>this</code>
     */
    @Override
    public String toString()
    {
        return "[Asteroid@ (" + getX() + "," + getY() + "), radius " + radius + "]";
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeInt( radius );
        stream.writeInt( life );
        stream.writeInt( lifeMax );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     */
    public Asteroid( DataInputStream stream ) throws IOException
    {
        super( stream );
        radius = stream.readInt();
        life = stream.readInt();
        lifeMax = Math.max( 1, stream.readInt() );
    }
}
