/*
 * DISASTEROIDS
 * Settings.java
 */
package disasteroids;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Stores user settings and writes/retrieves them from <code>Disasteroids.props</code>.
 * @since Nov 16, 2007
 * @author Phillip Cohen
 */
public class Settings implements Serializable
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
    public static boolean qualityRendering = true;

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
     * Our local player name.
     * @since December 20, 2007
     */
    public static String playerName = "Player";

    /**
     * Whether the user would like hardware accelerated graphics (true), or software (false).
     * @since December 21, 2007
     */
    public static boolean hardwareRendering = false;

    /**
     * The user's ship color.
     * @since April 9, 2008
     */
    public static Color playerColor = Color.red;
    
    /**
     * First time startup?
     * @since April 9, 2008
     */
    public static boolean inSetup = false;

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
                qualityRendering = Boolean.parseBoolean( p.getProperty( "antiAlias" ) );

            if ( p.containsKey( "waitForMissiles" ) )
                waitForMissiles = Boolean.parseBoolean( p.getProperty( "waitForMissiles" ) );

            if ( p.containsKey( "lastConnectionIP" ) )
                lastConnectionIP = p.getProperty( "lastConnectionIP" );

            if ( p.containsKey( "highScore" ) )
                highScore = Double.parseDouble( p.getProperty( "highScore" ) );

            if ( p.containsKey( "highScoreName" ) )
                highScoreName = p.getProperty( "highScoreName" );

            if ( p.containsKey( "playerName" ) )
                playerName = p.getProperty( "playerName" );

            if ( p.containsKey( "hardwareRendering" ) )
                hardwareRendering = Boolean.parseBoolean( p.getProperty( "hardwareRendering" ) );
            {
                int red = -1, green = -1, blue = -1;

                if ( p.containsKey( "playerColor_red" ) )
                    red = Integer.parseInt( p.getProperty( "playerColor_red" ) );
                if ( p.containsKey( "playerColor_green" ) )
                    green = Integer.parseInt( p.getProperty( "playerColor_green" ) );
                if ( p.containsKey( "playerColor_blue" ) )
                    blue = Integer.parseInt( p.getProperty( "playerColor_blue" ) );

                if ( red > 0 && green > 0 && blue > 0 )
                    playerColor = new Color( red, green, blue );
            }
        }
        catch ( IOException ex )
        {
            inSetup = true;
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
            p.put( "antiAlias", String.valueOf( qualityRendering ) );
            p.put( "waitForMissiles", String.valueOf( waitForMissiles ) );
            p.put( "lastConnectionIP", lastConnectionIP );
            p.put( "highScore", String.valueOf( highScore ) );
            p.put( "highScoreName", highScoreName );

            if ( playerName.equals( "" ) )
                p.put( "playerName", "Player" );
            else
                p.put( "playerName", playerName );

            p.put( "hardwareRendering", String.valueOf( hardwareRendering ) );
            p.put( "playerColor_red", String.valueOf( playerColor.getRed() ) );
            p.put( "playerColor_green", String.valueOf( playerColor.getGreen() ) );
            p.put( "playerColor_blue", String.valueOf( playerColor.getBlue() ) );

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

    public static String getLocalName()
    {
        if ( playerName.equals( "" ) )
            return "Player";
        else
            return playerName;
    }
    
    public static boolean isInSetup()
    {
        return inSetup;
    }

    public static void setInSetup( boolean inSetup )
    {
        Settings.inSetup = inSetup;
    }
    
    
}
