/*
 * DISASTEROIDS
 * BonusAsteroid.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import disasteroids.gui.ImageLibrary;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A darker <code>Asteroid</code> that gives bonuses when shot.
 * @author Andy Kooiman
 */
public class BonusAsteroid extends Asteroid
{
    /**
     * Constructs a new Asteroid from scratch.
     * 
     * @param x				the x coordinate
     * @param y				the y coordinate
     * @param dx			the x velocity
     * @param dy			the y velocity (up is negative)
     * @param size			the diameter
     * @param lifeMax                   total amount of life
     * @since Classic
     */
    public BonusAsteroid( double x, double y, double dx, double dy, int size, int lifeMax )
    {
        super( x, y, dx, dy, size, lifeMax );

        fill = Color.green;
        outline = Color.white;
    }

    /**
     * Constructs a new <code>Asteroid</code> from a parent <code>Asteroid</code>.
     * This is used when a missile splits an <code>Asteroid</code>.
     * 
     * @param parent	the parent <code>Asteroid</code> to kill from
     * @since Classic
     */
    public BonusAsteroid( Asteroid parent )
    {
        super( parent );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since December 29, 2007
     */
    public BonusAsteroid( DataInputStream stream ) throws IOException
    {
        super( stream );
    }

    @Override
    protected void kill()
    {
        Game.getInstance().createBonus( this );
        super.kill();
    }
    
    @Override
    public void draw(Graphics g)
    {
        AsteroidsFrame.frame().drawImage(g, ImageLibrary.getBonusAsteroid(),
                (int) getX(), (int)getY(), angle, radius*2.0/ImageLibrary.getBonusAsteroid().getWidth(null) );
    }
}
