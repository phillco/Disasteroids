/*
 * DISASTEROIDS
 * AsteroidsFrame.java
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * The main, central class.
 * @author Andy Kooiman
 */
public class AsteroidsFrame extends Frame implements KeyListener
{
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 800;
    private static final Color[] PLAYER_COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };
    private static double highScore;
    private static String highScoreName;
    private static Image virtualMem;
    private static Image background;
    private static Graphics gBuff;
    private static int level;
    private boolean isFirst = true;
    private Graphics g;
    private boolean paused = false;
    public static long timeStep = 0; //for synchronization
    public static long otherPlayerTimeStep = 0;
    private AsteroidManager asteroidManager = new AsteroidManager();
    private ActionManager actionManager = new ActionManager();
    public static Ship[] players;
    private static int localPlayer;

    public AsteroidsFrame( int playerCount, int localPlayer )
    {
        // Close when the exit key is pressed.
        addWindowListener( new WindowAdapter()
                   {
                       @Override
                       public void windowClosing( WindowEvent e )
                       {
                           try
                           {
                               AsteroidsServer.send( "exit" );
                               AsteroidsServer.dispose();
                           }
                           catch ( NullPointerException ex )
                           {
                           }
                           Running.quit();
                       }
                   } );

        // Set our size - fullscreen or windowed.
        updateFullscreen();

        players = new Ship[playerCount];
        AsteroidsFrame.localPlayer = localPlayer;
    }

    public void init( Graphics g )
    {
        this.g = g;
        virtualMem = createImage( WINDOW_WIDTH, WINDOW_HEIGHT );
        gBuff = virtualMem.getGraphics();
        if ( isFirst )
        {
            this.addKeyListener( this );
            virtualMem = createImage( getWidth(), getHeight() );
            background = createImage( getWidth(), getHeight() );
            drawBackground();
            gBuff = virtualMem.getGraphics();

            highScore = 1000000;
            highScoreName = "Phillip and Andy";
            isFirst = false;
        }
        level = 1;

        // Create the ships.       
        for ( int i = 0; i < players.length; i++ )
            players[i] = new Ship( getWidth() / 2 - ( i * 100 ), getHeight() / 2, g, PLAYER_COLORS[i], 3, "Player " + ( i + 1 ) );

        asteroidManager.setUpAsteroidField( level );
    }

    @Override
    public void paint( Graphics g )
    {
        if ( paused )
            return;
        if ( isFirst )
            init( g );

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
        updateBackground();
        gBuff.drawImage( background, 0, 0, this );

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
        ParticleManager.drawParticles( gBuff );
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

        drawScore( gBuff );
        g.drawImage( virtualMem, 0, 0, this );

        if ( asteroidManager.size() == 0 && level > -1 )
            nextLevel();
        repaint();
    }

    public void updateBackground()
    {
        Graphics g = background.getGraphics();
        g.drawImage( background, 0, -2, this );
        Random rand = RandNumGen.getStarInstance();
        for ( int y = getHeight() - 3; y < getHeight(); y++ )
            for ( int x = 0; x < getWidth(); x++ )
            {
                if ( rand.nextInt( 1000 ) < 1 )
                    g.setColor( Color.white );
                else
                    g.setColor( Color.black );
                g.fillRect( x, y, 1, 1 );
            }
    }

    public void warpDialog()
    {
        warp( Integer.parseInt( JOptionPane.showInputDialog( "Enter the level number to warp to.", level ) ) );
    }

    /**
     * Advances to the next level.
     */
    public void nextLevel()
    {
        warp( level + 1 );
    }

    /**
     * Advances the game to a new level.
     */
    public void warp( int newLevel )
    {
        paused = true;
        level = newLevel;

        // All players get bonuses.
        for ( int i = 0; i < players.length; i++ )
        {
            players[i].addLife();
            players[i].setInvincibilityCount( 100 );
            players[i].increaseScore( 2500 );
            players[i].getMisileManager().clear();
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
        paused = false;
    }

    public static void staticNextLevel()
    {
        Running.environment().nextLevel();
    }

    private void drawScore( Graphics g )
    {
    // TODO: Fix and implement scoreboard.
            /*
    g.setColor( ship.getColor() );
    g.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
    if ( ship != null )
    g.drawString( "Lives: " + ship.livesLeft(), 20, 50 );
    g.drawString( "Level: " + level, 120, 50 );
    g.drawString( "Score: " + ship.score(), 200, 50 );
    if ( ship2 != null )
    {//player 2
    g.setColor( ship2.getColor() );
    g.drawString( "Lives:" + ship2.livesLeft(), 20, 65 );
    g.drawString( "Score:" + ship2.score(), 200, 65 );
    }
    g.setColor( Color.green );
    // [PC] Do not draw high score if we don't have one.
    if ( ( highScoreName != null ) && ( !highScoreName.equals( "" ) ) )
    g.drawString( "High Score is " + highScore + " (by " + highScoreName + ")", 350, 50 );
    g.drawString( "Asteroids: " + asteroidManager.size(), 700, 50 );
     */
    }

    private void drawBackground()
    {
        Graphics g = background.getGraphics();
        g.setColor( Color.black );
        g.fillRect( 0, 0, getWidth(), getHeight() );
        g.setColor( Color.white );
        Random rand = RandNumGen.getStarInstance();
        for ( int star = 0; star < getWidth() * getHeight() / 1000; star++ )
            g.fillRect( rand.nextInt( getWidth() ), rand.nextInt( getHeight() ), 1, 1 );
    }

    public void newGame()
    {
        init( g );
        if ( ( players.length > 1 ) && ( localPlayer == 0 ) )
            AsteroidsServer.send( "ng" );
    }

    public void endGame( Graphics g )
    {
        paused = true;
        g.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
        Sound.wheeeargh();
        for ( float sat = 0; sat <= 1; sat += .00005 )
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

        /* Easter egg!
        if ( oldHighScore > 10000000 )
        {
        JOptionPane.showMessageDialog( null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT" );
        try
        {
        Runtime.getRuntime().exec( "C:/Windows/System32/calc" );
        }
        catch ( Exception e )
        {
        }
        }
         */

        newGame();
        this.setIgnoreRepaint( false );
        paused = false;
        repaint();
    }

    public static Graphics getGBuff()
    {
        return gBuff;
    }

    public synchronized void keyReleased( KeyEvent e )
    {
        if ( !paused || e.getKeyCode() == 80 )
        {
            if ( e.getKeyCode() >= 37 && e.getKeyCode() <= 40 )
                // Get the raw code from the keyboard
                //performAction(e.getKeyCode(), ship);
                actionManager.add( new Action( players[localPlayer], 0 - e.getKeyCode(), timeStep + 2 ) );
            AsteroidsServer.send( "k" + String.valueOf( 0 - e.getKeyCode() ) + "," + String.valueOf( timeStep + 2 ) );
        // [AK] moved to a new method to also be called by another class, receiving data from other computer
        //repaint();
        }
        repaint();
    }

    public void keyTyped( KeyEvent e )
    {
    }

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

    public synchronized void performAction( int action, Ship actor )
    {
        /*============================
         * Decide what key was pressed
         *==========================*/
        switch ( action )
        {
            case KeyEvent.VK_ESCAPE:
                Running.quit();
            case KeyEvent.VK_SPACE:
                if ( actor.canShoot() )
                    actor.shoot( true );
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
                actor.getMisileManager().explodeAll();
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
            default:
                break;
        }
        repaint();
    }

    public void toggleMusic()
    {
        writeOnBackground( "Music " + ( Sound.toggleMusic() ? " on." : "off." ), getWidth() / 2 - 10, getHeight() / 2, Color.green );
    }

    public void toggleSound()
    {
        writeOnBackground( "Sound " + ( Sound.toggleSound() ? " on." : "off." ), getWidth() / 2 - 10, getHeight() / 2, Color.green );
    }

    /**
     * 
     */
    public void toggleFullscreen()
    {
        Settings.useFullscreen = !Settings.useFullscreen;
        updateFullscreen();
    }

    /**
     * Updates the frame if needs to be windowed or fullscreened.
     * @author Phillip Cohen
     */
    public void updateFullscreen()
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

    public void setOtherPlayerTimeStep( int step )
    {
        otherPlayerTimeStep = step;
    }

    @Override
    public void update( Graphics g )
    {
        paint( g );
    }

    private void restoreBonusValues()
    {
        for ( Ship ship : players )
        {
            ship.getMisileManager().setHugeBlastProb( 5 );
            ship.getMisileManager().setHugeBlastSize( 50 );
            ship.getMisileManager().setProbPop( 2000 );
            ship.getMisileManager().setPopQuantity( 5 );
            ship.getMisileManager().setSpeed( 10 );
            ship.setMaxShots( 10 );
        }
    }

    public static int getLevel()
    {
        return level;
    }

    public static void safeSleep( int milis )
    {
        try
        {
            Thread.sleep( milis );
        }
        catch ( InterruptedException e )
        {
        }
    }

    public void writeOnBackground( String message, int x, int y, Color col )
    {
        Graphics g = background.getGraphics();
        g.setColor( col );
        g.setFont( new Font( "Tahoma", Font.BOLD, 9 ) );
        g.drawString( message, x, y );
    }

    public ActionManager actionManager()
    {
        return actionManager;
    }

    public static boolean isPlayerOne()
    {
        return localPlayer == 0;
    }

    public void setHighScore( String name )
    {
        highScoreName = name;
    }
}
