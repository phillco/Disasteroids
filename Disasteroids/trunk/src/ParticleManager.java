/*
 * DISASTEROIDS
 * ParticleManager.java
 */

import java.util.ArrayList;

/**
 * Game-wide manager of <code>Particle</code>s.
 * @author Phillip Cohen, Andy Kooiman
 */
public class ParticleManager
{
    /**
     * A list of all <code>Particle</code>s of all <code>Ship</code>s.
     * @since Classic
     */
    private static ArrayList<Particle> parts = new ArrayList<Particle>();

    /**
     * Adds the specified <code>Particle</code>.
     * @param p The <code>Particle</code> to be added.
     * @since Classic
     */
    public static void addParticle( Particle p )
    {
        parts.add( p );
    }

    /**
     * Instructs all <code>Particle</code>s to act, removes old ones, and draws the rest.
     * @param g The <code>Graphics</code> context in which to draw the <code>Particles</code>.
     * @author Phillip Cohen
     * @since Classic
     */
    public static void drawParticles()
    {
        if ( parts.size() > 0 )
        {
            for ( int i = 0; i < parts.size(); i += 0 )
            {
                Particle p = parts.get( i );
                if ( p.act() == true )
                    parts.remove( i );
                else
                {
                    Running.environment().fillCircle(p.color, (int)p.x, (int)p.y , (int) p.size/2);
                    i++;
                }
            }
        }
    }
}
