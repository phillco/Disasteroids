/*
 * DISASTEROIDS
 * Bullet.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A fast but simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class Bullet extends Weapon.Unit
{
    private BulletManager env;

    private Color myColor;

    private boolean needsRemoval = false;

    private int age = 0;

    public Bullet( BulletManager env, int x, int y, double angle, double dx, double dy, Color col )
    {
        setLocation( x, y );
        setSpeed( dx + env.getSpeed() * Math.cos( angle ), dy - env.getSpeed() * Math.sin( angle ) );
        this.myColor = col;
        this.env = env;
    }

    public int getRadius()
    {
        return env.getRadius() + 1;
    }

    public void explode()
    {
        env.remove( this );
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
        AsteroidsFrame.frame().fillCircle( g, myColor, (int) getX(), (int) getY(), env.getRadius() );
    }

    public int getDamage()
    {
        return env.getDamage();
    }
}
