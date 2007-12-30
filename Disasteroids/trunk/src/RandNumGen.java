/*
 * DISASTEROIDS
 * RandNumGen.java
 */

import java.util.Random;

/**
 * Statically stores random number generators.
 * This gives better randomization than constant re-instantiation.
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic
 */
public class RandNumGen
{

    private static Random numGen = new Random( (int) ( Math.random() * 10000 ) );

    private static Random particleGen = new Random( numGen.nextInt( 10000 ) );

    private static Random starGen = new Random( numGen.nextInt( 10000 ) );

    private static Random missileGen = new Random( numGen.nextInt( 10000 ) );

    private static Random asteroidGen = new Random( numGen.nextInt( 10000 ) );


    public static Random getParticleInstance()
    {
        return particleGen;
    }
    
    public static Random getStarInstance()
    {
        return starGen;
    }

    public static Random getMissileInstance()
    {
        return missileGen;
    }

    public static Random getAsteroidInstance()
    {
        return asteroidGen;
    }
}
