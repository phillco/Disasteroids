/*
 * DISASTEROIDS
 * RandNumGen.java
 */
package disasteroids;

import java.util.Random;

/**
 * Wraps a static random number generator. This gives better randomization than constant re-instantiation.
 * 
 * @author Andy Kooiman, Phillip Cohen
 * @since Classic
 */
public class RandomGenerator
{
    private static Random instance = new Random();
   
    /**
     * Returns the global random generator.
     * 
     * @return  a static instance of <code>Random</code>
     * @since January 18, 2008
     */
    public static Random get()
    {
        return instance;
    }
}
