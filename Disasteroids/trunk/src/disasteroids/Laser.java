/*
 * DISASTEROIDS
 * Laser.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 *  * @author Andy Kooiman
 */
class Laser extends Weapon.Unit
{
    private LaserManager env;

    private Color myColor;

    private boolean needsRemoval = false;
    
    private int life;

    private int length;
    
    private double angle;
    
    private Laser next;

    public Laser( LaserManager env, int x, int y, double angle, double dx, double dy, Color col )
    {
        setLocation( x, y );
        setSpeed( dx, dy );
        life=3;
        this.angle=angle;
     //   this.x1=x;
     //   this.y1=y;
     //   this.y2=(int) (y + env.length() * Math.sin(angle));
     //   this.x2=(int) (x + env.length() * Math.cos(angle));
        this.length=env.length();
        this.myColor = col;
        this.env = env;
    }
    
    public void setNext(Laser next)
    {
        this.next=next;
    }

    public int getRadius()
    {
        return env.getRadius();
    }

    public void explode()
    {
        if(next!=null)
            next.explode();
        env.remove(this);
    }

    public boolean needsRemoval()
    {
        if ( needsRemoval )
            env.remove( this );
        return needsRemoval;
    }

    public void act()
    {
       this.move();
        if (life-- <= 0)
            env.remove( this );
       // if(life<=0)
        //    needsRemoval=true;
    }

    public void draw( Graphics g )
    {
        
        AsteroidsFrame.frame().drawLine( g, myColor, (int)getX(), (int)getY(), length, angle);

    }

    public int getDamage()
    {
        return env.getDamage();
    }
}
