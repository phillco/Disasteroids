/*
 * DISASTEROIDS
 * SniperRound.java
 */
package disasteroids;

import disasteroids.gui.AsteroidsFrame;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A very fast bullet fired by the sniper rifle.
 * @author Andy Kooiman
 */
class SniperRound extends Weapon.Unit
{
    private SniperManager parent;

    private double angle;

    private int damage;

    public SniperRound( SniperManager parent, Color color, double x, double y, double dx, double dy, double angle )
    {
        super( color, x, y, dx + parent.getSpeed() * Math.cos( angle ), dy - parent.getSpeed() * Math.sin( angle ) );
        this.angle = angle;
        this.parent = parent;
        damage = parent.getDamage();
    }

    public double getRadius()
    {
        return parent.getRadius();
    }

    public void explode()
    {
        damage *= .8;
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
        AsteroidsFrame.frame().drawLine( g, color, (int) getX(), (int) getY(), 10, Math.PI + angle );
    }

    public int getDamage()
    {
        return damage;
    }
}
