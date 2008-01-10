/*
 * DISASTEROIDS
 * GameObject.java
 */

/**
 * A gameplay object that wraps.
 * @author Phillip Cohen
 * @since January 5, 2008
 */
public abstract class GameObject implements GameElement
{
    /**
     * Our location and speed data.
     * 
     * @since January 5, 2008
     */
    private double x,  y,  dx,  dy;

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
}
