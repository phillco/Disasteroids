/*
 * DISASTEROIDS
 * ParticleManager.java
 */

import java.awt.Graphics;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Game-wide manager of <code>Particle</code>s.
 * @author Phillip Cohen, Andy Kooiman
 */
public class ParticleManager // implements GameElement
{
    /**
     * A list of all <code>Particle</code>s of all <code>Ship</code>s.
     * @since Classic
     */
    private static LinkedList<Particle> parts = new LinkedList<Particle>();

    /**
     * Adds the specified <code>Particle</code>.
     * @param p The <code>Particle</code> to be added.
     * @since Classic
     */
    public static void addParticle( Particle p )
    {
        parts.add( p );
    }

    public static void act()
    {
        Iterator<Particle> itr = parts.iterator();
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
        Iterator<Particle> itr = parts.iterator();
        while ( itr.hasNext() )
            itr.next().draw( g );
    }
}
