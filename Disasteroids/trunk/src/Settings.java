/*
 * DISASTEROIDS
 * Settings.java
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Stores user settings and writes/retrieves them from <code>Disasteroids.props</code>.
 * 
 * @since Nov 16, 2007
 * @author Phillip Cohen
 */
public class Settings
{
    /**
     * Whether the user wants music to play.
     * @since November 15, 2007
     */
    public static boolean musicOn = true;

    /**
     * Whether the user wants sound to play.
     * @since November 15, 2007
     */
    public static boolean soundOn = true;

    /**
     * Whether the user wants fullscreen (true) or windowed (false).
     * @since December 7, 2007
     */
    public static boolean useFullscreen = true;

    /**
     * The last address the user entered in the "Select Server" dialog box.
     * @since December 9, 2007
     */
    public static String lastConnectionIP = "";

    /**
     * Whether the user wants to anti-alias the game graphics.
     * @since December 15, 2007
     */
    public static boolean antiAlias = true;

    /**
     * Whether the game should wait for all <code>Missile</code>s to be destroyed before advancing to the next level.
     * @since December 16, 2007
     */
    public static boolean waitForMissiles = false;

    /**
     * The highest score we've seen on this computer.
     * @since December 18, 2007
     */
    public static double highScore = 1000;

    /**
     * The name of the high scorer.
     * @since December 18, 2007
     */
    public static String highScoreName = "Phillip & Andy";

    /**
     * Loads settings from <code>Disasteroids.props</code>, if it exists.
     * 
     * @return  whether settings were loaded
     * @since December 7, 2007
     */
    public static boolean loadFromStorage()
    {
        try
        {
            // Load the settings file.
            Properties p = new Properties();
            p.load( new FileInputStream( "Disasteroids.props" ) );

            // Check for settings, and load them if they exist.
            if ( p.containsKey( "musicOn" ) )
                musicOn = Boolean.parseBoolean( p.getProperty( "musicOn" ) );

            if ( p.containsKey( "soundOn" ) )
                soundOn = Boolean.parseBoolean( p.getProperty( "soundOn" ) );

            if ( p.containsKey( "fullscreen" ) )
                useFullscreen = Boolean.parseBoolean( p.getProperty( "fullscreen" ) );

            if ( p.containsKey( "antiAlias" ) )
                antiAlias = Boolean.parseBoolean( p.getProperty( "antiAlias" ) );

            if ( p.containsKey( "waitForMissiles" ) )
                waitForMissiles = Boolean.parseBoolean( p.getProperty( "waitForMissiles" ) );

            if ( p.containsKey( "lastConnectionIP" ) )
                lastConnectionIP = p.getProperty( "lastConnectionIP" );

            if ( p.containsKey( "highScore" ) )
                highScore = Double.parseDouble( p.getProperty( "highScore" ) );

            if ( p.containsKey( "highScoreName" ) )
                highScoreName = p.getProperty( "highScoreName" );
        }
        catch ( IOException ex )
        {
            return false;
        }

        // Success!
        return true;
    }

    /**
     * Writes settings to <code>Disasteroids.props</code>.
     * 
     * @return  whether settings were saved
     * @since December 7, 2007
     */
    public static boolean saveToStorage()
    {
        Properties p = new Properties();
        try
        {
            p.put( "musicOn", String.valueOf( musicOn ) );
            p.put( "soundOn", String.valueOf( soundOn ) );
            p.put( "fullscreen", String.valueOf( useFullscreen ) );
            p.put( "antiAlias", String.valueOf( antiAlias ) );
            p.put( "waitForMissiles", String.valueOf( waitForMissiles ) );
            p.put( "lastConnectionIP", lastConnectionIP );
            p.put( "highScore", String.valueOf( highScore ) );
            p.put( "highScoreName", highScoreName );

            // Write the settings file.            
            p.store( new FileOutputStream( "Disasteroids.props" ), "Disasteroids settings file." );
        }
        catch ( IOException ex )
        {
            return false;
        }

        // Success!
        return true;
    }
}
