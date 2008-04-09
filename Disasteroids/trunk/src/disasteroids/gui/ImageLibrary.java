/*
 * Simple class to store Image objects for easy access
 */
package disasteroids.gui;

import java.awt.Image;
import java.awt.Toolkit;

public class ImageLibrary
{
    /**
     * The <code>Image</code> for an Asteroid
     */
    private static Image asteroid;

    /**
     * The <code>Image</code> for a bonusAsteroid
     */
    private static Image bonusAsteroid;

    private static Image alien;

    /**
     * Starts to load all of the <code>Image</code>s
     * 
     * @since March 24, 2008
     */
    public static void init()
    {
        asteroid = Toolkit.getDefaultToolkit().createImage( "asteroid.png" );
        bonusAsteroid = Toolkit.getDefaultToolkit().createImage( "bonusAsteroid.png" );
        alien = Toolkit.getDefaultToolkit().createImage( "alien.png" );
    }

    /**
     * @return The basic <code>Image</code> for an Asteroid
     * 
     * @since March 24, 2008
     */
    public static Image getAsteroid()
    {
        return asteroid;
    }

    public static Image getBonusAsteroid()
    {
        return bonusAsteroid;
    }

    public static Image getAlien()
    {
        return alien;
    }
    
    
}
