/**
 * DISASTEROIDS
 * Ship.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.Local;
import disasteroids.gui.ParticleManager;
import disasteroids.gui.Particle;
import disasteroids.networking.Server;
import disasteroids.sound.Sound;
import disasteroids.sound.SoundLibrary;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A player ship.
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic
 */
public class Ship extends GameObject implements ShootingObject
{
    public int id;

    public final static double SENSITIVITY = 20;

    /**
     * How many lives players start with.
     * @since April 9, 2008
     */
    public final static int START_LIVES = 4;

    private final static int RADIUS = 10;

    /**
     * Togglers for various actions.
     * @since Classic (last minute)
     */
    private boolean forward = false,  backwards = false,  left = false,  right = false,  shooting = false,  sniping = false;

    /**
     * The angle we're facing.
     * @since Classic
     */
    private double angle = Math.PI / 2;

    /**
     * Our name, showed on the scoreboard.
     * @since December 14, 2007
     */
    private String name;

    /**
     * Our color. The ship, missiles, <code>StarMessages</code>s, and explosions are drawn in this.
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
     * Our score.
     * @since Classic
     */
    private int score = 0;

    /**
     * How many <code>Asteroid</code>s we've killed on this level.
     * @since December 16, 2007
     */
    private int numAsteroidsKilled = 0;

    /**
     * How many <code>Ship</code>s we've killed on this level.
     * @since December 16, 2007
     */
    private int numShipsKilled = 0;

    /**
     * The current weapon selected in <code>allWeapons</code>.
     * @since December 25, 2007
     */
    private int weaponIndex = 0;

    /**
     * Our cache of weapons.
     * @since December 16, 2007
     */
    private Weapon[] allWeapons = { new MissileManager(), new BulletManager(), new MineManager(), new LaserManager() };

    /**
     * The <code>SniperManager</code> for this <code>Ship</code>
     * @since March 26, 2008
     */
    private SniperManager sniperManager = new SniperManager();

    /**
     * How long to draw the name of the current weapon.
     * @since December 25, 2007
     */
    private int drawWeaponNameTimer = 0;

    /**
     * How many acceleration particles should be emitted out the front. Used for smooth effects.
     * @since March 3, 2008
     */
    private double particleRateForward = 0.0;

    /**
     * How many acceleration particles should be emitted out the back. Used for smooth effects.
     * @since March 3, 2008
     */
    private double particleRateBackward = 0.0;

    /**
     * Timer for the endgame sequence.
     * @since March 8, 2008
     */
    private int explosionTime = 0;

    /**
     * How much acceleration should occur due to strafing. Positive is to the ship's right.
     * @since March 9, 2008
     */
    private double strafeSpeed = 0;

    /**
     * Flashing for the sniping indicator.
     * @since March 12, 2008
     */
    private boolean snipeFlash = false;

    /**
     * If this <code>Ship</code> has a shield; good for one free hit.
     */
    private boolean shielded = false;

    private boolean stopping = false;

    private double healthMax = 100,  health = healthMax;

    public Ship( double x, double y, Color c, int lives, String name )
    {
        super( x, y, 0, 0 );
        this.myColor = c;
        this.livesLeft = lives;
        this.name = name;

        // Colors.        
        double fadePct = 0.6;
        myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );

        // Start invincible.
        invincibilityCount = 200;

