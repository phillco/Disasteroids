/*
 * DISASTEROIDS
 * Asteroid.java
 */

import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * A game object which the players destroy to score.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Asteroid implements GameElement, Serializable
{

    /**
     * The <code>AsteroidManager</code> to which this <code>Asteroid</code> belongs.
     * @author Andy Kooiman
     * @since Classic
     */
    protected AsteroidManager environment;

    /**
     * The x and y coordinates of this <code>Asteroid</code>.
     * @author Andy Kooiman
     * @since Classic
     */
    protected double x,  y;

    /**
     * The x and y components of this <code>Asteroid</code>'s velocity.
     * @author Andy Kooiman.
     * @since Classic
     */
    protected double dx,  dy;

    /**
     * The number of child <code>Asteroid</code>s this <code>Asteroid</code> has.
     * @since Classic
     */
    protected int children;

    /**
     * The diameter of this <code>Asteroid</code>.
     * @since Classic
     */
    protected int radius;

    /**
     * Stores whether this <code>Asteroid</code> should be removed.
     * @since Classic
     */
    protected boolean shouldRemove = false;

    /**
     * Which player we accelerate towards.
     * @since December 14 2007
     */
    protected Ship victim = null;

    /**
     * The <code>Color</code>s of the inside and outline of <code>this</code>
     * @since December 15 2007
     */
    protected Color fill = Color.white,  outline = Color.gray;

    /**
     * The starting and current life of this <code>Asteroid</code>
     * @since December 21, 2007
     */
    protected int lifeMax,  life;

    /**
     * Constructs a new Asteroid from scratch.
     * 
     * @param x				the x coordinate
     * @param y				the y coordinate
     * @param dx			the x velocity
     * @param dy			the y velocity (up is negative)
     * @param size			the diameter
     * @param environment	the <code>AsteroidManager</code> responsible for <code>this</code>
     * @since Classic
     */
    public Asteroid( int x, int y, double dx, double dy, int size, int lifeMax, AsteroidManager environment )
    {
        Random rand = RandNumGen.getAsteroidInstance();
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.radius = size / 2;
        this.life = this.lifeMax = Math.max( 1, lifeMax );

        // Enforce a minimum size.
        if ( size < 25 )
            size = 25 + rand.nextInt( 25 );
        this.environment = environment;

        // Attack a random player.
        if ( Game.getInstance().players.size() > 0 )
            this.victim = Game.getInstance().players.get( rand.nextInt( Game.getInstance().players.size() ) );

        // Enforce a mininum speed.
        checkMovement();
    }

    /**
     * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
     * This is used when a missile splits an <code>Asteroid</code>.
     * 
     * @param parent	the parent <code>Asteroid</code> to split from
     * @since Classic
     */
    public Asteroid( Asteroid parent )
    {
        parent.children++;
        if ( parent.children > 2 )
            this.radius = 5;
        else
            this.radius = parent.radius / 2;
        Random rand = RandNumGen.getAsteroidInstance();
        this.x = parent.x;
        this.y = parent.y;
        this.dx = rand.nextDouble() * 2 - 1;
        this.dy = rand.nextDouble() * 2 - 1;
        this.environment = parent.environment;
        this.life = this.lifeMax = parent.lifeMax / 2 + 1;//live half as long as parents

        // Attack the same player that the parent did. (Would random be better?)
        this.victim = parent.victim;

        // Enforce a mininum speed.
        checkMovement();
    }

    /**
     * Draws <code>this</code> in the <code>AsteroidsFrame</code> context.
     * @author Andy Kooiman
     * @since Classic
     */
    public void draw( Graphics g )
    {

        Color f = new Color( fill.getRed() * life / lifeMax, fill.getGreen() * life / lifeMax, fill.getBlue() * life / lifeMax );
        AsteroidsFrame.frame().drawOutlinedCircle( g, f, outline, (int) x, (int) y, radius );
    }

    /**
     * Steps <code>this</code> through one timestep, then draws it.
     * @author Andy Kooiman
     * @since Classic
     */
    public void act()
    {
        if ( children > 1 || radius == 5 )
            shouldRemove = true;
        move();
        checkCollision();
    }

    /**
     * Moves <code>this</code> for one timestep, and gravitates towards players.
     * @author Andy Kooiman, Phillip Cohen
     * @since Classic
     */
    private void move()
    {
        // Accelerate towards our hapless victim. <g>
        if ( victim != null )
        {
//            dx += ( -x + victim.getX() ) * (double) ( AsteroidsFrame.getLevel() ) / 500000.0;
        //          dy += ( -y + victim.getY() ) * (double) ( AsteroidsFrame.getLevel() ) / 500000.0;
        }

        x += dx;
        y += dy;
        checkWrap();
    }

    /**
     * Makes sure that the ship remains on screen, and wraps around if necessary.
     * @author Andy Kooiman
     * @since Classic
     */
    private void checkWrap()
    {
        // Wrap to stay inside the level.
        if ( x < 0 )
            x += Game.getInstance().GAME_WIDTH - 1;
        if ( y < 0 )
            y += Game.getInstance().GAME_HEIGHT - 1;
        if ( x > Game.getInstance().GAME_WIDTH )
            x -= Game.getInstance().GAME_WIDTH - 1;
        if ( y > Game.getInstance().GAME_HEIGHT )
            y -= Game.getInstance().GAME_HEIGHT - 1;
    }

    /**
     * Called when the <code>Asteroid</code> is killed, as an indication to split into two new <code>Asteroid</code>s.
     * @param killer The <code>Ship</code> which killed <code>this</code>.
     * @author Andy Kooiman
     * @since Classic
     */
    protected void split( Ship killer )
    {
        if ( children > 2 )
        {
            shouldRemove = true;
            return;
        }
        killer.increaseScore( radius * 2 );
        killer.setNumAsteroidsKilled( killer.getNumAsteroidsKilled() + 1 );
        AsteroidsFrame.frame().writeOnBackground( "+" + String.valueOf( radius * 2 ), (int) x, (int) y, killer.getColor().darker() );
        if ( radius < 12 )
            shouldRemove = true;
        else
        {
            environment.add( new Asteroid( this ) );
            environment.add( new Asteroid( this ) );
            shouldRemove = true;
        }
    }

    /**
     * Checks, and acts, if we were hit by a missile or ship.
     * @author Andy Kooiman, Phillip Cohen
     * @since Classic
     */
    private void checkCollision()
    {
        // Don't check if already dead.
        if ( shouldRemove )
            return;

        // Go through all of the ships.        
        for ( Ship s : Game.getInstance().players )
        {
            // Were we hit by the ship?
            if ( s.livesLeft() >= 0 )
            {
                if ( Math.pow( x - s.getX(), 2 ) + ( Math.pow( y - s.getY(), 2 ) ) < ( radius + Ship.RADIUS ) * ( radius + Ship.RADIUS ) )
                {
                    if ( s.looseLife() )
                    {
                        split( s );
                        return;
                    }
                }
            }

            for ( WeaponManager wm : s.allWeapons() )
            {
                // Loop through all this ship's Missiles.
                for ( Weapon m : wm.getWeapons() )
                {

                    // Were we hit by a missile?
                    if ( Math.pow( x - m.getX(), 2 ) + Math.pow( y - m.getY(), 2 ) < Math.pow( radius + m.getRadius(), 2 ) )
                    {
                        Sound.bloomph();
                        m.explode();
                        life = Math.max( 0, life - m.getDamage() );
                        if ( life <= 0 )
                        {
                            split( s );
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes sure that we're moving fast enough.
     * @author Andy Kooiman
     * @since Classic
     */
    private void checkMovement()
    {
        if ( Math.abs( dx ) < .5 )
        {
            if ( dx < 0 )
                dx -= 1;
            else
                dx += 1;
        }
        if ( Math.abs( dy ) < .5 )
        {
            if ( dy < 0 )
                dy -= 1;
            else
                dy += 1;
        }
    }

    /**
     * Checks to see if <code>this</code> thinks it should be removed
     * @return If <code>this</code> should be removed
     * @author Andy Kooiman
     * @since Classic
     */
    public boolean shouldRemove()
    {
        return shouldRemove;
    }
    
    @Override
    public String toString()
    {
        return "[Asteroid@ (" + x + "," + y +  "), radius " + radius + "]";
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param d the stream to write to
     * @since December 29, 2007
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeDouble( x );
        stream.writeDouble( y );
        stream.writeDouble( dx );
        stream.writeDouble( dy );
        stream.writeInt( radius );
        stream.writeInt( life );
        stream.writeInt( lifeMax );
        stream.writeInt( children );
        
        // TODO: Add victim.
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @since December 29, 2007
     */
    public Asteroid( DataInputStream stream ) throws IOException
    {
        x = stream.readDouble();
        y = stream.readDouble();
        dx = stream.readDouble();
        dy = stream.readDouble();
        radius = stream.readInt();
        life = stream.readInt();
        lifeMax = stream.readInt();
        children = stream.readInt();
        
        // TODO: Add victim
        environment = Game.getInstance().asteroidManager;
    }
}
