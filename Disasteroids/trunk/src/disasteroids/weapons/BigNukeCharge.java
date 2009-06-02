/**
 * DISASTEROIDS
 * BigNukeCharge.java
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
 * An explosive particle speewed from a <code>BigNuke</code>.
 * @author Phillip Cohen
 */
public class BigNukeCharge extends Unit
{
    protected BigNukeLauncher parent;

    /**
     * Random acceleration vectors.
     */
    private double ax, ay;

    /**
     * Size of the explosion. If 0, we've yet to explode.
     */
    private int explosionSize = 0;

    /**
     * The age at which we'll explode.
     */
    private int explosionAge, contractionAge, numChainReactionCharges;

    /**
     * Whether we've begun to shrink.
     */
    private boolean isReducing = false;

    public BigNukeCharge( BigNukeLauncher parent, Color color, double x, double y, double dx, double dy, double angle, int explosionAge )
    {
        super( color, x, y, dx + Util.getGameplayRandomGenerator().nextDouble() * 14 * Math.cos( angle ), dy - Util.getGameplayRandomGenerator().nextDouble() * 14 * Math.sin( angle ) );
        this.parent = parent;
        ax = Util.getGameplayRandomGenerator().nextDouble() * 0.1 - 0.05;
        ay = Util.getGameplayRandomGenerator().nextDouble() * 0.1 - 0.05;
        this.explosionAge = explosionAge;
        contractionAge = (int) ( explosionAge * ( 1.2 + Util.getGameplayRandomGenerator().nextDouble() * 0.3 ) );
        numChainReactionCharges = (int) ( 0.114 * ( parent.getBonusValue( parent.BONUS_CHAINREACTIONCHANCE ).getValue() + Util.getGameplayRandomGenerator().nextInt( 10 ) ) );
    }

    @Override
    public void act()
    {
        super.act();

      /*  // Emit a few particles.
        if ( Util.getGameplayRandomGenerator().nextInt( 3 ) == 0 )
            ParticleManager.addParticle( new Particle(
                    getX() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                    getY() + Util.getGameplayRandomGenerator().nextInt( 8 ) - 4,
                    Util.getGameplayRandomGenerator().nextInt( 4 ),
                    color,
                    Util.getGameplayRandomGenerator().nextDouble(),
                    Util.getGameplayRandomGenerator().nextAngle(),
                    20, 1 ) );*/

        // Waning.
        if ( isReducing )
        {
            explosionSize -= 8;

            // Chain reaction.
            if ( numChainReactionCharges > 0 )
            {
                numChainReactionCharges--;
                parent.addUnit( new BigNukeCharge( parent, color, getX(), getY(), getDx(), getDy(), Util.getGameplayRandomGenerator().nextAngle(), (int) ( explosionAge * 0.7 ) ) );
            }
        }
        // Waxing.
        else if ( age > explosionAge )
        {
            if ( age > contractionAge )
                isReducing = true;
            explosionSize += 5;
        }

        // Gone.
        if ( explosionSize < 0 )
            explode();
    }

    @Override
    public void remove()
    {
        parent.remove( this );
    }

    @Override
    public void move()
    {
        super.move();
        setVelocity( getDx() + ax, getDy() + ay );
        ax *= 0.99;
        ay *= 0.99;
        decelerate( .94 );
    }

    @Override
    public double getRadius()
    {
        return explosionSize;
    }

    @Override
    public void explode()
    {
        if ( explosionSize < 0 )
            parent.remove( this );
    }

    @Override
    public int getDamage()
    {
        return explosionSize > 0 ? 10 : 0;
    }

    public void draw( Graphics g )
    {
        MainWindow.frame().fillCircle( g, color, (int) getX(), (int) getY(), Math.max( 1, explosionSize ) );
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
        stream.writeInt( explosionAge );
        stream.writeInt( contractionAge );
        stream.writeInt( numChainReactionCharges );
        stream.writeInt( explosionSize );
        stream.writeBoolean( isReducing );
    }

    /**
     * Reads <code>this</code> from a stream for client/server transmission.
     */
    public BigNukeCharge( DataInputStream stream, BigNukeLauncher parent ) throws IOException
    {
        super( stream );
        ax = stream.readDouble();
        ay = stream.readDouble();
        explosionAge = stream.readInt();
        contractionAge = stream.readInt();
        numChainReactionCharges = stream.readInt();
        explosionSize = stream.readInt();
        isReducing = stream.readBoolean();

        this.parent = parent;
    }
}