        // Assign an unique ID.
        boolean uniqueId = false;
        while ( !uniqueId )
        {
            uniqueId = true;
            id = RandomGenerator.get().nextInt( 5432 ) + Game.getInstance().players.size() + 4;
            for ( Ship s : Game.getInstance().players )
            {
                if ( s == this )
                    continue;
                if ( s.id == this.id )
                    uniqueId = false;
            }
        }

    }

    @Override
    public String toString()
    {
        return "Ship #" + id + " ~ [" + (int) getX() + "," + (int) getY() + "]";
    }

    public void clearWeapons()
    {
        for ( Weapon wM : allWeapons )
            wM.clear();
        sniperManager.clear();
    }

    public void restoreBonusValues()
    {
        for ( Weapon wM : allWeapons )
            wM.restoreBonusValues();
        sniperManager.restoreBonusValues();
    }

    public void draw( Graphics g )
    {
        for ( Weapon wm : allWeapons )
            wm.draw( g );
        sniperManager.draw( g );

        if ( livesLeft < 0 )
            return;

        allWeapons[weaponIndex].drawTimer( g, myColor );

        // Set our color.
        Color col = ( cannotDie() ? myInvicibleColor : myColor );

        double centerX, centerY;

        if ( this == AsteroidsFrame.frame().localPlayer() )
        {
            centerX = AsteroidsFrame.frame().getWidth() / 2;
            centerY = AsteroidsFrame.frame().getHeight() / 2;
        }
        else
        {
            centerX = ( getX() - AsteroidsFrame.frame().localPlayer().getX() + AsteroidsFrame.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
            centerY = ( getY() - AsteroidsFrame.frame().localPlayer().getY() + AsteroidsFrame.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;

            if ( !( centerX > -100 && centerX < Game.getInstance().GAME_WIDTH + 100 && centerY > -100 && centerY < Game.getInstance().GAME_HEIGHT + 100 ) )
                return;
        }

        // TODO: Create RelativeGraphics.transformPolygon()
        Polygon outline = new Polygon();
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ) + AsteroidsFrame.frame().getRumbleX(), (int) ( centerY - RADIUS * Math.sin( angle ) ) + AsteroidsFrame.frame().getRumbleY() );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ) + AsteroidsFrame.frame().getRumbleX(), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) - AsteroidsFrame.frame().getRumbleY() );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );

        // Flash when invincible.
        if ( !cannotDie() || ( cannotDie() && Local.getGlobalFlash() ) )
            AsteroidsFrame.frame().drawPolygon( g, col, ( myColor.getRed() + myColor.getGreen() + myColor.getBlue() > 64 * 3 ? Color.black : Color.darkGray ), outline );
        //  AsteroidsFrame.frame().drawImage(g, ImageLibrary.getShip(Color.red), (int)x, (int)y,Math.PI/2 -angle, Ship.RADIUS/37.5);

        if ( shielded )
            AsteroidsFrame.frame().drawCircle( g, Color.CYAN, (int) getX(), (int) getY(), RADIUS + 5 );

        if ( this == AsteroidsFrame.frame().localPlayer() && drawWeaponNameTimer > 0 )
        {
            drawWeaponNameTimer--;
            g.setFont( new Font( "Century Gothic", Font.BOLD, 14 ) );
            Graphics2D g2d = (Graphics2D) g;
            AsteroidsFrame.frame().drawString( g, (int) getX() - (int) g2d.getFont().getStringBounds( getWeaponManager().getWeaponName(), g2d.getFontRenderContext() ).getWidth() / 2, (int) getY() - 15, getWeaponManager().getWeaponName(), Color.gray );
            allWeapons[weaponIndex].getWeapon( (int) getX(), (int) getY() + 25, Color.gray ).draw( g );
        }

        if ( sniping )
        {
            snipeFlash = !snipeFlash;
            if ( snipeFlash )
            {
                float dash[] = { 8.0f };
                Stroke old = ( (Graphics2D) g ).getStroke();
                ( (Graphics2D) g ).setStroke( new BasicStroke( 3.0f, BasicStroke.CAP_ROUND,
                                                               BasicStroke.JOIN_ROUND, 5.0f, dash, 2.0f ) );
                AsteroidsFrame.frame().drawLine( g, myInvicibleColor, (int) getX(), (int) getY(), 1500, 15, angle );
                ( (Graphics2D) g ).setStroke( old );
            }
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
        if ( livesLeft >= 0 )
        {
            if ( shooting && canShoot() )
                shoot();
            invincibilityCount--;
            checkCollision();
            generateParticles();
            move();
        }
        else
        {
            explosionTime--;

            // Create particles.
            for ( int i = 0; i < explosionTime / 12; i++ )
            {
                ParticleManager.addParticle( new Particle(
                                             getX() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             getY() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             RandomGenerator.get().nextInt( 4 ) + 3,
                                             RandomGenerator.get().nextBoolean() ? myColor : myInvicibleColor,
                                             RandomGenerator.get().nextDouble() * 6,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             25 + explosionTime / 10, 10 ) );
            }
        }
        for ( Weapon wm : allWeapons )
        {
            if ( wm == this.allWeapons[weaponIndex] )
                wm.act( true );
            else
                wm.act( false );
        }
        sniperManager.act( true );

        if ( stopping == true )
        {
            slowStop();
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
        stopping = true;
    }

    public void rotateWeapons()
    {
        weaponIndex++;
        weaponIndex %= allWeapons.length;
        drawWeaponNameTimer = 50;
    }

    public String giveShield()
    {
        if ( shielded )
            return "";
        shielded = true;
        return "Shield";
    }

    private void checkCollision()
    {
        for ( ShootingObject other : Game.getInstance().shootingObjects )
        {
            if ( other == this )
                continue;

            for ( Weapon wm : other.getManagers() )
            {
                for ( Weapon.Unit m : wm.getWeapons() )
                {
                    if ( Math.pow( (int) ( getX() - m.getX() ), 2 ) + Math.pow( (int) ( getY() - m.getY() ), 2 ) < Math.pow( RADIUS + m.getRadius(), 2 ) )
                    {
                        String obit = "";
                        if ( other instanceof Ship )
                            obit = getName() + " was blasted by " + ( (Ship) other ).getName() + ".";
                        else if ( other instanceof Station )
                            obit = getName() + " was shot down by a satellite.";

                        if ( damage( 40, obit ) )
                        {
                            m.explode();
                            score -= 5000;
                            if ( other.getClass().isInstance( this ) )
                            {
                                Ship s = (Ship) other;
                                s.score += 5000;
                                s.numShipsKilled++;
                                s.livesLeft++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates particles for the ship's boosters.
     * 
     * @since December 16, 2007
     */
    private void generateParticles()
    {
        // System.out.println( 9 * -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- 8 ); // :O

        if ( forward && !( Math.abs( getDx() ) < 0.1 && Math.abs( getDy() ) < 0.15 ) )
            particleRateForward = Math.min( 1, Math.max( 0.4, particleRateForward + 0.05 ) );
        else
            particleRateForward = Math.max( 0, particleRateForward - 0.03 );

        if ( backwards && !( Math.abs( getDx() ) < 0.1 && Math.abs( getDy() ) < 0.15 ) )
            particleRateBackward = Math.min( 1, Math.max( 0.4, particleRateBackward + 0.05 ) );
        else
            particleRateBackward = Math.max( 0, particleRateBackward - 0.03 );


        for ( int i = 0; i < (int) ( particleRateForward * 3 ); i++ )
            ParticleManager.addParticle( new Particle(
                                         -15 * Math.cos( angle ) + getX() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         15 * Math.sin( angle ) + getY() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ) + 3,
                                         myColor,
                                         RandomGenerator.get().nextDouble() * 4,
                                         angle + RandomGenerator.get().nextDouble() * .4 - .2 + Math.PI,
                                         30, 10 ) );

        for ( int i = 0; i < (int) ( particleRateBackward * 3 ); i++ )
            ParticleManager.addParticle( new Particle(
                                         15 * Math.cos( angle ) + getX() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         -15 * Math.sin( angle ) + getY() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ) + 3,
                                         myColor,
                                         RandomGenerator.get().nextDouble() * 4,
                                         angle + RandomGenerator.get().nextDouble() * .4 - .2,
                                         30, 10 ) );



    }

    @Override
    public void move()
    {
        super.move();

        if ( forward )
            setSpeed( getDx() + Math.cos( angle ) / SENSITIVITY * 2, getDy() - Math.sin( angle ) / SENSITIVITY * 2 );
        if ( backwards )
            setSpeed( getDx() - Math.cos( angle ) / SENSITIVITY * 2, getDy() + Math.sin( angle ) / SENSITIVITY * 2 );

        if ( left )
            angle += Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );
        if ( right )
            angle -= Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );

        if ( Math.abs( strafeSpeed ) > 0 )
        {
            strafeSpeed *= 0.97;
            setLocation( getX() + Math.sin( angle ) * strafeSpeed, getY() + Math.cos( angle ) * strafeSpeed );

            if ( Math.abs( strafeSpeed ) <= 0.11 )
                strafeSpeed = 0;
        }
    }

    public void shoot()
    {
        if ( livesLeft < 0 )
            return;

        if ( sniping )
            sniperManager.add( (int) getX(), (int) getY(), angle, getDx(), getDy(), myColor, true );
        else
            getWeaponManager().add( (int) getX(), (int) getY(), angle, getDx(), getDy(), myColor, true );
    }

    public boolean canShoot()
    {
        return ( livesLeft >= 0 && getWeaponManager().canShoot() );
    }

    public Color getColor()
    {
        return myColor;
    }

    /**
     * If you can't figure out what this does you're retarded.
     */
    public void setInvincibilityCount( int num )
    {
        invincibilityCount = num;
    }

    /**
     * Does a certain amount of damage to this ship, potentially killing it.
     * 
     * @param obituary  the string to announce to the game. For example, <code>ship.getName() + " played with fire."</code>
     * @return          whether damage was dealt (ship not invincible)
     * @since Classic
     */
    public boolean damage( double amount, String obituary )
    {
        // We're invincible and can't die.
        if ( cannotDie() )
            return false;

        // Shield saved us.
        if ( shielded )
        {
            setInvincibilityCount( 50 );
            shielded = false;
            return true;
        }

        // Lose health, and some max health as well.
        health -= amount;
        healthMax -= amount / 3.0;

        // Bounce.
        setSpeed( getDx() * -.3, getDy() * -.3 );

        // If just a wound, play the sound and leave.
        if ( health > 0 )
        {
            Sound.playInternal( SoundLibrary.SHIP_HIT );
            AsteroidsFrame.frame().rumble( amount * 2 / 3.0 );
            return true;
        }

        // We lost a life.
        livesLeft--;

        // Continue play.
        if ( livesLeft >= 0 )
        {
            health = healthMax = 100;
            setInvincibilityCount( 300 );
            AsteroidsFrame.frame().rumble( 30 );
            Sound.playInternal( SoundLibrary.SHIP_DIE );

            // Create particles.
            for ( int i = 0; i < 80; i++ )
            {
                ParticleManager.addParticle( new Particle(
                                             getX() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             getY() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             RandomGenerator.get().nextInt( 4 ) + 3,
                                             myColor,
                                             RandomGenerator.get().nextDouble() * 6,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             30, 10 ) );
            }
        }
        // We lost the game.
        else
        {
            invincibilityCount = Integer.MAX_VALUE;
            explosionTime = 160;
            allStop();
            AsteroidsFrame.frame().rumble( 85 );
            if ( Settings.soundOn && this == AsteroidsFrame.frame().localPlayer() )
                Sound.playInternal( SoundLibrary.GAME_OVER );

            // Create lots of particles.
            for ( int i = 0; i < 500; i++ )
            {
                ParticleManager.addParticle( new Particle(
                                             getX() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             getY() + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             RandomGenerator.get().nextInt( 4 ) + 3,
                                             myColor,
                                             RandomGenerator.get().nextDouble() * 4,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             60, 5 ) );
            }
        }

        // Print the obit.
        if ( obituary.length() > 0 )
            Running.log( obituary );

        return true;
    }

    public boolean cannotDie()
    {
        return invincibilityCount > 0;
    }

    public Weapon getWeaponManager()
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

        if ( Server.is() )
            Server.getInstance().berserk( id );
        allWeapons[weaponIndex].berserk( this );
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

    /**
     * Writes our position, angle, key presses, and speed.
     * 
     * @param stream    the stream to write to
     * @throws java.io.IOException
     * @since January 2, 2008
     */
    public void flattenPosition( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeDouble( angle );
        stream.writeBoolean( left );
        stream.writeBoolean( right );
        stream.writeBoolean( backwards );
        stream.writeBoolean( forward );
        stream.writeBoolean( shooting );
        stream.writeInt( weaponIndex );
    }

    /**
     * Reads our position, angle, key presses, and speed.
     * 
     * @param stream    the stream to read from
     * @throws java.io.IOException
     * @since January 2, 2008
     */
    public void restorePosition( DataInputStream stream ) throws IOException
    {
        super.restore( stream );
        angle = stream.readDouble();
        left = stream.readBoolean();
        right = stream.readBoolean();
        backwards = stream.readBoolean();
        forward = stream.readBoolean();
        shooting = stream.readBoolean();
        weaponIndex = stream.readInt();
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt( id );
        flattenPosition( stream );
        stream.writeInt( invincibilityCount );
        stream.writeInt( myColor.getRGB() );

        stream.writeUTF( name );
        stream.writeInt( livesLeft );
        stream.writeInt( weaponIndex );
        stream.writeInt( numAsteroidsKilled );
        stream.writeInt( numShipsKilled );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    public Ship( DataInputStream stream ) throws IOException
    {
        id = stream.readInt();
        restorePosition( stream );
        invincibilityCount = stream.readInt();
        myColor = new Color( stream.readInt() );

        name = stream.readUTF();
        livesLeft = stream.readInt();
        weaponIndex = stream.readInt();
        numAsteroidsKilled = stream.readInt();
        numShipsKilled = stream.readInt();

        // Apply basic construction.        
        double fadePct = 0.6;
        myInvicibleColor = new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
    }

    public int getWeaponIndex()
    {
        return weaponIndex;
    }

    public ConcurrentLinkedQueue<Weapon> getManagers()
    {
        ConcurrentLinkedQueue<Weapon> c = new ConcurrentLinkedQueue<Weapon>();
        for ( Weapon w : allWeapons )
            c.add( w );
        c.add( sniperManager );
        return c;
    }

    public int getExplosionTime()
    {
        return explosionTime;
    }

    /**
     * Executes a quick burst of movement to one side.
     * 
     * @param toRight   whether we should strafe right (true) or left (false)
     * @since March 9, 2008
     */
    public void strafe( boolean toRight )
    {
        if ( Math.abs( strafeSpeed ) > 3 )
            return;

        strafeSpeed += ( toRight ? 1 : -1 ) * 7;

        for ( int i = 0; i < 9; i++ )
            ParticleManager.addParticle( new Particle(
                                         getX() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         getY() + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ) + 3,
                                         myColor,
                                         RandomGenerator.get().nextDouble() * 4,
                                         angle + RandomGenerator.get().nextDouble() * .8 - .2 + Math.PI,
                                         35, 35 ) );
    }

    /**
     * Toggles sniping mode, in which the player aims much more precisely.
     * @param on    whether sniping is on
     * 
     * @since March 11, 2008
     */
    public void setSnipeMode( boolean on )
    {
        sniping = on;
    }

    public int getRadius()
    {
        return shielded ? RADIUS + 5 : RADIUS;
    }

    public void setWeapon( int index )
    {
        weaponIndex = index % allWeapons.length;
        drawWeaponNameTimer = 50;
    }

    public double getHealth()
    {
        return health;
    }

    private void slowStop()
    {
        if ( getDx() > .4 || getDx() < -.4 )
            setDx( getDx() * .9 );
        else
            stopping = false;
        if ( getDy() > .4 || getDy() < -.4 )
            setDy( getDy() * .9 );
        else
            stopping = false;
    }
}
