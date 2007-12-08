/*
 * DISASTEROIDS
 * Settings.java
 */

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A simple class to store user settings.
 * @since Nov 16, 2007
 * @author Phillip Cohen
 */
public class Settings
{
    public static boolean musicOn = true;
    public static boolean soundOn = true;
    public static boolean useFullscreen = true;
    
    /**
     * Loads settings from <code>Disasteroids.prop</code> if it exists.
     * @return Whether settings were loaded.
     */
    public static boolean loadFromStorage()
    {
        try
        {
            Properties p = new Properties();
            p.load( new FileInputStream( "Disasteroids.props" ) );
            musicOn = Boolean.parseBoolean( p.getProperty( "musicOn" ) );
            soundOn = Boolean.parseBoolean( p.getProperty( "soundOn" ) );
            useFullscreen = Boolean.parseBoolean( p.getProperty( "fullscreen" ) );
        }
        catch ( IOException ex )
        {
            return false;
        }

        // Success!
        return true;
    }

     /**
     * Writes settings to <code>Disasteroids.prop</code>.
     * @return Whether settings were saved.
     */
    public static boolean saveToStorage()
    {
        Properties p = new Properties();
        try
        {
            p.put( "musicOn", String.valueOf( musicOn ) );
            p.put( "soundOn", String.valueOf( soundOn ) );
            p.put( "fullscreen", String.valueOf( useFullscreen ) );
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
