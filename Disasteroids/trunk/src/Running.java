/*
 * DISASTEROIDS
 * Running.java
 */

import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Main utility code for startup, exit, logs, and errors.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Running
{
    /**
     * The application entry point. Loads user settings and runs the menu.
     * Users can skip the menu and select a <code>MenuOption</code> via the command line.
     * 
     * @param args      the command line arguments. By passing a <code>MenuOption</code> parameter, clients may skip the menu.
     * @since Classic
     */
    public static void main( String[] args )
    {
        // Look like the local system..
        try
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch ( Exception ex )
        {

        }        

        // Do we have command-line arguments?
        MenuOption preselectedOption = null;
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
        System.out.println("Shutting down nicely...");
        
        // Find the player with the highest score.
        if ( AsteroidsFrame.frame() != null )
        {
            Ship highestScorer = Game.getInstance().players.getFirst();
            for ( Ship s : Game.getInstance().players )
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
        // Init - but do not start - the Game.
        new Game();

        switch ( option )
        {
            case SINGLEPLAYER:
                Game.getInstance().state = Game.Netstate.SINGLEPLAYER;
                Game.getInstance().addPlayer( Settings.playerName.equals( "" ) ? "Player" : Settings.playerName );
                new AsteroidsFrame( Game.getInstance().players.size() - 1 );
                break;
            case LOCALLOOP:
                new Server();
                Game.getInstance().safeSleep( 900 );
                try
                {
                    new Client( "localhost" );
                }
                catch ( UnknownHostException ex )
                {
                    fatalError( "Couldn't connect to the local server (o_O)." );
                }
                break;
            case START_SERVER:
                Game.getInstance().state = Game.Netstate.SERVER;
                new Server();
                break;
            case CONNECT:
                Game.getInstance().state = Game.Netstate.CLIENT;

                // Get the server address.
                String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", "localhost" );
                if ( ( address == null ) || ( address.equals( "" ) ) )
                    return;
                Settings.lastConnectionIP = address;

                // Connect to it.
                try
                {
                    new Client( address );
                }
                catch ( UnknownHostException ex )
                {
                    fatalError( "Couldn't look up " + address + "." );
                }
                break;
            default:
                Running.quit();
            }

    }

    /**
     * Logs a message to <code>println</code> and the <code>AsteroidsFrame</code> (if it exists).
     * 
     * @param message   the message to log
     * @since December 26, 2007
     */
    public static void log( String message )
    {
        System.out.println( message );
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.addNotificationMessage( message );
    }

    /**
     * Logs a message to <code>println</code> and the <code>AsteroidsFrame</code> (if it exists).
     * 
     * @param message   the message to log
     * @param life     life of the message in <code>AsteroidsFrame</code>
     * @since December 29, 2007
     */
    public static void log( String message, int life )
    {
        System.out.println( message );
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.addNotificationMessage( message, life );
    }

    /**
     * Shows a JOptionPane error dialog with the message text, logs it, and quits.
     * 
     * @param message   the error text to show
     * @since December 29, 2007
     */
    public static void fatalError( String message )
    {
        JOptionPane.showMessageDialog( null, message, "Disasteroids: Very Fatal Error", JOptionPane.ERROR_MESSAGE );
        System.out.println( "FATAL ERROR: " + message );
        Running.quit();
    }
    
    /**
     * Shows a JOptionPane error dialog with the message text, logs it and the exception's stack trace, and quits.
     * 
     * @param message   the error text to show
     * @param e         the exception to print the trace of
     * @since December 29, 2007
     */
    public static void fatalError( String message, Exception e )
    {
        JOptionPane.showMessageDialog( null, message, "Disasteroids: Very Fatal Error (e)", JOptionPane.ERROR_MESSAGE );
        System.out.println( "FATAL ERROR: " + message );
        e.printStackTrace();
        Running.quit();
    }
    
    

    /**
     * Utility class - no constructor. (Happy, NetBeans?)
     */
    private Running()
    {
    }
}
