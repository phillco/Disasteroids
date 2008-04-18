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
 * @since November 16, 2007
 * @author Phillip Cohen
 */
public class Settings implements Serializable
{
    /**
     * First time startup?
     * @since April 9, 2008
     */
    private static boolean inSetup = false;

    /**
     * The path to the settings file.
     * @since April 15, 2008
     */
    public static final String SETTINGS_FILE_PATH = "res\\UserSettings2.props";

    /**
     * The default settings.
     * @since April 15, 2008
     */
    private static Properties defaultSettings;

    /**
     * The user's settings.
     * @since April 15, 2008
     */
    private static Properties settingsFile;

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
            defaultSettings = new Properties();
            defaultSettings.put( "musicOn", String.valueOf( true ) );
            defaultSettings.put( "soundOn", String.valueOf( true ) );
            defaultSettings.put( "fullscreenMode", String.valueOf( true ) );
            defaultSettings.put( "qualityRendering", String.valueOf( false ) );
            defaultSettings.put( "lastConnectionIP", "localhost" );
            defaultSettings.put( "lastGameMode", "wave" );
            defaultSettings.put( "highScore", String.valueOf( 2000 ) );
            defaultSettings.put( "highScorer", "Phillip & Andy" );
            defaultSettings.put( "playerName", "Player" );
            defaultSettings.put( "playerColor", String.valueOf( Color.red.getRGB() ) );

            // Load the settings file.
            settingsFile = new Properties( defaultSettings );
            settingsFile.load( new FileInputStream( SETTINGS_FILE_PATH ) );
        }
        catch ( IOException ex )
        {
            setInSetup( true );
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
        try
        {
            // Write the settings file.            
            settingsFile.store( new FileOutputStream( Settings.SETTINGS_FILE_PATH ), "Disasteroids settings file (v2)." );
        }
        catch ( IOException e )
        {
            Running.warning( "Failed to save settings.", e );
            return false;
        }

        // Success!
        return true;
    }

    /**
     * Returns whether the user wants music to play.
     * @since November 15, 2007
     */
    public static boolean isMusicOn()
    {
        return Boolean.parseBoolean( settingsFile.getProperty( "musicOn" ) );
    }

    /**
     * Sets whether the user wants music to play.
     * @since November 15, 2007
     */
    public static void setMusicOn( boolean aMusicOn )
    {
        settingsFile.put( "musicOn", String.valueOf( aMusicOn ) );
    }

    /**
     * Returns whether the user wants sound to play.
     * @since November 15, 2007
     */
    public static boolean isSoundOn()
    {
        return Boolean.parseBoolean( settingsFile.getProperty( "soundOn" ) );
    }

    /**
     * Sets whether the user wants sound to play.
     * @since November 15, 2007
     */
    public static void setSoundOn( boolean aSoundOn )
    {
        settingsFile.put( "soundOn", String.valueOf( aSoundOn ) );
    }

    /**
     * Returns whether the user wants a fullscreen game (true) or a windowed one (false).
     * @since December 7, 2007
     */
    public static boolean isUseFullscreen()
    {
        return Boolean.parseBoolean( settingsFile.getProperty( "fullscreenMode" ) );
    }

    /**
     * Saves whether the user wants a fullscreen game (true) or a windowed one (false).
     * @since December 7, 2007
     */
    public static void setUseFullscreen( boolean aUseFullscreen )
    {
        settingsFile.put( "fullscreenMode", String.valueOf( aUseFullscreen ) );
    }

    /**
     * Returns the last address the user entered in the "Select Server" dialog box.
     * @since December 9, 2007
     */
    public static String getLastConnectionIP()
    {
        return settingsFile.getProperty( "lastConnectionIP" );
    }

    /**
     * Saves the last address the user entered in the "Select Server" dialog box.
     * @since December 9, 2007
     */
    public static void setLastConnectionIP( String aLastConnectionIP )
    {
        settingsFile.put( "lastConnectionIP", aLastConnectionIP );
    }

    /**
     * Saves the last used game mode.
     * @since April 17, 2008
     */
    public static void setLastGameMode( Class aMode )
    {
        if ( aMode == WaveGameplay.class )
            settingsFile.put( "lastGameMode", "wave" );
        else
            settingsFile.put( "lastGameMode", "linear" );
    }
    
    /**
     * Gets the last used game mode.
     * @since April 17, 2008
     */
    public static Class getLastGameMode( )
    {
        if ( settingsFile.getProperty("lastGameMode").equalsIgnoreCase("wave"))
            return WaveGameplay.class;
        else
            return LinearGameplay.class;
    }
    

    /**
     * Returns whether the user wants quality rendering. This makes graphics look nicer, but at the expense of speed.
     * @since December 15, 2007
     */
    public static boolean isQualityRendering()
    {
        return Boolean.parseBoolean( settingsFile.getProperty( "qualityRendering" ) );
    }

    /**
     * Sets whether the user wants quality rendering. This makes graphics look nicer, but at the expense of speed.
     * @since December 15, 2007
     */
    public static void setQualityRendering( boolean aQualityRendering )
    {
        settingsFile.put( "qualityRendering", String.valueOf( aQualityRendering ) );
    }

    /**
     * Returns the highest score we've seen on this computer.
     * @since December 18, 2007
     */
    public static double getHighScore()
    {
        return Double.parseDouble( settingsFile.getProperty( "highScore" ) );
    }

    /**
     * Updates the highest score we've seen on this computer.
     * @since December 18, 2007
     */
    public static void setHighScore( double aHighScore )
    {
        settingsFile.put( "highScore", String.valueOf( aHighScore ) );
    }

    /**
     * Returns the name of the high scorer.
     * @since December 18, 2007
     */
    public static String getHighScoreName()
    {
        return settingsFile.getProperty( "highScorer" );
    }

    /**
     * Sets the name of the high scorer.
     * @since December 18, 2007
     */
    public static void setHighScoreName( String aHighScoreName )
    {
        settingsFile.put( "highScorer", aHighScoreName );
    }

    /**
     * Returns the user's in-game name, or "Player" if that's empty.
     * @since December 20, 2007
     */
    public static String getPlayerName()
    {
        if ( settingsFile.getProperty( "playerName" ).length() == 0 )
            return "Player";
        else
            return settingsFile.getProperty( "playerName" );
    }

    /**
     * Sets the user's in-game name.
     * @since December 20, 2007
     */
    public static void setPlayerName( String aPlayerName )
    {
        settingsFile.put( "playerName", aPlayerName );
    }

    /**
     * Returns the user's in-game ship color.
     * @since April 9, 2008
     */
    public static Color getPlayerColor()
    {
        return new Color( Integer.parseInt( settingsFile.getProperty( "playerColor" ) ) );
    }

    /**
     * Sets the user's in-game ship color.
     * @since April 9, 2008
     */
    public static void setPlayerColor( Color aPlayerColor )
    {
        settingsFile.put( "playerColor", String.valueOf( aPlayerColor.getRGB() ) );
    }

    /**
     * Returns if this is a first-time run, where the user has no personal settings.
     * @since April 9, 2008
     */
    public static boolean isInSetup()
    {
        return inSetup;
    }

    /**
     * Sets whether this is a first-time, where the user has no personal settings.
     * @since April 9, 2008
     */
    public static void setInSetup( boolean aInSetup )
    {
        inSetup = aInSetup;
    }
}
