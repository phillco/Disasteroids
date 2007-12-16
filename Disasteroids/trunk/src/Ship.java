/*
 * DISASTEROIDS
 * by Phillip Cohen and Andy Kooiman
 * 
 * APCS 1, 2006 to 2007, Period 3
 * Version - 1.0 Final (exam release)
 *
 * Run Running.class to start
 */

import java.awt.Color;
import java.awt.Polygon;
import java.util.Random;
import java.util.LinkedList;

public class Ship
{
    public final static double SENSITIVITY = 30;

    public final static int RADIUS = 10;

    private double x,  y;

    private double dx,  dy;

    private boolean forward = false,  backwards = false,  left = false,  right = false;

    private double origin_x,  origin_y;

    private double angle;

    private String name;

    private int timeTillNextShot = 0;

    private Color myColor;

    private int invincibilityCount;

    private Color myInvicibleColor;

    private int livesLeft;

    private boolean invulFlash;

    private MissileManager manager;

    private int score = 0;

    private int maxShots = 10;

    private int numAsteroidsKilled = 0;

    private int numShipsKilled = 0;

    public Ship( int x, int y, Color c, int lives, String name )
    {
        this.x = x;
        this.y = y;
        this.origin_x = x;
        this.origin_y = y;
        this.name = name;

        invulFlash = true;
        angle = Math.PI / 2;
        dx = 0;
        dy = 0;
        livesLeft = lives;
        myColor = c;
        double fadePct = 0.6;
        myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );

