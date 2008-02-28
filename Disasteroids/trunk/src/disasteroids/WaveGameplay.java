/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import java.awt.Graphics;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A game mode where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class WaveGameplay implements GameMode
{
    public WaveGameplay()
    {
    }

    public void act()
    {
        if ( RandomGenerator.get().nextInt( 2 ) == 0 )
        {
            int x = 0, y = 0;

            // Choose a corner.
            x = -250;
            y = 300;

            Game.getInstance().asteroidManager().add(
                    new Asteroid( x, y, RandomGenerator.nextMidpointDouble() * x * -.008, RandomGenerator.nextMidpointDouble() * y * -.008, RandomGenerator.get().nextInt( 40 ) + 12, 15 ),
                    true );
        }

    }

    public void draw( Graphics g )
    {

    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
