/*
 * DISASTEROIDS
 * Running.java
 */

import java.awt.event.*;
import java.awt.*;
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
     * @param args Command line arguments. Ignored.
     */
    public static void main( String[] args )
    {
        // Read in our stored settings.
        Settings.loadFromStorage();

        // Start the menu.
        mF = new MainMenu();
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
        try
        {
            boolean isPlayerOne = true;
            boolean isMultiPlayer = true;
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
                    isPlayerOne = true;
                    seed = (int) ( Math.random() * 10000 );
                    AsteroidsServer.send( "Seed" + String.valueOf( seed ) );
                    RandNumGen.init( seed );
                    break;

                case MULTIJOIN:

                    // Get the server address.
                    String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", Settings.lastConnectionIP );
                    if ( ( address == null ) || ( address.isEmpty() ) )
                        return;

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
                    isPlayerOne = false;
                    while ( !RandNumGen.isInitialized() )
                        ;
                    break;

                case SINGLEPLAYER:

                    // Start the local game.
                    RandNumGen.init( seed );
                    isPlayerOne = true;
                    isMultiPlayer = false;
                    break;

                default:
                    Running.quit();
            }

            // Start the music.
            Sound.updateMusic();
            aF = new AsteroidsFrame( isPlayerOne, isMultiPlayer );
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
