/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package disasteroids;

import java.text.DecimalFormat;

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
     * @param one The first GameObject
     * @param two The second GameObject
     * @return The distance
     */
    public static double getDistance( GameObject one, GameObject two )
    {
        double deltaX = ( one.getX() - two.getX() + Game.getInstance().GAME_WIDTH * 2 ) % Game.getInstance().GAME_WIDTH;
        double deltaY = ( one.getY() - two.getY() + Game.getInstance().GAME_HEIGHT * 2 ) % Game.getInstance().GAME_HEIGHT;
        return Math.sqrt( deltaX * deltaX + deltaY * deltaY );
    }
}
