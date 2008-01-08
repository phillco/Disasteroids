/*
 * GameObject.java
 */

/**
 *
 * @author Phillip Cohen
 * @since Jan 5, 2008
 */
public abstract class GameObject implements GameElement
{
    /**
     * Our location and speed data.
     * 
     * @since Jan 5, 2008
     */
    private double x,  y,  dx,  dy;

    /**
     * Whether we should be destroyed.
     * 
     * @since Jan 5, 2008
     */
    private boolean shouldRemove;

    /**
     * Whether we should be destroyed.
     * 
     * @return whether we need to be removed from the game immediately
     * @since Jan 5, 2008
     */
    public boolean shouldRemove()
    {
        return shouldRemove;
    }

    /**
     * Kills this and marks us for removal.
     * 
     * @since Jan 5, 2008
     */
    void destroy()
    {
        shouldRemove = true;
    }

    /**
     * Returns whether we should wrap across the game world when we get out of bounds.
     * 
     * @return  whether to wrap
     * @since Jan 5, 2008
     */
    abstract boolean wrapWhenOutside();

    /**
     * Returns whether we should be destroyed when we get out of the game's bounds.
     * 
     * @return  whether to be destroyed
     * @since Jan 5, 2008
     */
    abstract boolean destroyWhenOutside();

    /**
     * Checks if we're outside the game world, and takes appropiate action.
     * 
     * @since Jan 5, 2008
     */
    void outsideLogic()
    {
        // Wrap to stay inside the level.
        if ( wrapWhenOutside() )
        {
            if ( x < 0 )
                addToX( Game.getInstance().GAME_WIDTH - 1 );
            if ( y < 0 )
                addToY( Game.getInstance().GAME_HEIGHT - 1 );
            if ( x > Game.getInstance().GAME_WIDTH )
                addToY( -( Game.getInstance().GAME_WIDTH - 1 ) );
            if ( y > Game.getInstance().GAME_HEIGHT )
                addToY( -( Game.getInstance().GAME_HEIGHT - 1 ) );
        }
        // Or self destruct!
        else if ( destroyWhenOutside() )
        {

        }
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
        setX(  getX() + addX );
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
    
    public void setLocation( double x, double y)
    {
        setX(x);
        setY(y);
    }
}
