/**
 * DISASTEROIDS
 * Game.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.KeystrokeManager;
import disasteroids.gui.MainMenu;
import disasteroids.gui.ParticleManager;
import disasteroids.networking.Client;
import disasteroids.networking.Constants;
import disasteroids.networking.Server;
import java.awt.Color;
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

/**
 * Central gameplay class that's separate from graphics.
 * @since December 17, 2007
 * @author Phillip Cohen
 */
public class Game implements Serializable
{
    /**
     * Dimensions of the game, regardless of the graphical depiction.
     * @since December 17, 2007
     */
    public final int GAME_WIDTH = 2000,  GAME_HEIGHT = 2000;

    /**
     * Stores whether the game is currently paused or not.
     * @since Classic
     */
    private boolean paused = true;

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
     * List of players.
     * @since December 14, 2007
     */
    public LinkedList<Ship> players = new LinkedList<Ship>();

    /**
     * List of miscellaneous game objects that aren't players or asteroids.
     * @since January 7, 2008
     */
    public ConcurrentLinkedQueue<GameObject> gameObjects;

    /**
     * List of all objects that shoot.
     * @since January 7, 2008
     */
    public ConcurrentLinkedQueue<ShootingObject> shootingObjects;

    /**
     * Reference list all of living objects on the level that the player must destroy (asteroids, aliens, stations, and so on).
     * @since March 31, 2008
     */
    public ConcurrentLinkedQueue<GameObject> baddies;

    /**
     * The game mode that we're playing.
     * @since February 28, 2008
     */
    private GameMode gameMode;

    /**
     * The thread that executes the game loop.
     * @since December 29, 2007
     */
    private transient GameLoop thread;

    /**
     * Reference to the Game instance. Game itself can't be static because of saving/restoring.
     * @since December 29, 2007
     */
    private static Game instance;

    /**
     * Creates the game.
     * @since December 29, 2007
     */
    public Game( Class gameMode )
    {

        Game.instance = this;
        timeStep = 0;
        otherPlayerTimeStep = 0;
        asteroidManager = new AsteroidManager();
        actionManager = new ActionManager();
        shootingObjects = new ConcurrentLinkedQueue<ShootingObject>();
        gameObjects = new ConcurrentLinkedQueue<GameObject>();
        baddies = new ConcurrentLinkedQueue<GameObject>();

        // Spawn players.
        for ( int index = 0; index < players.size(); index++ )
        {
            int id = players.get( index ).id;
            players.set( index, new Ship( players.get( index ).getX(), players.get( index ).getY(), players.get( index ).getColor(), Ship.START_LIVES, players.get( index ).getName() ) );
            players.get( index ).id = id;
            shootingObjects.add( players.get( index ) );
        }
        try
        {
            // Set the game mode.
            this.gameMode = (GameMode) gameMode.newInstance();
        }
        catch ( InstantiationException ex )
        {
            Running.fatalError( "Couldn't create game mode.", ex );
        }
        catch ( IllegalAccessException ex )
        {
            Running.fatalError( "Couldn't create game mode.", ex );
        }

        // Update the GUI.
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.frame().resetGame();
    }

    /**
     * Returns the <code>Ship</code> with the specified <code>id</code>.
     * 
     * @param id    the <code>id</code> to search for
     * @return      the <code>Ship</code> with that <code>id</code>, or <code>null</code>
     * @since December 30, 207
     */
    public Ship getPlayerFromId( int id )
    {
        for ( Ship s : players )
            if ( s.id == id )
                return s;
        return null;
    }

    /**
     * Saves the game to <code>Game.ser</code>.
     * 
     * @since December 29, 2007
     */
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

    /**
     * Loads the game from <code>Game.ser</code>.
     * 
     * @since December 29, 2007
     */
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
     * Returns the instance of the game.
     * 
     * @return  the currently active <code>Game</code>
     * @since December 29, 2007
     */
    public static Game getInstance()
    {
        return instance;
    }

