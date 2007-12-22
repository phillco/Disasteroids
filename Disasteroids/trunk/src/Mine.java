/*
 * DISASTEROIDS
 * Mine.java
 */

import java.awt.Color;
import java.awt.Graphics;


/**
 * A type of weapon which just sits without moving or exploding, until an
 * <code>Asteroid</code> strays too close, at which point it explodes violently.
 * 
 * @author Andy Kooiman
 * 
 */
public class Mine implements Weapon {
    
    /**
     * The coordinates of the center of this <code>Mine</code>
     */
    private int x, y;
    
    /**
     * How long until this <code>Mine</code> is removed.  It will explode on its
     * own if it has 5 or fewer timesteps to live.
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
    Mine(int x, int y, Color col) {
        this.x=x;
        this.y=y;
        this.isExploding=false;
        this.needsRemoval=false;
        this.color=col;
        life=10000;
    }
    
    /**
     * Returns the x coordinate.
     * 
     * @return The x coordinate.
     * 
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y coordinate.
     * 
     * @return The y coordinate.
     * 
     */
    public int getY() {
        return y;
    }

    /**
     * Returns the distance from the center of this mine that the mine is dangerous from.
     * 
     * @return The current radius.
     */
    public int getRadius() {
        return isExploding?(5-life)*20:10;
    }

    /**
     * Detonates this <code>Mine</code>
     * 
     */
    public void explode() {
        if(!isExploding)
        {
           life=5;
           isExploding=true;
        }
    }

    /**
     * Returns whether this <code>Mine</code> thinks it should be removed.
     * 
     * @return whetyer this <code>Mine</code> thinks it should be removed.
     */
    public boolean needsRemoval() {
        return needsRemoval;
    }

    /**
     * Allows this <code>Mine</code> to act, which just includes a check for if 
     * it should be removed or not.
     */
    public void act() {
        if(life--<=0)
            needsRemoval=true;
    }

    /**
     * Returns the damage caused by this <code>Mine</code>.
     * 
     * @return the damage caused by this <code>Mine</code>.
     */
    public int getDamage() {
        return 10000;
    }

    /**
     * Draws this <code>Mine</code> in the given context.
     * 
     * @param g The <code>Graphics</code> context in which to draw.
     */
    public void draw(Graphics g) {
        if(!isExploding)
        {
            Running.environment().fillCircle(g, color, x, y, 10);
            Running.environment().fillCircle(g, Color.black, x, y, 4);
        }else
        {
            Running.environment().fillCircle(g, color, x, y, (5-life)*20);
        }
    }

}
