/*
 * DISASTEROIDS
 * WaveGameplay.java
 */
package disasteroids;

import disasteroids.gui.Particle;
import disasteroids.gui.ParticleManager;
import java.awt.Color;
import java.awt.Graphics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A game mode where players fend off waves of asteroids.
 * @author Phillip Cohen
 */
public class Deathmatch implements GameMode
{

    public Deathmatch()
    {
        /*
        // Create some random black holes.
        for ( int i = 0, numHoles = Util.getUnsyncedGameplayRandomGenerator().nextInt( 2 ) + 1; i < numHoles; i++ )
            Game.getInstance().getObjectManager().addObject( new BlackHole( Util.getUnsyncedGameplayRandomGenerator().nextInt( Game.getInstance().GAME_WIDTH ), Util.getUnsyncedGameplayRandomGenerator().nextInt( Game.getInstance().GAME_HEIGHT ), 13, -1 ) );
         * */
    }

    public void act()
    {
        // Spawn bonuses at the black holes.
        for ( BlackHole b : Game.getInstance().getObjectManager().getBlackHoles() )
        {
            if ( Util.getGameplayRandomGenerator().nextInt( 1500 ) == 0 )
            {
                Game.getInstance().getObjectManager().addObject( new Bonus( b.getX(), b.getY() - 150, Util.getGameplayRandomGenerator().nextInt( 8 ) - 4, 0 ) , false);
                /* TODO: Sync, or move to bonus creation
                for ( int i = 0; i < 6; i++ )
                    ParticleManager.addParticle( new Particle(
                            b.getX() + Util.getUnsyncedGameplayRandomGenerator().nextInt( 8 ) - 4,
                            ( b.getY() - 150 ) + Util.getUnsyncedGameplayRandomGenerator().nextInt( 8 ) - 4,
                            Util.getUnsyncedGameplayRandomGenerator().nextInt( 4 ),
                            Color.white,
                            Util.getUnsyncedGameplayRandomGenerator().nextDouble() * 3,
                            Util.getUnsyncedGameplayRandomGenerator().nextAngle(),
                            50, 1 ) );
                 */
            }
        }
    }

    public void draw( Graphics g )
    {
    }

    int getWavePoints( int wave )
    {
        return 0;
    }

    public void flatten( DataOutputStream stream ) throws IOException
    {
    }

    public Deathmatch( DataInputStream stream ) throws IOException
    {
    }

    public void optionsKey()
    {
    }
}
