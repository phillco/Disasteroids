/*
 * DISASTEROIDS
 * ImageLibrary.java
 */
package disasteroids.gui;

import disasteroids.Main;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * Loads and stores the game's external graphics resources.
 * @author Andy Kooiman, Phillip Cohen
 */
public class ImageLibrary
{
    /**
     * External images that game objects use.
     */
    private static Image asteroid,  bonusAsteroid,  alien,  blackHole;

    /**
     * Imports all of the resources.
     */
    public static void init()
    {
        asteroid = Toolkit.getDefaultToolkit().createImage( ImageLibrary.class.getResource( "/asteroid.gif" ) );
        bonusAsteroid = Toolkit.getDefaultToolkit().createImage( ImageLibrary.class.getResource( "/bonusAsteroid.png" ) );
        alien = Toolkit.getDefaultToolkit().createImage( ImageLibrary.class.getResource( "/alien.png" ) );
        blackHole = Toolkit.getDefaultToolkit().createImage( ImageLibrary.class.getResource( "/blackHole.png" ) );
    }

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

    public static Image getBlackHole()
    {
        return blackHole;
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
