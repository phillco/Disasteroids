/*
 * DISASTEROIDS
 * Asteroid.java
 */

import java.awt.Color;
import java.util.ListIterator;
import java.util.Random;

/**
 * A game object which the players destroy to score.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Asteroid
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
    protected Color fill=Color.white,outline=Color.gray;

    /**
     * Constructs a new Asteroid from scratch.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param dx The x velocity.
     * @param dy The y velocity (up is negative).
     * @param size The diameter.
     * @param environment The <code>AsteroidManager</code> responsible for <code>this</code>.
     * @author Andy Kooiman, Phillip Cohen
     * @since Classic
     */
    public Asteroid( int x, int y, double dx, double dy, int size, AsteroidManager environment )
    {
        Random rand = RandNumGen.getAsteroidInstance();
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.radius = size/2;

        // Enforce a minimum size.
        if ( size < 25 )
            size = 25 + rand.nextInt( 25 );
        this.environment = environment;

        // Attack a random player.
        this.victim = AsteroidsFrame.players[rand.nextInt( AsteroidsFrame.players.length )];

        // Enforce a mininum speed.
        checkMovement();
    }

    /**
     * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
     * This is used when a missile splits an <code>Asteroid</code>.
     * @param parent The parent <code>Asteroid</code> to split from.
     * @author Andy Kooiman, Phillip Cohen
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
    protected void draw()
    {
        Running.environment().drawOutlinedCircle(fill, outline, (int)x, (int)y, radius);
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
        draw();
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
            x += AsteroidsFrame.GAME_WIDTH - 1;
        if ( y < 0 )
            y += AsteroidsFrame.GAME_HEIGHT - 1;
        if ( x > AsteroidsFrame.GAME_WIDTH )
            x -= AsteroidsFrame.GAME_WIDTH - 1;
        if ( y > AsteroidsFrame.GAME_HEIGHT )
            y -= AsteroidsFrame.GAME_HEIGHT - 1;
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
        killer.increaseScore( radius*2 );
        killer.setNumAsteroidsKilled(killer.getNumAsteroidsKilled() + 1);
        Running.environment().writeOnBackground( "+" + String.valueOf( radius*2 ), (int) x, (int) y, killer.getColor().darker() );
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
        // Don't check if already dead
        if ( shouldRemove )
            return;

        // Go through all of the ships.
        for ( Ship s : AsteroidsFrame.players )
        {
            ListIterator<Weapon> iter = s.getWeaponManager().getWeapons().listIterator();

            // Were we hit by the ship?
            if ( s.livesLeft() >= 0 )
            {
                if ( Math.pow( x - s.getX(), 2 ) + ( Math.pow( y - s.getY(), 2 ) ) < (radius + Ship.RADIUS )*(radius+Ship.RADIUS))
                {
                    if ( s.looseLife() )
                    {
                        split( s );
                        return;
                    }
                }
            }

            // Loop through all this ship's Missiles.
            while ( iter.hasNext() )
            {
                Weapon m = iter.next();

                // Were we hit by a missile?
                if ( Math.pow( x - m.getX(), 2 ) + Math.pow( y -m.getY(), 2 ) < Math.pow( radius + m.getRadius(), 2 ) )
                {
                    Sound.bloomph();
                    m.explode();
                    split( s );
                    return;
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
}
