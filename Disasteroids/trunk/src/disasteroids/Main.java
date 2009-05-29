/*
 * DISASTEROIDS
 * Running.java
 */
package disasteroids;

import disasteroids.gui.MenuOption;
import disasteroids.gui.MainWindow;
import disasteroids.gui.ImageLibrary;
import disasteroids.gui.Local;
import disasteroids.gui.MainMenu;
import disasteroids.gui.MenuOption;
import disasteroids.networking.*;
import disasteroids.sound.Sound;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

/**
 * Main utility code for startup, exit, logs, and errors.
 * @author Andy Kooiman, Phillip Cohen
 */
public class Main
{

    /**
     * Small counters that are incremented each time <code>warning</code> or <code>fatalError</code> is called.
     * Shown when Disasteroids shuts down.
     * 
     * @since January 18, 2008
     */
    private static int errorCount = 0, warningCount = 0;

    public static boolean isRunningFromJar = false;

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

        // Load external images.
        ImageLibrary.init();

        // Load stored settings.
        Settings.loadFromStorage();

        // The user can skip the menu with a command-line argument.
        for ( String arg : args )
        {
            for ( MenuOption option : MenuOption.values() )
            {
                // Matches a menu option. Use that.
                if ( arg.equals( option.getParameter() ) )
                {
                    startGame( option );
                    return;
                }
            }
        }

        // Otherwise, just start the menu.
        new MainMenu();
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

            // Tell server or clients that we're quitting.
            if ( Client.is() )
                Client.getInstance().quit();
            else if ( Server.is() )
                Server.getInstance().quit();

            // Save local settings.
            Settings.saveToStorage();

            // Show warning / error count.
            System.out.print( "Disasteroids concluded." );

            if ( errorCount > 0 )
                System.out.print( " " + errorCount + " error" + ( errorCount == 1 ? "" : "s" ) + ( warningCount > 0 ? "," : "." ) );

            if ( warningCount > 0 )
                System.out.print( " " + warningCount + " warning" + ( warningCount == 1 ? "." : "s." ) );

            // Daisy.....daisy....
            System.exit( 0 );

        }
        catch ( Throwable throwable )
        {
            System.out.println( "\nError shutting down!\nShutting down not-so-nicely..." );

            // [AK] This should help if we ran out of memory.
            if ( throwable instanceof java.lang.OutOfMemoryError )
            {
                System.gc();
                disasteroids.gui.ParticleManager.clear();
                System.gc();
            }

            // Again, try to write our settings.
            Settings.saveToStorage();

            throwable.printStackTrace();
            System.exit( 66 );
        }
        finally
        {
            //shouldn't get here... but if we do, just in case
            System.exit( 66 ); //It failed <i>real</i> bad
        }
    }

    /**
     * Starts the game based on the selected <code>MenuOption</code>.
     * 
     * @param option    the selected game choice
     * @since Classic
     */
    public static void startGame( MenuOption option )
    {
        if ( option == MenuOption.CONNECT )
        {
            new Client();
            return;
        }
        // Decide which game mode to use (yuck!).
        {
            Class gameMode = Settings.getLastGameMode();
            Game.GameType gameType = Game.GameType.COOPERATIVE;
            if ( option == MenuOption.START_SERVER )
            {
                gameMode = Deathmatch.class;
                gameType = Game.GameType.DEATHMATCH;
            }
            else if ( option == MenuOption.TUTORIAL )
                gameMode = TutorialMode.class;

            new Game( gameMode, gameType );
        }

        // Create the local player and window. Start the game.
        long localPlayerID = ( option == MenuOption.LOAD ) ? Game.loadFromFile() : Game.getInstance().addPlayer( Settings.getPlayerName(), Settings.getPlayerColor() );
        Local.init( localPlayerID );
        new MainWindow();
        GameLoop.startLoop();

        // Show start message.
        switch ( option )
        {
            case START_SERVER:
                MainWindow.frame().showStartMessage( "Server started!\nAddress is: " + Server.getLocalIP() + "\nPress F1 for help." );
                Main.log( "Server started! The address is: " + Server.getLocalIP() + "\n." );
                Game.getInstance().setPaused( false, false );
                break;
            case SINGLEPLAYER:
                MainWindow.frame().showStartMessage( "Press any key to begin.\nPress F1 for help." );
                break;
            case LOAD:
                Game.getInstance().setPaused( false, false );
                break;
            case TUTORIAL:
                MainWindow.frame().showStartMessage( "Press any key to start the tutorial." );
                break;
            default:
                Main.fatalError( "Unexpected menu selection." );
            case EXIT:
                Main.quit();
        }

        if ( option == MenuOption.START_SERVER )
            new Server();
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
        if ( MainWindow.frame() != null )
            MainWindow.addNotificationMessage( message );
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
        if ( MainWindow.frame() != null )
            MainWindow.addNotificationMessage( message, life );
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
        Main.quit();
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
    private Main()
    {
    }
}