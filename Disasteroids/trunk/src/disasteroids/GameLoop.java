/**
 * DISASTEROIDS
 * GameLoop.java
 */
package disasteroids;

/**
 * The thread that runs the game.
 * @author Phillip Cohen
 */
public class GameLoop extends Thread
{
    /**
     * The time in milliseconds between each action loop
     * @since December 30, 2007
     */
    private static int period = 10;

    public static void increaseSpeed()
    {
        period--;
    }

    public static void decreaseSpeed()
    {
        period++;
    }

    /**
     * Starts an infinite loop which acts the game, sleeps, and repeats.
     * 
     * The amount of time to sleep is set by <code>period></code>.
     * If the game is running behind, it uses this sleep time as a cushion.
     */
    @Override
    public void run()
    {
        long timeOfLast = System.currentTimeMillis();
        System.out.println( "Game loop started." );
        while ( true )
        {
            try
            {
                timeOfLast = System.currentTimeMillis();
                if ( !Game.getInstance().isPaused() )
                    Game.getInstance().act();

                while ( System.currentTimeMillis() - timeOfLast < period )
                    Thread.sleep( 1 );

            }
            catch ( InterruptedException ex )
            {
                Running.fatalError( "Game loop interrupted while sleeping.", ex );
            }
        }
    }
}
