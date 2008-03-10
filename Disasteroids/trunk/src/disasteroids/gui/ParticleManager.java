/*
 * DISASTEROIDS
 * ParticleManager.java
 */
package disasteroids.gui;

import disasteroids.*;
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
     * A list of all current <code>Particle</code>s.
     * @since Classic
     */
    private static ConcurrentLinkedQueue<Particle> allParticles = new ConcurrentLinkedQueue<Particle>();

    /**
     * Adds the specified <code>Particle</code>.
     * 
     * @param p the <code>Particle</code> to be added
     * @since Classic
     */
    public static void addParticle( Particle p )
    {
        allParticles.add( p );
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
                itr.remove();
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
        if ( !Settings.qualityRendering )
            return;

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
    }
}
