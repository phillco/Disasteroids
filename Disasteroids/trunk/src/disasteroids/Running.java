/*
 * DISASTEROIDS
 * Running.java
 */
package disasteroids;

import disasteroids.gui.MenuOption;
import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.ImageLibrary;
import disasteroids.gui.MainMenu;
import disasteroids.networking.Client;
import disasteroids.networking.Server;
import disasteroids.sound.Sound;
import java.io.File;
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
     * Small counters that are incremented each time <code>warning</code> or <code>fatalError</code> is called.
     * Shown when Disasteroids shuts down.
     * 
     * @since January 18, 2008
     */
    private static int errorCount = 0,  warningCount = 0;

    /**
     * The application entry point. Loads user settings and runs the menu.
     * Users can skip the menu and select a <code>MenuOption</code> via the command line.
     * 
     * @param args      the command line arguments. By passing a <code>MenuOption</code> parameter, clients may skip the menu.
     * @since Classic
     */
    public static void main( String[] args )
    {
        System.out.println( "DISASTEROIDS started!" );

        // Check for resources.
        if ( !new File( "res\\Music2.mid" ).exists() )
        {
            Running.fatalError( "Couldn't load resources.\nPlease make sure that your running directory is empty or set to the project's main directory." );
            return;
        }

        // Make swing dialogs like the local operating system.
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

        // Load images.
        ImageLibrary.init();

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
     * It saves user settings, notifies clients/server, updates high score, and shows warning and error count.
     * And then it calls <code>System.exit()</code>.
     * 
     * @since December 7, 2007
     */
    public static void quit()
    {
        try
        {
            GameLoop.stopLoop();
            System.out.println( "\nShutting down nicely..." );

            // Tell the server we're quitting.
            if ( Client.is() )
            {
                Client.getInstance().quit();
            } // And I told Bill, that if they move my desk one more time, then, then....
            // Tell clients we're quitting.

            else if ( Server.is() )
            {
                Server.getInstance().quit();
            }

            // Find the player with the highest score.
            if ( AsteroidsFrame.frame() != null )
            {
                Ship highestScorer = Game.getInstance().players.peek();
                for ( Ship s : Game.getInstance().players )
                {
                    if ( s.getScore() > Settings.getHighScore() )
                    {
                        Settings.setHighScoreName( highestScorer.getName() );
                        Settings.setHighScore( highestScorer.getScore() );
                    }
                }
            }

            // Write our settings.
            Settings.saveToStorage();

            // Show warning / error count.
            System.out.print( "Disasteroids concluded." );

            if ( errorCount > 0 )
            {
                System.out.print( " " + errorCount + " error" + ( errorCount == 1 ? "" : "s" ) + ( warningCount > 0 ? "," : "." ) );
            }

            if ( warningCount > 0 )
            {
                System.out.print( " " + warningCount + " warning" + ( warningCount == 1 ? "." : "s." ) );
            }

            // Daisy.....daisy....
            System.exit( 0 );

        }
        catch ( Throwable throwable )
        {
            System.out.println( "\nShut Down Failed! Killing now." );

            //this should help if we ran out of memory
            if ( throwable instanceof java.lang.OutOfMemoryError )
            {
                System.gc();
                Game.getInstance().gameObjects = null;
                Game.getInstance().shootingObjects = null;
                disasteroids.gui.ParticleManager.clear();
                System.gc();
            }

            // Write our settings.
            Settings.saveToStorage(); //we really hope that this survived

            throwable.printStackTrace();
            System.exit( 1 );
        }
        finally
        {
            //shouldn't get here... but if we do, just in case
            System.exit( 255 ); //It failed <i>real</i> bad

        }

    }

    /**
     * Starts the game based on the selected <code>MenuOption</code>.
     * 
     * @param option    the selected game choice
     * @since Classic
     */
    @SuppressWarnings ( "fallthrough" )
    public static void startGame( MenuOption option )
    {
        switch ( option )
        {
            case START_SERVER:
                new Server();
            // Fall-through

            case SINGLEPLAYER:
                new Game( Settings.getLastGameMode() );
                new AsteroidsFrame( Game.getInstance().addPlayer( Settings.getPlayerName(), Settings.getPlayerColor() ) );
                AsteroidsFrame.frame().showStartMessage( "Press any key to begin." );
                Sound.updateMusic();
                break;
            case LOAD:
                new AsteroidsFrame( Game.loadFromFile() );
                Game.getInstance().setPaused( false, false );
                break;

            case TUTORIAL:
                new Game( TutorialMode.class );
                new AsteroidsFrame( Game.getInstance().addPlayer( Settings.getPlayerName(), Settings.getPlayerColor() ) );
                Sound.updateMusic();
                break;

            case CONNECT:
                // Get the server address.
                String address = JOptionPane.showInputDialog( "Enter the IP address of the host computer.", Settings.getLastConnectionIP() );
                if ( ( address == null ) || ( address.equals( "" ) ) )
                    return;

                Settings.setLastConnectionIP( address );
                Settings.saveToStorage();
                if ( Settings.isMusicOn() )
                    Sound.startMusic();

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
                Running.fatalError( "Unexpected menu selection." );
            case EXIT:
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
     * @param life      life of the message in <code>AsteroidsFrame</code>
     * @since December 29, 2007
     */
    public static void log( String message, int life )
    {
        System.out.println( message );
        if ( AsteroidsFrame.frame() != null )
            AsteroidsFrame.addNotificationMessage( message, life );
    }

    /**
     * Logs a warning to the console and bumps the warningCount.
     * 
     * @param message   the message
     * @since January 18, 2008
     */
    public static void warning( String message )
    {
        log( "WARNING: " + message, 1200 );
        warningCount++;
    }

    /**
     * Logs a warning and exception to the console and bumps the warningCount.
     * 
     * @param message   the message
     * @param t         the Throwable
     * @since January 18, 2008
     */
    public static void warning( String message, Throwable t )
    {
        log( "WARNING: " + message, 1200 );
        t.printStackTrace();
        warningCount++;
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
        errorCount++;
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
        e.printStackTrace();
        fatalError( message + "\n\nWith exception: " + e.getLocalizedMessage() );
    }

    /**
     * Utility class - no constructor. (Happy, NetBeans?)
     */
    private Running()
    {
    }
}
