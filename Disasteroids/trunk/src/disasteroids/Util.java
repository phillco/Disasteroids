/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disasteroids;

import java.text.DecimalFormat;
import java.util.Random;

/**
 *
 * @author AK90454
 */
public class Util
{
    private static DecimalFormat thousands = new DecimalFormat( "" );

    /**
     * Inserts comma seperators at each grouping.
     * 
     * @param number    The number to be formatted.
     * @return  the formatted string.
     * @since December 15, 2007
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
        double deltaX = getDeltaX(one, two);
        double deltaY = getDeltaY(one, two);
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
    public static double getDeltaX( GameObject one, GameObject two)
    {
        double deltaX = ( one.getX() - two.getX() + Game.getInstance().GAME_WIDTH *2 ) % Game.getInstance().GAME_WIDTH;
        if(Math.abs(deltaX-Game.getInstance().GAME_WIDTH)< Math.abs(deltaX))
            return deltaX-Game.getInstance().GAME_WIDTH;
        return deltaX;
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
    public static double getDeltaY( GameObject one, GameObject two)
    {
        double deltaY = ( one.getY() - two.getY() + Game.getInstance().GAME_HEIGHT *2 ) % Game.getInstance().GAME_HEIGHT;
        if(Math.abs(deltaY-Game.getInstance().GAME_HEIGHT) < Math.abs(deltaY))
            return deltaY-Game.getInstance().GAME_HEIGHT;
        return deltaY;
    }
    
    /**
     * Calculates and returns an angle between -pi and pi that represents the direction
     * that one would go to coincide with two.
     * 
     * @param one The <code>GameObject</code> representing the start of this unit vector
     * @param two The <code>GameObject</code> representing the end of this unif vector
     * @return The angle from one to two
     */
    public static double getAngle(GameObject one, GameObject two)
    {
        return Math.atan2( getDeltaY(one, two), +getDeltaX(one, two));
    }
    
    private static Random[] instances = { new Random(), new Random(), new Random() };

    private static int nextRandomGenerator = 0;

    /**
     * Returns a global random generator.
     * Multiple instances may be used for speed
     * 
     * @return  a static instance of <code>Random</code>
     * @since January 18, 2008
     */
    public static Random getRandomGenerator()
    {

        nextRandomGenerator = ( nextRandomGenerator + 1 ) % instances.length;
        return instances[nextRandomGenerator];
    }

    public static double nextMidpointDouble()
    {
        return getRandomGenerator().nextDouble() * 2 - 1;
    }
}
