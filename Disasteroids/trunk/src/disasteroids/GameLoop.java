/**
 * DISASTEROIDS
 * GameLoop.java
 */
package disasteroids;

import disasteroids.networking.Client;

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
    
    public GameLoop()
    {
        super("Game Loop");
    }

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
                
                if ( shouldRun() )
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
    
    /**
     * Returns whether the game should run.
     * 
     * @return  whether we should run the next timestep
     * @since January 13, 2007
     */
    public boolean shouldRun()
    {
        // Don't run if the game is paused.
        if(Game.getInstance()==null)
            return false;
        if( Game.getInstance().isPaused() )
            return false;
        
        // If we're the cliient and the server isn't responding, hold up.
        if( Client.is() && Client.getInstance().serverTimeout() )
            return false;
        
        return true;
    }
}
