
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Game implements Serializable
{
    public enum Netstate
    {
        SINGLEPLAYER, SERVER, CLIENT;

    }
    Netstate state;

    /**
     * Dimensions of the game, regardless of the graphical depiction
     * @since December 17, 2007
     */
    final int GAME_WIDTH = 2000,  GAME_HEIGHT = 2000;

    /**
     * Default player colors. Inspired from AOE2.
     * @since December 14 2007
     */
    final Color[] PLAYER_COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };

    /**
     * The current level of the Game.getInstance().
     * @since Classic
     */
    int level = 1;

    /**
     * Stores whether the game is currently paused or not.
     * @since Classic
     */
    boolean paused = false;

    /**
     * The current game time.
     * @since Classic
     */
    public long timeStep = 0;

    /**
     * The current game time of the other player.
     * @since Classic
     */
    public long otherPlayerTimeStep = 0;

    /**
     * Stores the current <code>Asteroid</code> field.
     * @since Classic
     */
    AsteroidManager asteroidManager = new AsteroidManager();

    /**
     * Stores the current pending <code>Action</code>s.
     * @since Classic
     */
    ActionManager actionManager = new ActionManager();

    /**
     * Array of players.
     * @since December 14 2007
     */
    public LinkedList<Ship> players = new LinkedList<Ship>();

    /**
     * How many times we <code>act</code> per paint call. Can be used to make later levels more playable.
     * @since December 23, 2007
     */
    int gameSpeed = 1;

    private transient GameLoop thread;

    private static Game instance;

    public static Game getInstance()
    {
        return instance;
    }

    public Game()
    {
        Game.instance = this;
        resetEntireGame();
    }

    public static void saveToFile()
    {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream( "Game.ser" );
            out = new ObjectOutputStream( fos );
            out.writeObject( instance );
            out.close();
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }

        Running.log( "Game saved." );
    }

    public void loadFromFile()
    {
        FileInputStream fis = null;
        {
            try
            {
                fis = new FileInputStream( "Game.ser" );
                ObjectInputStream in = new ObjectInputStream( fis );
                instance = (Game) in.readObject();
                in.close();
                Running.log( "Game restored." );
            }
            catch ( IOException ex )
            {
                ex.printStackTrace();
            }
            catch ( ClassNotFoundException ex )
            {
                ex.printStackTrace();
            }
        }
    }

    void addPlayer( String name )
    {
        Ship s = new Ship( GAME_WIDTH / 2 - ( players.size() * 100 ), GAME_HEIGHT / 2, PLAYER_COLORS[players.size()], 1, name );
        players.add( s );
        Running.log( s.getName() + " entered the game." );
    }

    /**
     * Resets all gameplay elements. Ships, asteroids, timesteps, missiles and actions are all reset.
     * Connection elements like the number of players or the index of <code>localPlayer</code> are not reset.
     * 
     * @since December 16, 2007
     */
    void resetEntireGame()
    {
        timeStep = 0;
        otherPlayerTimeStep = 0;

        actionManager = new ActionManager();
        for ( Ship s : players )
            s = new Ship( s.getX(), s.getY(), s.getColor(), 0, s.getName() );

        // Create the asteroids.
        level = 1;
        asteroidManager = new AsteroidManager();
        asteroidManager.setUpAsteroidField( level );

        // Update the GUI.
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.frame().resetGame();
    }

    /**
     * Advances to the next level.
     * @author Phillip Cohen
     * @since November 15 2007
     */
    void nextLevel()
    {
        Game.getInstance().warp( level + 1 );
    }

    /**
     * Returns if the game is ready to advance levels.
     * Checks if the <code>Asteroids</code> have been cleared, then if we're on the sandbox level, and finally if the <code>Missile</code>s have been cleared.
     * 
     * @see Settings#waitForMissiles
     * @return  whether the game should advance to the next level
     */
    public boolean shouldExitLevel()
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
     * Undoes all changes that <code>BonusAsteroid</code>s may have caused.
     * 
     * @since Classic
     */
    void restoreBonusValues()
    {
        for ( Ship ship : players )
        {
            ship.restoreBonusValues();
        }
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
     * Sleeps the game for the specified time.
     * 
     * @param millis How many milliseconds to sleep for.
     * @since Classic
     */
    public void safeSleep( int millis )
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
     * Returns the current level.
     * 
     * @return  the current level
     * @since Classic
     */
    public int getLevel()
    {
        return level;
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
     * Steps the game through one timestep.
     * 
     * @since December 21, 2007
     */
    void act()
    {

        // Advance to the next level if it's time.
        if ( shouldExitLevel() )
        {
            nextLevel();
            return;
        }

        AsteroidsServer.send( "t" + String.valueOf( timeStep ) );

        // Are we are too far ahead?
        if ( ( timeStep - otherPlayerTimeStep > 2 ) && ( players.size() > 1 ) )
        {
            safeSleep( 20 );
            AsteroidsServer.send( "t" + String.valueOf( timeStep ) );
            AsteroidsServer.flush();
            return;
        }

        // Execute game actions.
        timeStep++;
        try
        {
            actionManager.act( timeStep );
        }
        catch ( UnsynchronizedException e )
        {
        }
        ParticleManager.act();
        asteroidManager.act();

        // Update the ships.
        for ( Ship s : players )
            s.act();
    }

    public boolean gameIsActive()
    {
        return ( Game.getInstance().players.size() > 1 );
    }

    /**
     * Advances the game to a new level.
     * 
     * @param newLevel  the level to warp to
     * @since November 15 2007
     */
    void warp( int newLevel )
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

        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.frame().nextLevel();
        restoreBonusValues();
        asteroidManager.setUpAsteroidField( level );
        AsteroidsFrame.addNotificationMessage( "Welcome to level " + newLevel + ".", 500 );
        paused = false;
    }

    public void startGame()
    {
        thread = new GameLoop();
        thread.setPriority( Thread.MAX_PRIORITY );
        thread.start();
    }

    /**
     * Performs the action specified by the action as applied to the actor.
     * 
     * @param action    the key code for the action
     * @param actor     the <code>Ship</code> to execute the action
     * @since Classic
     */
    public static void performAction( int action, Ship actor )
    {
        System.out.println( "Performing " + action + " to " + actor + "." );
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
            case KeyEvent.VK_Q:
                actor.rotateWeapons();
                break;

            case KeyEvent.VK_P:
                if ( Game.getInstance().state == Game.Netstate.SINGLEPLAYER )
                    Game.getInstance().paused = !Game.getInstance().paused;
                break;


            /*
            case KeyEvent.VK_EQUALS:
            case KeyEvent.VK_PLUS:
            Game.getInstance().gameSpeed++;
            AsteroidsFrame.addNotificationMessage( "Game speed increased to " + Game.getInstance().gameSpeed + "." );
            break;
            case KeyEvent.VK_MINUS:
            if ( Game.getInstance().gameSpeed > 1 )
            {
            Game.getInstance().gameSpeed--;
            AsteroidsFrame.addNotificationMessage( "Game speed decreased to " + Game.getInstance().gameSpeed + "." );
            }
            break;
             */

            // Saving & loading
            case KeyEvent.VK_T:
                if ( Game.getInstance().state == Game.Netstate.SINGLEPLAYER )
                    Game.getInstance().saveToFile();
                break;

            case KeyEvent.VK_Y:
                if ( Game.getInstance().state == Game.Netstate.SINGLEPLAYER )
                    Game.getInstance().loadFromFile();
                break;

            default:
                break;
        }
    }
}
