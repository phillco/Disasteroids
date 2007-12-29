/*
 * DISASTEROIDS
 * Background.java
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.VolatileImage;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The star background.
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic, December 23, 2007
 */
public class Background
{

    /**
     * Dimensions of the background.
     * @since December 23, 2007
     */
    private int width,  height;

    /**
     * The array of <code>Star</code>s that are always drawn.
     * @since December 16, 2007
     */
    private Star[] theStars;

    /**
     * Messages that are to be drawn temporarily on the star background.
     * @since December 16, 2007
     */
    private ConcurrentLinkedQueue<Background.BackgroundMessage> starMessages = new ConcurrentLinkedQueue<BackgroundMessage>();

    /**
     * The rendered output.
     * @since Classic
     */
    private Image image;

    /**
     * Creates the background.
     * 
     * @param width     width of the background
     * @param height    height of the background
     */
    public Background( int width, int height )
    {
        this.width = width;
        this.height = height;
        init();
    }

    /**
     * Removes all <code>BackgroundMessage</code>s.
     * 
     * @since December 23, 2007
     */
    public void clearMessages()
    {
        starMessages.clear();
    }

    /**
     * Creates the <code>background</code> image and <code>Star</code> array.
     * Uses hardware acceleration, if desired.
     * 
     * @since Classic
     */
    public void init()
    {
        // Create the image if we haven't yet.
        if ( image == null )
        {
            if ( Settings.hardwareRendering )
                image = Running.environment().getGraphicsConfiguration().createCompatibleVolatileImage( width, height );
            else
                image = Running.environment().createImage( width, height );
        }

        // Create the array of stars.
        Random rand = RandNumGen.getStarInstance();
        this.theStars = new Star[width * height / ( rand.nextInt( 1700 ) + 300 )];
        for ( int star = 0; star < theStars.length; star++ )
        {
            int sat = rand.nextInt( 255 );
            Color col = new Color( sat, sat, sat );
            theStars[star] = new Star( rand.nextInt( width ), rand.nextInt( height ), col );
        }
    }

    /**
     * Renders the <code>background</code>.
     * Uses hardware acceleration, if desired.
     *      
     * @return  the rendered background
     * @since Classic
     */
    public Image render()
    {
        // Create the background first.
        if ( image == null )
            init();

        // Render in hardware mode.
        if ( Settings.hardwareRendering )
        {
            do
            {
                // If the resolution has changed causing an incompatibility, re-create the VolatileImage.
                if ( ( (VolatileImage) image ).validate( Running.environment().getGraphicsConfiguration() ) == VolatileImage.IMAGE_INCOMPATIBLE )
                    init();

                // Draw the game's graphics.
                drawElements( image.getGraphics() );
            }
            while ( ( (VolatileImage) image ).contentsLost() );
        }
        // Render in software mode.
        else
        {
            // Draw the game's graphics.
            drawElements( image.getGraphics() );
        }

        return image;
    }

    /**
     * Draws the <code>background</code>: its <code>Star</code>s and <code>BackgroundMessage</code>s.
     * 
     * @param g the <code>Graphics</code> context to draw on.
     * @since December 23, 2007
     */
    private void drawElements( Graphics g )
    {
        // Fill with black.
        g.setColor( Color.black );
        g.fillRect( 0, 0, width, height );

        // Draw stars.
        for ( Star star : this.theStars )
            Running.environment().drawPoint( g, star.color, star.x, star.y );

        // Draw background messages.
        Iterator<BackgroundMessage> itr = starMessages.iterator();
        while ( itr.hasNext() )
        {
            BackgroundMessage m = itr.next();
            m.draw( g );
            if ( m.life-- <= 0 )
                itr.remove();
        }
    }

    /**
     * Writes a message onto the background.
     * 
     * @param message   the message to be written
     * @param x         the x coordinate where the message should be drawn
     * @param y         the y coordinate where the message should be drawn
     * @param col       the <code>Color</code> in which the message should be drawn
     * @since Classic
     */
    public void writeOnBackground( String message, int x, int y, Color col )
    {
        starMessages.add( new BackgroundMessage( x, y, message, col ) );
    }

    /**
     * The <code>Star</code> class is little more than an overblown
     * coordinate class and is used for storing the absolute locations of each Star.
     * 
     * @author Andy Kooiman
     * @since December 16, 2007
     */
    private static class Star
    {
        public int x,  y;

        public Color color;

        public Star( int x, int y, Color col )
        {
            this.x = x;
            this.y = y;
            this.color = col;
        }
    }

    /**
     * A message drawn on the star background.
     * @author Andy Kooiman
     * @since December 16, 2007
     */
    private class BackgroundMessage
    {
        public int x,  y;

        public double dy;

        public String message;

        public Color col;

        public int life = 70;

        public BackgroundMessage( int x, int y, String message, Color col )
        {
            this.x = x;
            this.y = y;
            this.message = message;
            this.col = col;
            dy = -RandNumGen.getStarInstance().nextDouble() * 4;
        }

        private void draw( Graphics gBack )
        {
            y += dy;
            Color c = new Color(
                    Math.min( col.getRed() * life / 70 + 80, 255 ),
                    col.getGreen() * life / 70,
                    col.getBlue() * life / 70 );            
            Running.environment().drawString( gBack, (int) ( x + 3 * Math.cos( life / 5.0 ) ), y, message, c );
        }
    }
}
