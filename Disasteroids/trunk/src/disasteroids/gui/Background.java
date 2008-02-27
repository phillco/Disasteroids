/*
 * DISASTEROIDS
 * Background.java
 */
package disasteroids.gui;

import disasteroids.*;
import java.awt.Color;
import java.awt.Font;
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
                image = AsteroidsFrame.frame().getGraphicsConfiguration().createCompatibleVolatileImage( width, height );
            else
                image = AsteroidsFrame.frame().createImage( width, height );
        }

        // Create the array of stars.
        Random rand = RandomGenerator.get();
        this.theStars = new Star[( width * height / ( rand.nextInt( 800 ) + 1000 ) ) / ( Settings.qualityRendering ? 1 : 3 )];
        for ( int star = 0; star < theStars.length; star++ )
        {
            int sat = rand.nextInt( 255 );
            int rgb[] = new int[3];
            for ( int i = 0; i < rgb.length; i++ )
            {
                int deviance = rand.nextInt( 90 );
                rgb[i] = Math.max( Math.min( sat + ( deviance - deviance / 2 ), 255 ), 0 );
            }

            //Color col = new Color( rgb[0], rgb[1], rgb[2] );
            Color col=Color.getHSBColor(rand.nextFloat(), rand.nextFloat()/3f, .7f+.3f*rand.nextFloat());
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
                if ( ( (VolatileImage) image ).validate( AsteroidsFrame.frame().getGraphicsConfiguration() ) == VolatileImage.IMAGE_INCOMPATIBLE )
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
        {
            // Move them.
            star.x += star.dx - AsteroidsFrame.frame().localPlayer().getDx() * star.depth;
            star.y += star.dy - AsteroidsFrame.frame().localPlayer().getDy() * star.depth;

            // Wrap them.
            star.checkWrap();

            AsteroidsFrame.frame().drawPoint( g, star.color, star.x, star.y );
        }


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

        public double dx,  dy;

        public Color color;

        public double depth;

        public Star( int x, int y, Color col )
        {
            this.x = x;
            this.y = y;
            this.color = col;

            // Simulated depth. Multiplied by the localPlayer's dx and dy to determine speed.
            depth = RandomGenerator.get().nextDouble() * 5;

            // Force some to the 'background'.
            if ( RandomGenerator.get().nextInt( 15 ) == 0 )
                depth /= 3;

            // Some stars also move.
            if ( RandomGenerator.get().nextInt( 10 ) == 0 )
            {
                dx = RandomGenerator.get().nextDouble() - 0.5;
                dy = RandomGenerator.get().nextDouble() - 0.5;
            }
        }

        public void checkWrap()
        {
            // Wrap to stay inside the level.
            if ( x <= Math.abs( dx ) )
                x += Game.getInstance().GAME_WIDTH - 1;
            if ( y <= Math.abs( dy ) )
                y += Game.getInstance().GAME_HEIGHT - 1;
            if ( x > Game.getInstance().GAME_WIDTH - Math.abs( dx ) )
                x -= Game.getInstance().GAME_WIDTH - 1;
            if ( y > Game.getInstance().GAME_HEIGHT - Math.abs( dy ) )
                y -= Game.getInstance().GAME_HEIGHT - 1;
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

        public int life,  lifeMax;

        public BackgroundMessage( int x, int y, String message, Color col )
        {
            this.x = x;
            this.y = y;
            this.message = message;
            this.col = col;

            life = lifeMax = RandomGenerator.get().nextInt( 30 ) + 40;
            dy = -RandomGenerator.get().nextDouble() * 4;
        }

        private void draw( Graphics gBack )
        {
            y += dy;
            Color c = new Color(
                    Math.min( col.getRed() * life / 70 + 80, 255 ),
                    col.getGreen() * life / lifeMax,
                    col.getBlue() * life / lifeMax );
            gBack.setFont(new Font("Century Gothic", Font.BOLD, 10));
            AsteroidsFrame.frame().drawString( gBack, (int) ( x + 3 * Math.cos( life / 5.0 ) ), y, message, c );
        }
    }
}