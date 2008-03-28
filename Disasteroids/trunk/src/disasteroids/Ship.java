/**
 * DISASTEROIDS
 * Ship.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.AsteroidsFrame;
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
public class Ship implements GameElement, ShootingObject
{
    public int id;

    public final static double SENSITIVITY = 20;

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
    private boolean forward = false,  backwards = false,  left = false,  right = false,  shooting = false,  sniping = false;

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
     * The <code>SniperManager</code> for this <code>Ship</code>
     * @since March 26, 2008
     */
    private SniperManager sniperManager;

    /**
     * How long to draw the name of the current weapon.
     * @since December 25, 2007
     */
    private int drawWeaponNameTimer;

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
     * If this <code>Ship</code> has a shield; good for one free hit
     */
    private boolean shielded;

    public Ship( int x, int y, Color c, int lives, String name )
    {
        this.x = x;
        this.y = y;
        this.myColor = c;
        this.livesLeft = lives;
        this.name = name;

        shielded = false;
        score = 0;
        numAsteroidsKilled = 0;
        drawWeaponNameTimer = 0;
        numShipsKilled = 0;
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
        sniperManager = new SniperManager();
        weaponIndex = 0;

        // Start invincible.
        invulFlash = true;
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
        return id + " ~ [" + (int) x + "," + (int) y + "]";
    }

    public void clearWeapons()
    {
        for ( WeaponManager wM : allWeapons )
            wM.clear();
        sniperManager.clear();
    }

    public void restoreBonusValues()
    {
        for ( WeaponManager wM : allWeapons )
            wM.restoreBonusValues();
        sniperManager.restoreBonusValues();
    }

    public void draw( Graphics g )
    {
        for ( WeaponManager wm : allWeapons )
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
            centerX = ( x - AsteroidsFrame.frame().localPlayer().getX() + AsteroidsFrame.frame().getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
            centerY = ( y - AsteroidsFrame.frame().localPlayer().getY() + AsteroidsFrame.frame().getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;

            if ( !( centerX > -100 && centerX < Game.getInstance().GAME_WIDTH + 100 && centerY > -100 && centerY < Game.getInstance().GAME_HEIGHT + 100 ) )
                return;
        }

        Polygon outline = new Polygon();
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle ) ), (int) ( centerY - RADIUS * Math.sin( angle ) ) );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle + Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle + Math.PI * .85 ) ) );
        outline.addPoint( (int) ( centerX + RADIUS * Math.cos( angle - Math.PI * .85 ) ), (int) ( centerY - RADIUS * Math.sin( angle - Math.PI * .85 ) ) );

        if ( ( cannotDie() && ( invulFlash = !invulFlash ) == true ) || !( cannotDie() ) )
        {
            AsteroidsFrame.frame().drawPolygon( g, col, Color.black, outline );
        }

        if ( shielded )
        {
            AsteroidsFrame.frame().drawCircle( g, Color.CYAN, (int) x, (int) y, RADIUS );
        }

        if ( this == AsteroidsFrame.frame().localPlayer() && drawWeaponNameTimer > 0 )
        {
            drawWeaponNameTimer--;
            g.setFont( new Font( "Century Gothic", Font.BOLD, 14 ) );
            Graphics2D g2d = (Graphics2D) g;
            AsteroidsFrame.frame().drawString( g, (int) x - (int) g2d.getFont().getStringBounds( getWeaponManager().getWeaponName(), g2d.getFontRenderContext() ).getWidth() / 2, (int) y - 15, getWeaponManager().getWeaponName(), Color.gray );
            allWeapons[weaponIndex].getWeapon( (int) x, (int) y + 25, Color.gray ).draw( g );
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
                AsteroidsFrame.frame().drawLine( g, myInvicibleColor, getX(), getY(), 1500, 15, angle );
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
                angle += Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );
            if ( right )
                angle -= Math.PI / SENSITIVITY / ( sniping ? 12 : 2 );
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
                                             x + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             y + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             RandomGenerator.get().nextInt( 4 ) + 3,
                                             RandomGenerator.get().nextBoolean() ? myColor : myInvicibleColor,
                                             RandomGenerator.get().nextDouble() * 6,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             25 + explosionTime / 10, 10 ) );
            }
        }
        for ( WeaponManager wm : allWeapons )
        {
            if ( wm == this.allWeapons[weaponIndex] )
                wm.act( true );
            else
                wm.act( false );
        }
        sniperManager.act(true);


        checkBounce();

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

    public String giveShield()
    {
        if ( shielded )
            return "";
        shielded = true;
        return "Shield";
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
        for ( ShootingObject other : Game.getInstance().shootingObjects )
        {
            if ( other == this )
                continue;

            for ( WeaponManager wm : other.getManagers() )
            {
                for ( WeaponManager.Unit m : wm.getWeapons() )
                {
                    if ( Math.pow( (int) ( x - m.getX() ), 2 ) + Math.pow( (int) ( y - m.getY() ), 2 ) < Math.pow( RADIUS + m.getRadius(), 2 ) )
                    {
                        String obit = "";
                        if ( other instanceof Ship )
                            obit = getName() + " was blasted by " + ( (Ship) other ).getName() + ".";
                        else if ( other instanceof Station )
                            obit = getName() + " was shot down by a satellite.";

                        if ( looseLife( obit ) )
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

        if ( forward && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
            particleRateForward = Math.min( 1, Math.max( 0.4, particleRateForward + 0.05 ) );
        else
            particleRateForward = Math.max( 0, particleRateForward - 0.03 );

        if ( backwards && !( Math.abs( dx ) < 0.1 && Math.abs( dy ) < 0.15 ) )
            particleRateBackward = Math.min( 1, Math.max( 0.4, particleRateBackward + 0.05 ) );
        else
            particleRateBackward = Math.max( 0, particleRateBackward - 0.03 );


        for ( int i = 0; i < (int) ( particleRateForward * 3 ); i++ )
            ParticleManager.addParticle( new Particle(
                                         -15 * Math.cos( angle ) + x + RandomGenerator.get().nextInt( 8 ) - 4,
                                         15 * Math.sin( angle ) + y + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ) + 3,
                                         myColor,
                                         RandomGenerator.get().nextDouble() * 4,
                                         angle + RandomGenerator.get().nextDouble() * .4 - .2 + Math.PI,
                                         30, 10 ) );

        for ( int i = 0; i < (int) ( particleRateBackward * 3 ); i++ )
            ParticleManager.addParticle( new Particle(
                                         15 * Math.cos( angle ) + x + RandomGenerator.get().nextInt( 8 ) - 4,
                                         -15 * Math.sin( angle ) + y + RandomGenerator.get().nextInt( 8 ) - 4,
                                         RandomGenerator.get().nextInt( 4 ) + 3,
                                         myColor,
                                         RandomGenerator.get().nextDouble() * 4,
                                         angle + RandomGenerator.get().nextDouble() * .4 - .2,
                                         30, 10 ) );



    }

    private void move()
    {
        x += dx;
        y +=
                dy;

        dx *=
                .996;
        dy *=
                .996;

        if ( Math.abs( strafeSpeed ) > 0 )
        {
            strafeSpeed *= 0.97;
            x += Math.sin( angle ) * strafeSpeed;
            y += Math.cos( angle ) * strafeSpeed;

            if ( Math.abs( strafeSpeed ) <= 0.11 )
                strafeSpeed = 0;
        }
    }

    public int getX()
    {
        return (int) x;
    }

    public int getY()
    {
        return (int) y;
    }

    public void shoot()
    {
        if ( livesLeft < 0 )
            return;

        if ( sniping )
            sniperManager.add( (int) x, (int) y, angle, dx, dy, myColor, true );
        else
            getWeaponManager().add( (int) x, (int) y, angle, dx, dy, myColor, true );
    }

    public boolean canShoot()
    {
        return ( livesLeft >= 0 && getWeaponManager().canShoot() );
    }

    public Color getColor()
    {
        return myColor;
    }//if you can't figure out what this does you're retarded
    public void setInvincibilityCount( int num )
    {
        invincibilityCount = num;
    }

    /**
     * If this ship isn't invincible, executes the 'killing': takes a life, creates particles, etc.
     * 
     * @param obituary  the string to announce to the game. For example, <code>ship.getName() + " played with fire."</code>
     * @return  whether the live was taken
     * @since Classic
     */
    public boolean looseLife( String obituary )
    {
        // We're invincible and can't die.
        if ( cannotDie() )
            return false;

        //shield saved us
        if ( shielded )
        {
            setInvincibilityCount( 50 );
            shielded = false;
            return true;

        }

        livesLeft--;
        if ( livesLeft >= 0 )
        {
            setInvincibilityCount( 300 );
            if ( Settings.soundOn )
                Sound.playInternal( SoundLibrary.SHIP_DIE );

            // Bounce.
            dx *= -.3;
            dy *= -.3;

            // Create particles.
            for ( int i = 0; i < 80; i++ )
            {
                ParticleManager.addParticle( new Particle(
                                             x + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             y + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             RandomGenerator.get().nextInt( 4 ) + 3,
                                             myColor,
                                             RandomGenerator.get().nextDouble() * 6,
                                             RandomGenerator.get().nextDouble() * 2 * Math.PI,
                                             30, 10 ) );
            }
        }
        else
        {
            invincibilityCount = Integer.MAX_VALUE;
            explosionTime = 160;
            allStop();

            if ( Settings.soundOn && this == AsteroidsFrame.frame().localPlayer() )
                Sound.playInternal( SoundLibrary.GAME_OVER );

            // Create lots of particles.
            for ( int i = 0; i < 500; i++ )
            {
                ParticleManager.addParticle( new Particle(
                                             x + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
                                             y + RandomGenerator.get().nextInt( 16 ) - 8 - RADIUS,
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
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeInt( id );

        flattenPosition(
                stream );

        stream.writeInt( weaponIndex );

        stream.writeInt( invincibilityCount );

        // Find our color.
        int colorIndex = -1;
        for ( int i = 0; i <
                Game.PLAYER_COLORS.length; i++ )
        {
            if ( Game.PLAYER_COLORS[i] == myColor )
            {
                colorIndex = i;
                break;
            }

        }
        if ( colorIndex == -1 )
            Running.fatalError( "Unknown ship color: " + myColor );

        stream.writeInt( colorIndex );
        stream.writeUTF( name );
        stream.writeInt( livesLeft );
        stream.writeInt( weaponIndex );
        stream.writeInt( numAsteroidsKilled );
        stream.writeInt( numShipsKilled );

    // (Whew!)
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
        stream.writeDouble( x );
        stream.writeDouble( y );
        stream.writeDouble( dx );
        stream.writeDouble( dy );
        stream.writeDouble( angle );

        stream.writeBoolean( left );
        stream.writeBoolean( right );
        stream.writeBoolean( backwards );
        stream.writeBoolean( forward );
        stream.writeBoolean( shooting );
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
        x = stream.readDouble();
        y =
                stream.readDouble();
        dx =
                stream.readDouble();
        dy =
                stream.readDouble();
        angle =
                stream.readDouble();

        left =
                stream.readBoolean();
        right =
                stream.readBoolean();
        backwards =
                stream.readBoolean();
        forward =
                stream.readBoolean();
        shooting =
                stream.readBoolean();

        weaponIndex =
                stream.readInt();
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    public Ship(
            DataInputStream stream ) throws IOException
    {
        id = stream.readInt();

        restorePosition(
                stream );

        invincibilityCount =
                stream.readInt();

        myColor =
                Game.PLAYER_COLORS[stream.readInt()];

        name =
                stream.readUTF();
        livesLeft =
                stream.readInt();
        weaponIndex =
                stream.readInt();
        numAsteroidsKilled =
                stream.readInt();
        numShipsKilled =
                stream.readInt();

        // Apply basic construction.        
        double fadePct = 0.6;
        myInvicibleColor =
                new Color( (int) ( myColor.getRed() * fadePct ), (int) ( myColor.getGreen() * fadePct ), (int) ( myColor.getBlue() * fadePct ) );
        allWeapons =
                new WeaponManager[3];
        allWeapons[0] = new MissileManager();
        allWeapons[1] = new BulletManager();
        allWeapons[2] = new MineManager();
    }

    public double getDx()
    {
        return dx;
    }

    public double getDy()
    {
        return dy;
    }

    public int getWeaponIndex()
    {
        return weaponIndex;
    }

    public ConcurrentLinkedQueue<WeaponManager> getManagers()
    {
        ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
        for ( WeaponManager w : allWeapons )
            c.add( w );
        c.add(sniperManager);
        return c;
    }

    public double getSpeed()
    {
        return Math.sqrt( getDx() * getDx() + getDy() * getDy() );
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
                                         x + RandomGenerator.get().nextInt( 8 ) - 4,
                                         y + RandomGenerator.get().nextInt( 8 ) - 4,
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
}
