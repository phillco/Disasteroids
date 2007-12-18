/*
 * DISASTEROIDS
 * AsteroidsFrame.java
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * The big momma class up in the sky.
 * 
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic
 */
public class AsteroidsFrame extends Frame implements KeyListener
{
    /**
     * Dimensions of the window when not in fullscreen mode.
     * @since November 15 2007
     */
    private static final int WINDOW_WIDTH = 800,  WINDOW_HEIGHT = 800;
    
    /**
     * Dimensions of the game, regardless of the graphical depiction
     * @since December 17, 2007
     */
    public static final int GAME_WIDTH = 2000, GAME_HEIGHT = 2000;

    /**
     * Default player colors. Inspired from AOE2.
     * @since December 14 2007
     */
    private static final Color[] PLAYER_COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };

    /**
     * The record high score.
     * @since Classic
     */
    private static double highScore;

    /**
     * The record high-scorer's name.
     * @since Classic
     */
    private static String highScoreName;

    /**
     * The <code>Image</code> used for double buffering.
     * @since Classic
     */
    private static Image virtualMem;

    /**
     * The <code>Image</code> storing the star background.
     * @since Classic
     */
    private static Image background;

    /**
     * The current level of the game.
     * @since Classic
     */
    private static int level;

    /**
     * The <code>Graphics</code> context of the screen.
     * @since Classic
     */
    private Graphics g;

    /**
     * The <code>Graphics</code> context of virtualMem; used for double buffering.
     * @since Classic
     */
    private static Graphics gBuff;

    /**
     * Stores whether the game is currently paused or not.
     * @since Classic
     */
    private boolean paused = false;

    /**
     * The current game time.
     * @since Classic
     */
    public static long timeStep = 0;

    /**
     * The current game time of the other player.
     * @since Classic
     */
    public static long otherPlayerTimeStep = 0;

    /**
     * Stores the current <code>Asteroid</code> field.
     * @since Classic
     */
    private AsteroidManager asteroidManager;

    /**
     * Stores the current pending <code>Action</code>s.
     * @since Classic
     */
    private ActionManager actionManager;

    /**
     * Array of players.
     * @since December 14 2007
     */
    public static Ship[] players;

    /**
     * Index of the player that's at this computer.
     * @see <code>players</code>
     * @since December 14 2007
     */
    private static int localPlayer;
    
    /**
     * The time stored at the beginning of the last call to paint; Used for FPS.
     * @since December 15, 2007
     */
    private long timeOfLastRepaint;
    
    /**
     * Stores every <code>Star</code> for the background
     * @since December 16, 2007
     */
    private Star[] theStars;

    /**
     * Whether the user is pressing the scoreboard key.
     * @since December 15 2007
     */
    private boolean drawScoreboard;
    
    private LinkedList<Message> theMessages= new LinkedList<Message>();

    /**
     * Constructs the game frame and game elements.
     * 
     * @param playerCount   number of players that will be playing
     * @param localPlayer   index of the player at this computer
     * @since December 14, 2007
     */
    public AsteroidsFrame( int playerCount, int localPlayer )
    {
        Running.setEnvironment( this );

        // Close when the exit key is pressed.
        addWindowListener( new CloseAdapter() );

        // Set our size - fullscreen or windowed.
        updateFullscreen();

        highScore = 1000000;
        highScoreName = "Phillip and Andy";

        // Set up the connection/game settings.
        players = new Ship[playerCount];
        AsteroidsFrame.localPlayer = localPlayer;

        // Receive key events.
        this.addKeyListener( this );

        // 

        // Set up the game.
        resetEntireGame();
    }

    /**
     * Resets all gameplay elements. Ships, asteroids, timesteps, missiles and actions are all reset.
     * Connection elements like the number of players or the index of <code>localPlayer</code> are not reset.
     * 
     * @since December 16, 2007
     */
    private void resetEntireGame()
    {
        timeStep = 0;
        otherPlayerTimeStep = 0;

        // Create the ships.
        actionManager = new ActionManager();
        for ( int i = 0; i < players.length; i++ )
            players[i] = new Ship( GAME_WIDTH / 2 - ( i * 100 ), GAME_HEIGHT / 2, PLAYER_COLORS[i], 0, "Player " + ( i + 1 ) , Ship.WeaponType.MISSILES);

        // Create the asteroids.
        level = 1;
        asteroidManager = new AsteroidManager();
        asteroidManager.setUpAsteroidField( level );

        // Reset the background.
        drawBackground();
    }

    /**
     * Sets up double buffering.
     * 
     * @param g the unbuffered <code>Graphics</code> context
     * @since December 25, 2007
     */
    private void initBuffering( Graphics g )
    {
        this.g = g;

        // Create the buffer.
        virtualMem = createImage( getWidth(), getHeight() );
        gBuff = virtualMem.getGraphics();

        // Anti-alias, if the user wants it.
        updateAntiAliasing( Settings.antiAlias );
    }

    /**
     * Steps the game through one timestep and paints all components onto the screen.
     * 
     * @param g the <code>Graphics</code> context of the screen
     * @since Classic
     */
    @Override
    public void paint( Graphics g )
    {
        if ( paused )
            return;
        long timeSinceLast=-timeOfLastRepaint+(timeOfLastRepaint=System.currentTimeMillis());
        System.out.println("FPS="+1000.0/timeSinceLast);
        if ( gBuff == null )
            initBuffering( g );

        AsteroidsServer.send( "t" + String.valueOf( timeStep ) );

        // Are we are too far ahead?
        if ( ( timeStep - otherPlayerTimeStep > 2 ) && ( players.length > 1 ) )
        {
            safeSleep( 20 );
            AsteroidsServer.send( "t" + String.valueOf( timeStep ) );
            AsteroidsServer.flush();
            repaint();
            return;
        }

        // Scroll the stars up and draw the background.
        updateBackground();
        gBuff.drawImage( background, 0, 0, this );

        // Execute game actions.
        timeStep++;
        try
        {
            actionManager.act( timeStep );
        }
        catch ( UnsynchronizedException e )
        {
            System.out.println( "Action missed! " + e + "\nOur timestep: " + timeStep + ", their timestep: " + otherPlayerTimeStep + "." );
            Running.quit();
        }

        // Draw stuff in order of importance, from least to most.
        ParticleManager.drawParticles();
        asteroidManager.act();

        // Update the ships.
        for ( int i = 0; i < players.length; i++ )
        {
            players[i].act();

            // Game over?
            if ( players[i].livesLeft() < 0 )
            {
                endGame( g );
                continue;
            }
        }

        // Draw the on-screen HUD.
        drawHud();

        // Draw the entire scoreboard.
        if ( drawScoreboard )
            drawScoreboard();

        // Flip the buffer to the screen.
        g.drawImage( virtualMem, 0, 0, this );

        // Advance to the next level if it's time.
        if ( shouldExitLevel() )
            nextLevel();

        // This causes 100% cpu usage, but it's safe to say the game always has to be updated.
        repaint();
    }

    /**
     * Draws the star field over itself to make stars move.
     *      
     * @since Classic
     */
    private void updateBackground()
    {
        // Create the background first.
        if ( background == null )
            drawBackground();

        // Don't anti-alias this.
        updateAntiAliasing( false );

        Graphics gBack = background.getGraphics();
        gBack.setColor(Color.black);
        gBack.fillRect(0, 0, getWidth(), getHeight());
        for(Star star: this.theStars)
            drawPoint(gBack, star.color, star.x, star.y);
        ListIterator<Message> itr=theMessages.listIterator();
        while(itr.hasNext())
        {
            Message m=itr.next();
            drawString(gBack, m.x, m.y, m.message, m.col);
            if(m.life--==0)
                itr.remove();
        }
        

        updateAntiAliasing( Settings.antiAlias );
    }

    /**
     * Shows a dialog to warps to a particular level.
     * 
     * @since November 15 2007
     */
    private void warpDialog()
    {
        warp( Integer.parseInt( JOptionPane.showInputDialog( "Enter the level number to warp to.", level ) ) );
    }

    /**
     * Returns if the game is ready to advance levels.
     * Checks if the <code>Asteroids</code> have been cleared, then if we're on the sandbox level, and finally if the <code>Missile</code>s have been cleared.
     * 
     * @see Settings.waitForMissiles
     * @return  whether the game should advance to the next level
     */
    private boolean shouldExitLevel()
    {
        // Have the asteroids been cleared?
        if ( asteroidManager.size() > 0 )
            return false;

        // Level -999 is a sandbox and never exits.
        if ( level == -999 )
            return false;

        // The user can choose to wait for missiles.
        if ( Settings.waitForMissiles )
        {
            for ( Ship s : players )
            {
                if ( s.getWeaponManager().getNumLiving() > 0 )
                    return false;
            }
        }
        
        // Ready to advance!
        return true;
    }

    /**
     * Advances to the next level.
     * @author Phillip Cohen
     * @since November 15 2007
     */
    private void nextLevel()
    {
        warp( level + 1 );
    }

    /**
     * Advances the game to a new level.
     * 
     * @param newLevel The level to warp to.
     * @since November 15 2007
     */
    private void warp( int newLevel )
    {
        paused = true;
        level = newLevel;

        // All players get bonuses.
        for ( Ship s : players )
        {
            s.addLife();
            s.setInvincibilityCount( 100 );
            s.increaseScore( 2500 );
            s.clearWeapons();
            s.setNumAsteroidsKilled( 0 );
            s.setNumShipsKilled( 0 );
        }

        asteroidManager.clear();
        actionManager.clear();

        drawBackground();
        restoreBonusValues();
        System.out.println( "Welcome to level " + newLevel + ". Random asteroid numbers: " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " (Seed: " + RandNumGen.seed + ")" );
        asteroidManager.setUpAsteroidField( level );
        writeOnBackground( "Welcome to level " + newLevel + ".", GAME_WIDTH / 2 - 80, GAME_HEIGHT / 2, Color.green );
        paused = false;
    }

    /**
     * Advances to the next level from a static context.
     * 
     * @deprecated Use <code>Running.environment()</code> to access frame methods.
     * @since Classic
     */
    @Deprecated
    public static void staticNextLevel()
    {
        Running.environment().nextLevel();
    }

    /**
     * Draws the on-screen information for the local player.
     * 
     * @since December 15, 2007
     */
    private void drawHud()
    {
        Graphics2D g2d = (Graphics2D) gBuff;
        String text = "";
        int x = 0, y = 0;

        // Draw the "lives" string.
        g2d.setColor( players[localPlayer].getColor() );
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        if ( players[localPlayer].livesLeft() == 1 )
            text = "life";
        else
            text = "lives";
        x = getWidth() - 40;
        y = getHeight() - 15;
        g2d.drawString( text, x, y );
        x -= 10;

        // Draw the lives counter.
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + players[localPlayer].livesLeft();
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );
        x -= 15;

        // Draw the level counter.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = "" + level;
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
        g2d.drawString( text, x, y );

        // Draw the "level" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "level";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );

        // Draw the score counter.
        g2d.setColor( Color.darkGray );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
        text = insertThousandCommas( players[localPlayer].getScore() );
        y = ( isFullscreen() ? 0 : 30 ) + 20; // Offset for the titlebar (yuck)!
        x = getWidth() - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() - 12;
        g2d.drawString( text, x, y );

        // Draw the "score" string.
        g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
        text = "score";
        x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
        g2d.drawString( text, x, y );
    }

    /**
     * Draws the full scoreboard of all players.
     * 
     * @since December 15, 2007
     */
    private void drawScoreboard()
    {
        Graphics2D g2d = (Graphics2D) gBuff;
        String text = "";
        int x = 0, y = 0;

        // Draw the "scoreboard" string.
        g2d.setColor( Color.white );
        g2d.setFont( new Font( "Tahoma", Font.BOLD, 24 ) );
        text = "Scoreboard";
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        y = getHeight() / 4;
        g2d.drawString( text, x, y );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Draw the asteroid count.
        g2d.setColor( Color.lightGray );
        g2d.setFont( new Font( "Tahoma", Font.PLAIN, 16 ) );
        text = insertThousandCommas( asteroidManager.size() ) + " asteroid" + ( asteroidManager.size() == 1 ? " remains" : "s remain" );
        x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
        g2d.drawString( text, x, y );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Create the columns.
        ScoreboardColumn[] columns = new ScoreboardColumn[4];
        columns[0] = new ScoreboardColumn( getWidth() * 2 / 7, "Name" );
        columns[1] = new ScoreboardColumn( getWidth() * 1 / 2, "Score" );
        columns[2] = new ScoreboardColumn( getWidth() * 3 / 5, "Lives" );
        columns[3] = new ScoreboardColumn( getWidth() * 2 / 3, "# Destroyed" );

        // Draw the column headers.
        y += 15;
        g2d.setColor( Color.darkGray );
        g2d.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
        for ( ScoreboardColumn c : columns )
            g2d.drawString( c.getTitle(), c.getX(), y );

        g2d.drawLine( columns[0].getX(), y + 5, columns[columns.length - 1].getX() + (int) g2d.getFont().getStringBounds( columns[columns.length - 1].getTitle(), g2d.getFontRenderContext() ).getWidth(), y + 5 );
        y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;

        // Draw the entries.
        for ( Ship s : players )
        {
            g2d.setColor( s.getColor() );
            for ( int i = 0; i < columns.length; i++ )
            {
                switch ( i )
                {
                    case 0:
                        text = s.getName();
                        break;
                    case 1:
                        text = insertThousandCommas( s.score() );
                        break;
                    case 2:
                        text = "" + s.livesLeft();
                        break;
                    case 3:
                        text = "" + s.getNumAsteroidsKilled();
                        break;
                    default:
                        text = "";
                }
                g2d.drawString( text, columns[i].getX(), y );
            }
            y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
        }
    }

    /**
     * Draws the star background completely from scratch.
     * 
     * @since Classic
     */
    private void drawBackground()
    {
        // Create the image if we haven't yet.
        if ( background == null )
            background = createImage( getWidth(), getHeight() );

        Graphics gBack = background.getGraphics();

        // Fill the background with black.
        gBack.setColor( Color.black );
        gBack.fillRect( 0, 0, getWidth(), getHeight() );

        // Add stars of varying lightness.
        Random rand = RandNumGen.getStarInstance();
        this.theStars=new Star[GAME_WIDTH*GAME_HEIGHT/(1700+300)];
        for ( int star = 0; star < theStars.length; star++ )
        {
            int sat = rand.nextInt( 255 );
            Color col=new Color( sat, sat, sat );
            theStars[star]=new Star( rand.nextInt( GAME_WIDTH ), rand.nextInt( GAME_HEIGHT ), col );
        }
        
    }

    /**
     * Starts a new game.
     * 
     * @deprecated Doesn't work.
     * @since Classic
     */
    @Deprecated
    public void newGame()
    {
        resetEntireGame();
        if ( ( players.length > 1 ) && ( localPlayer == 0 ) )
            AsteroidsServer.send( "ng" );
    }

    /**
     * Displays the end game messages.
     * @param g the <code>Graphics</code> context of the screen
     * 
     * @since Classic
     */
    private void endGame( Graphics g )
    {
        paused = true;
        g.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
        if ( Settings.soundOn )
            Sound.wheeeargh();
        for ( float sat = 0; sat <= 1; sat += .005 )
        {
            g.setColor( Color.getHSBColor( sat, sat, sat ) );
            g.drawString( "Game Over", 250, 400 );
        }
        this.setIgnoreRepaint( true );

        // Find the player with the highest score.
        Ship highestScorer = players[0];
        for ( Ship s : players )
        {
            if ( s.getScore() > highestScorer.getScore() )
                highestScorer = s;
        }

        String victoryMessage = ( players.length > 1 ) ? highestScorer.getName() + " wins" : "You died";
        victoryMessage += " with a score of " + highestScorer.getScore() + "!";

        // Is this a new high score?
        if ( highestScorer.getScore() > highScore )
        {
            victoryMessage += "\nThis is a new high score!";
            highScoreName = highestScorer.getName();
            highScore = highestScorer.getScore();
            if ( ( players.length > 1 ) && ( highestScorer == players[localPlayer] ) )
                AsteroidsServer.send( "HS" + highScoreName );
        }

        // Show the message box declaring victory!
        JOptionPane.showMessageDialog( this, victoryMessage );

        // Easter egg!
        //        if ( highScore > 1000000 )
        //        {
        //            try
        //            {
        //                JOptionPane.showMessageDialog( null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT" );
        //                Runtime.getRuntime().exec( "C:/Windows/system32/calc" );
        //            }
        //            catch ( Exception e )
        //            {
        //            }
        //        }

        newGame();
        this.setIgnoreRepaint( false );
        paused = false;
        repaint();
    }

    /**
     * Called automatically by key listener, and used only for arrow keys.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public synchronized void keyReleased( KeyEvent e )
    {
        drawScoreboard = false;

        if ( !paused || e.getKeyCode() == 80 )
        {
            if ( (e.getKeyCode() >= 37 && e.getKeyCode() <= 40 )||e.getKeyCode()==KeyEvent.VK_SPACE)
                // Get the raw code from the keyboard
                //performAction(e.getKeyCode(), ship);
                actionManager.add( new Action( players[localPlayer], 0 - e.getKeyCode(), timeStep + 2 ) );
            AsteroidsServer.send( "k" + String.valueOf( 0 - e.getKeyCode() ) + "," + String.valueOf( timeStep + 2 ) );
        // [AK] moved to a new method to also be called by another class, receiving data from other computer
        //repaint();
        }
        repaint();
    }

    /**
     * Dummy method to satisfy <code>KeyListener</code> interface.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public void keyTyped( KeyEvent e )
    {
    }

    /**
     * Called automatically by key listener for all keys pressed, and creates an <code>Action</code> to store the relevent data.
     * 
     * @param e the <code>KeyEvent</code> generated by the key listener
     * @since Classic
     */
    public synchronized void keyPressed( KeyEvent e )
    {
        if ( !paused || e.getKeyCode() == 80 )
        {
            // Get the raw code from the keyboard
            //performAction(e.getKeyCode(), ship);
            actionManager.add( new Action( players[localPlayer], e.getKeyCode(), timeStep + 2 ) );
            AsteroidsServer.send( "k" + String.valueOf( e.getKeyCode() ) + "," + String.valueOf( timeStep + 2 ) );
        // [AK] moved to a new method to also be called by another class, receiving data from other computer
        //repaint();
        }
        repaint();
    }

    /**
     * Performs the action specified by the action as applied to the actor.
     * 
     * @param action    the key code for the action
     * @param actor     the <code>Ship</code> to execute the action
     * @since Classic
     */
    public synchronized void performAction( int action, Ship actor )
    {
        // Decide what key was pressed.
        switch ( action )
        {
            case KeyEvent.VK_ESCAPE:
                Running.quit();
            case KeyEvent.VK_SPACE:
                actor.startShoot();
                break;
            case -KeyEvent.VK_SPACE:
                actor.stopShoot();
                break;
            case KeyEvent.VK_LEFT:
                actor.left();
                break;
            case KeyEvent.VK_RIGHT:
                actor.right();
                break;
            case KeyEvent.VK_UP:
                actor.forward();
                break;
            case KeyEvent.VK_DOWN:
                actor.backwards();
                break;

            // Releasing keys.
            case -KeyEvent.VK_LEFT:
                actor.unleft();
                break;
            case -KeyEvent.VK_RIGHT:
                actor.unright();
                break;
            case -KeyEvent.VK_UP:
                actor.unforward();
                break;
            case -KeyEvent.VK_DOWN:
                actor.unbackwards();
                break;

            // Special keys.
            case KeyEvent.VK_PAGE_UP:
                actor.fullUp();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                actor.fullDown();
                break;
            case KeyEvent.VK_INSERT:
                actor.fullLeft();
                break;
            case KeyEvent.VK_DELETE:
                actor.fullRight();
                break;
            case KeyEvent.VK_END:
                actor.allStop();
                break;
            case 192:	// ~ activates berserk!
                actor.berserk();
                break;
            case KeyEvent.VK_HOME:
                actor.getWeaponManager().explodeAll();
                break;
            case KeyEvent.VK_P:
                // [PC] Not unpausable!
                // paused = !paused;
                break;
            case KeyEvent.VK_M:
                toggleMusic();
                break;
            case KeyEvent.VK_S:
                toggleSound();
                break;
            case KeyEvent.VK_F4:
                toggleFullscreen();
                break;
            case KeyEvent.VK_W:
                warpDialog();
                break;
            case KeyEvent.VK_Q:
                actor.rotateWeapons();
                break;
            case KeyEvent.VK_A:
                toggleAntiAliasing();
                break;
            case KeyEvent.VK_BACK_SLASH:
                drawScoreboard = true;
                break;
            default:
                drawScoreboard = false;
                break;
        }
        repaint();
    }

    /**
     * Toggles music on/off, and writes the setting to the background.
     * 
     * @since November 15, 2007
     */
    private void toggleMusic()
    {
        writeOnBackground( "Music " + ( Sound.toggleMusic() ? " on." : "off." ), GAME_WIDTH / 2 - 10, GAME_HEIGHT / 2, Color.green );
    }

    /**
     * Toggles sound on/off, and writes the setting to the background.
     * 
     * @since November 15, 2007
     */
    private void toggleSound()
    {
        writeOnBackground( "Sound " + ( Sound.toggleSound() ? " on." : "off." ), GAME_WIDTH / 2 - 10, GAME_HEIGHT / 2, Color.green );
    }

    /**
     * Toggles fullscreen on/off. This is rather problematic.
     * 
     * @since December 11, 2007
     */
    private void toggleFullscreen()
    {
        Settings.useFullscreen = !Settings.useFullscreen;
        updateFullscreen();
    }

    /**
     * Toggles antialiasing on/off, and writes the setting to the background.
     * 
     * @since December 15, 2007
     */
    private void toggleAntiAliasing()
    {
        Settings.antiAlias = !Settings.antiAlias;
        updateAntiAliasing( Settings.antiAlias );
        writeOnBackground( "Antialiasing " + ( Settings.antiAlias ? " on." : "off." ), GAME_WIDTH / 2 - 10, GAME_HEIGHT / 2, Color.green );
    }

    /**
     * Enables or disables anti-aliasing of all graphics.
     * 
     * @param useAntiAliasing   whether to use anti-aliasing
     * @since December 15, 2007
     */
    private void updateAntiAliasing( boolean useAntiAliasing )
    {
        if ( useAntiAliasing )
            ( (Graphics2D) getGBuff() ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        else
            ( (Graphics2D) getGBuff() ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
    }

    /**
     * Sets the fullscreen/window mode based on the setting.
     *
     * @since December 11, 2007
     */
    private void updateFullscreen()
    {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Set fullscreen mode.
        if ( Settings.useFullscreen )
        {
            // Don't change anything if we're already in fullscreen mode.
            if ( graphicsDevice.getFullScreenWindow() != this )
            {
                setVisible( false );
                dispose();
                setUndecorated( true );
                setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
                graphicsDevice.setFullScreenWindow( this );

                // Hide the cursor.
                Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
                setCursor( blankCursor );

                // Re-create the background.
                if ( background != null )
                    drawBackground();
            }
        }
        // Set windowed mode.
        else
        {
            // Don't change anything if we're already in windowed mode.
            if ( ( getSize().width != WINDOW_WIDTH ) || ( getSize().height != WINDOW_HEIGHT ) )
            {
                setVisible( false );
                dispose();
                setUndecorated( false );
                setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
                graphicsDevice.setFullScreenWindow( null );

                // Show the cursor.
                setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );

                // Re-create the background.
                if ( background != null )
                    drawBackground();
            }
        }
        setVisible( true );
    }

    /**
     * Returns if we're in fullscreen (regardless of the setting).
     * 
     * @return  whether the frame is in fullscreen mode
     * @since December 15, 2007
     */
    public boolean isFullscreen()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow() == this;
    }

    /**
     * Sets the current time of the other player.
     * 
     * @param step  the new value for the other player's current time
     * @since Classic
     */
    public void setOtherPlayerTimeStep( int step )
    {
        otherPlayerTimeStep = step;
    }

    /**
     * Included to prevent the clearing of the screen between repaints.
     * 
     * @param g the <code>Graphics</code> context of the screen
     * @since Classic
     */
    @Override
    public void update( Graphics g )
    {
        paint( g );
    }

    /**
     * Undoes all changes that <code>BonusAsteroid</code>s may have caused.
     * 
     * @since Classic
     */
    private void restoreBonusValues()
    {
        for ( Ship ship : players )
        {
            ship.restoreBonusValues();
        }
    }

    /**
     * Returns the current level.
     * 
     * @return  the current level
     * @since Classic
     */
    public static int getLevel()
    {
        return level;
    }

    /**
     * Sleeps the game for the specified time.
     * 
     * @param millis How many milliseconds to sleep for.
     * @since Classic
     */
    public static void safeSleep( int millis )
    {
        try
        {
            Thread.sleep( millis );
        }
        catch ( InterruptedException e )
        {
        }
    }

    /**
     * Writes a message onto the background.
     * 
     * @param message   the message to be written.
     * @param x         the x coordinate where the message should be drawn
     * @param y         the y coordinate where the message should be drawn
     * @param col       the <code>Color</code> in which the message should be drawn
     * @since Classic
     */
    public void writeOnBackground( String message, int x, int y, Color col )
    {
        theMessages.add(new Message(x, y, message, col));
    }

    /**
     * Draws a point at specified coordinates, translated relative to local ship.
     * @param graph The <code>Graphics</code> context in which to draw
     * @param col The <code>Color</code> in which to draw
     * @param x The x coordinate
     * @param y The y coordinate
     * @author Andy Kooiman
     * @since December 16, 2007
     */
    public void drawPoint(Graphics graph, Color col, int x, int y) {
        x=(x-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y=(y-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        graph.setColor(col);
        graph.drawRect(x, y, 0, 0);
    }

    
    /**
     * Draws a circle with center at coordinates translated relative to local ship with given radius in given color
     * @param col The <code>Color</code> in which the circle will be drawn
     * @param x The x coordinate of the center
     * @param y The y coordinate of the center
     * @param radius The radius of the circle
     * @since December 15, 2007
     */
    public void drawCircle( Color col, int x, int y, int radius )
    {
        x=(x-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y=(y-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        gBuff.setColor(col);
        gBuff.drawOval(x-radius/2-1, y-radius/2-1, radius*2, radius*2);
    }

    /**
     * Draws a line from one coordinate to another in a given color.
     * 
     * @param col   the <code>Color</code> in wihch the circle will be drawn
     * @param x1    the first x coordinate
     * @param y1    the first y coordinate
     * @param x2    the second x coordinate
     * @param y2    the second y coordinate
     * @since December 15, 2007
     */
    public void drawLine( Color col, int x1, int y1, int x2, int y2 )
    {
        x1=(x1-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y1=(y1-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        x2=(x2-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y2=(y2-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        gBuff.setColor(col);
        gBuff.drawLine(x1,y1, x2, y2);
    }
    
        /**
     * Draws a line from one coordinate to another in a given color
     * @param col The <code>Color</code> in wihch the circle will be drawn
     * @param x1 The first x coordinate
     * @param y1 The first y coordinate
     * @param x2 The second x coordinate
     * @param y2 The second y coordinate
     * @since December 15, 2007
     */
    public void drawLine(Graphics graph, Color col, int x1, int y1, int x2, int y2)
    {
        x1=(x1-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y1=(y1-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        x2=(x2-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y2=(y2-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        graph.setColor(col);
        graph.drawLine(x1,y1, x2, y2);
    }
    
    public void drawLine(Color col, int x, int y, int length, double angle)
    {
        x=(x-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y=(y-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        gBuff.setColor(col);
        gBuff.drawLine(x, y,(int)(x+length*Math.cos(angle)),(int)(y-length*Math.sin(angle)));
    }
    
     /**
     * Draws a circle with center at coordinates with given radius in given color.
     * 
     * @param col       the <code>Color</code> in which the circle will be drawn
     * @param x         the x coordinate of the center
     * @param y         the y coordinate of the center
     * @param radius    the radius of the circle
     * @since December 15, 2007
     */
    public void fillCircle( Color col, int x, int y, int radius )
    {
        x=(x-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y=(y-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        gBuff.setColor(col);
        gBuff.fillOval(x-radius/2, y-radius/2, radius*2, radius*2);
    }
    
    public void drawString(Graphics graph, int x, int y, String str, Color col)
    {
        x=(x-players[localPlayer].getX()+getWidth()/2+4*GAME_WIDTH)%GAME_WIDTH;
        y=(y-players[localPlayer].getY()+getHeight()/2+4*GAME_HEIGHT)%GAME_HEIGHT;
        graph.setColor(col);       
        graph.drawString(str,x,y);
    }

    /**
     * Draws a polygon in one color with a background of another color.
     * 
     * @param p         the <code>Polygon</code> to be drawn
     * @param fill      the <code>Color</code> in which the <code>Polygon</code> will be drawn
     * @param outline   the <code>Color</code> of the outline
     * @since December 15, 2007
     */
    public void drawPolygon( Color fill, Color outline, Polygon p )
    {
        gBuff.setColor( fill );
        gBuff.fillPolygon( p );
        gBuff.setColor( outline );
        gBuff.drawPolygon( p );
    }

    /**
     * Draws a circle with center at given point with given radius in one color, with an outline of another color.
     * 
     * @param fill      the <code>Color</code> in which the circle will be drawn
     * @param outline   the <code>Color</code> of the outline
     * @param x         the x coordinate of the center
     * @param y         the y coordinate of the center
     * @param radius    the radius
     * @since December 15, 2007
     */
    public void drawOutlinedCircle( Color fill, Color outline, int x, int y, int radius )
    {
        fillCircle( fill, x - radius / 2, y - radius / 2, radius );
        drawCircle( outline, x - radius / 2, y - radius / 2, radius );
    }

    /**
     * Returns the game's <code>ActionManager</code>.
     * 
     * @return  the <code>ActionManager</code> of <code>this</code>.
     * @since Classic
     */
    public ActionManager actionManager()
    {
        return actionManager;
    }

    /**
     * Returns the buffered <code>Graphics</code> context.
     * 
     * @return  the <code>Graphics</code> context of the offscreen <code>Image</code> used for double buffering
     * @since Classic
     */
    public static Graphics getGBuff()
    {
        return gBuff;
    }

    /**
     * Returns whether this computer is the first player.
     * This occurs in singleplayer or when hosting.
     * 
     * @return  whether the local computer is player #1
     * @since Classic
     */
    public static boolean isPlayerOne()
    {
        return localPlayer == 0;
    }
    
    /**
     * Sets a new high score name.
     * 
     * @param name  the name of the player who has the high score
     * @since Classic
     */
    public void setHighScore( String name )
    {
        highScoreName = name;
    }

    /**
     * Inserts comma seperators at each grouping, up to 10^34.
     * 
     * @param number    The number to be formatted.
     * @return  the formatted string.
     * @since December 15, 2007
     */
    public static String insertThousandCommas( int number )
    {
        return new DecimalFormat( "#,###,###,###,###,###,###,###,###,###,###,###" ).format( number );
    }

    /**
     * A simple handler for the frame's window buttons.
     * 
     * @since December 15, 2007
     */
    private static class CloseAdapter extends WindowAdapter
    {
        /**
         * Invoked when a window has been closed.
         * 
         * @param e see <code>WindowListener</code>
         */
        @Override
        public void windowClosing( WindowEvent e )
        {
            try
            {
                // Tell the other players that we're leaving.
                AsteroidsServer.send( "exit" );
                AsteroidsServer.dispose();
            }
            catch ( NullPointerException ex )
            {
            }
            Running.quit();
        }
    }

    /**
     * A small class for the storage of scoreboard colums.
     * 
     * @see <code>drawScoreboard</code>
     * @author Phillip Cohen
     * @since December 15, 2007
     */
    private static class ScoreboardColumn
    {
        /**
         * x coordinate of the column.
         */
        private int x;

        /**
         * Header text.
         */
        private String title;

        /**
         * Returns the column's header text.
         * 
         * @return  the text of the column's header
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * Returns the column's left edge.
         * 
         * @return  the x coordinate of the left edge
         */
        public int getX()
        {
            return x;
        }

        /**
         * Assigns this column's data.
         * 
         * @param x     the x coordinate of the left edge
         * @param title the text of the column's header
         */
        public ScoreboardColumn( int x, String title )
        {
            this.x = x;
            this.title = title;
        }
    }
    
    /**
     * The <code>Star</code> class is little more than an overblown
     * coordinate class and is used for storing the absolute locations of each Star.
     * 
     * @since December 16,2007
     */
    private static class Star
    {
        public int x, y;
        public Color color;
        public Star(int x, int y, Color col)
        {
            this.x=x;
            this.y=y;
            this.color=col;
        }
    }
    
    /**
     * 
     */
    private static class Message
    {
        public int x, y;
        public String message;
        public Color col;
        public int life=40;

        public Message(int x, int y, String message, Color col) {
            this.x = x;
            this.y = y;
            this.message = message;
            this.col = col;
        }
        
    }
    
}