    /**
     * Creates and adds a new player into the game.
     * 
     * @param name  the player's name
     * @return      the new player's id
     * @since December 29, 2007
     */
    public int addPlayer( String name, Color c )
    {
        Ship s = new Ship( GAME_WIDTH / 2 - ( players.size() * 100 ), GAME_HEIGHT / 2, c, Ship.START_LIVES, name );
        players.add( s );
        shootingObjects.add( s );
        Running.log( s.getName() + " entered the game (id " + s.id + ")." );
        return s.id;
    }

    /**
     * Adds a player into the game.
     * 
     * @param newPlayer     the player
     * @since December 31, 2007
     */
    public void addPlayer( Ship newPlayer )
    {
        players.add( newPlayer );
        shootingObjects.add( newPlayer );
        Running.log( newPlayer.getName() + " entered the game (id " + newPlayer.id + ").", 800 );
    }

    /**
     * Removes a player from the game.
     * 
     * @param leavingPlayer     the player
     * @since January 1, 2007
     */
    public void removePlayer( Ship leavingPlayer )
    {
        removePlayer( leavingPlayer, " left the game." );
    }

    /**
     * Removes an object from the game.
     * 
     * @param o the object
     * @since March 2, 2008
     */
    public void removeObject( GameObject o )
    {
        gameObjects.remove( o );
        shootingObjects.remove( o );
        baddies.remove( o );
    }

    /**
     * Removes a player from the game with a custom message.
     * "(player name)" + quitReason
     * 
     * @param leavingPlayer     the player
     * @param quitReason        the message
     * @since January 11, 2007
     */
    public void removePlayer( Ship leavingPlayer, String quitReason )
    {
        players.remove( leavingPlayer );
        shootingObjects.remove( leavingPlayer );

        if ( quitReason.length() > 0 )
            Running.log( leavingPlayer.getName() + quitReason, 800 );
    }

