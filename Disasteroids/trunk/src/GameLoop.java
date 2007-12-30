public class GameLoop extends Thread
{
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
                if ( !Game.getInstance().paused )
                    Game.getInstance().act();

                while(System.currentTimeMillis()-timeOfLast<10)
                    Thread.sleep( 1 );

            }
            catch ( InterruptedException ex )
            {
                Running.fatalError( "Game loop interrupted while sleeping.", ex );
            }
        }
    }
}
