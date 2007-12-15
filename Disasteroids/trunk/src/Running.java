/*
 * DISASTEROIDS
 * Running.java
 */


import java.net.ConnectException;
import javax.swing.JOptionPane;

/**
 * Main startup-related code.
 * @author Andy Kooiman
 */
public class Running
{
    private static AsteroidsFrame aF;
    private static MainMenu mF;

    /**
     * Application entry point.
     * @param args Command line arguments.
     */
    public static void main( String[] args )
    {
        MenuOption preselectedOption = null;

        // Do we have command-line arguments?
        for ( String arg : args )
        {
            for ( MenuOption option : MenuOption.values() )
                if ( arg.equals( option.getParameter() ) )
                    preselectedOption = option;
        }


        // Read in our stored settings.
        Settings.loadFromStorage();

        // If the user wants to launch a mode, skip the menu.
        if ( preselectedOption != null )
            startGame( preselectedOption );
        else
        {
            // Start the menu.
            mF = new MainMenu();
        }
    }

    public static void quit()
    {
        // Write our settings.
        Settings.saveToStorage();

        // Daisy.....daisy....
        System.exit( 0 );
    }

    /**
     * Called from within <code>MenuFrame</code> when the user selects an option.
     * @param option The selected menu choice.
     */
    public static void exitMenu( MenuOption option )
    {
        mF.dispose();
        startGame( option );
    }

    /**
     * Method that starts the game (host, slave, or single player).
     * @param option The selected menu choice.
     */
    private static void startGame( MenuOption option )
    {
        int playerCount = 1;
        int localPlayer = 0;
        try
        {
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

                    // Now start the local game.
                    // Assume player 1.
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

                    // Start the local game.
                    // Assume player 2.
                    playerCount = 2;
                    localPlayer = 1;
                    while ( !RandNumGen.isInitialized() )
                        ;
                    break;

                case SINGLEPLAYER:

                    // Start the local game.
                    RandNumGen.init( seed );
                    break;

                default:
                    Running.quit();
            }

            // Start the music.
            Sound.updateMusic();
            
            // Create the game!
            aF = new AsteroidsFrame( playerCount, localPlayer );
        }
        catch ( Exception e )
        {
            JOptionPane.showMessageDialog( null, "There has been a fatal error:\n" + e.toString() + "\nThe system is down." );
            e.printStackTrace();
            Running.quit();
        }
    }

    public static AsteroidsFrame environment()
    {
        return aF;
    }
}
