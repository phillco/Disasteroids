/**
 * DISASTEROIDS
 * Game.java
 */
package disasteroids.game;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import disasteroids.Main;
import disasteroids.game.levels.Level;
import disasteroids.game.modes.GameMode;
import disasteroids.game.objects.Alien;
import disasteroids.game.objects.BlackHole;
import disasteroids.game.objects.Bonus;
import disasteroids.game.objects.GameObject;
import disasteroids.game.objects.Ship;
import disasteroids.game.objects.Station;
import disasteroids.game.weapons.Weapon;
import disasteroids.gui.KeystrokeManager;
import disasteroids.gui.Local;
import disasteroids.gui.MainMenu;
import disasteroids.gui.MainWindow;
import disasteroids.gui.ParticleManager;
import disasteroids.networking.Client;
import disasteroids.networking.Constants;
import disasteroids.networking.Server;
import disasteroids.networking.ServerCommands;

/**
 * Central gameplay class that's separate from graphics.
 * @since December 17, 2007
 * @author Phillip Cohen
 */
public class Game
{
	/**
	 * Dimensions of the game, regardless of the graphical depiction.
	 */
	public final int GAME_WIDTH = 2000, GAME_HEIGHT = 2000;

	/**
	 * Stores whether the game is currently paused or not.
	 */
	private boolean paused = true;

	/**
	 * The current game time.
	 */
	public long timeStep = 0;

	/**
	 * Stores the current pending <code>Action</code>s.
	 */
	ActionManager actionManager = new ActionManager();

	ObjectManager objectManager;

	private GameMode gameMode;

	/**
	 * The level that we're playing.
	 */
	private Level level;

	/**
	 * Reference to the currently running Game instance.
	 */
	private static Game instance;

	/**
	 * Creates the game.
	 */
	public Game( Class level, Class gameMode )
	{
		Game.instance = this;
		timeStep = 0;

		actionManager = new ActionManager();
		objectManager = new ObjectManager();

		try
		{
			// Set the level and game mode.
			this.level = (Level) level.newInstance();
			this.gameMode = (GameMode) gameMode.newInstance();
		}
		catch ( InstantiationException ex )
		{
			Main.fatalError( "Couldn't create level.", ex );
		}
		catch ( IllegalAccessException ex )
		{
			Main.fatalError( "Couldn't create level.", ex );
		}

		// Update the GUI.
		if ( MainWindow.frame() != null )
			MainWindow.frame().resetGame();
	}

	/**
	 * Saves the game to <code>res\Game.save</code>.
	 */
	public static void saveToFile()
	{
		GameLoop.stopLoop();
		while ( GameLoop.isRunning() )
			try
			{
				Thread.sleep( 10 );
			}
			catch ( InterruptedException ex )
			{
			}

		try
		{
			FileOutputStream fos = new FileOutputStream( "res\\Game.save" );
			DataOutputStream stream = new DataOutputStream( fos );

			instance.flatten( stream );

			stream.writeLong( Local.getLocalPlayer().getId() );
			stream.flush();
			fos.close();
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
		}

		Main.log( "Game saved!" );
		GameLoop.startLoop();
	}

	/**
	 * Loads the game from <code>res\Game.save</code> and returns the local player ID.
	 */
	public static long loadFromFile()
	{
		File file = new File( "res\\Game.save" );
		if ( !file.exists() )
		{
			Main.log( "There's no saved game to restore!" );
			return Local.getLocalPlayer().getId();
		}
		GameLoop.stopLoop();

		// Wait for the frame to stop drawing so we don't get null pointers.
		if ( MainWindow.frame() != null )
		{
			while ( MainWindow.frame().getPanel().isDrawing() )
				try
				{
					Thread.sleep( 3 );
				}
				catch ( InterruptedException ex )
				{
					Main.fatalError( "Rudely awaken in Game.loadFromFile().", ex );
				}
		}

		int id = -1;
		try
		{
			FileInputStream fis = new FileInputStream( file );
			DataInputStream stream = new DataInputStream( fis );

			instance = new Game( stream );
			id = stream.readInt();

			fis.close();
			Main.log( "Game restored!" );
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
		}

		// [PC] This is required, possibly because we have to go back and do the timestep that was done after saving.
		instance.timeStep -= 1;
		return id;
	}

	/**
	 * Returns the instance of the game.
	 * 
	 * @return the currently active <code>Game</code>
	 * @since December 29, 2007
	 */
	public static Game getInstance()
	{
		return instance;
	}

	/**
	 * Creates and adds a new player into the game.
	 * 
	 * @param name the player's name
	 * @param c the player's color
	 * @return the new player's id
	 */
	public long addPlayer( String name, Color c )
	{
		Ship s = new Ship( GAME_WIDTH / 2 - ( objectManager.getPlayers().size() * 100 ), GAME_HEIGHT / 2, c, Ship.START_LIVES, name );
		addPlayer( s );
		return s.getId();
	}

	/**
	 * Adds a player into the game.
	 */
	public void addPlayer( Ship newPlayer )
	{
		objectManager.addObject( newPlayer, false );
		Main.log( newPlayer.getName() + " entered the game.", 800 );
	}

	/**
	 * Removes a player from the game.
	 * 
	 * @param leavingPlayer the player
	 * @since January 1, 2007
	 */
	public void removePlayer( Ship leavingPlayer )
	{
		removePlayer( leavingPlayer, " left the game." );
	}

	/**
	 * Removes a player from the game with a custom message.
	 * "(player name)" + quitReason
	 * 
	 * @param leavingPlayer the player
	 * @param quitReason the message
	 * @since January 11, 2007
	 */
	public void removePlayer( Ship leavingPlayer, String quitReason )
	{
		objectManager.removeObject( leavingPlayer );
		if ( quitReason.length() > 0 )
			Main.log( leavingPlayer.getName() + quitReason, 800 );
	}

