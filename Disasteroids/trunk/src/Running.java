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
            Ship highestScorer = AsteroidsFrame.players[0];
            for ( Ship s : AsteroidsFrame.players )
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
        int playerCount = 1;
        int localPlayer = 0;
        int seed = 0;

        switch ( option )
        {
            case MULTIHOST:

                // Start the network server.
                try
                {
                    AsteroidsServer.master();
                }
                catch ( ConnectException e )
                {
                    return;
                }

                // Now start the local game. Assume player 1.
                playerCount = 2;
                localPlayer = 0;

                seed = (int) ( Math.random() * 10000 );
                AsteroidsServer.send( "Seed" + String.valueOf( seed ) );
                RandNumGen.init( seed );
                break;

            case MULTIJOIN:

                // Get the server address.
                String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", Settings.lastConnectionIP );
                if ( ( address == null ) || ( address.equals( "" ) ) )
                    return;

                Settings.lastConnectionIP = address;

                // Connect to it.
                try
                {
                    AsteroidsServer.slave( address );
                }
                catch ( ConnectException e )
                {
                    return;
                }

                // Start the local game. Assume player 2.
                playerCount = 2;
                localPlayer = 1;
                while ( !RandNumGen.isInitialized() )
                    ;
                break;

            case SINGLEPLAYER:

                // Start the local game.
                seed = (int) ( Math.random() * 10000 );
                RandNumGen.init( seed );
                break;

            default:
                Running.quit();
        }

        // Start the music.
        Sound.updateMusic();

        // Create the game frame. aF is not assigned here; because game initialization is done in the constructor, and those calls use Running.environment(), aF must be set at the beginning of the constructor.
        new AsteroidsFrame( playerCount, localPlayer );
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
}
