/*
 * DISASTEROIDS
 * GameObject.java
 */
package disasteroids;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A gameplay object that wraps.
 * @author Phillip Cohen
 * @since January 5, 2008
 */
public abstract class GameObject implements GameElement
{
    /**
     * Unique ID for this class. Used for C/S.
     * @since April 11, 2008
     */
    public static final int TYPE_ID = -1;
    
    /**
     * Our location and speed data.
     * 
     * @since January 5, 2008
     */
    private double x,  y,  dx,  dy;

    public GameObject()
    {
    }

    public GameObject( double x, double y, double dx, double dy )
    {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns whether we should wrap across the game world when we get out of bounds.
     * 
     * @return  whether to wrap
     * @since January 5, 2008
     */
    boolean wrapWhenOutside()
    {
        return true;
    }

    /**
     * Updates our <code>x</code> and <code>y</code> based on our velocity (<code>dx</code> and <code>dy</code>).
     * 
     * @since January 5, 2008
     */
    void move()
    {
        addToX( getDx() );
        addToY( getDy() );
    }

    public double getDx()
    {
        return dx;
    }

    public void setDx( double dx )
    {
        this.dx = dx;
    }

    public double getDy()
    {
        return dy;
    }

    public void setDy( double dy )
    {
        this.dy = dy;
    }

    public double getX()
    {
        return x;
    }

    public void setX( double x )
    {
        this.x = ( x + Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
    }

    public void addToX( double addX )
    {
        setX( getX() + addX );
    }

    public double getY()
    {
        return y;
    }

    public void setY( double y )
    {
        this.y = ( y + Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
    }

    public void addToY( double addY )
    {
        setY( getY() + addY );
    }

    public void setLocation( double x, double y )
    {
        setX( x );
        setY( y );
    }

    public void setSpeed( double dx, double dy )
    {
        setDx( dx );
        setDy( dy );
    }

    public double getSpeed()
    {
        return Math.sqrt( getDx() * getDx() + getDy() * getDy() );
    }

    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since March 7, 2008
     */
    public void flatten( DataOutputStream stream ) throws IOException
    {
        stream.writeDouble( getX() );
        stream.writeDouble( getY() );
        stream.writeDouble( getDx() );
        stream.writeDouble( getDy() );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream the stream to write to
     * @throws java.io.IOException 
     * @since March 7, 2008
     */
    public void restore( DataInputStream stream ) throws IOException
    {
        setLocation( stream.readDouble(), stream.readDouble() );
        setSpeed( stream.readDouble(), stream.readDouble() );
    }

    /**
     * Creates <code>this</code> from a stream for client/server transmission.
     * 
     * @param stream    the stream to read from (sent by the server)
     * @throws java.io.IOException 
     * @since March 7, 2008
     */
    public GameObject( DataInputStream stream ) throws IOException
    {
        restore( stream );
    }
    
    public int getTypeId()
    {
        return TYPE_ID;
    }
}