    /**
     * Undoes all changes that <code>BonusAsteroid</code>s may have caused.
     * 
     * @since Classic
     */
    void restoreBonusValues()
    {
        for ( Ship ship : players )
            ship.restoreBonusValues();
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
     * Returns the game's <code>ActionManager</code>.
     * 
     * @return  the <code>ActionManager</code>
     * @since Classic
     */
    public ActionManager actionManager()
    {
        return actionManager;
    }

    /**
     * Returns the game's <code>AsteroidManager</code>
     * 
     * @return  the <code>AsteroidManager</code>
     * @since January 11, 2008
     */
    public AsteroidManager asteroidManager()
    {
        return asteroidManager;
    }

    /**
     * Steps the game through one timestep.
     * 
     * @since December 21, 2007
     */
    void act()
    {
        // Update the game mode.
        gameMode.act();

        if ( AsteroidsFrame.frame().getPanel().getStarBackground() != null )
            AsteroidsFrame.frame().getPanel().getStarBackground().act();

        // Execute game actions.
        timeStep++;
        actionManager.act( timeStep );
        ParticleManager.act();
        asteroidManager.act();

        // Update the ships.
        for ( Ship s : players )
        {
            s.act();
            if ( s.livesLeft() < 0 && s.getExplosionTime() < 0 && !Client.is() )
            {
                // Return to the menu.
                //  Game.instance = null;
                AsteroidsFrame.frame().setVisible( false );
                paused = true;
                new MainMenu();

                return;
            }
        }

        for ( GameObject g : gameObjects )
            g.act();
    }

    public boolean gameIsActive()
    {
        return ( Game.getInstance().players.size() > 1 );
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
     * @param keystroke    the key code for the action
     * @param actor     the <code>Ship</code> to execute the action
     * @since Classic
     */
    public static void performAction( int keystroke, Ship actor )
    {
        // Ignore actions about players that have left the game.
        if ( actor == null || !getInstance().players.contains( actor ) )
            return;

        KeystrokeManager.ActionType action = KeystrokeManager.getInstance().translate( keystroke );
        // Decide what key was pressed.
        switch ( action )
        {
            case START_SHOOT: // 5 on numpad w/o numlock
                actor.setShooting( true );
                break;
            case STOP_SHOOT:
                actor.setShooting( false );
                break;
            case LEFT:
                actor.setLeft( true );
                break;
            case RIGHT:
                actor.setRight( true );
                break;
            case FORWARDS:
                actor.setForward( true );
                break;
            case BACKWARDS:
                actor.setBackwards( true );
                break;
            case UN_LEFT:
                actor.setLeft( false );
                break;
            case UN_RIGHT:
                actor.setRight( false );
                break;
            case UN_FORWARDS:
                actor.setForward( false );
                break;
            case UN_BACKWARDS:
                actor.setBackwards( false );
                break;

            // Special keys.
            case BRAKE:
                actor.setBrake( true );
                break;
            case UN_BRAKE:
                actor.setBrake( false );
                break;
            case BERSERK:
                actor.berserk();
                break;
            case STRAFE_RIGHT:
                actor.strafe( true );
                break;
            case STRAFE_LEFT:
                actor.strafe( false );
                break;
            case EXPLODE_ALL:
                actor.getWeaponManager().explodeAll();
                break;
            case ROTATE_WEAPONS:
                actor.rotateWeapons();
                break;
            case SET_WEAPON_1:
                actor.setWeapon( 0 );
                break;
            case SET_WEAPON_2:
                actor.setWeapon( 1 );
                break;
            case SET_WEAPON_3:
                actor.setWeapon( 2 );
                break;
            case SET_WEAPON_4:
                actor.setWeapon( 3 );
                break;
            case SET_WEAPON_5:
                actor.setWeapon( 4 );
                break;
            case SET_WEAPON_6:
                actor.setWeapon( 5 );
                break;
            case SET_WEAPON_7:
                actor.setWeapon( 6 );
                break;
            case SET_WEAPON_8:
                actor.setWeapon( 7 );
                break;
            case SET_WEAPON_9:
                actor.setWeapon( 8 );
                break;
            case PAUSE:

                if ( !Client.is() )
                    Game.getInstance().setPaused( !Game.getInstance().isPaused() );
                break;
            // Saving & loading
            case SAVE:
                if ( !Client.is() )
                    Game.saveToFile();
                break;

            case LOAD:
                if ( !Client.is() )
                    Game.loadFromFile();
                break;

            default:
                break;
        }

        if ( Server.is() )
            Server.getInstance().updatePlayerPosition( actor );
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
        stream.writeInt( Constants.parseGameMode( gameMode ) );
        gameMode.flatten( stream );
        stream.writeLong( timeStep );

        stream.writeInt( players.size() );
        for ( Ship s : players )
            s.flatten( stream );

        stream.writeInt( gameObjects.size() );
        for ( GameObject o : gameObjects )
        {
            stream.writeInt( Constants.parseGameObject( o ) );
            o.flatten( stream );
        }

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

        // Create the game mode.
        gameMode = Constants.parseGameMode( stream.readInt(), stream );
        this.timeStep = stream.readLong();

        players = new LinkedList<Ship>();
        int size = stream.readInt();
        for ( int i = 0; i < size; i++ )
            players.add( new Ship( stream ) );

        shootingObjects = new ConcurrentLinkedQueue<ShootingObject>();
        baddies = new ConcurrentLinkedQueue<GameObject>();
        gameObjects = new ConcurrentLinkedQueue<GameObject>();
        size = stream.readInt();
        for ( int i = 0; i < size; i++ )
        {
            switch ( Constants.GameObjectTIDs.values()[stream.readInt()] )
            {
                case ALIEN:
                    Alien a = new Alien( stream );
                    gameObjects.add( a );
                    shootingObjects.add( a );
                    baddies.add( a );
                    break;
                case BONUS:
                    Bonus b = new Bonus( stream );
                    gameObjects.add( b );
                    break;
                case STATION:
                    Station t = new Station( stream );
                    gameObjects.add( t );
                    shootingObjects.add( t );
                    baddies.add( t );
                    break;

            }
        }
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
            Running.fatalError( "Couldn't set pause status in server.", ex );
        }
    }

    public GameMode getGameMode()
    {
        return gameMode;
    }

    public void createBonus( GameObject parent )
    {
        gameObjects.add( new Bonus( parent.getX(), parent.getY() ) );
    }

    public ConcurrentLinkedQueue<GameObject> getBaddies()
    {
        return baddies;
    }
}