        manager = new MissileManager();
        invincibilityCount = 200;
    }

    private void draw()
    {
        Color col;

        // Set our color
        if ( cannotDie() )
            col = myInvicibleColor;
        else
            col = myColor;

        // Flash when invunerable
        Polygon outline = new Polygon();
        outline.addPoint( (int) ( x + RADIUS * Math.cos( angle ) ), (int) ( y - RADIUS * Math.sin( angle ) ) );
        outline.addPoint( (int) ( x + RADIUS * Math.cos( angle + Math.PI * .85 ) ), (int) ( y - RADIUS * Math.sin( angle + Math.PI * .85 ) ) );
        outline.addPoint( (int) ( x + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( y - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );
        if ( ( cannotDie() && ( invulFlash = !invulFlash ) == true ) || !( cannotDie() ) )
        {
            Running.environment().drawPolygon( col, Color.black, outline );
        }
    }

    public void forward()
    {
        //	dx+=Math.cos(angle)/20;
        //	dy-=Math.sin(angle)/20;
        forward = true;
    }

    public void backwards()
    {
        //	dx-=Math.cos(angle)/20;
        //	dy+=Math.sin(angle)/20;
        backwards = true;
    }

    public void left()
    {//angle+=Math.PI/20;
        left = true;
    }

    public void right()
    {//angle-=Math.PI/20;
        right = true;
    }

    public void unforward()
    {
        forward = false;
    }

    public void unbackwards()
    {
        backwards = false;
    }

    public void unleft()
    {
        left = false;
    }

    public void unright()
    {
        right = false;
    }

    public void act()
    {
        if ( forward )
        {
            dx += Math.cos( angle ) / SENSITIVITY * 2;
            dy -= Math.sin( angle ) / SENSITIVITY * 2;
        }
        if ( backwards )
        {
            dx -= Math.cos( angle ) / SENSITIVITY * 2;
            dy += Math.sin( angle ) / SENSITIVITY * 2;
        }
        if ( left )
            angle += Math.PI / SENSITIVITY / 2;
        if ( right )
            angle -= Math.PI / SENSITIVITY / 2;

        manager.act();
        if ( livesLeft < 0 )
            return;

        timeTillNextShot--;
        invincibilityCount--;
        move();
        checkBounce();
        checkCollision();
        draw();
        if ( forward && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
        {
            Random rand = RandNumGen.getParticleInstance();
            for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             x + rand.nextInt( 8 ) - 4,
                                             y + rand.nextInt( 8 ) - 4,
                                             rand.nextInt( 4 ) + 3,
                                             myColor,
                                             rand.nextDouble() * 3,
                                             angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                             30, 10 ) );
        }

        if ( backwards && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
        {
            Random rand = RandNumGen.getParticleInstance();
            for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             x + rand.nextInt( 16 ) - 8,
                                             y + rand.nextInt( 16 ) - 8,
                                             rand.nextInt( 4 ) + 3,
                                             myColor,
                                             rand.nextDouble() * 3,
                                             angle + rand.nextDouble() * .4 - .2,
                                             30, 10 ) );
        }
    }

    public void fullRight()
    {
        angle = 0;
    }

    public void fullLeft()
    {
        angle = Math.PI;
    }

    public void fullUp()
    {
        angle = Math.PI / 2;
    }

    public void fullDown()
    {
        angle = Math.PI * 3 / 2;
    }

    public void allStop()
    {
        dx = dy = 0;
    }

    private void checkBounce()
    {
        // Wrap to stay inside the level.
        if ( x < 0 )
            x += Running.environment().getWidth() - 1;
        if ( y < 0 )
            y += Running.environment().getHeight() - 1;
        if ( x > Running.environment().getWidth() )
            x -= Running.environment().getWidth() - 1;
        if ( y > Running.environment().getHeight() )
            y -= Running.environment().getHeight() - 1;
    }

    private void checkCollision()
    {
        for ( Ship other : AsteroidsFrame.players )
        {
            if ( other == this )
                continue;

            LinkedList<Missile> enemyMissiles = other.getMissileManager().getMissiles();

            for ( Missile m : enemyMissiles )
            {
                if ( Math.pow( x - m.getX(), 2 ) + Math.pow( y - m.getY(), 2 ) < 400 )
                    if ( looseLife() )
                    {
                        m.explode();
                        score -= 5000;
                        other.score += 5000;
                        other.numShipsKilled++;
                        other.livesLeft++;
                    }
            }
        }
    }

    private void move()
    {
        x += dx;
        y += dy;

        dx *= .996;
        dy *= .996;
    }

    public int getX()
    {
        return (int) x;
    }

    public int getY()
    {
        return (int) y;
    }

    public void shoot( boolean useSound )
    {
        if ( livesLeft < 0 )
            return;
        timeTillNextShot = manager.getIntervalShoot();
        manager.addMissile( (int) x, (int) y, angle, dx, dy, myColor );

        if ( useSound )
            Sound.click();
    }

    public void setMaxShots( int newMax )
    {
        maxShots = newMax;
    }

    public boolean canShoot()
    {
        return ( manager.getNumLivingMissiles() < maxShots && timeTillNextShot < 1 && invincibilityCount < 400 && livesLeft >= 0 );
    }

    public Color getColor()
    {
        return myColor;
    }//if you can't figure out what this does you're retarded
    public void setInvincibilityCount( int num )
    {
        invincibilityCount = num;
    }

    public boolean looseLife()
    {
        if ( cannotDie() )
            return false;//died too soon, second chance
        timeTillNextShot = 0;
        //	berserk();
        livesLeft--;
        setInvincibilityCount( 300 );
        if ( Settings.soundOn )
            Sound.bleargh();
        // Disabled, very sensitiuve to lag --> desync
//		x = origin_x;
//		y = origin_y;
//		dx = 0.0;
//		dy = 0.0;
//		angle=Math.PI/2;
        dx *= -.3;
        dy *= -.3;
        Random rand = RandNumGen.getParticleInstance();
        for ( int i = 0; i < 80; i++ )
            ParticleManager.addParticle( new Particle(
                                         x + rand.nextInt( 16 ) - 8 - RADIUS,
                                         y + rand.nextInt( 16 ) - 8 - RADIUS,
                                         rand.nextInt( 4 ) + 3,
                                         myColor,
                                         rand.nextDouble() * 6,
                                         rand.nextDouble() * 2 * Math.PI,
                                         30, 10 ) );
        return true;
    }

    public boolean cannotDie()
    {
        return invincibilityCount > 0;
    }

    public MissileManager getMissileManager()
    {
        return manager;
    }

    public void increaseScore( int points )
    {
        score += points;
    }

    public int getScore()
    {
        return score;
    }

    public void addLife()
    {
        if ( livesLeft >= 0 )
            livesLeft++;
    }

    public int score()
    {
        return score;
    }

    public int livesLeft()
    {
        return livesLeft;
    }

    public void berserk()
    {

        if ( !canShoot() )
            return;
        double angleBefore = angle;
        if ( Settings.soundOn )
            Sound.kablooie();
        for ( double ang = 0; ang <= 2 * Math.PI; ang += Math.PI / 10 )
        {
            shoot( false );
            angle = angleBefore + ang;
        }
        angle = angleBefore + .1;
        timeTillNextShot = 100;
    }

    /**
     * Returns <code>this</code> player's in-game name.
     * Names are assigned by <code>AsteroidsFrame</code>; "Player 1", "Player2", and so on.
     * 
     * @return the player's name
     * @since December 15, 2007
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the statistic of <code>Asteroid</code>s killed on this level.
     * An <code>Asteroid</code> is "killed" when it splits, so both bullets and collisions will affect this counter.
     * 
     * @return The number of <code>Asteroid</code>s <code>this</code> has killed on this level.
     * @since December 16, 2007
     */
    public int getNumAsteroidsKilled()
    {
        return numAsteroidsKilled;
    }

    /**
     * Sets the number of <code>Asteroid</code>s killed on this level.
     * 
     * @param numAsteroidsKilled The new number of <code>Asteroid</code>s <code>this</code> has killed on this level.
     * @since December 16, 2007
     */
    public void setNumAsteroidsKilled( int numAsteroidsKilled )
    {
        this.numAsteroidsKilled = numAsteroidsKilled;
    }

    /**
     * Returns the statistic of <code>Ship</code>s killed this level.
     * 
     * @return The number of <code>Ship</code>s <code>this</code> has killed on this level.
     * @since December 16, 2007
     */
    public int getNumShipsKilled()
    {
        return numShipsKilled;
    }

    /**
     * Sets the number of <code>Ship</code>s killed on this level.
     * @param numShipsKilled The new number of <code>Ship</code>s <code>this</code> has killed on this level.
     * @since December 16, 2007
     */
    public void setNumShipsKilled( int numShipsKilled )
    {
        this.numShipsKilled = numShipsKilled;
    }
}
