/*
 * Simple class to store Image objects for easy access
 */
package disasteroids.gui;

import disasteroids.JarResources;
import disasteroids.Running;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;

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
        asteroid = Toolkit.getDefaultToolkit().createImage( "res\\asteroid.gif" );
        bonusAsteroid = Toolkit.getDefaultToolkit().createImage( "res\\bonusAsteroid.png" );
        alien = Toolkit.getDefaultToolkit().createImage( "res\\alien.png" );

        if ( new File( "Disasteroids.jar" ).exists() )
        {
            JarResources jar = new JarResources( "Disasteroids.jar" );
            asteroid = Toolkit.getDefaultToolkit().createImage( jar.getResource( "asteroid.gif" ) );
            Running.isRunningFromJar = true;
        }
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

    public static Image hueShift( BufferedImage img, Color target )
    {
        //start making the image
        BufferedImage ret = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB );

        //figure out what color you want
        float targetHue;
        {//I want these variables to go out of scope soon so I can reuse names

            int red = target.getRed();
            int green = target.getGreen();
            int blue = target.getBlue();
            targetHue = Color.RGBtoHSB( red, green, blue, new float[3] )[0];
        }
        //prepare to draw
        Graphics2D gimg = ret.createGraphics();

        //loop through pixels
        for ( int x = 0; x < img.getWidth(); x++ )
        {
            for ( int y = 0; y < img.getHeight(); y++ )
            {
                //get what's there, and split it up
                int present = img.getRGB( x, y );
                int blue = ( present & 0x0000FF );
                int green = ( ( present & 0x00FF00 ) >> 8 );
                int red = ( ( present & 0xFF0000 ) >> 16 );

                //convert to HSB
                float[] hsb =
                        {
                    0f, 0f, 0f
                };
                Color.RGBtoHSB( red, green, blue, hsb );

                //change the hue, and draw the pixel
                Color newCol = Color.getHSBColor( ( 9f * targetHue + hsb[0] ) / 10f, hsb[1], hsb[2] );
                gimg.setColor( newCol );
                gimg.drawRect( x, y, 1, 1 );
            }
        }
        return ret;
    }
}
