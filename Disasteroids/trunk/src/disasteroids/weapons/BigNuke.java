/*
 * DISASTEROIDS
 * BigNuke.java
 */
package disasteroids.weapons;

import disasteroids.*;
import disasteroids.gui.MainWindow;
import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An initially harmless projectile launched by a <code>BigNukeLauncher</code>.
 * After a short time it releases dozens of not-so-harmless <code>BigNukeCharge</code>s, which then explode.
 * @author Phillip Cohen
 */
public class BigNuke extends Unit
{
    protected BigNukeLauncher parent;

    /**
     * Random acceleration vectors.
     */
    private double ax,  ay;

    /**
     * How many charge we've spewed so far.
     */
    private int chargesDeployed = 0;

    public BigNuke( BigNukeLauncher parent, Color color, double x, double y, double dx, double dy, double angle )
    {
        super( color, x, y, dx + 6 * Math.cos( angle ), dy - 6 * Math.sin( angle ) );
        this.parent = parent;
        ax = Util.getGameplayRandomGenerator().nextDouble() / 8 - 1 / 16.0;
        ay = Util.getGameplayRandomGenerator().nextDouble() / 8 - 1 / 16.0;
    }

    @Override
    public void act()
    {
        super.act();
        setSpeed( Math.min( 8,  Math.abs( getDx() + ax ) ) , Math.min( 8, Math.abs( getDy() + ay ) ) );
        decelerate(.98);
        ax *= 0.98;
        ay *= 0.98;

        for ( int i = 0; i < 18; i++ )
            ParticleManager.addParticle( new Particle(
                                         getX() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                                         getY() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                                         Util.getGameplayRandomGenerator().nextInt( 4 ),
                                         color,
                                         Util.getGameplayRandomGenerator().nextDouble(),
                                         Util.getGameplayRandomGenerator().nextAngle(),
                                         20, 1 ) );

        if ( age > 30 )
        {
            for ( int i = 0; i < 9; i++ )
            {
                parent.addUnit( new BigNukeCharge( parent, color, getX(), getY(), getDx(), getDy(), Math.PI * 2 * Util.getGameplayRandomGenerator().nextDouble(), 30 + Util.getGameplayRandomGenerator().nextInt( 15 ) ) );
                ++chargesDeployed;
            }

            if ( chargesDeployed > 140 )
                parent.remove( this );
        }

    }

    @Override
    public double getRadius()
    {
        return 0;
    }

    @Override
    public void explode()
    {
        return;
    }

    @Override
    public int getDamage()
    {
        return 0;
    }

    @Override
    public void remove()
    {
        parent.remove( this );
    }

    public void draw( Graphics g )
    {
        MainWindow.frame().drawOutlinedCircle( g, color, color.darker(), (int) getX(), (int) getY(), 8 );
        MainWindow.frame().drawOutlinedCircle( g, color.darker().darker(), color.darker(), (int) getX(), (int) getY(), 6 );
        MainWindow.frame().drawOutlinedCircle( g, color.brighter(), color, (int) getX(), (int) getY(), 2 );
    }

    //                                                                            \\
    // ------------------------------ NETWORKING -------------------------------- \\
    //                                                                            \\
    /**
     * Writes <code>this</code> to a stream for client/server transmission.
     */
    @Override
    public void flatten( DataOutputStream stream ) throws IOException
    {
        super.flatten( stream );
        stream.writeDouble( ax );
        stream.writeDouble( ay );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public BigNuke( DataInputStream stream, BigNukeLauncher parent ) throws IOException
    {
        super( stream );
        ax = stream.readDouble();
        ay = stream.readDouble();

        this.parent = parent;
    }
}
