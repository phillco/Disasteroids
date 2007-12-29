
import java.awt.Color;
import java.util.LinkedList;

public class Game extends Thread
{

    public enum Netstate
    {

        SINGLEPLAYER, SERVER, CLIENT;

    }
    
    static Netstate state;
    
    /**
     * Dimensions of the game, regardless of the graphical depiction
     * @since December 17, 2007
     */
    static final int GAME_WIDTH = 2000,  GAME_HEIGHT = 2000;

    /**
     * Default player colors. Inspired from AOE2.
     * @since December 14 2007
     */
    static final Color[] PLAYER_COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };

    /**
     * The current level of the game.
     * @since Classic
     */
    static int level = 1;

    /**
     * Stores whether the game is currently paused or not.
     * @since Classic
     */
    static boolean paused = false;

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
    static AsteroidManager asteroidManager = new AsteroidManager();

    /**
     * Stores the current pending <code>Action</code>s.
     * @since Classic
     */
    static ActionManager actionManager = new ActionManager();

    /**
     * Array of players.
     * @since December 14 2007
     */
    public static LinkedList<Ship> players = new LinkedList<Ship>();

    /**
     * How many times we <code>act</code> per paint call. Can be used to make later levels more playable.
     * @since December 23, 2007
     */
    static int gameSpeed = 1;

    static Game thread;

    public Game()
    {
        RandNumGen.init( 0 );
        resetEntireGame();
    }

    @Override
    public void run()
    {
        System.out.println( "Game loop started." );
        while ( true )
        {
            if ( !paused )
                Game.act();            
            Game.safeSleep( 15 );
        }
    }

    static void addPlayer( String name )
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
    static void resetEntireGame()
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
    static void nextLevel()
    {
        Game.warp( level + 1 );
    }

    /**
     * Returns if the game is ready to advance levels.
     * Checks if the <code>Asteroids</code> have been cleared, then if we're on the sandbox level, and finally if the <code>Missile</code>s have been cleared.
     * 
     * @see Settings#waitForMissiles
     * @return  whether the game should advance to the next level
     */
    public static boolean shouldExitLevel()
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
    static void restoreBonusValues()
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
    public static void setOtherPlayerTimeStep( int step )
    {
        otherPlayerTimeStep = step;
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
     * Returns the game's <code>ActionManager</code>.
     * 
     * @return  the <code>ActionManager</code> of <code>this</code>.
     * @since Classic
     */
    public static ActionManager actionManager()
    {
        return actionManager;
    }

    /**
     * Steps the game through one timestep.
     * 
     * @since December 21, 2007
     */
    static void act()
    {
        if ( players.size() < 1 )
            return;        

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
            System.out.println( "Action missed! " + e + "\nOur timestep: " + timeStep + ", their timestep: " + otherPlayerTimeStep + "." );
            Running.quit();
        }
        ParticleManager.act();
        asteroidManager.act();

        // Update the ships.
        for ( Ship s : players )
            s.act();        
    }

    public static boolean gameIsActive()
    {
        return ( Game.players.size() > 1 );
    }

    /**
     * Advances the game to a new level.
     * 
     * @param newLevel  the level to warp to
     * @since November 15 2007
     */
    static void warp( int newLevel )
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
        System.out.println( "Welcome to level " + newLevel + ". Random asteroid numbers: " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                            RandNumGen.getAsteroidInstance().nextInt( 9 ) + " (Seed: " + RandNumGen.seed + ")" );
        asteroidManager.setUpAsteroidField( level );
        AsteroidsFrame.addNotificationMessage( "Welcome to level " + newLevel + ".", 500 );
        paused = false;
    }
}
