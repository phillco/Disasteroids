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
class Bullet implements Weapon, GameElement
{
    private double x,  y;

    private double dx,  dy;

    private BulletManager env;

    private Color myColor;

    private boolean needsRemoval = false;

    private int age = 0;

    private int damage = 10;

    public Bullet( BulletManager env, int x, int y, double angle, double dx, double dy, Color col )
    {
        this.x = x;
        this.y = y;
        this.dx = dx + env.getSpeed() * Math.cos( angle );
        this.dy = dy - env.getSpeed() * Math.sin( angle );
        this.myColor = col;
        this.env = env;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public int getRadius()
    {
        return env.getRadius() + 1;
    }

    public void explode()
    {
        needsRemoval = true;
    }

    public boolean needsRemoval()
    {
        return needsRemoval;
    }

    public void act()
    {
        age++;
        if ( age > 50 )
            needsRemoval = true;
        move();
        checkWrap();
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().fillCircle( g, myColor, (int) x, (int) y, env.getRadius() );
    }

    private void move()
    {
        x += dx;
        y += dy;
        checkWrap();
    }

    /**
     * Checks to see if <code>this</code> has left the screen and adjusts accordingly.
     * @author Andy Kooiman
     * @since December 16, 2007
     */
    private void checkWrap()
    {
        // Wrap to stay inside the level.
        if ( x < 0 )
            x += Game.getInstance().GAME_WIDTH - 1;
        if ( y < 0 )
            y += Game.getInstance().GAME_HEIGHT - 1;
        if ( x > Game.getInstance().GAME_WIDTH )
            x -= Game.getInstance().GAME_WIDTH - 1;
        if ( y > Game.getInstance().GAME_HEIGHT )
            y -= Game.getInstance().GAME_HEIGHT - 1;
    }

    public int getDamage()
    {
        return env.getDamage();
    }
}
