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
    private static double highScore;
    private static String highScoreName;
    private static Ship ship,  ship2;
    private static Image virtualMem;
    private static Image background;
    private static Graphics gBuff;
    private static int level;
    private boolean isFirst = true;
    private Graphics g;
    private boolean paused = false;
    public static long timeStep = 0; //for synchronization
    public static long otherPlayerTimeStep = 0;
    public static boolean isPlayerOne;
    public static boolean isMultiplayer;
    private AsteroidManager asteroidManager = new AsteroidManager();
    private ActionManager actionManager = new ActionManager();

    public AsteroidsFrame( boolean isPlayer1, boolean isMult )
    {
        isPlayerOne = isPlayer1;
        isMultiplayer = isMult;

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
        ship = new Ship( getWidth() / 2, getHeight() / 2, gBuff, Color.red, 5 );
        if ( isMultiplayer )
            ship2 = new Ship( getWidth() / 2 - 100, getHeight() / 2, gBuff, Color.blue, 5 );
        if ( !isPlayerOne )
        {
            Ship temp = ship2;
            ship2 = ship;
            ship = temp;
        }
        asteroidManager.setUpAsteroidField( level, gBuff );
    }

    public void paint( Graphics g )
    {
        if ( paused )
            return;
        if ( isFirst )
            init( g );

        //	if(timeStep%9==0)
        AsteroidsServer.send( "t" + String.valueOf( timeStep ) );

        if ( timeStep - otherPlayerTimeStep > 2 && ship2 != null )//you are too far ahead
        {
            safeSleep( 20 );
//			AsteroidsServer.send("PING");//encourage the creation of packets to be sent
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
            JOptionPane.showMessageDialog( null, "A fatal error has occured:\n" + e );
            Running.quit();
        }
        ParticleManager.drawParticles( gBuff );
        asteroidManager.act();
        ship.act();
        if ( ship2 != null )
            ship2.act();

        drawScore( gBuff );

        g.drawImage( virtualMem, 0, 0, this );
        if ( ship.livesLeft() < 0 && ( ship2 == null || ship2.livesLeft() < 0 ) )
            endGame( g );
        if ( asteroidManager.size() == 0 && level>-1)
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
        ship.addLife();
        ship.setInvincibilityCount( 100 );
        ship.increaseScore( 2500 );
        ship.getMisileManager().clear();
        if ( ship2 != null )
        {
            ship2.addLife();
            ship2.setInvincibilityCount( 100 );
            ship2.increaseScore( 2500 );
            ship2.getMisileManager().clear();
        }

        asteroidManager.clear();
        actionManager.clear();

        drawBackground();
        restoreBonusValues();
        System.out.println( "Seed: " + RandNumGen.seed + "\nAsteroid generated numbers:\n" +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + "\n" +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + "\n" +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + "\n" +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + "\n" +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + "\n" );
        asteroidManager.setUpAsteroidField( level, gBuff );
        paused = false;
    }

    public static void staticNextLevel()
    {
        Running.environment().nextLevel();
    }

    private void drawScore( Graphics g )
    {
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
        if ( isMultiplayer && isPlayerOne )
            AsteroidsServer.send( "ng" );
    //	repaint();
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
//		JOptionPane.showMessageDialog(null,"You died with a score of "+score);
        if ( ship2 != null )
            if ( ship.score() > ship2.score() )
                JOptionPane.showMessageDialog( null, "Player One Wins" );
            else
                JOptionPane.showMessageDialog( null, "Player Two Wins" );
        if ( ship.score() > highScore || ( ship2 != null && ship2.score() > highScore ) )
        {
            JOptionPane.showMessageDialog( null, "NEW HIGH SCORE!!!" );
            if ( ship2 == null || ship.score() > ship2.score() )
            {
                highScoreName = JOptionPane.showInputDialog( null, "Input name here:" );
                AsteroidsServer.send( "HS" + highScoreName );
            }
            if ( ship2 != null )
                highScore = Math.max( ship.score(), ship2.score() );
            else
                highScore = ship.score();
        //if(oldHighScore>10000000)
        //{
        //	JOptionPane.showMessageDialog(null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT");
        //	try{Runtime.getRuntime().exec("C:/Windows/System32/calc");}catch(Exception e){}
        //}
        }
        newGame();
        this.setIgnoreRepaint( false );
        paused = false;
        repaint();
    }

    public static Ship getShip()
    {
        return ship;
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
                actionManager.add( new Action( ship, 0 - e.getKeyCode(), timeStep + 2 ) );
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
            actionManager.add( new Action( ship, e.getKeyCode(), timeStep+2) );
            AsteroidsServer.send( "k" + String.valueOf( e.getKeyCode() ) + "," + String.valueOf( timeStep+2) );
        // [AK] moved to a new method to also be called by another class, receiving data from other computer
        //repaint();
        }
        repaint();
    }

    public synchronized void performAction( int action, Ship actor )
    {
        // Network start ket
//		if(action == 123) {
//			Net.startNetworkPrompt();
//		}

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
        boolean wasVisible = isVisible();

        dispose();
        if ( wasVisible )
            setVisible( false );

        setUndecorated( false );
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Set fullscreen mode, if supported.
        if ( ( graphicsDevice.isFullScreenSupported() ) && ( Settings.useFullscreen ) )
        {
            // Don't change anything if we're already in fullscreen mode.
            if ( graphicsDevice.getFullScreenWindow() != this )
            {
                setUndecorated( true );
                setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
                graphicsDevice.setFullScreenWindow( this );

                // Hide the cursor.
                Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
                Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
                setCursor( blankCursor );
            }
        }
        // Set windowed mode.
        else
        {
            // Don't change anything if we're already in windowed mode.
            if ( ( getSize().width != WINDOW_WIDTH ) || ( getSize().height != WINDOW_HEIGHT ) )
            {
                setSize( WINDOW_WIDTH, WINDOW_HEIGHT );

                // Show the cursor.
                setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
            }
        }

        if ( wasVisible )
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
        ship.getMisileManager().setHugeBlastProb( 5 );
        ship.getMisileManager().setHugeBlastSize( 50 );
        ship.getMisileManager().setProbPop( 2000 );
        ship.getMisileManager().setPopQuantity( 5 );
        ship.getMisileManager().setSpeed( 10 );
        ship.setMaxShots( 10 );

        if ( ship2 != null )
        {
            ship2.getMisileManager().setHugeBlastProb( 5 );
            ship2.getMisileManager().setHugeBlastSize( 50 );
            ship2.getMisileManager().setProbPop( 2000 );
            ship2.getMisileManager().setPopQuantity( 5 );
            ship2.getMisileManager().setSpeed( 10 );
            ship2.setMaxShots( 10 );
        }
    }

    public static int getLevel()
    {
        return level;
    }

    public static Ship getShip2()
    {
        return ship2;
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
        return isPlayerOne;
    }

    public void setHighScore( String name )
    {
        highScoreName = name;
    }
}
