
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game implements Serializable
{
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
    private boolean paused = false;

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
    
    public ConcurrentLinkedQueue<GameObject> gameObjects;
    
    public LinkedList<ShootingObject> shootingObjects;
    
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

    /**
     * Returns the <code>Ship</code> with the specified <code>id</code>.
     * 
     * @param id    the <code>id</code> to search for
     * @return      the <code>Ship</code> with that <code>id</code>, or <code>null</code>
     * @since December 30, 207
     */
    public Ship getFromId( int id )
    {
        for ( Ship s : players )
            if ( s.id == id )
                return s;
        return null;
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

    public static void loadFromFile()
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

    /**
     * Creates and adds a new player into the game.
     * 
     * @param name  the player's name
     * @return      the new player's id
     * @since December ?, 2007
     */
    int addPlayer( String name )
    {
        Ship s = new Ship( GAME_WIDTH / 2 - ( players.size() * 100 ), GAME_HEIGHT / 2, PLAYER_COLORS[players.size()], 4, name );
        players.add( s );
        shootingObjects.add(s);
        Running.log( s.getName() + " entered the game (id " + s.id + ")." );
        return s.id;
    }

    /**
     * Adds a player into the game.
     * 
     * @param newPlayer     the player
     * @since December 31, 2007
     */
    void addPlayer( Ship newPlayer )
    {
        players.add( newPlayer );
        shootingObjects.add(newPlayer);
        Running.log( newPlayer.getName() + " entered the game (id " + newPlayer.id + ").", 800 );
        
    }
    
    /**
     * Removes a player from the game.
     * 
     * @param leavingPlayer     the player
     * @since January 1, 2007
     */
    void removePlayer( Ship leavingPlayer )
    {
        players.remove(leavingPlayer);
        shootingObjects.remove(leavingPlayer);
        Running.log( leavingPlayer.getName() + " left the game.", 800);
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
        shootingObjects = new LinkedList<ShootingObject>();
        
        for ( int index = 0; index < players.size(); index++ )
        {
            players.set( index, new Ship( players.get( index ).getX(), players.get( index ).getY(), players.get( index ).getColor(), 1, players.get( index ).getName() ) );
            shootingObjects.add(players.get(index));
        }

        // Create the asteroids.
        level = 1;
        asteroidManager = new AsteroidManager();
        asteroidManager.setUpAsteroidField( level );
        
        gameObjects = new ConcurrentLinkedQueue<GameObject>();
        
        Station s = new Station(100, 1500);
        gameObjects.add(s);
        shootingObjects.add(s);

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
        {
            s.act();
            if ( s.livesLeft() < 0 )
            {
                AsteroidsFrame.frame().endGame();
                break;
            }
        }
        
        for( GameObject g : gameObjects )
        {
            g.act();
        }
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
        // Decide what key was pressed.
        switch ( action )
        {      
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
                if ( !Client.is() )
                    Game.getInstance().setPaused( !Game.getInstance().isPaused() );
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
                if ( !Client.is() )
                    Game.saveToFile();
                break;

            case KeyEvent.VK_Y:
                if ( !Client.is() )
                    Game.loadFromFile();
                break;

            default:
                break;
        }
        
        if(Server.is())
            Server.getInstance().updatePlayerPosition(actor);
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
        stream.writeInt( level );
        stream.writeLong( timeStep );

        stream.writeInt( players.size() );
        for ( Ship s : players )
            s.flatten( stream );

        asteroidManager.flatten( stream );
        actionManager.flatten( stream );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 30, 2007
     */
    public Game( DataInputStream stream ) throws IOException
    {
        instance = this;

        this.level = stream.readInt();
        this.timeStep = stream.readLong();

        players = new LinkedList<Ship>();
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            players.add( new Ship( stream ) );

        asteroidManager = new AsteroidManager( stream );
        actionManager = new ActionManager( stream );
    }

    /**
     * Whether the game is currently paused.
     * 
     * @return  whether the game is paused
     * @since December 31, 2007
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Pauses or unpauses the game.
     * 
     * @param paused    whether the game should be paused
     * @since December 31, 2007
     */
    public void setPaused( boolean paused )
    {
        try
        {
            this.paused = paused;
            Running.log( "Game " + ( paused ? "paused" : "unpaused" ) + "." );
            if ( Server.is() )
                Server.getInstance().updatePause( paused );
        }
        catch ( IOException ex )
        {
            Running.fatalError("Couldn't set pause status in server.", ex);
        }
    }
}
