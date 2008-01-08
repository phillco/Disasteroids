public class GameLoop extends Thread
{
    
    /**
     * The time in milliseconds between each action loop
     * @since December 30, 2007
     */
    private static int period=10;
    
    public static void increaseSpeed()
    {
        period--;
    }
    
    public static void decreaseSpeed()
    {
        period++;
    }
    
    @Override
    public void run()
    {
        long timeOfLast=System.currentTimeMillis();
        System.out.println( "Game loop started." );
        while ( true )
        {
            try
            {
                timeOfLast=System.currentTimeMillis();
                if ( !Game.getInstance().isPaused() )
                    Game.getInstance().act();

                while(System.currentTimeMillis()-timeOfLast<period)
                    Thread.sleep( 1 );

            }
            catch ( InterruptedException ex )
            {
                Running.fatalError( "Game loop interrupted while sleeping.", ex );
            }
        }
    }
}
