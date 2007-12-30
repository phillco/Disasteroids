
/**
 * DISASTEROIDS
 * Ship.java
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The player ships.
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic
 */
public class Ship implements GameElement
{

    public final static double SENSITIVITY = 30;

    public final static int RADIUS = 10;

    /**
     * Our location on the absolute game world.
     * @since Classic
     */
    private double x,  y;

    /**
     * Our speed (delta).
     * @since Classic
     */
    private double dx,  dy;

    /**
     * Togglers for various actions.
     * @since Classic (last minute)
     */
    private boolean forward = false,  backwards = false,  left = false,  right = false,  shooting = false;

    /**
     * The angle we're facing.
     * @since Classic
     */
    private double angle;

    /**
     * Our name, showed on the scoreboard.
     * @since December 14, 2007
     */
    private String name;

    /**
     * Time left until we can shoot.
     * @since Classic
     */
    private int timeTillNextShot;

    /**
     * Our color. The ship, missiles, <code>StarMissile</code>s, and explosions are drawn in this.
     * @see Game#PLAYER_COLORS
     * @since Classic
     */
    private Color myColor;

    /**
     * A darker version of <code>myColor</code> shown when invincible.
     * @since Classic
     */
    private Color myInvicibleColor;

    /**
     * Time left until we become vincible.
     * @since Classic
     */
    private int invincibilityCount;

    /**
     * How many reserve lives remain. When less than 0, the game is over.
     * @since Classic
     */
    private int livesLeft;

    /**
     * Toggler for the invincibility flashing during drawing.
     * @since Classic
     */
    private boolean invulFlash;

    /**
     * Our score.
     * @since Classic
     */
    private int score;

    /**
     * How many <code>Asteroid</code>s we've killed on this level.
     * @since December 16, 2007
     */
    private int numAsteroidsKilled;

    /**
     * How many <code>Ship</code>s we've killed on this level.
     * @since December 16, 2007
     */
    private int numShipsKilled;

    /**
     * The current weapon selected in <code>allWeapons</code>.
     * @since December 25, 2007
     */
    private int weaponIndex;

    /**
     * Our cache of weapons.
     * @since December 16, 2007
     */
    private WeaponManager[] allWeapons;

    /**
     * How long to draw the name of the current weapon.
     * @since December 25, 2007
     */
    private int drawWeaponNameTimer;

    public Ship( int x, int y, Color c, int lives, String name )
    {
        this.x = x;
        this.y = y;
        this.myColor = c;
        this.livesLeft = lives;
        this.name = name;

        score = 0;
        numAsteroidsKilled = 0;
        drawWeaponNameTimer = 0;
        numShipsKilled = 0;
        timeTillNextShot = 0;
        angle = Math.PI / 2;
        dx = 0;
        dy = 0;

        // Colors.        
        double fadePct = 0.6;
        myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );

        // Init weapons.
        allWeapons = new WeaponManager[3];
        allWeapons[0] = new MissileManager();
        allWeapons[1] = new BulletManager();
        allWeapons[2] = new MineManager();
        weaponIndex = 0;

