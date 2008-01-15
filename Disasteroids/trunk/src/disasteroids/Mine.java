/*
 * DISASTEROIDS
 * Mine.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Color;
import java.awt.Graphics;

/**
 * A weapon that sits without moving or exploding, until something strays too close.
 * At this point it explodes violently!
 * 
 * @author Andy Kooiman
 */
public class Mine implements Weapon
{
    /**
     * The coordinates of the center of this <code>Mine</code>
     */
    private int x,  y;

    /**
     * How long until this <code>Mine</code> is removed.  It will explode on its
     * own if it has 5 or fewer timesteps to live.
     * 
     * If above 9900, the mine is still arming and is harmless.
     */
    private int life;

    /**
     * Whether this <code>Mine</code> is currently exploding.
     */
    private boolean isExploding;

    /**
     * The color of the outside ring of this <code>Mine</code>
     */
    private Color color;

    /**
     * Whether this <code>Mine</code> needs removing.
     */
    private boolean needsRemoval;

    /**
     * Creates a new <code>Minde</code>
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param col The <code>Color</code> of the outside ring.
     * @author Andy Kooiman
     */
    Mine( int x, int y, Color col )
    {
        this.x = x;
        this.y = y;
        this.isExploding = false;
        this.needsRemoval = false;
        this.color = col;
        life = 10000;
    }

    /**
     * Returns the x coordinate.
     * 
     * @return The x coordinate.
     * 
     */
    public double getX()
    {
        return x;
    }

    /**
     * Returns the y coordinate.
     * 
     * @return The y coordinate.
     * 
     */
    public double getY()
    {
        return y;
    }

    /**
     * Returns the distance from the center of this mine that the mine is dangerous from.
     * 
     * @return The current radius.
     */
    public int getRadius()
    {
        return isExploding ? ( 5 - life ) * 20 : 10;
    }

    /**
     * Detonates this <code>Mine</code>
     * 
     */
    public void explode()
    {
        if ( !isExploding && life < 9900 )
        {
            life = 5;
            isExploding = true;
        }
    }

    /**
     * Resets this <code>Mine</code>'s life counter
     * 
     * @param newLife The new amount of life, in timesteps
     */
    public void setLife( int newLife )
    {
        life = newLife;
    }

    /**
     * Returns whether this <code>Mine</code> thinks it should be removed.
     * 
     * @return whetyer this <code>Mine</code> thinks it should be removed.
     */
    public boolean needsRemoval()
    {
        return needsRemoval;
    }

    /**
     * Allows this <code>Mine</code> to act, which just includes a check for if 
     * it should be removed or not.
     */
    public void act()
    {
        life -= 2;
        if ( life <= 0 )
            needsRemoval = true;
    }

    /**
     * Returns the damage caused by this <code>Mine</code>.
     * 
     * @return the damage caused by this <code>Mine</code>.
     */
    public int getDamage()
    {
        if ( life < 9900 )
            return 10000;
        else
            return 0;
    }

    /**
     * Draws this <code>Mine</code> in the given context.
     * 
     * @param g The <code>Graphics</code> context in which to draw.
     */
    public void draw( Graphics g )
    {
        if ( life > 9900 )
        {
            double multiplier = ( 10000 - life ) / 100.0;
            Color outline = new Color( (int) ( color.getRed() * multiplier ), (int) ( color.getGreen() * multiplier ), (int) ( color.getBlue() * multiplier ) );
            AsteroidsFrame.frame().fillCircle( g, outline, x, y, 10 );
            AsteroidsFrame.frame().fillCircle( g, Color.black, x, y, 4 );
        }
        else if ( !isExploding )
        {
            AsteroidsFrame.frame().fillCircle( g, color, x, y, 10 );
            AsteroidsFrame.frame().fillCircle( g, Color.black, x, y, 4 );
        }
        else
        {
            AsteroidsFrame.frame().fillCircle( g, color, x, y, ( 5 - life ) * 20 );
        }
    }
}
