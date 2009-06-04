/*
 * DISASTEROIDS
 * GameObject.java
 */
package disasteroids;

import disasteroids.networking.Client;
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
     * This object's ID.
     */
    private long id;

    /**
     * Our location and speed data.
     */
    private double x, y, dx, dy;

    public GameObject()
    {
        if ( !Client.is() )
            id = Game.getInstance().getObjectManager().getNewId();
    }

    public GameObject( double x, double y, double dx, double dy )
    {
        this();
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
    public void move()
    {
        addToX( getDx() );
        addToY( getDy() );
    }

    /**
     * Called whenever we get sucked into in a black hole.
     * By default, it just removes the object. You should override this whenever Game.removeObject() will not work.
     * 
     * @see BlackHole
     */
    public void inBlackHole()
    {
        Game.getInstance().getObjectManager().removeObject( this );
    }

    public double getDx()
    {
        return dx;
    }

    /**
     * Returns the x-coordinate of the point where this object "shoots" - that is, where its bullets are launched.
     * If not overridden, it just returns getX().
     */
    public double getFiringOriginX()
    {
        return getX();
    }

    /**
     * Returns the y-coordinate of the point where this object "shoots" - that is, where its bullets are launched.
     * If not overridden, it just returns getY().
     */
    public double getFiringOriginY()
    {
        return getY();
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

    /**
     * Projects where this game object will be in the future, if it goes this speed
     * @param time how long in the future to look
     * @return the x coordinate of the future position
     */
    public double projectX(int time)
    {
        return getX() + time * getDx();
    }

    /**
     * Projects where this game object will be in the future, if it goes this speed
     * @param time how long in the future to look
     * @return the x coordinate of the future position
     */
    public double projectY(int time)
    {
        return getY() + time * getDy();
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

    /**
     * Gives <code>this</code> a new velocity, disregarding the old velocity.
     * 
     * @param dx The new x velocity
     * @param dy The new y velocity
     */
    public void setVelocity( double dx, double dy )
    {
        setDx( dx );
        setDy( dy );
    }

    /**
     * Gives <code>this</code> a new speed, but preserves the sign of dx and dy.
     * If dx or dy is zero, their sign is treated as positive.
     * 
     * @param dx The new magnitude of the x velocity
     * @param dy The new magnitude of the y velocity
     */
    public void setSpeed( double dx, double dy )
    {
        int signX = 1, signY = 1;
        if ( getDx() < 0 )
            signX = -1;
        if ( getDy() < 0 )
            signY = -1;
        setVelocity( signX * Math.abs( dx ), signY * Math.abs( dy ) );
    }

    /**
     * Multiplies this <code>GameObject</code>'s dx and dy by the given factor.
     * A parameter of 0 will stop <code>this</code> immediately; a parameter of 1.0
     * will have no effect, and any number above 1 will be an increase in speed.
     * 
     * @param factor a number between 0.0 and 1.0 representing the amount to slow down.
     */
    public void decelerate( double factor )
    {
        setVelocity( getDx() * factor, getDy() * factor );
    }

    public double getSpeed()
    {
        return Math.sqrt( getDx() * getDx() + getDy() * getDy() );
    }

    public void flattenPosition( DataOutputStream stream ) throws IOException
    {
        stream.writeDouble( getX() );
        stream.writeDouble( getY() );
        stream.writeDouble( getDx() );
        stream.writeDouble( getDy() );
    }

    public void restorePosition( DataInputStream stream ) throws IOException
    {
        setLocation( stream.readDouble(), stream.readDouble() );
        setVelocity( stream.readDouble(), stream.readDouble() );
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
        stream.writeLong( getId() );
        flattenPosition( stream );

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
        id = stream.readLong();
        restorePosition( stream );
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

    public long getId()
    {
        return id;
    }
}
