/*
 * DISASTEROIDS
 * ParticleManager.java
 */
package disasteroids.gui;

import disasteroids.*;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Game-wide manager of <code>Particle</code>s.
 * @author Phillip Cohen, Andy Kooiman
 */
public class ParticleManager
{
    /**
     * A list of all current <code>Particle</code>s.
     * @since Classic
     */
    private static ConcurrentLinkedQueue<Particle> allParticles = new ConcurrentLinkedQueue<Particle>();
    
    /**
     * An estimated count of the <code>Particles</code>, since allParticles#size() is an O(n) operation
     * @see ConcurrentLinkedQueue#allParticles
     */
    private static int particleCount = 0;
    

    /**
     * Adds the specified <code>Particle</code>.
     * 
     * @param p the <code>Particle</code> to be added
     * @since Classic
     */
    public static void addParticle( Particle p )
    {   //always add if less than 1000; less likely if there are already that many in the list.
        if( Util.getGraphicsRandomGenerator().nextInt( Math.max( particleCount , 1 ) ) < 400 )
        {
            allParticles.add( p );
            ++particleCount;
        }
    }

    /**
     * Instructs all <code>Particle</code>s to act and removes old ones. 
     * 
     * @since Classic
     */
    public static void act()
    {
        Iterator<Particle> itr = allParticles.iterator();
        while ( itr.hasNext() )
        {
            Particle p = itr.next();
            if ( p.shouldRemove() )
            {
                itr.remove();
                --particleCount;
            }
            else
                p.act();
        }
    }

    /**
     * Draws all particles.
     * 
     * @since Classic
     */
    public static void draw( Graphics g )
    {
        for ( Particle p : allParticles )
            p.draw( g );
    }

    /**
     * Removes all particles.
     * 
     * @since March 9, 2008
     */
    public static void clear()
    {
        allParticles.clear();
        particleCount=0;
    }

    public static void createSmoke( double x, double y, double amount )
    {
        for ( int i = 0; i < getNumberToMake( amount ); i++ )
        {
            addParticle( new Particle( x, y, Util.getGraphicsRandomGenerator().nextInt( 5 ) + 2,
                                       Util.getGraphicsRandomGenerator().nextBoolean() ? Color.gray : Color.darkGray,
                                       Util.getGraphicsRandomGenerator().nextDouble() * 3 + 1, Util.getGraphicsRandomGenerator().nextDouble() * 1.6 + 0.3,
                                       50, 30 ) );
        }
    }

    public static void createFlames( double x, double y, double amount )
    {
        for ( int i = 0; i < getNumberToMake( amount ); i++ )
        {
            addParticle( new Particle( x, y, Util.getGraphicsRandomGenerator().nextInt( 5 ) + 2,
                                       new Color( (float) ( Util.getGraphicsRandomGenerator().nextDouble() * 0.4 + 0.6 ),
                                                  (float) ( Util.getGraphicsRandomGenerator().nextDouble() * 0.3 + 0.2 ), 0.01f, 1 ),
                                       Util.getGraphicsRandomGenerator().nextDouble() * 3 + 1, Util.getGraphicsRandomGenerator().nextDouble() * 1.6 + 0.3,
                                       50, 30 ) );
        }
    }

    private static int getNumberToMake( double amountScalar )
    {
        if ( amountScalar < 0 )
            return Util.getGraphicsRandomGenerator().nextDouble() < amountScalar ? 1 : 0;
        else
            return (int) amountScalar;
    }
}
