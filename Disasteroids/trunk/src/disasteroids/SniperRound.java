/*
 * DISASTEROIDS
 * SniperRound.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A fast but simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class SniperRound extends Weapon.Unit
{
    private SniperManager env;

    private Color myColor;

    private boolean needsRemoval = false;

    private int age = 0;
    
    private double angle;
    
    private int damage;

    public SniperRound( SniperManager env, int x, int y, double angle, double dx, double dy, Color col )
    {
        setLocation( x, y );
        this.angle=angle;
        setSpeed( dx + env.getSpeed() * Math.cos( angle ), dy - env.getSpeed() * Math.sin( angle ) );
        this.myColor = col;
        this.env = env;
        damage=env.getDamage();
    }

    public int getRadius()
    {
        return env.getRadius();
    }

    public void explode()
    {
        damage*=.8;
    }

    public boolean needsRemoval()
    {
        if ( needsRemoval )
            env.remove( this );
        return needsRemoval;
    }

    public void act()
    {
        age++;
        if ( age > 50 )
        {
            needsRemoval = true;
            env.remove( this );
        }
        move();
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().drawLine(g, myColor, (int) getX(), (int) getY(), 10,Math.PI+ angle);
    }

    public int getDamage()
    {
        return damage;
    }
}
