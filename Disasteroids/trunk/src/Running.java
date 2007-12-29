/*
 * DISASTEROIDS
 * Running.java
 */

import java.net.ConnectException;
import javax.swing.JOptionPane;

/**
 * Main startup-related code.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Running
{

    /**
     * The instance of the <code>AsteroidsFrame</code> in which the game runs.
     * @since Classic
     */
    private static AsteroidsFrame aF;

    /**
     * The application entry point. Loads user settings and runs the menu.
     * Users can skip the menu and select a <code>MenuOption</code> via the command line.
     * 
     * @param args      the command line arguments. By passing a <code>MenuOption</code> parameter, clients may skip the menu.
     * @since Classic
     */
    public static void main( String[] args )
    {
        MenuOption preselectedOption = null;

        // Do we have command-line arguments?
        for ( String arg : args )
        {
            // Check if any match a supported parameter.
            for ( MenuOption option : MenuOption.values() )
                if ( arg.equals( option.getParameter() ) )
                    preselectedOption = option;
        }


        // Read in our stored settings.
        Settings.loadFromStorage();

        // If the user has provided a selection, skip the menu.
        if ( preselectedOption != null )
            startGame( preselectedOption );
        else
        {
            // Start the menu.
            new MainMenu();
        }
    }

    /**
     * The main quit method that should replace <code>System.exit</code>.
     * It saves user settings before closing down.
     * 
     * @since December 7, 2007
     */
    public static void quit()
    {
        // Find the player with the highest score.
        if ( aF != null )
        {
            Ship highestScorer = Game.players.getFirst();
            for ( Ship s : Game.players )
            {
                if ( s.getScore() > Settings.highScore )
                {
                    Settings.highScoreName = highestScorer.getName();
                    Settings.highScore = highestScorer.getScore();
                }
            }
        }

        // Write our settings.
        Settings.saveToStorage();

        // Daisy.....daisy....
        System.exit( 0 );
    }

    /**
     * Starts the game based on the selected <code>MenuOption</code>.
     * 
     * @param option    the selected game choice
     * @since Classic
     */
    public static void startGame( MenuOption option )
    {
        // Init - but do not start - the game.
        Game.thread = new Game();

        // Add our player.
        switch ( option )
        {
            case SINGLEPLAYER:
                Game.state = Game.Netstate.SINGLEPLAYER;
                Game.addPlayer( ( Settings.playerName.equals( "" ) ? "Player" : Settings.playerName ) );
                new AsteroidsFrame( Game.players.size() - 1 );
                break;
            case LOCALLOOP:
                new Server();
                Game.safeSleep( 900 );
                new Client( "localhost" );
                break;
            case START_SERVER:
                Game.state = Game.Netstate.SERVER;
                new Server();
                break;

            case CONNECT:
                Game.state = Game.Netstate.CLIENT;
                // Get the server address.
                String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", "localhost" );
                if ( ( address == null ) || ( address.equals( "" ) ) )
                    return;

                Settings.lastConnectionIP = address;

                // Connect to it.
                new Client( address );
                break;
            default:
                Running.quit();
        }
    }

    /**
     * Returns the running <code>AsteroidsFrame</code>.
     * 
     * @return  the <code>AsteroidsFrame</code> that the game is being played in
     * @since Classic
     */
    public static AsteroidsFrame environment()
    {
        return aF;
    }

    /**
     * Initializes the static reference to the <code>AsateroidsFrame</code>. Can only be called once.
     * Typically called by the <code>AsteroidsFrame</code> constructor to assign itself. This is pretty hackish.
     * 
     * @param aF the AsteroidsFrame
     * @since December 16, 2007
     */
    public static void setEnvironment( AsteroidsFrame aF )
    {
        if ( Running.aF == null )
            Running.aF = aF;
    }

    public static void log( String message )
    {
        System.out.println( message );
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.addNotificationMessage( message );
    }
}