        // Start invincible.
        invulFlash = true;
        invincibilityCount = 200;
    }

    public void clearWeapons()
    {
        for ( WeaponManager wM : allWeapons )
            wM.clear();
    }

    public void restoreBonusValues()
    {
        for ( WeaponManager wM : allWeapons )
            wM.restoreBonusValues();
    }

    public WeaponManager[] allWeapons()
    {
        return allWeapons;
    }

    public void draw( Graphics g )
    {
        Color col;

        // Set our color
        if ( cannotDie() )
            col = myInvicibleColor;
        else
            col = myColor;

        int centerX = AsteroidsFrame.frame().getWidth() / 2;
        int centerY = AsteroidsFrame.frame().getHeight() / 2;

        Polygon outline = new Polygon();
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ), (int) ( centerY - RADIUS * Math.sin( angle ) ) );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );

        if ( ( cannotDie() && ( invulFlash = !invulFlash ) == true ) || !( cannotDie() ) )
        {
            AsteroidsFrame.frame().drawPolygon( g, col, Color.black, outline );
        }

        for ( WeaponManager wm : allWeapons )
            wm.draw( g );

        if ( drawWeaponNameTimer > 0 )
        {
            drawWeaponNameTimer--;
            g.setFont( g.getFont().deriveFont( Font.BOLD ) );
            Graphics2D g2d = (Graphics2D) g;
            AsteroidsFrame.frame().drawString( g, (int) x - (int) g2d.getFont().getStringBounds( getWeaponManager().getWeaponName(), g2d.getFontRenderContext() ).getWidth() / 2, (int) y - 15, getWeaponManager().getWeaponName(), Color.gray );
        }
    }

    public void forward()
    {
        forward = true;
    }

    public void backwards()
    {
        backwards = true;
    }

    public void left()
    {
        left = true;
    }

    public void right()
    {
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

    public void startShoot()
    {
        this.shooting = true;
    }

    public void stopShoot()
    {
        this.shooting = false;
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
        if ( shooting && canShoot() )
            shoot( Settings.soundOn );

        for ( WeaponManager wm : allWeapons )
            wm.act();

        if ( livesLeft < 0 )
            return;

        timeTillNextShot--;
        invincibilityCount--;
        move();
        checkBounce();
        checkCollision();
        generateParticles();
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

    public void rotateWeapons()
    {
        weaponIndex++;
        weaponIndex %= allWeapons.length;
        drawWeaponNameTimer = 50;
    }

    private void checkBounce()
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

    private void checkCollision()
    {
        for ( Ship other : Game.getInstance().players )
        {
            if ( other == this )
                continue;

            ConcurrentLinkedQueue<Weapon> enemyWeapons = other.getWeaponManager().getWeapons();

            for ( Weapon m : enemyWeapons )
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

    /**
     * Generates particles for the ships boosters, moved from act metod
     * @snice December 16, 2007
     */
    private void generateParticles()
    {
        if ( forward && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
        {
            Random rand = RandNumGen.getParticleInstance();
            for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             -15 * Math.cos( angle ) + x + rand.nextInt( 8 ) - 4,
                                             15 * Math.sin( angle ) + y + rand.nextInt( 8 ) - 4,
                                             rand.nextInt( 4 ) + 3,
                                             myColor,
                                             rand.nextDouble() * 4,
                                             angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                             30, 10 ) );
        }

        if ( backwards && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
        {
            Random rand = RandNumGen.getParticleInstance();
            for ( int i = 0; i < (int) ( Math.sqrt( dx * dx + dy * dy ) ); i++ )
                ParticleManager.addParticle( new Particle(
                                             15 * Math.cos( angle ) + x + rand.nextInt( 8 ) - 4,
                                             -15 * Math.sin( angle ) + y + rand.nextInt( 8 ) - 4,
                                             rand.nextInt( 4 ) + 3,
                                             myColor,
                                             rand.nextDouble() * 4,
                                             angle + rand.nextDouble() * .4 - .2,
                                             30, 10 ) );
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
        timeTillNextShot = getWeaponManager().getIntervalShoot();
        getWeaponManager().add( (int) x, (int) y, angle, dx, dy, myColor );

        if ( useSound )
            Sound.click();
    }

    public boolean canShoot()
    {
        return ( getWeaponManager().getNumLiving() < getWeaponManager().getMaxShots() && timeTillNextShot < 1 && invincibilityCount < 400 && livesLeft >= 0 );
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

    public WeaponManager getWeaponManager()
    {
        return allWeapons[weaponIndex];
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
     * @return  the number of <code>Asteroid</code>s <code>this</code> has killed on this level
     * @since December 16, 2007
     */
    public int getNumAsteroidsKilled()
    {
        return numAsteroidsKilled;
    }

    /**
     * Sets the number of <code>Asteroid</code>s killed on this level.
     * 
     * @param numAsteroidsKilled    the new number of <code>Asteroid</code>s <code>this</code> has killed on this level
     * @since December 16, 2007
     */
    public void setNumAsteroidsKilled( int numAsteroidsKilled )
    {
        this.numAsteroidsKilled = numAsteroidsKilled;
    }

    /**
     * Returns the statistic of <code>Ship</code>s killed this level.
     * 
     * @return  the number of <code>Ship</code>s <code>this</code> has killed on this level.
     * @since December 16, 2007
     */
    public int getNumShipsKilled()
    {
        return numShipsKilled;
    }

    /**
     * Sets the number of <code>Ship</code>s killed on this level.
     * 
     * @param numShipsKilled    the new number of <code>Ship</code>s <code>this</code> has killed on this level
     * @since December 16, 2007
     */
    public void setNumShipsKilled( int numShipsKilled )
    {
        this.numShipsKilled = numShipsKilled;
    }
}
