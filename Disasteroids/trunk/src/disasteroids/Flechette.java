/*
 * DISASTEROIDS
 * Flechette.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A fast but simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class Flechette extends Weapon.Unit
{
    private FlechetteManager env;

    private Color myColor;

    private boolean needsRemoval = false;

    private int age = 0;

    public Flechette( FlechetteManager env, int x, int y, double angle, double dx, double dy, Color col )
    {
        int speed = env.getSpeed();
        setLocation( x, y );
        setSpeed( dx + speed * Math.cos( angle ), dy - speed * Math.sin( angle ) );
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

    @Override
    public String getName()
    {
        return "Flachettespray";
    }
}
