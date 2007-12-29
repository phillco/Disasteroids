/*
 * DISASTEROIDS
 * ParticleManager.java
 */

import java.awt.Graphics;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Game-wide manager of <code>Particle</code>s.
 * @author Phillip Cohen, Andy Kooiman
 */
public class ParticleManager implements Serializable
{

    /**
     * A list of all <code>Particle</code>s of all <code>Ship</code>s.
     * @since Classic
     */
    private static ConcurrentLinkedQueue<Particle> allParticles = new ConcurrentLinkedQueue<Particle>();

    /**
     * Adds the specified <code>Particle</code>.
     * @param p The <code>Particle</code> to be added.
     * @since Classic
     */
    public static void addParticle( Particle p )
    {
        allParticles.add( p );
    }

    public static void act()
    {
        Iterator<Particle> itr = allParticles.iterator();
        while ( itr.hasNext() )
        {
            Particle p = itr.next();
            if ( p.shouldRemove() )
                itr.remove();
            else
                p.act();
        }
    }

    /**
     * Instructs all <code>Particle</code>s to act, removes old ones, and draws the rest.
     * @author Phillip Cohen
     * @since Classic
     */
    public static void draw( Graphics g )
    {
        if ( !Settings.qualityRendering )
            return;

        for ( Particle p : allParticles )
            p.draw( g );
    }
}
