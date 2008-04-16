/*
 * DISASTEROIDS
 * Particle.java
 */
package disasteroids.gui;


import disasteroids.GameElement;
import disasteroids.RandomGenerator;
import disasteroids.Settings;
import java.awt.Color;
import java.awt.Graphics;

/**
 * A simple particle used in effects. 
 * @author Phillip Cohen, Andy Kooiman
 */
public class Particle implements GameElement
{
    /**
     * The time until this <code>Particle</code> will be removed.
     * @since Classic
     */
    public double life;

    /**
     * The original life of this <code>Particle</code>.
     * @since Classic
     */
    public double life_max;

    /**
     * The current <code>Color</code> of this <code>Particle</code>.
     * @since Classic
     */
    public Color color;

    /**
     * The coordinates of this <code>Particle</code>.
     * @since Classic
     */
    public double x,  y;

    /**
     * The diameter of this <code>Particle</code>.
     * @since Classic
     */
    public double size;

    /**
     * The x and y components of velocity.
     * @since Classic
     */
    public double dx,  dy;

    /**
     * The <code>Color</code> represented as an array of red, green, and blue, each as a double between 0 and 1.0.
     * @since Classic
     */
    public double[] rgb = new double[3];

    /**
     * The rate at which we grow or shrink.
     * @since January 16, 2008
     */
    private double deltaSize;

    /**
     * Whether this particle is of the few drawn in software.
     * @since March 29, 2008
     */
    private boolean drawInSpeedRendering;

    /**
     * Creates a new <code>Particle</code>.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param size The diameter.
     * @param c The <code>Color</code>.
     * @param speed The speed.
     * @param angle The direction.
     * @param lifemax The longest this <code>Particle</code> can live.
     * @param lifemin The least amount of time this <code>Particle</code> can live.
     * @author Phillip Cohen, Andy Kooiman
     * @since Classic
     */
    public Particle( double x, double y, double size, Color c, double speed, double angle, double lifemax, double lifemin )
    {
        life = ( RandomGenerator.get().nextDouble() ) * lifemax + lifemin;
        this.life_max = life;
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = c;
        this.dx = speed * Math.cos( angle );
        this.dy = -speed * Math.sin( angle );

        rgb[0] = c.getRed();
        rgb[1] = c.getGreen();
        rgb[2] = c.getBlue();

        deltaSize = RandomGenerator.get().nextDouble() - 0.6;
        drawInSpeedRendering = RandomGenerator.get().nextInt( 10 ) == 0;

    }

    /**
     * Returns whether we should be removed.
     * 
     * @return  whether this needs to be removed
     * @since Classic
     */
    public boolean shouldRemove()
    {
        return ( life <= 1 );
    }

    public void act()
    {
        life--;
        x += dx;
        y += dy;
        size += deltaSize;
        dx *= 0.998;
        dy *= 0.998;
    }

    public void draw( Graphics g )
    {
        if ( !drawInSpeedRendering && !Settings.isQualityRendering() )
            return;

        double fadePct = life / life_max;
        color = new Color( (int) ( rgb[0] * fadePct ), (int) ( rgb[1] * fadePct ), (int) ( rgb[2] * fadePct ) );
        AsteroidsFrame.frame().fillCircle( g, color, (int) x, (int) y, (int) size / 2 );
    }
}
