/*
 * DISASTEROIDS
 * Bullet.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A fast and simple bullet that dies on impact.
 * @author Andy Kooiman
 */
class Bullet extends Weapon.Unit
{
    private BulletManager parent;

    public Bullet( BulletManager parent, Color color, double x, double y, double dx, double dy, double angle )
    {
        super( color, x, y, dx + parent.getSpeed() * Math.cos( angle ), dy - parent.getSpeed() * Math.sin( angle ) );
        this.parent = parent;
    }

    @Override
    public void act()
    {
        super.act();
        if ( age > 50 )
            parent.remove( this );
    }

    public void draw( Graphics g )
    {
        AsteroidsFrame.frame().fillCircle( g, color, (int) getX(), (int) getY(), parent.getRadius() );
    }

    /**
     * Removes the bullet immediately.
     */
    public void explode()
    {
        parent.remove( this );
    }

    public double getRadius()
    {
        return parent.getRadius() + 1;
    }

    public int getDamage()
    {
        return parent.getDamage();
    }
}