	/**
	 * Returns the game's <code>ActionManager</code>.
	 */
	public ActionManager getActionManager()
	{
		return actionManager;
	}

	/**
	 * Steps the game through one timestep.
	 */
	void act()
	{
		// Update the level.
		level.act();

		if ( !Local.isStuffNull() && MainWindow.frame().getPanel().getStarBackground() != null )
			MainWindow.frame().getPanel().getStarBackground().act();

		// Execute game actions.
		timeStep++;
		actionManager.act( timeStep );
		ParticleManager.act();

		// Check if we've lost in singleplayer.
		if ( !Client.is() && !Local.isStuffNull() && Local.getLocalPlayer().livesLeft() < 0 && Local.getLocalPlayer().getExplosionTime() < 0 )
		{
			Local.dispose();
			new MainMenu();
			return;
		}

		objectManager.act();
	}

	public static void dispose()
	{
		instance = null;
	}

	/**
	 * Performs the action specified by the action as applied to the actor.
	 * 
	 * @param keystroke the key code for the action
	 * @param actor the <code>Ship</code> to execute the action
	 */
	public void performAction( int keystroke, Ship actor )
	{
		// Ignore actions about players that have left the game.
		if ( actor == null || !getObjectManager().contains( actor.getId() ) )
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
				actor.getActiveWeapon().explodeAllUnits();
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
					Game.getInstance().setPaused( !Game.getInstance().isPaused(), true );
				break;
			// Saving & loading
			case SAVE:
				if ( !Client.is() )
				{
					instance.getActionManager().removeAll( KeystrokeManager.ActionType.SAVE );
					Game.saveToFile();
				}
				break;

			case LOAD:
				if ( !Client.is() && !Server.is() )
				{
					Local.loadGame();
					Game.getInstance().setPaused( false, false );
					GameLoop.startLoop();
				}
				break;
			case DEVKEY_SPAWN_BONUS:
				if ( canUseDevKey( true ) )
					objectManager.addObject( new Bonus( Local.getLocalPlayer().getX(), Local.getLocalPlayer().getY() - 50 ), false );
				break;
			case DEVKEY_SPAWN_STATION:
				if ( canUseDevKey( false ) )
					objectManager.addObject( new Station( Local.getLocalPlayer().getX(), Local.getLocalPlayer().getY() - 250, 0, 0 ), false );
				break;
			case DEVKEY_SPAWN_ALIEN:
				if ( canUseDevKey( true ) )
					objectManager.addObject( new Alien( Local.getLocalPlayer().getX(), Local.getLocalPlayer().getY() - 250, 0, 0 ), false );
				break;
			case DEVKEY_SPAWN_BLACKHOLE:
				if ( canUseDevKey( true ) )
					objectManager.addObject( new BlackHole( Local.getLocalPlayer().getX(), Local.getLocalPlayer().getY() - 250 ), false );
				break;
			case DEVKEY_INFINITE_AMMO:
				if ( canUseDevKey( false ) )
				{
					for ( Weapon w : Local.getLocalPlayer().getWeapons() )
						w.setInfiniteAmmo();
					Main.log( "Cheat: infinite ammo for all weapons!" );
				}
				break;
			case DEVKEY_SHIELD:
				if ( canUseDevKey( false ) )
					Local.getLocalPlayer().giveShield( 20 );
				break;
			case DEVKEY_DEBUG:
				getObjectManager().printDebugInfo();
				break;
			default:
				break;
		}

		if ( Server.is() )
			ServerCommands.updateObjectVelocity( actor );
	}

	private boolean canUseDevKey( boolean synchedAction )
	{
		if ( synchedAction )
			return ( !Client.is() );
		else
			return ( !Client.is() && ( !Server.is() || Game.getInstance().getObjectManager().getPlayers().size() < 2 ) ); // Action
		// not
		// synched,
		// so
		// it
		// can
		// only
		// be
		// done
		// when
		// alone.
	}

	/**
	 * Writes <code>this</code> to a stream for client/server transmission.
	 */
	public void flatten( DataOutputStream stream ) throws IOException
	{
		stream.writeInt( Constants.parseLevel( level ) );
		level.flatten( stream );
		stream.writeLong( timeStep );
		objectManager.flatten( stream );
		actionManager.flatten( stream );
		stream.writeBoolean( paused );
	}

	/**
	 * Creates <code>this</code> from a stream for client/server transmission.
	 */
	public Game( DataInputStream stream ) throws IOException
	{
		instance = this;

		level = Constants.parseLevel( stream.readInt(), stream );
		this.timeStep = stream.readLong();
		objectManager = new ObjectManager( stream );
		actionManager = new ActionManager( stream );
		paused = stream.readBoolean();

	}

	/**
	 * Returns whether the game is currently paused.
	 */
	public boolean isPaused()
	{
		return paused;
	}

	/**
	 * Pauses or unpauses the game.
	 * 
	 * @param paused whether the game should be paused
	 * @param announce whether to print the change
	 */
	public void setPaused( boolean paused, boolean announce )
	{
		this.paused = paused;
		if ( announce )
			Main.log( "Game " + ( paused ? "paused" : "unpaused" ) + "." );
		if ( Server.is() )
			ServerCommands.updatePause( paused );
	}

	public Level getLevel()
	{
		return level;
	}

	public void createBonus( GameObject parent )
	{
		objectManager.addObject( new Bonus( parent.getX(), parent.getY() ), false );
	}

	public ObjectManager getObjectManager()
	{
		return objectManager;
	}

	public GameMode getGameMode()
	{
		return gameMode;
	}
}
