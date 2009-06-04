/*
 * DISASTEROIDS
 * Util.java
 */
package disasteroids;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * A centrallized class for useful methods.
 * @author Andy Kooiman
 */
public class Util
{
    /**
     * A boolean that's toggled every step. Useful for flashing objects.
     */
    private static boolean globalFlash = true;

    /**
     * Random generators.
     */
    private static ExtendedRandom graphicsRandomGenerator = new ExtendedRandom(), gameplayRandomGenerator = new ExtendedRandom();


    /**
     * The formatter used in <code>insertThousandCommas</code>
     * @see Util#insertThousandCommas(int) 
     */
    private static DecimalFormat thousands = new DecimalFormat( "" );

    /**
     * Returns a boolean that's toggled every step. Useful for flashing objects.
     */
    public static boolean getGlobalFlash()
    {
        return globalFlash;
    }

    public static void flipGlobalFlash()
    {
        globalFlash = !globalFlash;
    }

    /**
     * Takes the given number and inserts comma seperators at each grouping.
     * "491911920518159419" becomes "491,911,920,518,159,419".
     */
    public static String insertThousandCommas( int number )
    {
        return thousands.format( number );
    }

    /**
     * Finds the distance between two game object using pythagorous, and compensating for the 
     * boundary of the level
     * 
     * Note: When the distance is large (about halve the width of the level) the distance may not be the
     * smallest possible.
     * 
     * @param one The first GameObject
     * @param two The second GameObject
     * @return The distance
     */
    public static double getDistance( GameObject one, GameObject two )
    {
        double deltaX = getDeltaX( one, two );
        double deltaY = getDeltaY( one, two );
        return Math.sqrt( deltaX * deltaX + deltaY * deltaY );
    }

    /**
     * Returns the x distance between two <code>GameObject</code>s.  Always a number between 
     * -GAME_WIDTH/2 and GAME_WIDTH/2.
     * Calculated as one-two.  A positive value indicates that one is to the right of two.
     * 
     * @param one The first <code>GameObject</code>
     * @param two The second <code>GameObject</code>
     * @return The distance between the two <code>GameObject</code>s
     */
    public static double getDeltaX( GameObject one, GameObject two )
    {
        /*
        double deltaX = ( one.getX() - two.getX() + Game.getInstance().GAME_WIDTH * 2 ) % Game.getInstance().GAME_WIDTH;
        if ( Math.abs( deltaX - Game.getInstance().GAME_WIDTH ) < Math.abs( deltaX ) )
            return deltaX - Game.getInstance().GAME_WIDTH;
        return deltaX;*/
        return getDeltaX( one.getX(), two.getX() );
    }

    /**
     * Returns the y distance between two <code>GameObject</code>s.  Always a number between 
     * -GAME_HEIGHT/2 and GAME_HEIGHT/2.
     * Calculated as one-two.  A positive value indicates that one is below two.
     * 
     * @param one The first <code>GameObject</code>
     * @param two The second <code>GameObject</code>
     * @return The distance between the two <code>GameObject</code>s
     */
    public static double getDeltaY( GameObject one, GameObject two )
    {
        /*double deltaY = ( one.getY() - two.getY() + Game.getInstance().GAME_HEIGHT * 2 ) % Game.getInstance().GAME_HEIGHT;
        if ( Math.abs( deltaY - Game.getInstance().GAME_HEIGHT ) < Math.abs( deltaY ) )
            return deltaY - Game.getInstance().GAME_HEIGHT;
        return deltaY;*/
        return getDeltaY( one.getY(), two.getY() );
    }
    
    
    public static double getDeltaX( double x1, double x2)
    {
        double deltaX = x1 - x2;//the most common case
        if ( Math.abs(deltaX) > Game.getInstance().GAME_WIDTH / 2 )//it's shorter to go around the world
            if(deltaX<0)//on the finite plane we're below
                return deltaX + Game.getInstance().GAME_WIDTH;//in the real world we're above
            else//on the finite plane we're above
                return deltaX - Game.getInstance().GAME_WIDTH;//in the real world we're below
        else
            return deltaX;//no adjustment needed
    }
    
    public static double getDeltaY( double y1, double y2 )
    {
        double deltaY = y1 - y2;//the most common case
        if ( Math.abs(deltaY) > Game.getInstance().GAME_HEIGHT / 2 )//it's shorter to go around the world
            if(deltaY<0)//on the finite plane we're below
                return deltaY + Game.getInstance().GAME_HEIGHT;//in the real world we're above
            else//on the finite plane we're above
                return deltaY - Game.getInstance().GAME_HEIGHT;//in the real world we're below
        else
            return deltaY;//no adjustment needed
    }

    /**
     * Calculates and returns an angle between -pi and pi that represents the direction
     * that one would go to coincide with two.
     * 
     * @param one The <code>GameObject</code> representing the start of this unit vector
     * @param two The <code>GameObject</code> representing the end of this unif vector
     * @return The angle from one to two
     */
    public static double getAngle( GameObject one, GameObject two )
    {
        return Math.atan2( getDeltaY( one, two ), -getDeltaX( one, two ) );
    }
    
    
    public static double getAngle( GameObject one, double x, double y)
    {
        return Math.atan2( getDeltaY(one.getY(), y), -getDeltaX(one.getX(), x));
    }

    /**
     * Returns a random generator for use in the graphics thread only.
     */
    public static ExtendedRandom getGraphicsRandomGenerator()
    {
        return graphicsRandomGenerator;
    }

    /**
     * Returns a random generator for the gameplay thread.
     */
    public static ExtendedRandom getGameplayRandomGenerator()
    {
        return gameplayRandomGenerator;
    }

    /**
     * Java's random generator, with a few additional methods.
     * @author Phillip Cohen
     */
    public static class ExtendedRandom extends Random
    {

        /**
         * Randoms a random double between 0 and 2 * PI.
         */
        public double nextAngle()
        {
            return nextDouble() * 2 * Math.PI;
        }

        /**
         * Returns a random double from -n/2 to n/2.
         */
        public double nextMidpointDouble( int n )
        {
            return nextDouble() * n - n / 2;
        }
    }
}
