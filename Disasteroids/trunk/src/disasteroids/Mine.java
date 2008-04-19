/*
 * DISASTEROIDS
 * Mine.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

/**
 * A weapon that sits without moving or exploding, until something strays too close.
 * At this point it explodes violently!
 * 
 * @author Andy Kooiman
 */
public class Mine extends Weapon.Unit
{
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
     * Stores whether or not this <code>Mine</code> is accelerating towards a 
     * target
     */
    private boolean shouldAccelerate = false;

    private MineManager env;

    /**
     * Creates a new <code>Minde</code>
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param col The <code>Color</code> of the outside ring.
     * @author Andy Kooiman
     */
    Mine( int x, int y, double dx, double dy, Color col, MineManager env )
    {
        setLocation( x, y );
        setSpeed(dx, dy);
        this.isExploding = false;
        this.needsRemoval = false;
        this.color = col;
        life = 10000;
        this.env = env;
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
     * @return whether this <code>Mine</code> thinks it should be removed.
     */
    public boolean needsRemoval()
    {
        if ( needsRemoval )
            env.remove( this );
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
        {
            needsRemoval = true;
            env.remove( this );
        }
        
        move();
        setSpeed(getDx()*.95, getDy()*.95);
        
        
        shouldAccelerate=false;
        if (life > 9900 )
            return;
        Set<GameObject> close=new HashSet<GameObject>();
        for(Asteroid ast : Game.getInstance().getAsteroidManager().getAsteroids() )
        {
            if(Math.pow( ast.getX() - getX(), 2 ) + Math.pow( ast.getY() - getY(), 2 ) < env.sight() * env.sight() )
            {
                shouldAccelerate=true;
                close.add( ast );
            }
        }
        for(GameObject go : Game.getInstance().baddies)
        {
            if(Math.pow( go.getX() - getX(), 2 ) + Math.pow( go.getY() - getY(), 2 ) < env.sight() * env.sight() )
            {
                shouldAccelerate=true;
                close.add( go );
            }
        }
        if( !shouldAccelerate )
            return;
        for( GameObject go : close )
        {
            double angle = Math.atan((go.getY()-getY())/(go.getX()-getX()));
            if(go.getX()<getX())
                angle+=Math.PI;
            double magnitude = 10.0 / Math.sqrt( ( Math.pow( go.getX() - getX() , 2 ) + Math.pow( go.getY() - getY() , 2 ) ) );
            magnitude=Math.min(magnitude, 1);//regulate the acceleration for (essentially) dividing by zero
            setSpeed(getDx()+magnitude*Math.cos(angle), getDy()+magnitude*Math.sin(angle));
        }
    }

    /**
     * Returns the damage caused by this <code>Mine</code>.
     * 
     * @return the damage caused by this <code>Mine</code>.
     */
    public int getDamage()
    {
        if ( life < 9900 )
            return 200;
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
            AsteroidsFrame.frame().fillCircle( g, outline, (int) getX(), (int) getY(), 10 );
     //       AsteroidsFrame.frame().fillCircle( g, Color.black, (int) getX(), (int) getY(), 4 );
        }
        else if ( !isExploding )
        {
            AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), 10 );
            AsteroidsFrame.frame().fillCircle( g, shouldAccelerate ? Color.red : Color.black, (int) getX(), (int) getY(), 4 );
        }
        else
            AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), ( 5 - life ) * 20 );
    }
    
    public boolean isExploding()
    {
        return isExploding;
    }
    
    public boolean isArmed()
    {
        return life < 9900;
    }

    @Override
    public String getName()
    {
        return "Mine";
    }
}
